package Info;

import java.util.List;
import java.util.stream.Collectors;

import Symbols.Type;

/**
 * Shared global info
 */
public class Info {
	public static String file;
	public static String currentClass;
	public static int numErrors = 0;

	public static void errorNoSymbol(int ln, String type, String symbol) {
        System.err.printf("%s:%d: error: cannot find symbol\n", Info.file, ln);
        System.err.printf("  symbol:   %s %s\n", type, symbol);
		numErrors++;
	}

	public static void errorAlreadyDefined(int ln, String type, String symbol, String context) {
		System.err.printf("%s:%d: error: %s %s is already defined in %s\n", Info.file, ln, type, symbol, context);
		numErrors++;
	}

	public static void errorDuplicateClass(int ln, String symbol) {
		System.err.printf("%s:%d: error: duplicate class: %s\n", Info.file, ln, symbol);
		Info.numErrors++;
	}

	public static void errorCyclicInheritance(int ln, String symbol) {
		System.err.printf("%s:%d: error: cyclic inheritance involving %s\n", Info.file, ln, symbol);
		Info.numErrors++;
	}

	public static void errorIncompatibleTypes(int ln, Type expected, Type actual) {
		System.err.printf("%s:%d: error: incompatible types: %s cannot be converted to %s\n", Info.file, ln, actual, expected);
		Info.numErrors++;
	}

	public static void errorBadOperand(int ln, String op, Type actual1, Type actual2) {
		System.err.printf("%s:%d: error: bad operand types for binary operator '%s'\n", Info.file, ln, op);
		System.err.printf("  first type:  %s\n", actual1);
		System.err.printf("  second type: %s\n", actual2);
		Info.numErrors++;
	}

	public static void errorNotDereferenceable(int ln, Type actual) {
		System.err.printf("%s:%d: error: %s cannot be dereferenced\n", Info.file, ln, actual);
		Info.numErrors++;
	}

	public static void errorMethodNotApplicable(int ln, String className, String method, List<Type> formal, List<Type> actual) {
		System.err.printf("%s:%d: error: method %s in class %s cannot be applied to given types;\n", Info.file, ln, method, className);
		System.err.printf("  required: %s\n", String.join(",", formal.stream().map(f -> f.toString()).collect(Collectors.toList())));
		System.err.printf("  found: %s\n", String.join(",", actual.stream().map(f -> f.toString()).collect(Collectors.toList())));
		System.err.printf("  reason: actual and formal argument lists differ in length\n");
		Info.numErrors++;
	}

	public static void reportLocation() {
		System.err.printf("  location: class %s\n", Info.currentClass);
	}
}
