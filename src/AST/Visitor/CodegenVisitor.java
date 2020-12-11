package AST.Visitor;

import java.util.ArrayDeque;
import java.util.Deque;

import AST.*;
import IO.Asm;
import Symbols.ClassType;
import Symbols.SymbolTable;
import IO.Error;

public class CodegenVisitor extends AbstractVisitor {
	private static final String INSTANCEOF = ".$instanceof";

	public CodegenVisitor(SymbolTable symbols) {
		this.symbols = symbols;
	}

	public void visit(Program n) {
		if (Error.numErrors == 0) {
			Asm.text();

			Asm.label(INSTANCEOF);
			Asm.mov(Asm.mem(Asm.rdi, null, 0), Asm.rax);
			String loop = newLabel();
			Asm.label(loop);
			Asm.cmp(Asm.rax, Asm.rsi);
			String end = newLabel();
			Asm.je(end);
			Asm.mov(Asm.mem(Asm.rax, null, 0), Asm.rax);
			Asm.test(Asm.rax);
			Asm.jne(loop);
			Asm.label(end);
			Asm.ret();

			String printAndExit = newLabel();
			Asm.label(printAndExit);
			Asm.and(Asm.lit(-1 - 0xf), Asm.rsp); // 0x ffffffff fffffff0
			Asm.callc("printf");
			Asm.mov("stdout(%rip)", Asm.rdi);
			Asm.callc("fflush");
			Asm.callc("abort");

			Asm.label(Asm.ARRAYINDEXOUTOFBOUND_HANDLER);
			Asm.lea(Asm.litLabel(Asm.ARRAYINDEXOUTOFBOUND_MSG), Asm.rdi);
			Asm.mov(Asm.mem(Asm.rax, null, -1), Asm.rsi);
			Asm.jmp(printAndExit);

			Asm.label(Asm.NULLPOINTER_HANDLER);
			Asm.lea(Asm.litLabel(Asm.NULLPOINTER_MSG), Asm.rdi);
			Asm.jmp(printAndExit);

			Asm.label(Asm.EXCEPTION_HANDLER);
			Asm.lea(Asm.litLabel(Asm.EXCEPTION_MSG), Asm.rdi);
			Asm.jmp(printAndExit);

			n.m.accept(this);
			n.cl.stream().forEach(cd -> cd.accept(this));
		}
	}

	public void visit(MainClass n) {
		symbols = symbols.enterClassScope(n.i1.s);
		Asm.label("asm_main");
		numStackAllocated = 0;
		TryInfo tryInfo = new TryInfo(Asm.EXCEPTION_HANDLER, null);
		tryHandlers.push(tryInfo);
		n.s.accept(this);
		Asm.ret();
		tryHandlers.pop();
		symbols = symbols.exitScope();
	}

	private void visitClassDecl(ClassDecl n) {
		symbols = symbols.enterClassScope(n.i.s);
		n.ml.stream().forEach(md -> md.accept(this));
		symbols = symbols.exitScope();
	}

	public void visit(ClassDeclSimple n) {
		visitClassDecl(n);
	}

	public void visit(ClassDeclExtends n) {
		visitClassDecl(n);
	}

	public void visit(MethodDecl n) {
		symbols = symbols.enterMethodScope(n.i.s);
		Asm.label(Asm.method(Error.currentClass, n.i.s));
		Asm.push(Asm.rbp);
		Asm.mov(Asm.rsp, Asm.rbp);
		int numRegArgs = Asm.numRegArgs(n.fl.size());
		numStackAllocated = 1 + numRegArgs + n.vl.size();
		Asm.sub(Asm.lit(numStackAllocated * Asm.WS), Asm.rsp);
		Asm.mov(Asm.rdi, Asm.mem(Asm.rbp, null, -1));
		for (int i = 0; i < numRegArgs; i++)
			Asm.mov(Asm.ARGS.get(i), Asm.mem(Asm.rbp, null, -2 - i));
		for (int i = 0; i < n.vl.size(); i++)
			Asm.mov(Asm.lit(0), Asm.mem(Asm.rsp, null, i));
		n.sl.stream().forEach(s -> s.accept(this));
		n.e.accept(this);
		Asm.mov(Asm.lit(0), Asm.rdx);
		Asm.leave();
		Asm.ret();
		symbols = symbols.exitScope();
	}

	private void throwRax() {
		if (tryHandlers.isEmpty()) {
			Asm.mov(Asm.lit(1), Asm.rdx);
			Asm.leave();
			Asm.ret();
		} else {
			Asm.jmp(tryHandlers.peek().handler);
		}
	}

