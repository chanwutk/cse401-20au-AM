package AST.Visitor;

import java.util.Map;
import java.util.HashMap;

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
		n.e.accept(this);
		Type condtype = types.get(n.e);
		if (condtype != BaseType.BOOLEAN) {
			Info.numErrors++;
			System.err.printf("%s:%d: error: incompatible type: if condition: %s cannot be converted to boolean\n", Info.file,
					n.e.line_number, condtype);
		}

		n.s1.accept(this);
		n.s2.accept(this);
	}

	public void visit(While n) {
		n.e.accept(this);
		Type condtype = types.get(n.e);
		if (condtype != BaseType.BOOLEAN) {
			Info.numErrors++;
			System.err.printf("%s:%d: error: incompatible type: while-loop condition: %s cannot be converted to boolean\n", Info.file,
					n.e.line_number, condtype);
		}

		n.s.accept(this);
	}

	public void visit(Print n) {
		n.e.accept(this);
		Type exptype = types.get(n.e);
		if (exptype != BaseType.INT) {
			Info.numErrors++;
			System.err.printf("%s:%d: error: incompatible type: print: %s cannot be converted to int\n", Info.file,
					n.e.line_number, exptype);
		}
	}

	public void visit(Assign n) {
		n.e.accept(this);
		Type idType = symbols.getVariable(n.i);
		Type expType = types.get(n.e);
		if (!expType.subtypeOf(idType)) {
			Info.numErrors++;
			System.err.printf("%s:%d: error: incompatible types: assign: %s cannot be converted to %s\n", Info.file,
					n.e.line_number, expType, idType);
		}
	}

	public void visit(ArrayAssign n) {
		n.e1.accept(this);
		n.e2.accept(this);
		Type idtype = symbols.getVariable(n.i);
		Type indextype = types.get(n.e1);
		Type exptype = types.get(n.e2);

		if (idtype != BaseType.ARRAY) {
			Info.numErrors++;
			System.err.printf("%s:%d: error: incompatible type: array-assign: %s cannot be converted to int[]\n", Info.file,
					n.i.line_number, idtype);
		}

		if (indextype != BaseType.INT) {
			Info.numErrors++;
			System.err.printf("%s:%d: error: incompatible type: array-assign: %s cannot be converted to int\n", Info.file,
					n.e1.line_number, indextype);
		}

		if (exptype != BaseType.INT) {
			Info.numErrors++;
			System.err.printf("%s:%d: error: incompatible type: array-assign: %s cannot be converted to int\n", Info.file,
					n.e2.line_number, exptype);
		}
	}

	public void visit(And n) {
		n.e1.accept(this);
		n.e2.accept(this);

		Type t1 = types.get(n.e1);
		Type t2 = types.get(n.e2);
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
		n.e1.accept(this);
		n.e2.accept(this);

		Type t1 = types.get(n.e1);
		Type t2 = types.get(n.e2);
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
		n.e1.accept(this);
		n.e2.accept(this);

		Type t1 = types.get(n.e1);
		Type t2 = types.get(n.e2);
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
		n.e1.accept(this);
		n.e2.accept(this);

		Type t1 = types.get(n.e1);
		Type t2 = types.get(n.e2);
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
		n.e1.accept(this);
		n.e2.accept(this);

		Type t1 = types.get(n.e1);
		Type t2 = types.get(n.e2);
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
		n.e1.accept(this);
		n.e2.accept(this);

		Type arraytype = types.get(n.e1);
		if (arraytype != BaseType.ARRAY) {
			Info.numErrors++;
			System.err.printf("%s:%d: error: incompatible type: array lookup: %s cannot be converted to int[]\n", Info.file,
					n.e1.line_number, arraytype);
		}

		Type indextype = types.get(n.e2);
		if (indextype != BaseType.INT) {
			Info.numErrors++;
			System.err.printf("%s:%d: error: incompatible type: array lookup: %s cannot be converted to int\n", Info.file,
					n.e1.line_number, indextype);
		}

    types.put(n, BaseType.INT);
	}

	public void visit(ArrayLength n) {
		n.e.accept(this);
		Type arraytype = types.get(n.e);
		if (arraytype != BaseType.ARRAY) {
			Info.numErrors++;
			System.err.printf("%s:%d: error: incompatible type: array length: %s cannot be converted to int[]\n", Info.file,
					n.e.line_number, arraytype);
		}

    types.put(n, BaseType.INT);
	}

	public void visit(Call n) {
		n.e.accept(this);
		n.el.stream().forEach(e -> e.accept(this));
		n.i.accept(this);

		Type itype = types.get(n.e);
		if (itype instanceof ClassType) {
			Info.numErrors++;
			System.err.printf("%s:%d: error: method call on primitive: expecting a class type but received %s\n", Info.file,
					n.e.line_number, itype.toString());
		}
		ClassType objtype = (ClassType) itype;
		Signature signature = objtype.getMethod(n.i.s);
		if (signature == null) return;

		ExpList el = n.el;
		if (signature.params.size() != el.size()) {
			Info.numErrors++;
			System.err.printf("%s:%d: error: incorrect number of parameter(s): %s expected %d parameter(s) but received %d parameter(s)\n", Info.file,
					n.i.line_number, n.i.s, signature.params.size(), el.size());
		}

		int len = signature.params.size();
		for (int i = 0; i < len; i++) {
			Exp e = el.get(i);
			Type ptype = types.get(e);
			Type stype = signature.params.get(i);
			if (!ptype.subtypeOf(stype)) {
				Info.numErrors++;
				System.err.printf("%s:%d: error: incompatible type: array length: %s cannot be converted to %s\n", Info.file,
						e.line_number, ptype, stype);
			}
		}
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
		n.e.accept(this);
		Type indextype = types.get(n.e);
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
		n.e.accept(this);
		Type t = types.get(n.e);
		if (t != BaseType.BOOLEAN) {
			Info.numErrors++;
			System.err.printf("%s:%d: error: incompatible type: not (!): %s cannot be converted to boolean\n", Info.file,
					n.e.line_number, t);
		}
		types.put(n, BaseType.BOOLEAN);
	}

	public void visit(Identifier n) {}
}
