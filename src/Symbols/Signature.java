package Symbols;

import java.util.List;

public class Signature {
	public final Type ret;
	public final List<Type> params;

	public Signature(Type ret, List<Type> params) {
		this.ret = ret;
		this.params = params;
	}
}