	public void visit(Try n) {
		TryInfo tryInfo = new TryInfo(newLabel(), newLabel());
		tryHandlers.push(tryInfo);
		n.s.stream().forEach(s -> s.accept(this));
		Asm.jmp(tryInfo.end);
		Asm.label(tryInfo.handler);
		Asm.lea(Asm.mem(Asm.rbp, null, -numStackAllocated), Asm.rsp);
		push(Asm.rax);
		n.c.stream().forEach(c -> c.accept(this));
		pop(Asm.rax);
		tryHandlers.pop();
		throwRax();
		Asm.label(tryInfo.end);
	}

	public void visit(Catch n) {
		symbols = symbols.enterCatchScope(n);
		assert -numStackAllocated == symbols.getVariableLocation(n.f.i.s).offset;
		Asm.mov(location(n.f.i.s, Asm.rdx), Asm.rdi);
		ClassType t = (ClassType) symbols.getClass(((IdentifierType) n.f.t).s, 0);
		Asm.lea(Asm.litLabel(Asm.vtable(t.name)), Asm.rsi);
		Asm.callc(INSTANCEOF);
		Asm.test(Asm.rax);
		String end = newLabel();
		Asm.je(end);
		TryInfo tryInfo = tryHandlers.pop();
		n.s.stream().forEach(s -> s.accept(this));
		tryHandlers.push(tryInfo);
		pop(null);
		numStackAllocated++; // this is just one catch clause
		Asm.jmp(tryInfo.end);
		Asm.label(end);
		symbols = symbols.exitScope();
	}

	public void visit(Throw n) {
		n.e.accept(this);
		Asm.test(Asm.rax);
		Asm.je(Asm.NULLPOINTER_HANDLER);
		throwRax();
	}

	public void visit(Block n) {
		n.sl.stream().forEach(s -> s.accept(this));
	}

	public void visit(If n) {
		var elseBranch = newLabel();
		var end = newLabel();
		n.e.accept(this);
		Asm.test(Asm.rax);
		Asm.je(elseBranch);
		n.s1.accept(this);
		Asm.jmp(end);
		Asm.label(elseBranch);
		n.s2.accept(this);
		Asm.label(end);
	}

	public void visit(While n) {
		var check = newLabel();
		var loop = newLabel();
		Asm.jmp(check);
		Asm.label(loop);
		n.s.accept(this);
		Asm.label(check);
		n.e.accept(this);
		Asm.test(Asm.rax);
		Asm.jne(loop);
	}

	public void visit(Print n) {
		n.e.accept(this);
		boolean pad = numStackAllocated % 2 != 0;
		if (pad)
			push(null);
		Asm.mov(Asm.rax, Asm.rdi);
		assert numStackAllocated % 2 == 0;
		Asm.callc("put");
		if (pad)
			pop(null);
	}

	public void visit(Assign n) {
		n.e.accept(this);
		Asm.mov(Asm.rax, location(n.i.s, Asm.rdx));
	}

	public void visit(ArrayAssign n) {
		visitBinaryOp(n.e1, n.e2);
		Asm.mov(location(n.i.s, Asm.rcx), Asm.rdi);
		Asm.cmp(Asm.mem(Asm.rdi, null, -1), Asm.rax);
		Asm.jge(Asm.ARRAYINDEXOUTOFBOUND_HANDLER);
		Asm.mov(Asm.rdx, Asm.mem(Asm.rdi, Asm.rax, 0));
	}

	public void visit(And n) {
		var end = newLabel();
		n.e1.accept(this);
		Asm.test(Asm.rax);
		Asm.je(end);
		n.e2.accept(this);
		Asm.label(end);
	}

	public void visitBinaryOp(Exp e1, Exp e2) {
		e2.accept(this);
		push(Asm.rax);
		e1.accept(this);
		pop(Asm.rdx);
	}

	public void visit(LessThan n) {
		visitBinaryOp(n.e1, n.e2);
		Asm.cmp(Asm.rdx, Asm.rax);
		Asm.setl(Asm.al);
		Asm.movzbq(Asm.al, Asm.rax);
	}

	public void visit(Plus n) {
		visitBinaryOp(n.e1, n.e2);
		Asm.add(Asm.rdx, Asm.rax);
	}

	public void visit(Minus n) {
		visitBinaryOp(n.e1, n.e2);
		Asm.sub(Asm.rdx, Asm.rax);
	}

	public void visit(Times n) {
		visitBinaryOp(n.e1, n.e2);
		Asm.imul(Asm.rdx, Asm.rax);
	}

	public void visit(ArrayLookup n) {
		visitBinaryOp(n.e1, n.e2);
		Asm.cmp(Asm.mem(Asm.rax, null, -1), Asm.rdx);
		Asm.jge(Asm.ARRAYINDEXOUTOFBOUND_HANDLER);
		Asm.mov(Asm.mem(Asm.rax, Asm.rdx, 0), Asm.rax);
	}

