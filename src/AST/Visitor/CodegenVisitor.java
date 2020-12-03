package AST.Visitor;

import AST.*;
import IO.Asm;
import Symbols.SymbolTable;
import static IO.Error.numErrors;

public class CodegenVisitor extends AbstractVisitor {
	public CodegenVisitor(SymbolTable symbols) {
		this.symbols = symbols;
		symbols.prettyPrint(System.out, 0);
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
		stack16Aligned = n.vl.size() % 2 == 1;
		if (n.vl.size() > 0)
			Asm.sub(Asm.lit(n.vl.size() * Asm.WS), Asm.rsp);
		n.sl.stream().forEach(s -> s.accept(this));
		n.e.accept(this);
		if (n.vl.size() > 0)
			Asm.add(Asm.lit(n.vl.size() * Asm.WS), Asm.rsp);
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
		Asm.put(Asm.rax, stack16Aligned);
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
		Asm.push(Asm.rax);
		e1.accept(this);
		Asm.pop(Asm.rdx);
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
		assert false;
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
	private int lastLabel = 0;
	private String currentClass;
	private boolean stack16Aligned;

	private String newLabel() {
		lastLabel++;
		return "L" + lastLabel;
	}
}
