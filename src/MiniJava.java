import java.io.FileReader;
import java.util.List;
import java.util.TreeSet;
import java.util.Set;

import AST.Program;
import AST.Visitor.*;
import IO.Error;
import java_cup.runtime.Symbol;
import java_cup.runtime.ComplexSymbolFactory;
import Scanner.scanner;
import Symbols.SymbolTable;
import Parser.parser;
import Parser.sym;

public class MiniJava {

	private static Set<String> getFlags(String[] args) {
		if (args.length < 1) {
			return null;
		}

		Set<String> flags = new TreeSet<String>();
		for (int i = 0; i < args.length - 1; i++) {
			String arg = args[i];
			if ((!"-S".equals(arg) && !"-P".equals(arg) && !"-A".equals(arg) && !"-T".equals(arg))
					|| flags.contains(arg)) {
				return null;
			}
			flags.add(arg);

		}

		return flags;
	}

	public static void main(String[] args) {
		Set<String> flags = getFlags(args);
		if (flags == null) {
			System.err.println("Usage: MiniJava [-S] [-A] [-P] [-T] <filename>");
			System.exit(1);
		}

		String file = args[args.length - 1];
		Error.file = file;
		boolean error = false;

		if (flags.isEmpty()) {
			SymbolTable symbols = new SymbolTable();
			error |= parse(file, List.of(new DeclarationVisitor(symbols), new TypecheckVisitor(symbols),
					new VtableVisitor(symbols), new CodegenVisitor(symbols)));
			if (Error.numErrors > 0) {
				System.err.printf("%d error%s\n", Error.numErrors, Error.numErrors > 1 ? "s" : "");
				error = true;
			}
		} else {
			for (String flag : flags) {
				if ("-S".equals(flag)) {
					error |= scan(file);
				} else if ("-P".equals(flag)) {
					error |= parse(file, List.of(new PrettyPrintVisitor()));
				} else if ("-A".equals(flag)) {
					error |= parse(file, List.of(new ASTPrintVisitor()));
				} else if ("-T".equals(flag)) {
					SymbolTable symbols = new SymbolTable();
					error |= parse(file, List.of(new DeclarationVisitor(symbols), new TypecheckVisitor(symbols)));
					if (Error.numErrors > 0) {
						System.err.printf("%d error%s\n", Error.numErrors, Error.numErrors > 1 ? "s" : "");
						error = true;
					} else {
						symbols.prettyPrint(System.out, 0);
					}
				} else {
					assert false;
				}
			}
		}

		System.exit(error ? 1 : 0);
	}

	private static boolean scan(String file) {
		ComplexSymbolFactory sf = new ComplexSymbolFactory();
		boolean error = false;

		try {
			scanner s = new scanner(new FileReader(file), sf);
			for (Symbol t = s.next_token(); t.sym != sym.EOF; t = s.next_token()) {
				System.out.print(s.symbolToString(t) + " ");
				if (t.sym == sym.error) {
					error = true;
				}
			}
			System.out.println();
		} catch (Exception e) {
			System.err.println("Unexpected internal compiler error: " + e.toString());
			e.printStackTrace();
			error = true;
		}

		return error;
	}

	private static boolean parse(String file, List<Visitor> visitors) {
		ComplexSymbolFactory sf = new ComplexSymbolFactory();
		boolean error = false;

		try {
			scanner s = new scanner(new FileReader(file), sf);
			parser p = new parser(s, sf);
			Symbol root = p.parse();
			Program program = (Program) root.value;
			visitors.forEach(visitor -> program.accept(visitor));
		} catch (Exception e) {
			System.err.println("Unexpected internal compiler error: " + e.toString());
			e.printStackTrace();
			error = true;
		}

		return error;
	}
}
