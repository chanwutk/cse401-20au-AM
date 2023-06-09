import Scanner.scanner;
import Parser.parser;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.io.FileReader;
import java.util.List;
import java.util.ArrayList;

import java_cup.runtime.ComplexSymbolFactory;

import static org.junit.Assert.*;

import org.junit.Before;
import org.junit.Test;
import org.junit.rules.TestName;
import org.junit.Rule;

import AST.*;
import AST.Visitor.ASTPrintVisitor;

public class TestParser {
	private static final String TEST_FILES_LOCATION = "test/resources/Parser/";
	private static final String TEST_FILES_INPUT_EXTENSION = ".java";

	@Rule
	public TestName testName = new TestName();
	private Program actual;

	@Before
	public void before() {
		try {
			var name = testName.getMethodName().substring(4);
			var sf = new ComplexSymbolFactory();
			var s = new scanner(new FileReader(TEST_FILES_LOCATION + name + TEST_FILES_INPUT_EXTENSION), sf);
			var p = new parser(s, sf);
			var root = p.parse();
			actual = (Program) root.value;
		} catch (Exception e) {
			fail(e.getMessage());
		}
	}

	@Test
	public void testMinimal() {
		var main = new MainClass(
			id("Minimal"), id("args"),
			new Block(new StatementList(null), null), null);
		assertAST(program(main, new ClassDecl[] {}));
	}

	@Test
	public void testPrecedence() {
		var expr = new LessThan(
			new Plus(
				new IdentifierExp("b", null),
				new Times(
					new ArrayLookup(
						new ArrayLookup(
							new IdentifierExp("c", null),
							new IdentifierExp("i", null),
							null),
						new IdentifierExp("j", null),
						null),
					call(
						new ArrayLookup(
							new IdentifierExp("d", null),
							new IdentifierExp("i", null),
							null),
						"e",
						new IdentifierExp[]{}),
					null),
				null),
			call(
				new Minus(
					new IdentifierExp("f", null),
					new IdentifierExp("g", null),
					null),
				"h",
				new IdentifierExp[]{
					new IdentifierExp("i", null),
					new IdentifierExp("j", null),
				}),
			null);
		var assign = new Assign(id("a"), expr, null);
		var main = new MainClass(id("Precedence"), id("args"), assign, null);
		assertAST(program(main, new ClassDecl[]{}));
	}

	@Test
	public void testClasses() {
		var main = new MainClass(
			id("Classes"),
			id("args"),
			new Print(
				new IntegerLiteral(0, null),
				null),
			null);
		var ex = classDecl("Ex", true, new VarDecl[]{}, new MethodDecl[]{});
		var base = classDecl(
			"Base",
			false,
			new VarDecl[]{
				new VarDecl(new IntegerType(null), id("x"), null),
			},
			new MethodDecl[]{
				method(
					new IntegerType(null),
					"f",
					new Formal[]{
						new Formal(
							new IdentifierType("Base", null),
							id("b"),
							null),
					},
					new VarDecl[]{
						new VarDecl(new IntegerType(null), id("y"), null),
					},
					new Statement[]{
						new Assign(
							id("y"),
							new Plus(
								new IdentifierExp("x", null),
								new IntegerLiteral(5, null),
								null),
							null),
					},
					new IdentifierExp("y", null)),
			});
		var s1 = new StatementList(null);
		s1.add(new Assign(new Identifier("ret", null), call(new This(null), "f", new Exp[]{new This(null)}), null));
		var s2 = new StatementList(null);
		s2.add(new Throw(new NewObject(new Identifier("Ex", null), null), null));
		// s2.add(new Assign(new Identifier("ret", null), new IntegerLiteral(0, null), null));
		var s3 = new StatementList(null);
		s3.add(new Throw(new IdentifierExp("e", null), null));
		var simpleTry = new Try(s1, null);
		simpleTry.c.add(new Catch(new Formal(new IdentifierType("Ex", null), id("e"), null), s2, null));
		var simpleTryList = new StatementList(null);
		simpleTryList.add(simpleTry);
		var s1AndTry = new StatementList(null);
		s1AndTry.add(s1.get(0));
		var tryMultipleCatches = new Try(s1, null);
		tryMultipleCatches.c.add(new Catch(new Formal(new IdentifierType("Ex", null), id("e"), null), simpleTryList, null));
		tryMultipleCatches.c.add(new Catch(new Formal(new IdentifierType("Ex2", null), id("e"), null), new StatementList(null), null));
		s1AndTry.add(tryMultipleCatches);
		var complexTry = new Try(s1AndTry, null);
		complexTry.c.add(new Catch(new Formal(new IdentifierType("Ex", null), id("e"), null), s3, null));
		var derived = classDecl(
			"Derived",
			"Base",
			new VarDecl[]{},
			new MethodDecl[]{
				method(
					new IntegerType(null),
					"f",
					new Formal[]{
						new Formal(
							new IdentifierType("Base", null),
							id("b"),
							null),
					},
					new VarDecl[]{new VarDecl(new IntegerType(null), id("ret"), null)},
					new Statement[]{
						simpleTry,
						complexTry,
					},
					new IdentifierExp("ret", null)),
			});
		assertAST(program(main, new ClassDecl[]{ex, base, derived}));
	}

	private void assertAST(Program expected) {
		var outExpected = new ByteArrayOutputStream();
		var outActual = new ByteArrayOutputStream();

		expected.accept(new ASTPrintVisitor(new PrintStream(outExpected), false));
		actual.accept(new ASTPrintVisitor(new PrintStream(outActual), false));

		assertEquals(outExpected.toString(), outActual.toString());
	}

	private static Program program(MainClass main, ClassDecl[] classes) {
		var list = new ClassDeclList(null);
		for (var cls : classes)
			list.add(cls);
		return new Program(main, list, null);
	}

	private static Identifier id(String id) {
		return new Identifier(id, null);
	}

	private static Call call(Exp object, String method, Exp[] args) {
		var params = new ExpList(null);
		for (var arg : args)
			params.add(arg);
		return new Call(object, id(method), params, null);
	}

	private static ClassDeclSimple classDecl(String name, boolean throwable, VarDecl[] fields, MethodDecl[] methods) {
		var fieldList = new VarDeclList(null);
		for (var f : fields)
			fieldList.add(f);
		var methodList = new MethodDeclList(null);
		for (var m : methods)
			methodList.add(m);
		return new ClassDeclSimple(id(name), throwable, fieldList, methodList, null);
	}

	private static ClassDeclExtends classDecl(String name, String base, VarDecl[] fields, MethodDecl[] methods) {
		var fieldList = new VarDeclList(null);
		for (var f : fields)
			fieldList.add(f);
		var methodList = new MethodDeclList(null);
		for (var m : methods)
			methodList.add(m);
		return new ClassDeclExtends(id(name), id(base), fieldList, methodList, null);
	}

	private static MethodDecl method(Type rett, String name, Formal[] formals, VarDecl[] vars, Statement[] stmts, Exp retv) {
		var fieldList = new FormalList(null);
		for (var f : formals)
			fieldList.add(f);
		var varList = new VarDeclList(null);
		for (var v : vars)
			varList.add(v);
		var body = new StatementList(null);
		for (var stmt : stmts)
			body.add(stmt);
		return new MethodDecl(rett, id(name), fieldList, varList, body, retv, null);
	}
}
