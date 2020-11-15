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
		symbols = symbols.enterClassScope(n.i1.s).enterMethodScope("main");
		n.s.accept(this);
		symbols = symbols.exitScope().exitScope();
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
		}

		n.s.accept(this);
	}

	public void visit(Print n) {
		Type exptype = acceptExp(n.e);
		if (exptype != BaseType.INT) {
			Info.numErrors++;
			System.err.printf("%s:%d: error: incompatible type: print: %s cannot be converted to int\n", Info.file,
					n.e.line_number, exptype);
		}
	}

	public void visit(Assign n) {
		Type expType = acceptExp(n.e);
		Type idType = symbols.getVariable(n.i);
		if (!expType.subtypeOf(idType)) {
			Info.numErrors++;
			System.err.printf("%s:%d: error: incompatible types: assign: %s cannot be converted to %s\n", Info.file,
					n.e.line_number, expType, idType);
		}
	}

	public void visit(ArrayAssign n) {
		Type idtype = symbols.getVariable(n.i);
		if (idtype != BaseType.ARRAY) {
			Info.numErrors++;
			System.err.printf("%s:%d: error: incompatible type: array-assign: %s cannot be converted to int[]\n", Info.file,
					n.i.line_number, idtype);
		}

		Type indextype = acceptExp(n.e1);
		if (indextype != BaseType.INT) {
			Info.numErrors++;
			System.err.printf("%s:%d: error: incompatible type: array-assign: %s cannot be converted to int\n", Info.file,
					n.e1.line_number, indextype);
		}

		Type exptype = acceptExp(n.e2);
		if (exptype != BaseType.INT) {
			Info.numErrors++;
			System.err.printf("%s:%d: error: incompatible type: array-assign: %s cannot be converted to int\n", Info.file,
					n.e2.line_number, exptype);
		}
	}

	public void visit(And n) {
		Type t1 = acceptExp(n.e1);
		Type t2 = acceptExp(n.e2);
		if (t1 != BaseType.BOOLEAN) {
			Info.numErrors++;
			System.err.printf("%s:%d: error: incompatible type: expected `boolean && boolen` but found `%s && %s`\n", Info.file,
					n.e1.line_number, t1, t2);
		}
		if (t2 != BaseType.BOOLEAN) {
			Info.numErrors++;
			System.err.printf("%s:%d: error: incompatible type: expected `boolean && boolen` but found `%s && %s`\n", Info.file,
					n.e2.line_number, t1, t2);
		}

		types.put(n, BaseType.BOOLEAN);
	}

	public void visit(LessThan n) {
		Type t1 = acceptExp(n.e1);
		Type t2 = acceptExp(n.e2);
		if (t1 != BaseType.INT) {
			Info.numErrors++;
			System.err.printf("%s:%d: error: incompatible type: expected `int < int` but found `%s < %s`\n", Info.file,
					n.e1.line_number, t1, t2);
		}
		if (t2 != BaseType.INT) {
			Info.numErrors++;
			System.err.printf("%s:%d: error: incompatible type: expected `int && int` but found `%s < %s`\n", Info.file,
					n.e2.line_number, t1, t2);
		}

		types.put(n, BaseType.BOOLEAN);
	}

	public void visit(Plus n) {
		Type t1 = acceptExp(n.e1);
		Type t2 = acceptExp(n.e2);
		if (t1 != BaseType.INT) {
			Info.numErrors++;
			System.err.printf("%s:%d: error: incompatible type: expected `int + int` but found `%s + %s`\n", Info.file,
					n.e1.line_number, t1, t2);
		}
		if (t2 != BaseType.INT) {
			Info.numErrors++;
			System.err.printf("%s:%d: error: incompatible type: expected `int + int` but found `%s + %s`\n", Info.file,
					n.e2.line_number, t1, t2);
		}

		types.put(n, BaseType.INT);
	}

	public void visit(Minus n) {
		Type t1 = acceptExp(n.e1);
		Type t2 = acceptExp(n.e2);
		if (t1 != BaseType.INT) {
			Info.numErrors++;
			System.err.printf("%s:%d: error: incompatible type: expected `int - int` but found `%s - %s`\n", Info.file,
					n.e1.line_number, t1, t2);
		}
		if (t2 != BaseType.INT) {
			Info.numErrors++;
			System.err.printf("%s:%d: error: incompatible type: expected `int - int` but found `%s - %s`\n", Info.file,
					n.e2.line_number, t1, t2);
		}

		types.put(n, BaseType.INT);
	}

	public void visit(Times n) {
		Type t1 = acceptExp(n.e1);
		Type t2 = acceptExp(n.e2);
		if (t1 != BaseType.INT) {
			Info.numErrors++;
			System.err.printf("%s:%d: error: incompatible type: expected `int * int` but found `%s * %s`\n", Info.file,
					n.e1.line_number, t1, t2);
		}
		if (t2 != BaseType.INT) {
			Info.numErrors++;
			System.err.printf("%s:%d: error: incompatible type: expected `int * int` but found `%s * %s`\n", Info.file,
					n.e2.line_number, t1, t2);
		}

		types.put(n, BaseType.INT);
	}

	public void visit(ArrayLookup n) {
		Type arraytype = acceptExp(n.e1);
		if (arraytype != BaseType.ARRAY) {
			Info.numErrors++;
			System.err.printf("%s:%d: error: incompatible type: array lookup: %s cannot be converted to int[]\n", Info.file,
					n.e1.line_number, arraytype);
		}

		Type indextype = acceptExp(n.e2);
		if (indextype != BaseType.INT) {
			Info.numErrors++;
			System.err.printf("%s:%d: error: incompatible type: array lookup: %s cannot be converted to int\n", Info.file,
					n.e1.line_number, indextype);
		}

    types.put(n, BaseType.INT);
	}

	public void visit(ArrayLength n) {
		Type arraytype = acceptExp(n.e);
		if (arraytype != BaseType.ARRAY) {
			Info.numErrors++;
			System.err.printf("%s:%d: error: incompatible type: array length: %s cannot be converted to int[]\n", Info.file,
					n.e.line_number, arraytype);
		}

    types.put(n, BaseType.INT);
	}

	public void visit(Call n) {
		Type exptype = acceptExp(n.e);
		if (exptype instanceof ClassType) {
			Info.numErrors++;
			System.err.printf("%s:%d: error: method call on primitive: expecting a class type but received %s\n", Info.file,
					n.e.line_number, exptype.toString());
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
		}

		int len = signature.params.size();
		for (int i = 0; i < len; i++) {
			Type ptype = el.get(i);
			Type stype = signature.params.get(i);
			if (!ptype.subtypeOf(stype)) {
				Info.numErrors++;
				System.err.printf("%s:%d: error: incompatible type: array length: %s cannot be converted to %s\n", Info.file,
						n.el.get(i).line_number, ptype, stype);
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
		}
		types.put(n, BaseType.BOOLEAN);
	}

	public void visit(Identifier n) {}
}
