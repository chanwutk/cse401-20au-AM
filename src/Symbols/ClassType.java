package Symbols;

import java.util.Map;
import java.util.HashMap;

public class ClassType implements Type {
	public final String name;
	public final ClassType base;
	public final Map<String, Type> fields = new HashMap<>();
	public final Map<String, Signature> methods = new HashMap<>();

	public ClassType(String name, ClassType base) {
		this.name = name;
		this.base = base;
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
