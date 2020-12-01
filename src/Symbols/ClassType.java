package Symbols;

import java.util.Map;

import AST.Identifier;
import IO.Error;

import java.util.HashMap;

public class ClassType implements Type {
	public final String name;
	public final ClassType base;
	// only methods, not fields, can be accessed by `x.y` dot natation
	private final Map<String, Signature> methods = new HashMap<>();

	public ClassType(String name) {
		this(name, null);
	}

	public ClassType(String name, ClassType base) {
		this.name = name;
		this.base = base;
	}

	public void putMethod(String name, Signature signature) {
		methods.put(name, signature);
	}

	public Signature getMethod(Identifier id) {
		if (methods.containsKey(id.s)) {
			return methods.get(id.s);
		} else if (base != null) {
			return base.getMethod(id);
		} else {
			Error.errorNoSymbol(id.line_number, "method", id.s + "(...)");
			return null;
		}
	}

	@Override
	public String toString() {
		return name;
	}

	@Override
	public boolean subtypeOf(Type that) {
		if (this == that || that == BaseType.UNKNOWN)
			return true;
		else if (base == null)
			return false;
		else
			return base.subtypeOf(that);
	}
}
