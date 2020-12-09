package AST.Visitor;

import AST.*;

import java.io.PrintStream;

public class ExpPrintVisitor implements Visitor {

  private PrintStream out;

  public ExpPrintVisitor() {
    this(System.out);
  }

  public ExpPrintVisitor(PrintStream out) {
    this.out = out;
  }

  private void binary(String op, Exp e1, Exp e2) {
    out.print("(");
    e1.accept(this);
    out.print(" " + op + " ");
    e2.accept(this);
    out.print(")");
  }

  public void visit(And n) {
    binary("&&", n.e1, n.e2);
  }

  public void visit(LessThan n) {
    binary("<", n.e1, n.e2);
  }

  public void visit(Plus n) {
    binary("+", n.e1, n.e2);
  }

  public void visit(Minus n) {
    binary("-", n.e1, n.e2);
  }

  public void visit(Times n) {
    binary("*", n.e1, n.e2);
  }

  public void visit(Not n) {
    out.print("!");
    n.e.accept(this);
  }

  public void visit(IntegerLiteral n) {
    out.print(n.i);
  }

  public void visit(True n) {
    out.print("true");
  }

  public void visit(False n) {
    out.print("false");
  }

  public void visit(IdentifierExp n) {
    out.print("id(" + n.s + ")");
  }

  public void visit(This n) {
    out.print("this");
  }

  public void visit(Identifier n) {
    out.print("id(" + n.s + ")");
  }

  public void visit(Program n) {}
  public void visit(MainClass n) {}
  public void visit(ClassDeclSimple n) {}
  public void visit(ClassDeclExtends n) {}
  public void visit(VarDecl n) {}
  public void visit(MethodDecl n) {}
  public void visit(Formal n) {}
  public void visit(IntArrayType n) {}
  public void visit(BooleanType n) {}
  public void visit(IntegerType n) {}
  public void visit(IdentifierType n) {}
  public void visit(Block n) {}
  public void visit(TryCatch n) {}
  public void visit(If n) {}
  public void visit(While n) {}
  public void visit(Print n) {}
  public void visit(Assign n) {}
  public void visit(ArrayAssign n) {}
  public void visit(ArrayLookup n) {out.print("unexpected(" + n.getClass().getSimpleName() + ")");}
  public void visit(ArrayLength n) {out.print("unexpected(" + n.getClass().getSimpleName() + ")");}
  public void visit(Call n) {out.print("unexpected(" + n.getClass().getSimpleName() + ")");}
  public void visit(NewArray n) {out.print("unexpected(" + n.getClass().getSimpleName() + ")");}
  public void visit(NewObject n) {out.print("unexpected(" + n.getClass().getSimpleName() + ")");}
}
