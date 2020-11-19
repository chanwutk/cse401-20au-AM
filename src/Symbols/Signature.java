package Symbols;

import java.util.List;

public class Signature {
	public final String name;
	public final Type ret;
	public final List<Type> params;

	public Signature(String name, Type ret, List<Type> params) {
		this.name = name;
		this.ret = ret;
		this.params = params;
	}
}
