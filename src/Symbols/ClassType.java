package Symbols;

import java.util.Map;

import AST.Identifier;
import IO.Error;

import java.util.HashMap;

public class ClassType implements Type {
	public final String name;
	public final boolean throwable;
	public final ClassType base;
	// only methods, not fields, can be accessed by `x.y` dot natation
	public final Map<String, Signature> methods = new HashMap<>();

	// used by codegen
	public final Map<String, Integer> offset = new HashMap<>();
	public int size;

	public ClassType(String name, boolean ex) {
		this.name = name;
		this.throwable = ex;
		this.base = null;
	}

	public ClassType(String name, ClassType base) {
		this.name = name;
		this.throwable = base.throwable;
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

	public int getOffset(String method) {
		Integer offset = this.offset.get(method);
		if (offset != null)
			return offset;
		assert base != null;
		return base.getOffset(method);
	}

	@Override
	public String toString() {
		return name;
	}

	@Override
	public boolean subtypeOf(Type that) {
		if (this == that || that == BaseType.UNKNOWN)
			return true;
		else if (base != null)
			return base.subtypeOf(that);
		else
			return false;
	}
}
