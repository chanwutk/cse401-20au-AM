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
		int locals = n.vl.size();
		if (locals % 2 == 0)
			locals++;
		Asm.sub(Asm.lit(locals * Asm.WS), Asm.rsp);
		n.sl.stream().forEach(s -> s.accept(this));
		n.e.accept(this);
		Asm.add(Asm.lit(locals * Asm.WS), Asm.rsp);
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
		assert false;
	}

	public void visit(If n) {
		assert false;
	}

	public void visit(While n) {
		assert false;
	}

	public void visit(Print n) {
		n.e.accept(this);
		Asm.put(Asm.rax);
	}

	public void visit(Assign n) {
		assert false;
	}

	public void visit(ArrayAssign n) {
		assert false;
	}

	public void visit(And n) {
		assert false;
	}

	public void visit(LessThan n) {
		assert false;
	}

	public void visit(Plus n) {
		assert false;
	}

	public void visit(Minus n) {
		assert false;
	}

	public void visit(Times n) {
		assert false;
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

	public void visit(IntegerLiteral n) {
		Asm.mov(Asm.lit(n.i), Asm.rax);
	}

	public void visit(True n) {
		assert false;
	}

	public void visit(False n) {
		assert false;
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
		assert false;
	}

	public void visit(Identifier n) {
		assert false;
	}

	private SymbolTable symbols;
	private String currentClass;
}
