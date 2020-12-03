package Symbols;

import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import java.io.PrintStream;
import java.util.HashMap;

import AST.Identifier;
import AST.IdentifierExp;
import IO.Error;

public class SymbolTable {
	public SymbolTable base() {
		return base;
	}

	public Type getClass(String s, int line_number) {
		ClassInfo info = classes.get(s);
		if (info == null && base != null)
			info = base.classes.get(s);
		if (info != null) {
			return info.type;
		} else if (parent != null) {
			return parent.getClass(s, line_number);
		} else {
			Error.errorNoSymbol(line_number, "class", s);
			Error.reportLocation();
			classes.put(s, new ClassInfo(BaseType.UNKNOWN, this, null));
			return BaseType.UNKNOWN;
		}
	}

	public Type getClass(Identifier id) {
		return getClass(id.s, id.line_number);
	}

	public Signature getMethodIfExists(String id) {
		if (methods.containsKey(id)) {
			return methods.get(id).signature;
		} else if (base != null) {
			return base.getMethodIfExists(id);
		} else {
			return null;
		}
	}

	public Signature getMethod(Identifier id) {
		Signature signature = getMethodIfExists(id.s);
		if (signature != null) {
			return signature;
		} else if (parent != null) {
			return parent.getMethod(id);
		} else {
			Error.errorNoSymbol(id.line_number, "method", id.s);
			Error.reportLocation();
			return null;
		}
	}

	public VarLocation getVariableLocation(String s) {
		VariableInfo info = vars.get(s);
		if (info == null && base != null)
			info = base.vars.get(s);
		if (info != null) {
			return info.location;
		} else {
			assert parent != null;
			return parent.getVariableLocation(s);
		}
	}

	private Type getVariable(String s, int line_number) {
		VariableInfo info = vars.get(s);
		if (info == null && base != null)
			info = base.vars.get(s);
		if (info != null) {
			return info.type;
		} else if (parent != null) {
			return parent.getVariable(s, line_number);
		} else {
			Error.errorNoSymbol(line_number, "variable", s);
			Error.reportLocation();
			vars.put(s, new VariableInfo(BaseType.UNKNOWN));
			return BaseType.UNKNOWN;
		}
	}

	public Type getVariable(Identifier id) {
		return getVariable(id.s, id.line_number);
	}

	public Type getVariable(IdentifierExp id) {
		return getVariable(id.s, id.line_number);
	}

	public void putClass(Identifier id, Type type) throws SymbolException {
		if (classes.containsKey(id.s)) {
			Error.errorDuplicateClass(id.line_number, id.s);
			throw new SymbolException();
		} else {
			SymbolTable base = null;
			if (type instanceof ClassType && ((ClassType) type).base != null)
				base = classes.get(((ClassType) type).base.name).scope;
			classes.put(id.s, new ClassInfo(type, this, base));
		}
	}

	public void putMethod(Identifier id, Signature signature) throws SymbolException {
		if (methods.containsKey(id.s)) {
			Error.errorAlreadyDefined(id.line_number, "method", id.s + "(...)", name);
			throw new SymbolException();
		} else {
			methods.put(id.s, new MethodInfo(signature, this));
		}
	}

	public void putVariable(Identifier id, Type type) throws SymbolException {
		if (vars.containsKey(id.s)) {
			Error.errorAlreadyDefined(id.line_number, "variable", id.s, name);
			throw new SymbolException();
		} else {
			vars.put(id.s, new VariableInfo(type));
		}
	}

	public SymbolTable enterClassScope(String id) {
		ClassInfo info = classes.get(id);
		Error.currentClass = ((ClassType) info.type).name;
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
		this("GLOBAL", null, null);
	}

	public static class SymbolException extends Exception {
		private static final long serialVersionUID = 0;
	}

	public Stream<ClassType> allClasses() {
		return classes.values().stream().map(ci -> (ClassType) ci.type);
	}

	public void prettyPrint(PrintStream out, int indent) {
		Function<Integer, PrintStream> print = i -> {
			for (int j = 0; j < i; j++)
				out.print("    ");
			return out;
		};
		// print vars
		if (base != null)
			base.vars.entrySet().stream().filter(e -> !vars.containsKey(e.getKey()))
					.forEach(e -> print.apply(indent).printf("VAR %s: %s\n", e.getKey(), e.getValue().type));
		vars.entrySet().stream()
				.forEach(e -> print.apply(indent).printf("VAR %s: %s\n", e.getKey(), e.getValue().type));
		// print classes
		if (base != null)
			base.classes.entrySet().stream().filter(e -> !classes.containsKey(e.getKey())).forEach(e -> {
				print.apply(indent).printf("CLASS %s:\n", e.getKey());
				e.getValue().scope.prettyPrint(out, indent + 1);
			});
		classes.entrySet().stream().forEach(e -> {
			print.apply(indent).printf("CLASS %s\n", e.getKey());
			e.getValue().scope.prettyPrint(out, indent + 1);
		});
		// print methods
		if (base != null)
			base.methods.entrySet().stream().filter(e -> !methods.containsKey(e.getKey())).forEach(e -> {
				print.apply(indent)
						.printf("METHOD %s(%s): %s\n", e.getKey(), String.join(", ", e.getValue().signature.params
								.stream().map(f -> f.toString()).collect(Collectors.toList())),
								e.getValue().signature.ret);
				e.getValue().scope.prettyPrint(out, indent + 1);
			});
		methods.entrySet().stream().forEach(e -> {
			print.apply(indent).printf("METHOD %s(%s): %s\n", e.getKey(),
					String.join(", ",
							e.getValue().signature.params.stream().map(f -> f.toString()).collect(Collectors.toList())),
					e.getValue().signature.ret);
			e.getValue().scope.prettyPrint(out, indent + 1);
		});
	}

	private final String name;
	private final SymbolTable parent;
	private final SymbolTable base;
	private final Map<String, ClassInfo> classes = new HashMap<>();
	private final Map<String, MethodInfo> methods = new HashMap<>();
	private final Map<String, VariableInfo> vars = new HashMap<>();

	private SymbolTable(String name, SymbolTable parent, SymbolTable base) {
		this.name = name;
		this.parent = parent;
		this.base = base;
		assert base == null || parent == base.parent;
	}

	public static class ClassInfo {
		final SymbolTable scope;
		final Type type;

		public ClassInfo(Type type, SymbolTable parent, SymbolTable base) {
			String name = type instanceof ClassType ? ((ClassType) type).name : "UNKNOWN";
			this.scope = new SymbolTable("class" + name, parent, base);
			this.type = type;
		}
	}

	private static class MethodInfo {
		final SymbolTable scope;
		final Signature signature;

		public MethodInfo(Signature signature, SymbolTable parent) {
			this.scope = new SymbolTable("method " + signature.name + "(...)", parent, null);
			this.signature = signature;
		}
	}

	private static class VariableInfo {
		public final Type type;

		// used by codegen
		public final VarLocation location = new VarLocation();

		public VariableInfo(Type type) {
			this.type = type;
		}
	}

	public static class VarLocation {
		public int offset;
		public Type type;

		public static enum Type {
			THIS, RBP
		}
	}
}
