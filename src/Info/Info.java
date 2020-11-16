package Info;

/**
 * Shared global info
 */
public class Info {
	public static String file;
	public static String currentClass;
	public static int numErrors = 0;

	public static void errorInClass() {
		System.err.printf("  location: class %s\n", Info.currentClass);
	}
}
