import Scanner.*;
import java.io.*;
import java.util.*;

import Parser.sym;
import java_cup.runtime.Symbol;
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

@RunWith(Parameterized.class)
public class TestScanner {
    public static final String TEST_FILES_LOCATION = "test/resources/Scanner/";
    public static final String TEST_FILES_INPUT_EXTENSION = ".java";
    public static final String TEST_FILES_EXPECTED_EXTENSION = ".expected";

    @Parameter(0)
    public String name;
    @Parameter(1)
    public scanner scanner;
    @Parameter(2)
    public String[] expected;

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
                scanner scanner = new scanner(new FileReader(input), sf);
                String[] expected = new String(Files.readAllBytes(Paths.get(expectedPath)), Charset.defaultCharset())
                        .split(" ");
                data.add(new Object[] { name, scanner, expected });
            } catch (IOException e) {
                // skip this test
            }
        }

        return data;
    }

    @Test
    public void testScanner() {
        try {
            for (String e : expected) {
                Symbol t = scanner.next_token();
                assertEquals(e, scanner.symbolToString(t));
            }
            assertEquals(sym.EOF, scanner.next_token().sym);
        } catch (IOException e) {
            fail(e.getMessage());
        }
    }
}
