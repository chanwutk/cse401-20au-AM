package AST.Visitor;

import AST.*;
import IO.Asm;
import Symbols.ClassType;
import Symbols.SymbolTable;
import static IO.Error.numErrors;

public class CodegenVisitor extends AbstractVisitor {
	public CodegenVisitor(SymbolTable symbols, TypecheckVisitor typecheck) {
		this.symbols = symbols;
		this.typecheck = typecheck;
	}

	public void visit(Program n) {
		if (numErrors == 0) {
			Asm.text();
			n.m.accept(this);
			n.cl.stream().forEach(cd -> cd.accept(this));
		}
	}

	public void visit(MainClass n) {
		Asm.label("asm_main");
		numStackAllocated = 0;
		n.s.accept(this);
		Asm.ret();
	}

	private void visitClassDecl(ClassDecl n) {
		currentClass = n.i.s;
		symbols = symbols.enterClassScope(currentClass);
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
		Asm.label(Asm.method(currentClass, n.i.s));
		Asm.push(Asm.rbp);
		Asm.mov(Asm.rsp, Asm.rbp);
		int numRegArgs = Asm.numRegArgs(n.fl.size());
		numStackAllocated = 1 + numRegArgs + n.vl.size();
		Asm.sub(Asm.lit(numStackAllocated * Asm.WS), Asm.rsp);
		Asm.mov(Asm.rdi, Asm.mem(Asm.rbp, null, -1));
		for (int i = 0; i < numRegArgs; i++)
			Asm.mov(Asm.ARGS.get(i), Asm.mem(Asm.rbp, null, -2 - i));
		n.sl.stream().forEach(s -> s.accept(this));
		n.e.accept(this);
		Asm.leave();
		Asm.ret();
	}

	public void visit(IntArrayType n) {
		assert false;
	}

	public void visit(BooleanType n) {
		assert false;
	}

	public void visit(IntegerType n) {
		assert false;
	}

	public void visit(IdentifierType n) {
		assert false;
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
		Asm.put();
		if (pad)
			pop(null);
	}

	public void visit(Assign n) {
		assert false;
	}

	public void visit(ArrayAssign n) {
		assert false;
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
		Asm.setl(Asm.rax);
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
		assert false;
	}

	public void visit(ArrayLength n) {
		assert false;
	}

	public void visit(Call n) {
		int numArgs = n.el.size();
		boolean pad = numStackAllocated + Asm.numStackArgs(numArgs) % 2 != 0;
		if (pad)
			push(null);
		for (int i = n.el.size() - 1; i >= 0; i--) {
			n.el.get(i).accept(this);
			push(Asm.rax);
		}
		n.e.accept(this);
		Asm.mov(Asm.rax, Asm.rdi);
		for (int i = 0; i < Asm.numRegArgs(n.el.size()); i++) {
			pop(Asm.ARGS.get(i));
		}
		assert numStackAllocated % 2 == 0;
		var t = (ClassType) typecheck.typeof(n.e);
		Asm.call(Asm.mem(Asm.rdi, null, t.offset.get(n.i.s)));
		Asm.add(Asm.lit(Asm.numStackArgs(numArgs) + (pad ? 1 : 0)), Asm.rsp);
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
		assert false;
	}

	public void visit(This n) {
		assert false;
	}

	public void visit(NewArray n) {
		assert false;
	}

	public void visit(NewObject n) {
		assert false;
	}

	public void visit(Not n) {
		n.e.accept(this);
		// A boolean true must be exactly 1
		Asm.xor(Asm.lit(1), Asm.rax);
	}

	public void visit(Identifier n) {
		assert false;
	}

	private SymbolTable symbols;
	private TypecheckVisitor typecheck;
	private int lastLabel = 0;
	private String currentClass;
	private int numStackAllocated;

	private String newLabel() {
		lastLabel++;
		return "L" + lastLabel;
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
}
