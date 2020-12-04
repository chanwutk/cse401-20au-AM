import Scanner.scanner;
import Parser.parser;

import java.io.*;
import java.util.List;
import java.util.ArrayList;

import java_cup.runtime.ComplexSymbolFactory;
import java.nio.file.Paths;
import java.nio.file.Path;
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
import AST.Visitor.VtableVisitor;
import AST.Visitor.CodegenVisitor;
import IO.Asm;
import IO.Error;
import Symbols.SymbolTable;

@RunWith(Parameterized.class)
public class TestCodegen {
    public static final String RUNTIME_LOCATION = "src/runtime/";
    public static final String RUNTIME_BOOT = "boot.c";
    public static final String ASM_EXTENSION = ".s";

    public static final String TEST_FILES_LOCATION = "test/resources/CodeGen/";
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
        File[] inputs = testLocation.listFiles((dir, name) -> name.endsWith(TEST_FILES_INPUT_EXTENSION));
        for (File input : inputs) {
            String inputName = input.getName();
            String name = inputName.substring(0, inputName.length() - TEST_FILES_INPUT_EXTENSION.length());
            String inputPath = input.getPath();
            String expectedPath = inputPath.substring(0, inputPath.length() - TEST_FILES_INPUT_EXTENSION.length())
                    + TEST_FILES_EXPECTED_EXTENSION;
            try {
                var s = new scanner(new FileReader(input), sf);
                var p = new parser(s, sf);
                var ast = (Program) p.parse().value;
                String expected = null;
                if (Files.exists(Paths.get(expectedPath))) {
                    expected = new String(Files.readAllBytes(Paths.get(expectedPath)), Charset.defaultCharset());
                }
                data.add(new Object[] { name, ast, expected });
            } catch (IOException e) {
                // skip this test
            } catch (Exception e) {
                fail(e.getMessage());
            }
        }

        return data;
    }

    public static String minijavaBuildCommand(String execPath, String asmPath) {
        return "gcc -o " + execPath + " " + asmPath + " " + RUNTIME_LOCATION + RUNTIME_BOOT;
    }

    public static String javaBuildComman(String path) {
        return "javac " + path;
    }

    public static String minijavaRunCommand(String path) {
        return "./" + path;
    }

    public static String javaRunCommand(String path) {
        return "java " + path;
    }

    public static String execute(String command, int error) throws IOException, InterruptedException {
        var process = Runtime.getRuntime().exec(command);
        var br = new BufferedReader(new InputStreamReader(process.getInputStream()));
        String ret = "";
        String line;
        while ((line = br.readLine()) != null) {
            ret += line + "\n";
        }
        var br2 = new BufferedReader(new InputStreamReader(process.getInputStream()));
        while ((line = br2.readLine()) != null) {
            System.out.println(line);
        }
        process.waitFor();
        // assertEquals(error, process.exitValue());
        process.destroy();
        return ret;
    }

    @Test
    public void testCodegen() {
        var err = new ByteArrayOutputStream();
        Error.err = new PrintStream(err);
        Error.file = name + ".java";
        Error.numErrors = 0;

        var out = new ByteArrayOutputStream();
        Asm.out = new PrintStream(System.out);

        var symbols = new SymbolTable();
        ast.accept(new DeclarationVisitor(symbols));
        ast.accept(new TypecheckVisitor(symbols));
        ast.accept(new VtableVisitor(symbols));
        ast.accept(new CodegenVisitor(symbols));

        if (Error.numErrors == 0)
            symbols.prettyPrint(Error.err, 0);
        
        var execName = "__" + name;
        var asmName = execName + ASM_EXTENSION;
        var execPath = RUNTIME_LOCATION + execName;
        var asmPath = RUNTIME_LOCATION + asmName;
        var execJavaPath = TEST_FILES_LOCATION + name;
        var javaPath = execJavaPath + TEST_FILES_INPUT_EXTENSION;

        FileWriter asmFile = null;
        try {
            Files.deleteIfExists(Path.of(asmName));
            Files.deleteIfExists(Path.of(execName));

            (new File(asmPath)).createNewFile();
            asmFile = new FileWriter(asmPath);
            asmFile.write(out.toString());

            var minijavaBuild = execute(minijavaBuildCommand(execName, asmPath), 0);
            System.out.println(minijavaBuild);
            assertEquals("", minijavaBuild);
            var minijavaRun = execute(minijavaRunCommand(execPath), expected == null ? 0 : 1);
            
            if (expected == null) {
                var javaBuild = execute(javaBuildComman(javaPath), 0);
                assertEquals("", javaBuild);
                var javaRun = execute(javaRunCommand(execJavaPath), 0);
                assertEquals(javaRun, minijavaRun);
            } else {
                assertEquals(expected, minijavaRun);
            }
        } catch (IOException e) {
            System.err.println("io error");
            e.printStackTrace();
        } catch (InterruptedException e) {
            System.err.println("interrupted");
            e.printStackTrace();
        } finally {
            try {
                asmFile.close();
            } catch (IOException e) {
                System.err.println("cannot close file");
                e.printStackTrace();
            }
        }
    }
}
