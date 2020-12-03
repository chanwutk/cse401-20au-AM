package IO;

import java.io.PrintStream;
import java.util.List;

public class Asm {
	public static PrintStream out = System.out;

	/** word size */
	public static final int WS = 8;

	public static final String rip = "%rip";
	public static final String rsp = "%rsp";
	public static final String rbp = "%rbp";
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

	public static String litLabel(String label) {
		return label + "(%rip)";
	}

	public static String mem(String base, String index, int offset) {
		String asm = base;
		if (index != null)
			asm = asm + ", " + index + ", " + WS;
		asm = "(" + asm + ")";
		if (offset != 0)
			asm = (offset * WS) + asm;
		return asm;
	}

	public static void rodata() {
		out.println("    .section .data.rel.ro");
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

	public static void fieldString(String val) {
		out.printf("    .string \"%s\"\n", val);
	}

	public static void text() {
		out.println("    .text");
		out.println("    .globl asm_main");
	}

	public static void label(String name) {
		out.printf("%s:\n", name);
	}

	public static void mov(String src, String dst) {
		out.printf("    movq %s, %s\n", src, dst);
	}

	public static void lea(String src, String dst) {
		out.printf("    lea %s, %s\n", src, dst);
	}

	public static void push(String val) {
		out.printf("    push %s\n", val);
	}

	public static void pop(String val) {
		out.printf("    pop %s\n", val);
	}

	public static void leave() {
		out.printf("    leave\n");
	}

	public static void ret() {
		out.printf("    ret\n");
	}

	public static void callc(String c) {
		out.printf("    call %s\n", c);
	}

	public static void call(String method) {
		out.printf("    call *%s\n", method);
	}

	public static void test(String val) {
		out.printf("    test %s, %s\n", val, val);
	}

	public static void cmp(String val1, String val2) {
		out.printf("    cmp %s, %s\n", val1, val2);
	}

	public static void jmp(String target) {
		out.printf("    jmp %s\n", target);
	}

	public static void je(String target) {
		out.printf("    je %s\n", target);
	}

	public static void jne(String target) {
		out.printf("    jne %s\n", target);
	}

	public static void setl(String reg) {
		out.printf("    setl %%al\n");
		out.printf("    movzbq %%al, %s\n", reg);
	}

	public static void add(String src, String dst) {
		out.printf("    add %s, %s\n", src, dst);
	}

	public static void sub(String src, String dst) {
		out.printf("    sub %s, %s\n", src, dst);
	}

	public static void imul(String src, String dst) {
		out.printf("    imul %s, %s\n", src, dst);
	}

	public static void xor(String src, String dst) {
		out.printf("    xor %s, %s\n", src, dst);
	}

	// rdi reserved for this
	public static final List<String> ARGS = List.of(rsi, rdx, rcx, r8, r9);

	public static int numRegArgs(int numArgs) {
		return numArgs < ARGS.size() ? numArgs : ARGS.size();
	}

	public static int numStackArgs(int numArgs) {
		return numArgs < ARGS.size() ? 0 : numArgs - ARGS.size();
	}

	public static final String ARRAYINDEXOUTOFBOUND_MSG = ".$ArrayIndexOutOfBound$msg";
	public static final String ARRAYINDEXOUTOFBOUND_HANDLER = ".$ArrayIndexOutOfBound$handler";
}
