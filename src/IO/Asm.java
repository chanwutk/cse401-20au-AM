package IO;

import java.io.PrintStream;
import java.util.List;

public class Asm {
	public static PrintStream out = System.out;

	/** word size */
	public static final int WS = 8;

	public static final String rsp = "%rsp";
	public static final String rax = "%rax";
	public static final String rdi = "%rdi";
	public static final String rsi = "%rsi";
	public static final String rdx = "%rdx";
	public static final String rcx = "%rcx";
	public static final String r8 = "%r8";
	public static final String r9 = "%r9";

	public static String lit(int num) {
		return "$" + num;
	}

	public static String mem(String base, String index, int scale, int offset) {
		return offset + "(" + base + ", " + index + ", " + scale + ")";
	}

	public static void rodata() {
		out.println("    .rodata");
	}

	public static String vtable(String c) {
		return c + "$";
	}

	public static String method(String c, String m) {
		return vtable(c) + m;
	}

	public static void field(String val) {
		out.printf("    .quad %s\n", val);
	}

	public static void text() {
		out.println("    .text");
		out.println("    .globl asm_main");
	}

	public static void label(String name) {
		out.printf("%s:\n", name);
	}

	public static void mov(String src, String dst) {
		out.printf("    mov %s, %s\n", src, dst);
	}

	public static void push(String val) {
		out.printf("    push %s\n", val);
	}

	public static void ret() {
		out.printf("    ret\n");
	}

	public static void put(String arg, boolean aligned) {
		if (!aligned)
			sub(lit(WS), rsp);
		mov(arg, ARGS.get(0));
		out.printf("    call put\n");
		if (!aligned)
			add(lit(WS), rsp);
	}

	public static void call(String method, List<String> args, boolean aligned) {
		int numStackArgs = args.size() <= ARGS.size() ? 0 : args.size() - ARGS.size();
		aligned ^= numStackArgs % 2 == 0;
		if (!aligned) {
			sub(lit(WS), rsp);
			numStackArgs++;
		}
		for (int i = args.size() - 1; i >= 0; i--) {
			if (i >= ARGS.size())
				push(args.get(i));
			else
				mov(args.get(i), ARGS.get(i));
		}
		out.printf("    call *%s\n", method);
		if (numStackArgs > 0)
			add(lit(numStackArgs * WS), rsp);
	}

	public static void add(String src, String dst) {
		out.printf("    add %s, %s\n", src, dst);
	}

	public static void sub(String src, String dst) {
		out.printf("    sub %s, %s\n", src, dst);
	}

	public static final List<String> ARGS = List.of(rdi, rsi, rdx, rcx, r8, r9);
}
