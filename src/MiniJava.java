import java.io.FileReader;
import java_cup.runtime.Symbol;
import java_cup.runtime.ComplexSymbolFactory;

import Scanner.scanner;
import Parser.sym;

public class MiniJava {
	public static void main(String[] args) {
		if (args.length != 2 || !args[0].equals("-S")) {
			System.err.println("Usage: MiniJava -S <filename>");
			System.exit(1);
		}
		String file = args[1];

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

		System.exit(error ? 1 : 0);
	}
}
