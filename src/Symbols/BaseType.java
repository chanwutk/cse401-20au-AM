package Symbols;

public enum BaseType implements Type {
	INT {
		@Override
		public String toString() {
			return "int";
		}
	},
	BOOLEAN {
		@Override
		public String toString() {
			return "boolean";
		}
	},
	ARRAY {
		@Override
		public String toString() {
			return "int[]";
		}
	},
	UNKNOWN {
		@Override
		public boolean subtypeOf(Type that) {
			return true;
		}

		@Override
		public String toString() {
			return "UNKNOWN";
		}
	};

	@Override
	public boolean subtypeOf(Type that) {
		return this == that || that == BaseType.UNKNOWN;
	}
}
