import Scanner.scanner;
import Parser.parser;

import java.io.*;
import java.util.List;
import java.util.ArrayList;

import java_cup.runtime.ComplexSymbolFactory;
import java.nio.file.Paths;
import java.nio.file.Files;
import java.nio.charset.Charset;

import static org.junit.Assert.*;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameter;
import org.junit.runners.Parameterized.Parameters;

import AST.Program;
import AST.Visitor.DeclarationVisitor;
import AST.Visitor.TypecheckVisitor;
import IO.Error;
import Symbols.SymbolTable;

@RunWith(Parameterized.class)
public class TestTypecheck {
    public static final String TEST_FILES_LOCATION = "test/resources/Typecheck/";
    public static final String TEST_FILES_INPUT_EXTENSION = ".java";
    public static final String TEST_FILES_EXPECTED_EXTENSION = ".expected";

    @Parameter(0)
    public String name;
    @Parameter(1)
    public Program ast;
    @Parameter(2)
    public String expected;

    @Parameters(name = "{0}")
    public static List<Object[]> data() {
        ComplexSymbolFactory sf = new ComplexSymbolFactory();
        List<Object[]> data = new ArrayList<>();

        File testLocation = new File(TEST_FILES_LOCATION);
        for (File input : testLocation.listFiles(new FilenameFilter() {
            public boolean accept(File dir, String name) {
                return name.endsWith(TEST_FILES_INPUT_EXTENSION);
            }
        })) {
            String inputName = input.getName();
            String name = inputName.substring(0, inputName.length() - TEST_FILES_INPUT_EXTENSION.length());
            String inputPath = input.getPath();
            String expectedPath = inputPath.substring(0, inputPath.length() - TEST_FILES_INPUT_EXTENSION.length())
                    + TEST_FILES_EXPECTED_EXTENSION;
            try {
                var s = new scanner(new FileReader(input), sf);
                var p = new parser(s, sf);
                var ast = (Program) p.parse().value;
                String expected = new String(Files.readAllBytes(Paths.get(expectedPath)), Charset.defaultCharset());
                data.add(new Object[] { name, ast, expected });
            } catch (IOException e) {
                // skip this test
            } catch (Exception e) {
                fail(e.getMessage());
            }
        }

        return data;
    }

    @Test
    public void testTypecheck() {
        var out = new ByteArrayOutputStream();
        Error.err = new PrintStream(out);
        Error.file = name + ".java";
        Error.numErrors = 0;
        var symbols = new SymbolTable();
        ast.accept(new DeclarationVisitor(symbols));
        ast.accept(new TypecheckVisitor(symbols));
        if (Error.numErrors == 0)
            symbols.prettyPrint(Error.err, 0);
		assertEquals(expected, out.toString());
    }
}
