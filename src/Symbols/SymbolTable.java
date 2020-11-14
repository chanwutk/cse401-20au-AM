package Symbols;

import java.util.Map;
import java.util.HashMap;
import AST.Identifier;
import Info.Info;

public class SymbolTable {
	public Type getClass(Identifier id) {
		ClassInfo info = classes.get(id.s);
		if (info != null) {
			return info.type;
		} else if (parent != null) {
			return parent.getClass(id);
		} else {
			System.err.printf("%s:%d: error: cannot find symbol", Info.file, id.line_number);
			System.err.printf("  symbol:   class %s\n", id.s);
			System.err.printf("  location: class %s\n", Info.currentClass);
			Info.numErrors++;
			putClass(id.s, BaseType.UNKNOWN);
			return BaseType.UNKNOWN;
		}
	}

	public Signature getMethod(Identifier id) {
		MethodInfo info = methods.get(id.s);
		if (info != null) {
			return info.signature;
		} else if (parent != null) {
			return parent.getMethod(id);
		} else {
			System.err.printf("%s:%d: error: cannot find symbol", Info.file, id.line_number);
			System.err.printf("  symbol:   method %s\n", id.s);
			System.err.printf("  location: class %s\n", Info.currentClass);
			Info.numErrors++;
			return null;
		}
	}

	public Type getVariable(Identifier id) {
		VariableInfo info = vars.get(id.s);
		if (info != null) {
			return info.type;
		} else if (parent != null) {
			return parent.getVariable(id);
		} else {
			System.err.printf("%s:%d: error: cannot find symbol", Info.file, id.line_number);
			System.err.printf("  symbol:   variable %s\n", id.s);
			System.err.printf("  location: class %s\n", Info.currentClass);
			Info.numErrors++;
			putVariable(id.s, BaseType.UNKNOWN);
			return BaseType.UNKNOWN;
		}
	}

	public void putClass(String id, Type type) {
		classes.put(id, new ClassInfo(type, this));
	}

	public void putMethod(String id, Signature signature) {
		methods.put(id, new MethodInfo(signature, this));
	}

	public void putVariable(String id, Type type) {
		vars.put(id, new VariableInfo(type));
	}

	public boolean containsClass(String id) {
		return classes.containsKey(id);
	}

	public boolean containsMethod(String id) {
		return methods.containsKey(id);
	}

	public boolean containsVariable(String id) {
		return vars.containsKey(id);
	}

	public SymbolTable enterClassScope(String id) {
		ClassInfo info = classes.get(id);
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
	private final Map<String, ClassInfo> classes = new HashMap<>();
	private final Map<String, MethodInfo> methods = new HashMap<>();
	private final Map<String, VariableInfo> vars = new HashMap<>();

	private SymbolTable(SymbolTable parent) {
		this.parent = parent;
	}

	public static class ClassInfo {
		final SymbolTable scope;
		final Type type;

		public ClassInfo(Type type, SymbolTable parent) {
			this.scope = new SymbolTable(parent);
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
