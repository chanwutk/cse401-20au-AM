package Symbols;

import java.util.Map;
import java.util.HashMap;
import AST.Identifier;
import AST.IdentifierExp;
import Info.Info;

public class SymbolTable {
	public Type getClass(String s, int line_number) {
		ClassInfo info = classes.get(s);
		if (info != null) {
			return info.type;
		} else if (base != null) {
			Type ret = base.getClass(s, line_number);
			if (ret instanceof ClassType) return ret;
		}
		
		if (parent != null) {
			return parent.getClass(s, line_number);
		} else {
			System.err.printf("%s:%d: error: cannot find symbol", Info.file, line_number);
			System.err.printf("  symbol:   class %s\n", s);
			System.err.printf("  location: class %s\n", Info.currentClass);
			Info.numErrors++;
			putClass(s, line_number, BaseType.UNKNOWN);
			return BaseType.UNKNOWN;
		}
	}

	public Type getClass(Identifier id) {
		return getClass(id.s, id.line_number);
	}

	public Signature getMethod(Identifier id) {
		MethodInfo info = methods.get(id.s);
		if (info != null) {
			return info.signature;
		} else if (base != null) {
			Signature ret = base.getMethod(id);
			if (ret != null) return ret;
		}
		
		if (parent != null) {
			return parent.getMethod(id);
		} else {
			System.err.printf("%s:%d: error: cannot find symbol", Info.file, id.line_number);
			System.err.printf("  symbol:   method %s\n", id.s);
			System.err.printf("  location: class %s\n", Info.currentClass);
			Info.numErrors++;
			return null;
		}
	}

	private Type getVariable(String s, int line_number) {
		VariableInfo info = vars.get(s);
		if (info != null) {
			return info.type;
		} else if (base != null) {
			Type ret = base.getVariable(s, line_number);
			if (ret != null && ret != BaseType.UNKNOWN) return ret;
		}
		
		if (parent != null) {
			return parent.getVariable(s, line_number);
		} else {
			System.err.printf("%s:%d: error: cannot find symbol", Info.file, line_number);
			System.err.printf("  symbol:   variable %s\n", s);
			System.err.printf("  location: class %s\n", Info.currentClass);
			Info.numErrors++;
			putVariable(s, line_number, BaseType.UNKNOWN);
			return BaseType.UNKNOWN;
		}
	}

	public Type getVariable(Identifier id) {
		return getVariable(id.s, id.line_number);
	}

	public Type getVariable(IdentifierExp id) {
		return getVariable(id.s, id.line_number);
	}

	public void putClass(Identifier id, Type type) {
		putClass(id.s, id.line_number, type);
	}

	private void putClass(String id, int line_number, Type type) {
		if (classes.containsKey(id)) {
			System.err.printf("%s:%d: error: class already exist", Info.file, line_number);
			System.err.printf("  symbol:    class %s\n", id);
			System.err.printf("  location:  class %s\n", Info.currentClass);
			Info.numErrors++;
			return;
		}

		SymbolTable base = null;
		if (type instanceof ClassType && ((ClassType) type).base != null) {
			base = classes.get(((ClassType) type).base.name).scope;
		}
		classes.put(id, new ClassInfo(type, this, base));
	}

	public void putMethod(Identifier id, Signature signature) {
		if (methods.containsKey(id.s)) {
			System.err.printf("%s:%d: error: method already exist", Info.file, id.line_number);
			System.err.printf("  symbol:  method %s\n", id.s);
			System.err.printf("  location: class %s\n", Info.currentClass);
			Info.numErrors++;
		}
		methods.put(id.s, new MethodInfo(signature, this));
	}

	public void putVariable(Identifier id, Type type) {
		putVariable(id.s, id.line_number, type);
	}

	private void putVariable(String id, int line_number, Type type) {
		if (vars.containsKey(id)) {
			System.err.printf("%s:%d: error: variable already exist", Info.file, line_number);
			System.err.printf("  symbol: variable %s\n", id);
			System.err.printf("  location:  class %s\n", Info.currentClass);
			Info.numErrors++;
		}
		vars.put(id, new VariableInfo(type));
	}

	public SymbolTable enterClassScope(String id) {
		ClassInfo info = classes.get(id);
		Info.currentClass = ((ClassType) info.type).name;
		return info.scope;
	}

	public SymbolTable enterMethodScope(String id) {
		MethodInfo info = methods.get(id);
		return info.scope;
	}

	public SymbolTable exitScope() {
		return parent;
	}

	public SymbolTable() {
		this(null);
	}

	private final SymbolTable parent;
	private final SymbolTable base;
	private final Map<String, ClassInfo> classes = new HashMap<>();
	private final Map<String, MethodInfo> methods = new HashMap<>();
	private final Map<String, VariableInfo> vars = new HashMap<>();

	private SymbolTable(SymbolTable parent) {
		this(parent, null);
	}

	private SymbolTable(SymbolTable parent, SymbolTable base) {
		this.parent = parent;
		this.base = base;
	}

	public static class ClassInfo {
		final SymbolTable scope;
		final Type type;

		public ClassInfo(Type type, SymbolTable parent, SymbolTable base) {
			this.scope = new SymbolTable(parent, base);
			this.type = type;
		}
	}

	private static class MethodInfo {
		final SymbolTable scope;
		final Signature signature;

		public MethodInfo(Signature signature, SymbolTable parent) {
			this.scope = new SymbolTable(parent);
			this.signature = signature;
		}
	}

	private static class VariableInfo {
		public final Type type;

		public VariableInfo(Type type) {
			this.type = type;
		}
	}
}
