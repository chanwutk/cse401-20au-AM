package IO;

import java.io.PrintStream;
import java.util.List;
import java.util.stream.Collectors;

import Symbols.Type;

/**
 * Shared global info
 */
public class Error {
	public static PrintStream err = System.err;
	public static String file;
	public static String currentClass;
	public static int numErrors = 0;

	public static void errorNoSymbol(int ln, String type, String symbol) {
        err.printf("%s:%d: error: cannot find symbol\n", Error.file, ln);
        err.printf("  symbol:   %s %s\n", type, symbol);
		numErrors++;
	}

	public static void errorAlreadyDefined(int ln, String type, String symbol, String context) {
		err.printf("%s:%d: error: %s %s is already defined in %s\n", Error.file, ln, type, symbol, context);
		numErrors++;
	}

	public static void errorDuplicateClass(int ln, String symbol) {
		err.printf("%s:%d: error: duplicate class: %s\n", Error.file, ln, symbol);
		Error.numErrors++;
	}

	public static void errorCyclicInheritance(int ln, String symbol) {
		err.printf("%s:%d: error: cyclic inheritance involving %s\n", Error.file, ln, symbol);
		Error.numErrors++;
	}

	public static void errorIncompatibleTypes(int ln, Type expected, Type actual) {
		err.printf("%s:%d: error: incompatible types: %s cannot be converted to %s\n", Error.file, ln, actual, expected);
		Error.numErrors++;
	}

	public static void errorNumberOfParametersWhenOverride(int ln, int expected, int actual) {
		err.printf("%s:%d: error: cannot override method with different number of parameters: expected %d parameters, but received %d parameters\n", Error.file, ln, expected, actual);
		Error.numErrors++;
	}

	public static void errorBadOperand(int ln, String op, Type actual1, Type actual2) {
		err.printf("%s:%d: error: bad operand types for binary operator '%s'\n", Error.file, ln, op);
		err.printf("  first type:  %s\n", actual1);
		err.printf("  second type: %s\n", actual2);
		Error.numErrors++;
	}

	public static void errorNotDereferenceable(int ln, Type actual) {
		err.printf("%s:%d: error: %s cannot be dereferenced\n", Error.file, ln, actual);
		Error.numErrors++;
	}

	public static void errorMethodNotApplicable(int ln, String className, String method, List<Type> formal, List<Type> actual) {
		err.printf("%s:%d: error: method %s in class %s cannot be applied to given types;\n", Error.file, ln, method, className);
		err.printf("  required: %s\n", String.join(",", formal.stream().map(f -> f.toString()).collect(Collectors.toList())));
		err.printf("  found: %s\n", String.join(",", actual.stream().map(f -> f.toString()).collect(Collectors.toList())));
		err.printf("  reason: actual and formal argument lists differ in length\n");
		Error.numErrors++;
	}

	public static void reportLocation() {
		err.printf("  location: class %s\n", Error.currentClass);
	}
}
