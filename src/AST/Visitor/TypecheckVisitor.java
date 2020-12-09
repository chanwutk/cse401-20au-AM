package AST.Visitor;

import java.util.List;
import java.util.stream.Collectors;

import AST.*;
import IO.Error;
import Symbols.BaseType;
import Symbols.ClassType;
import Symbols.Signature;
import Symbols.SymbolTable;
import Symbols.Type;

public class TypecheckVisitor extends AbstractVisitor {
	private SymbolTable symbols;
	private Type expType = null;

	public TypecheckVisitor(SymbolTable symbols) {
		this.symbols = symbols;
	}

	public Type typeof(Exp n) {
		n.accept(this);
		return expType;
	}

	private void check(int ln, Type supertype, Type subtype) {
		if (!subtype.subtypeOf(supertype))
			Error.errorIncompatibleTypes(ln, supertype, subtype);
	}

	private void check(int ln, String op, Type expected1, Type expected2, Type actual1, Type actual2) {
		if (!(actual1.subtypeOf(expected1) && actual2.subtypeOf(expected2)))
			Error.errorBadOperand(ln, op, actual1, actual2);
	}

	public void visit(Program n) {
		n.m.accept(this);
		n.cl.stream().forEach(cd -> cd.accept(this));
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
		SymbolTable base = symbols.base();
		Signature _signature;
		if (base != null && (_signature = base.getMethodIfExists(n.i.s)) != null) {
			Signature signature = symbols.getMethodIfExists(n.i.s);
			List<Type> _params = _signature.params;
			List<Type> params = signature.params;
			int len = params.size();
			if (_params.size() != len) {
				Error.errorNumberOfParametersWhenOverride(n.i.line_number, _params.size(), len);
			} else {
				check(n.i.line_number, _signature.ret, signature.ret);
				for (int i = 0; i < len; i++) {
					check(n.i.line_number, params.get(i), _params.get(i));
				}
			}
		}
		symbols = symbols.enterMethodScope(n.i.s);
		n.sl.stream().forEach(s -> s.accept(this));
		check(n.e.line_number, symbols.getMethod(n.i).ret, typeof(n.e));
		symbols = symbols.exitScope();
	}

	public void visit(Block n) {
		n.sl.stream().forEach(s -> s.accept(this));
	}

	public void visit(TryCatch n) {
		n.s1.stream().forEach(s -> s.accept(this));
		String decl = symbols.getVariableDeclaration(n.i.s);
		if (decl != null) {
			Error.errorAlreadyDefined(n.i.line_number, "Exception", n.i.s, decl);
		}
		n.s1.stream().forEach(s -> s.accept(this));
	}

	public void visit(If n) {
		check(n.e.line_number, BaseType.BOOLEAN, typeof(n.e));
		n.s1.accept(this);
		n.s2.accept(this);
	}

	public void visit(While n) {
		check(n.e.line_number, BaseType.BOOLEAN, typeof(n.e));
		n.s.accept(this);
	}

	public void visit(Print n) {
		check(n.e.line_number, BaseType.INT, typeof(n.e));
	}

	public void visit(Assign n) {
		check(n.e.line_number, symbols.getVariable(n.i), typeof(n.e));
	}

	public void visit(ArrayAssign n) {
		check(n.i.line_number, BaseType.ARRAY, symbols.getVariable(n.i));
		check(n.e1.line_number, BaseType.INT, typeof(n.e1));
		check(n.e2.line_number, BaseType.INT, typeof(n.e2));
	}

	public void visit(And n) {
		check(n.line_number, "&&", BaseType.BOOLEAN, BaseType.BOOLEAN, typeof(n.e1), typeof(n.e2));
		expType = BaseType.BOOLEAN;
	}

	public void visit(LessThan n) {
		check(n.line_number, "<", BaseType.INT, BaseType.INT, typeof(n.e1), typeof(n.e2));
		expType = BaseType.BOOLEAN;
	}

	public void visit(Plus n) {
		check(n.line_number, "+", BaseType.INT, BaseType.INT, typeof(n.e1), typeof(n.e2));
		expType = BaseType.INT;
	}

	public void visit(Minus n) {
		check(n.line_number, "-", BaseType.INT, BaseType.INT, typeof(n.e1), typeof(n.e2));
		expType = BaseType.INT;
	}

	public void visit(Times n) {
		check(n.line_number, "*", BaseType.INT, BaseType.INT, typeof(n.e1), typeof(n.e2));
		expType = BaseType.INT;
	}

	public void visit(ArrayLookup n) {
		check(n.e1.line_number, BaseType.ARRAY, typeof(n.e1));
		check(n.e2.line_number, BaseType.INT, typeof(n.e2));
		expType = BaseType.INT;
	}

	public void visit(ArrayLength n) {
		check(n.e.line_number, BaseType.ARRAY, typeof(n.e));
		expType = BaseType.INT;
	}

	public void visit(Call n) {
		Type argType = typeof(n.e);
		if (!(argType instanceof ClassType)) {
			if (argType != BaseType.UNKNOWN)
				Error.errorNotDereferenceable(n.e.line_number, argType);
			expType = BaseType.UNKNOWN;
		} else {
			ClassType objType = (ClassType) argType;
			Signature signature = objType.getMethod(n.i);
			if (signature == null) {
				expType = BaseType.UNKNOWN;
			} else if (signature.params.size() != n.el.size()) {
				Error.errorMethodNotApplicable(n.el.line_number, objType.name, n.i.s, signature.params,
						n.el.stream().map(this::typeof).collect(Collectors.toList()));
				expType = BaseType.UNKNOWN;
			} else {
				for (int i = 0; i < signature.params.size(); i++)
					check(n.el.get(i).line_number, signature.params.get(i), typeof(n.el.get(i)));
				expType = signature.ret;
			}
		}
	}

	public void visit(IntegerLiteral n) {
		expType = BaseType.INT;
	}

	public void visit(True n) {
		expType = BaseType.BOOLEAN;
	}

	public void visit(False n) {
		expType = BaseType.BOOLEAN;
	}

	public void visit(IdentifierExp n) {
		expType = symbols.getVariable(n);
	}

	public void visit(This n) {
		expType = symbols.getClass(Error.currentClass, n.line_number);
	}

	public void visit(NewArray n) {
		check(n.e.line_number, BaseType.INT, typeof(n.e));
		expType = BaseType.ARRAY;
	}

	public void visit(NewObject n) {
		expType = symbols.getClass(n.i);
	}

	public void visit(Not n) {
		check(n.e.line_number, BaseType.BOOLEAN, typeof(n.e));
		expType = BaseType.BOOLEAN;
	}
}