	public void visit(ArrayLength n) {
		n.e.accept(this);
		Asm.mov(Asm.mem(Asm.rax, null, -1), Asm.rax);
	}

	public void visit(Call n) {
		int numArgs = n.el.size();
		boolean pad = (numStackAllocated + Asm.numStackArgs(numArgs)) % 2 != 0;
		if (pad)
			push(null);
		for (int i = n.el.size() - 1; i >= 0; i--) {
			n.el.get(i).accept(this);
			push(Asm.rax);
		}
		n.e.accept(this);
		Asm.test(Asm.rax);
		Asm.je(Asm.NULLPOINTER_HANDLER);
		Asm.mov(Asm.rax, Asm.rdi);
		for (int i = 0; i < Asm.numRegArgs(n.el.size()); i++) {
			pop(Asm.ARGS.get(i));
		}
		var t = (ClassType) new TypecheckVisitor(symbols).typeof(n.e);
		Asm.mov(Asm.mem(Asm.rdi, null, 0), Asm.rax);
		assert numStackAllocated % 2 == 0;
		Asm.call(Asm.mem(Asm.rax, null, t.getOffset(n.i.s)));
		int numCleanup = Asm.numStackArgs(numArgs) + (pad ? 1 : 0);
		if (numCleanup > 0) {
			Asm.add(Asm.lit(numCleanup * Asm.WS), Asm.rsp);
			numStackAllocated -= numCleanup;
		}
		Asm.test(Asm.rdx);
		String end = newLabel();
		Asm.je(end);
		throwRax();
		Asm.label(end);
	}

	public void visitLit(int lit) {
		Asm.mov(Asm.lit(lit), Asm.rax);
	}

	public void visit(IntegerLiteral n) {
		visitLit(n.i);
	}

	public void visit(True n) {
		visitLit(1);
	}

	public void visit(False n) {
		visitLit(0);
	}

	public void visit(IdentifierExp n) {
		Asm.mov(location(n.s, Asm.rdx), Asm.rax);
	}

	public void visit(This n) {
		Asm.mov(Asm.mem(Asm.rbp, null, -1), Asm.rax);
	}

	public void visit(NewArray n) {
		n.e.accept(this);
		push(Asm.rax);
		boolean pad = numStackAllocated % 2 != 0;
		if (pad)
			push(null);
		Asm.lea(Asm.mem("", Asm.rax, 1), Asm.rdi);
		assert numStackAllocated % 2 == 0;
		Asm.callc("mjcalloc");
		if (pad)
			pop(null);
		pop(Asm.rdx);
		Asm.mov(Asm.rdx, Asm.mem(Asm.rax, null, 0));
		Asm.add(Asm.lit(Asm.WS), Asm.rax);
	}

	public void visit(NewObject n) {
		ClassType t = (ClassType) symbols.getClass(n.i);
		boolean pad = numStackAllocated % 2 != 0;
		if (pad)
			push(null);
		Asm.mov(Asm.lit(t.size * Asm.WS), Asm.rdi);
		assert numStackAllocated % 2 == 0;
		Asm.callc("mjcalloc");
		if (pad)
			pop(null);
		Asm.lea(Asm.litLabel(Asm.vtable(t.name)), Asm.rdx);
		Asm.mov(Asm.rdx, Asm.mem(Asm.rax, null, 0));
	}

	public void visit(Not n) {
		n.e.accept(this);
		// A boolean true must be exactly 1
		Asm.xor(Asm.lit(1), Asm.al);
	}

	private SymbolTable symbols;
	private int lastLabel = 0;
	private int numStackAllocated;

	private static class TryInfo {
		final String handler;
		final String end;

		TryInfo(String handler, String end) {
			this.handler = handler;
			this.end = end;
		}
	}

	private Deque<TryInfo> tryHandlers = new ArrayDeque<>();

	private String newLabel() {
		lastLabel++;
		return ".L" + lastLabel;
	}

	private void push(String reg) {
		if (reg == null)
			Asm.sub(Asm.lit(Asm.WS), Asm.rsp);
		else
			Asm.push(reg);
		numStackAllocated++;
	}

	private void pop(String reg) {
		if (reg == null)
			Asm.add(Asm.lit(Asm.WS), Asm.rsp);
		else
			Asm.pop(reg);
		numStackAllocated--;
	}

	public String location(String v, String regTemp) {
		var loc = symbols.getVariableLocation(v);
		switch (loc.type) {
			case THIS:
				Asm.mov(Asm.mem(Asm.rbp, null, -1), regTemp);
				return Asm.mem(regTemp, null, loc.offset);
			case RBP:
				return Asm.mem(Asm.rbp, null, loc.offset);
			default:
				assert false;
				return null;
		}
	}
}
