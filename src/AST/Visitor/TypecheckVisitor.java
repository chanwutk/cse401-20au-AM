package AST.Visitor;

import java.util.Map;
import java.util.HashMap;
import AST.*;
import Symbols.SymbolTable;
import Symbols.Type;
import Info.Info;

public class TypecheckVisitor implements Visitor {
	private SymbolTable symbols;
	private final Map<Exp, Type> types = new HashMap<>();

	public TypecheckVisitor(SymbolTable symbols) {
		this.symbols = symbols;
	}

	public void visit(Program n) {
		n.m.accept(this);
		for (int i = 0; i < n.cl.size(); i++) {
			n.cl.get(i).accept(this);
		}
	}

	public void visit(MainClass n) {
		symbols = symbols.enterClassScope(n.i1.s).enterMethodScope("main");
		n.s.accept(this);
		symbols = symbols.exitScope().exitScope();
	}

	public void visit(ClassDeclSimple n) {
	}

	public void visit(ClassDeclExtends n) {
	}

	public void visit(VarDecl n) {
	}

	public void visit(MethodDecl n) {
	}

	public void visit(Formal n) {
	}

	public void visit(IntArrayType n) {
	}

	public void visit(BooleanType n) {
	}

	public void visit(IntegerType n) {
	}

	public void visit(IdentifierType n) {
	}

	public void visit(Block n) {
	}

	public void visit(If n) {
	}

	public void visit(While n) {
	}

	public void visit(Print n) {
	}

	public void visit(Assign n) {
		n.e.accept(this);
		Type idType = symbols.getVariable(n.i);
		Type expType = types.get(n.e);
		if (!expType.subtypeOf(idType)) {
			Info.numErrors++;
			System.err.printf("%s:%d: error: incompatible types: %s cannot be converted to %s\n", Info.file,
					n.e.line_number, expType, idType);
		}
	}

	public void visit(ArrayAssign n) {
	}

	public void visit(And n) {
	}

	public void visit(LessThan n) {
	}

	public void visit(Plus n) {
	}

	public void visit(Minus n) {
	}

	public void visit(Times n) {
	}

	public void visit(ArrayLookup n) {
	}

	public void visit(ArrayLength n) {
	}

	public void visit(Call n) {
	}

	public void visit(IntegerLiteral n) {
	}

	public void visit(True n) {
	}

	public void visit(False n) {
	}

	public void visit(IdentifierExp n) {
	}

	public void visit(This n) {
	}

	public void visit(NewArray n) {
	}

	public void visit(NewObject n) {
	}

	public void visit(Not n) {
	}

	public void visit(Identifier n) {
	}
}
