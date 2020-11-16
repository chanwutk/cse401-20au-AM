package Symbols;

import java.util.Map;
import java.util.HashMap;

public class ClassType implements Type {
	public final String name;
	public ClassType base;
	private final Map<String, Type> fields = new HashMap<>();
	private final Map<String, Signature> methods = new HashMap<>();

	public ClassType(String name) {
		this(name, null);
	}

	public ClassType(String name, ClassType base) {
		this.name = name;
		this.base = base;
	}

	public void setField(String name, Type type) {
		fields.put(name, type);
	}

	public void setMethod(String name, Signature signature) {
		methods.put(name, signature);
	}

	public Type getField(String name) {
		if (fields.containsKey(name)) {
			return fields.get(name);
		}
		
		if (base != null) {
			return base.getField(name);
		}

		return null;
	}

	public Signature getMethod(String name) {
		if (methods.containsKey(name)) {
			return methods.get(name);
		}
		
		if (base != null) {
			return base.getMethod(name);
		}

		return null;
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
