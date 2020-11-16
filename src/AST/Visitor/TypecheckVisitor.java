package AST.Visitor;

import java.util.Map;
import java.util.stream.Collectors;
import java.util.HashMap;
import java.util.List;

import AST.*;
import Symbols.BaseType;
import Symbols.ClassType;
import Symbols.Signature;
import Symbols.SymbolTable;
import Symbols.Type;
import Info.Info;

public class TypecheckVisitor implements Visitor {
	private SymbolTable symbols;
	private final Map<Exp, Type> types = new HashMap<>();

	public TypecheckVisitor(SymbolTable symbols) {
		this.symbols = symbols;
	}

	private Type acceptExp(Exp n) {
		n.accept(this);
		return types.get(n);
	}

	public void visit(Program n) {
		n.m.accept(this);
		for (int i = 0; i < n.cl.size(); i++) {
			n.cl.get(i).accept(this);
		}
	}

	public void visit(MainClass n) {
		symbols = symbols.enterClassScope(n.i1.s);
		n.s.accept(this);
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

	public void visit(VarDecl n) {}

	public void visit(MethodDecl n) {
		symbols = symbols.enterMethodScope(n.i.s);
		n.sl.stream().forEach(s -> s.accept(this));
		Type rettype = acceptExp(n.e);
		Signature signature = symbols.getMethod(n.i);
		if (!rettype.subtypeOf(signature.ret)) {
			Info.numErrors++;
			System.err.printf("%s:%d: error: incompatible type: %s cannot be convered to %s\n", Info.file,
					n.e.line_number, rettype, signature.ret);
			System.err.printf("  location: class %s\n", Info.currentClass);
		}
		symbols = symbols.exitScope();
	}

	public void visit(Formal n) {}

	public void visit(IntArrayType n) {}

	public void visit(BooleanType n) {}

	public void visit(IntegerType n) {}

	public void visit(IdentifierType n) {}

	public void visit(Block n) {
		n.sl.stream().forEach(s -> s.accept(this));
	}

	public void visit(If n) {
		Type condtype = acceptExp(n.e);
		if (condtype != BaseType.BOOLEAN) {
			Info.numErrors++;
			System.err.printf("%s:%d: error: incompatible type: if condition: %s cannot be converted to boolean\n", Info.file,
					n.e.line_number, condtype);
			System.err.printf("  location: class %s\n", Info.currentClass);
		}

		n.s1.accept(this);
		n.s2.accept(this);
	}

	public void visit(While n) {
		Type condtype = acceptExp(n.e);
		if (condtype != BaseType.BOOLEAN) {
			Info.numErrors++;
			System.err.printf("%s:%d: error: incompatible type: while-loop condition: %s cannot be converted to boolean\n", Info.file,
					n.e.line_number, condtype);
			System.err.printf("  location: class %s\n", Info.currentClass);
		}

		n.s.accept(this);
	}

	public void visit(Print n) {
		Type exptype = acceptExp(n.e);
		if (exptype != BaseType.INT) {
			Info.numErrors++;
			System.err.printf("%s:%d: error: incompatible type: print: %s cannot be converted to int\n", Info.file,
					n.e.line_number, exptype);
			System.err.printf("  location: class %s\n", Info.currentClass);
		}
	}

	public void visit(Assign n) {
		Type expType = acceptExp(n.e);
		Type idType = symbols.getVariable(n.i);
		if (!expType.subtypeOf(idType)) {
			Info.numErrors++;
			System.err.printf("%s:%d: error: incompatible types: assign: %s cannot be converted to %s\n", Info.file,
					n.e.line_number, expType, idType);
			System.err.printf("  location: class %s\n", Info.currentClass);
		}
	}

	public void visit(ArrayAssign n) {
		Type idtype = symbols.getVariable(n.i);
		if (idtype != BaseType.ARRAY) {
			Info.numErrors++;
			System.err.printf("%s:%d: error: incompatible type: array-assign: %s cannot be converted to int[]\n", Info.file,
					n.i.line_number, idtype);
			System.err.printf("  location: class %s\n", Info.currentClass);
		}

		Type indextype = acceptExp(n.e1);
		if (indextype != BaseType.INT) {
			Info.numErrors++;
			System.err.printf("%s:%d: error: incompatible type: array-assign: %s cannot be converted to int\n", Info.file,
					n.e1.line_number, indextype);
			System.err.printf("  location: class %s\n", Info.currentClass);
		}

		Type exptype = acceptExp(n.e2);
		if (exptype != BaseType.INT) {
			Info.numErrors++;
			System.err.printf("%s:%d: error: incompatible type: array-assign: %s cannot be converted to int\n", Info.file,
					n.e2.line_number, exptype);
			System.err.printf("  location: class %s\n", Info.currentClass);
		}
	}

	public void visit(And n) {
		handleBinaryOp("&&", n.e1, n.e2, BaseType.BOOLEAN, BaseType.BOOLEAN);
		types.put(n, BaseType.BOOLEAN);
	}

	public void visit(LessThan n) {
		handleBinaryOp("<", n.e1, n.e2, BaseType.INT, BaseType.INT);
		types.put(n, BaseType.BOOLEAN);
	}

	public void visit(Plus n) {
		handleBinaryOp("+", n.e1, n.e2, BaseType.INT, BaseType.INT);
		types.put(n, BaseType.INT);
	}

	public void visit(Minus n) {
		handleBinaryOp("-", n.e1, n.e2, BaseType.INT, BaseType.INT);
		types.put(n, BaseType.INT);
	}

	public void visit(Times n) {
		handleBinaryOp("*", n.e1, n.e2, BaseType.INT, BaseType.INT);
		types.put(n, BaseType.INT);
	}

	private static String BINARY_OP_ERROR = "%s:%d: error: incompatible type: expected `%s %s %s` but found `%s %s %s`\n";
	private void handleBinaryOp(String op, Exp e1, Exp e2, Type expected1, Type expected2) {
		Type t1 = acceptExp(e1);
		Type t2 = acceptExp(e2);

		if (!t1.subtypeOf(expected1)) {
			Info.numErrors++;
			System.err.printf(BINARY_OP_ERROR, Info.file, e1.line_number, expected1, op, expected2, t1, op, t2);
			Info.errorInClass();
		}

		if (!t2.subtypeOf(expected2)) {
			Info.numErrors++;
			System.err.printf(BINARY_OP_ERROR, Info.file, e2.line_number, expected1, op, expected2, t1, op, t2);
			Info.errorInClass();
		}
	}

	public void visit(ArrayLookup n) {
		Type arraytype = acceptExp(n.e1);
		if (arraytype != BaseType.ARRAY) {
			Info.numErrors++;
			System.err.printf("%s:%d: error: incompatible type: array lookup: %s cannot be converted to int[]\n", Info.file,
					n.e1.line_number, arraytype);
			System.err.printf("  location: class %s\n", Info.currentClass);
		}

		Type indextype = acceptExp(n.e2);
		if (indextype != BaseType.INT) {
			Info.numErrors++;
			System.err.printf("%s:%d: error: incompatible type: array lookup: %s cannot be converted to int\n", Info.file,
					n.e1.line_number, indextype);
			System.err.printf("  location: class %s\n", Info.currentClass);
		}

    types.put(n, BaseType.INT);
	}

	public void visit(ArrayLength n) {
		Type arraytype = acceptExp(n.e);
		if (arraytype != BaseType.ARRAY) {
			Info.numErrors++;
			System.err.printf("%s:%d: error: incompatible type: array length: %s cannot be converted to int[]\n", Info.file,
					n.e.line_number, arraytype);
			System.err.printf("  location: class %s\n", Info.currentClass);
		}

    types.put(n, BaseType.INT);
	}

	public void visit(Call n) {
		Type exptype = acceptExp(n.e);
		if (!(exptype instanceof ClassType)) {
			Info.numErrors++;
			System.err.printf("%s:%d: error: referencing non-class: expecting a class type but received %s\n", Info.file,
					n.e.line_number, exptype.toString());
			System.err.printf("  location: class %s\n", Info.currentClass);
			types.put(n, BaseType.UNKNOWN);
			return;
		}
		ClassType objtype = (ClassType) exptype;
		Signature signature = objtype.getMethod(n.i.s);
		if (signature == null) {
			types.put(n, BaseType.UNKNOWN);
			return;
		}

		List<Type> el = n.el.stream().map(e -> acceptExp(e)).collect(Collectors.toList());
		if (signature.params.size() != el.size()) {
			Info.numErrors++;
			System.err.printf("%s:%d: error: incorrect number of parameter(s): %s expected %d parameter(s) but received %d parameter(s)\n", Info.file,
					n.i.line_number, n.i.s, signature.params.size(), el.size());
			System.err.printf("  location: class %s\n", Info.currentClass);
		}

		int len = signature.params.size();
		for (int i = 0; i < len; i++) {
			Type ptype = el.get(i);
			Type stype = signature.params.get(i);
			if (!ptype.subtypeOf(stype)) {
				Info.numErrors++;
				System.err.printf("%s:%d: error: incompatible type: array length: %s cannot be converted to %s\n", Info.file,
						n.el.get(i).line_number, ptype, stype);
			System.err.printf("  location: class %s\n", Info.currentClass);
			}
		}

		types.put(n, signature.ret);
	}

	public void visit(IntegerLiteral n) {
    types.put(n, BaseType.INT);
	}

	public void visit(True n) {
    types.put(n, BaseType.BOOLEAN);
	}

	public void visit(False n) {
    types.put(n, BaseType.BOOLEAN);
	}

	public void visit(IdentifierExp n) {
    types.put(n, symbols.getVariable(n));
	}

	public void visit(This n) {
		types.put(n, symbols.getClass(Info.currentClass, n.line_number));
	}

	public void visit(NewArray n) {
		Type indextype = acceptExp(n.e);
		if (indextype != BaseType.INT) {
			Info.numErrors++;
			System.err.printf("%s:%d: error: incompatible type: new array: %s cannot be converted to int\n", Info.file,
					n.e.line_number, indextype);
			System.err.printf("  location: class %s\n", Info.currentClass);
		}
		types.put(n, BaseType.ARRAY);
	}

	public void visit(NewObject n) {
		types.put(n, symbols.getClass(n.i));
	}

	public void visit(Not n) {
		Type t = acceptExp(n.e);
		if (t != BaseType.BOOLEAN) {
			Info.numErrors++;
			System.err.printf("%s:%d: error: incompatible type: not (!): %s cannot be converted to boolean\n", Info.file,
					n.e.line_number, t);
			System.err.printf("  location: class %s\n", Info.currentClass);
		}
		types.put(n, BaseType.BOOLEAN);
	}

	public void visit(Identifier n) {}
}
