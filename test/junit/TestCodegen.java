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
import AST.Visitor.AllocationVisitor;
import AST.Visitor.CodegenVisitor;
import IO.Asm;
import IO.Error;
import Symbols.SymbolTable;

@RunWith(Parameterized.class)
public class TestCodegen {
    public static final String RUNTIME_PREFIX = "__runtime_";
    public static final String RUNTIME_LOCATION = "src/runtime/";
    public static final String RUNTIME_BOOT = "boot.c";
    public static final String ASM_EXTENSION = ".s";

    public static final String TEST_FILES_LOCATION = "test/resources/Codegen/";
    public static final String TEST_FILES_INPUT_EXTENSION = ".java";
    public static final String TEST_FILES_EXPECTED_EXTENSION = ".expected";
    public static final String TEST_FILES_CLASS_EXTENSION = ".class";

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

    public static String javaBuildCommand(String path) {
        return "javac " + path;
    }

    public static String minijavaRunCommand(String path) {
        return path;
    }

    public static String javaRunCommand(String path) {
        return "java -cp " + TEST_FILES_LOCATION + " " + path;
    }

    public static List<String> execute(String command, int error) throws IOException, InterruptedException {
        var process = Runtime.getRuntime().exec(command);
        var brOut = new BufferedReader(new InputStreamReader(process.getInputStream()));
        String out = "";
        String line;
        while ((line = brOut.readLine()) != null) {
            out += line + "\n";
        }
        var brErr = new BufferedReader(new InputStreamReader(process.getErrorStream()));
        String err = "";
        while ((line = brErr.readLine()) != null) {
            err += line + "\n";
        }
        process.waitFor();
        assertEquals(error, process.exitValue());
        process.destroy();
        return List.of(out, err);
    }

    @Test
    public void testCodegen() {
        var err = new ByteArrayOutputStream();
        Error.err = new PrintStream(err);
        Error.file = name + ".java";
        Error.numErrors = 0;

        var out = new ByteArrayOutputStream();
        Asm.out = new PrintStream(out);

        var symbols = new SymbolTable();
        ast.accept(new DeclarationVisitor(symbols));
        ast.accept(new TypecheckVisitor(symbols));
        ast.accept(new AllocationVisitor(symbols));
        ast.accept(new CodegenVisitor(symbols));

        if (Error.numErrors == 0)
            symbols.prettyPrint(Error.err, 0);

        var execName = RUNTIME_PREFIX + name;
        var asmName = execName + ASM_EXTENSION;
        var execPath = RUNTIME_LOCATION + execName;
        var asmPath = RUNTIME_LOCATION + asmName;
        var javaPath = TEST_FILES_LOCATION + name + TEST_FILES_INPUT_EXTENSION;

        try {
            Files.deleteIfExists(Path.of(asmName));
            Files.deleteIfExists(Path.of(execName));

            (new File(asmPath)).createNewFile();
            FileWriter asmFile = new FileWriter(asmPath);
            asmFile.write(out.toString());
            asmFile.close();

            var minijavaBuild = execute(minijavaBuildCommand(execPath, asmPath), 0);
            assertEquals("", minijavaBuild.get(0));
            assertEquals("", minijavaBuild.get(1));
            var minijavaRun = execute(minijavaRunCommand(execPath), expected == null ? 0 : 134); // SIGABRT

            if (expected == null) {
                var javaBuild = execute(javaBuildCommand(javaPath), 0);
                assertEquals("", javaBuild.get(0));
                assertEquals("", javaBuild.get(1));
                var javaRun = execute(javaRunCommand(name), 0);
                assertEquals(javaRun.get(0), minijavaRun.get(0));
                assertEquals(javaRun.get(1), minijavaRun.get(1));
            } else {
                assertEquals(expected, minijavaRun.get(0));
                assertEquals("", minijavaRun.get(1));
            }
        } catch (IOException e) {
            System.err.println("io error");
            e.printStackTrace();
        } catch (InterruptedException e) {
            System.err.println("interrupted");
            e.printStackTrace();
        } finally {
            // clean up all the tmp files
            File testLocation = new File(TEST_FILES_LOCATION);
            File[] classes = testLocation.listFiles((dir, name) -> name.endsWith(TEST_FILES_CLASS_EXTENSION));
            for (File _class : classes) {
                _class.delete();
            }

            File runtimeLocation = new File(RUNTIME_LOCATION);
            File[] tmps = runtimeLocation.listFiles((dir, name) -> name.startsWith(RUNTIME_PREFIX));
            for (File tmp : tmps) {
                tmp.delete();
            }
        }
    }
}
