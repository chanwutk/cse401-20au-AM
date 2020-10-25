package AST.Visitor;

import AST.*;

// Sample print visitor from MiniJava web site with small modifications for UW CSE.
// HP 10/11

public class ASTPrintVisitor implements Visitor {

  private int indent = 0;

  private void print(ASTNode node, String s) {
    for (int i = 0; i < indent; i++) {
      System.out.print("  ");
    }
    System.out.println(
      node.getClass().getName() +
      ("".equals(s) ? "" : " (" + s + ")") +
      " (line: " + node.line_number + ")"
    );
  }

  private void print(ASTNode node) {
    print(node, "");
  }

  // MainClass m;
  // ClassDeclList cl;
  public void visit(Program n) {
    print(n);
    indent++;
    n.m.accept(this);
    for (int i = 0; i < n.cl.size(); i++) {
      n.cl.get(i).accept(this);
    }
    indent--;
  }

  // Identifier i1,i2;
  // Statement s;
  public void visit(MainClass n) {
    print(n);
    indent++;
    n.i1.accept(this);
    n.i2.accept(this);
    n.s.accept(this);
    indent--;
  }

  // Identifier i;
  // VarDeclList vl;
  // MethodDeclList ml;
  public void visit(ClassDeclSimple n) {
    print(n);
    indent++;
    n.i.accept(this);
    for (int i = 0; i < n.vl.size(); i++) {
      n.vl.get(i).accept(this);
    }
    for (int i = 0; i < n.ml.size(); i++) {
      n.ml.get(i).accept(this);
    }
    indent--;
  }

  // Identifier i;
  // Identifier j;
  // VarDeclList vl;
  // MethodDeclList ml;
  public void visit(ClassDeclExtends n) {
    print(n);
    indent++;
    n.i.accept(this);
    n.j.accept(this);
    for (int i = 0; i < n.vl.size(); i++) {
      n.vl.get(i).accept(this);
    }
    for (int i = 0; i < n.ml.size(); i++) {
      n.ml.get(i).accept(this);
    }
    indent--;
  }

  // Type t;
  // Identifier i;
  public void visit(VarDecl n) {
    print(n);
    indent++;
    n.t.accept(this);
    n.i.accept(this);
    indent--;
  }

  // Type t;
  // Identifier i;
  // FormalList fl;
  // VarDeclList vl;
  // StatementList sl;
  // Exp e;
  public void visit(MethodDecl n) {
    print(n);
    indent++;
    n.t.accept(this);
    n.i.accept(this);
    for (int i = 0; i < n.fl.size(); i++) {
      n.fl.get(i).accept(this);
    }
    System.out.println(") { ");
    for (int i = 0; i < n.vl.size(); i++) {
      n.vl.get(i).accept(this);
    }
    for (int i = 0; i < n.sl.size(); i++) {
      n.sl.get(i).accept(this);
    }
    n.e.accept(this);
    indent--;
  }

  // Type t;
  // Identifier i;
  public void visit(Formal n) {
    print(n);
    indent++;
    n.t.accept(this);
    n.i.accept(this);
    indent--;
  }

  public void visit(IntArrayType n) {
    print(n);
  }

  public void visit(BooleanType n) {
    print(n);
  }

  public void visit(IntegerType n) {
    print(n);
  }

  // String s;
  public void visit(IdentifierType n) {
    print(n, n.s);
  }

  // StatementList sl;
  public void visit(Block n) {
    print(n);
    indent++;
    for (int i = 0; i < n.sl.size(); i++) {
      n.sl.get(i).accept(this);
    }
    indent--;
  }

  // Exp e;
  // Statement s1,s2;
  public void visit(If n) {
    print(n);
    indent++;
    n.e.accept(this);
    n.s1.accept(this);
    n.s2.accept(this);
    indent--;
  }

  // Exp e;
  // Statement s;
  public void visit(While n) {
    print(n);
    indent++;
    n.e.accept(this);
    n.s.accept(this);
    indent--;
  }

  // Exp e;
  public void visit(Print n) {
    print(n);
    indent++;
    n.e.accept(this);
    indent--;
  }

  // Identifier i;
  // Exp e;
  public void visit(Assign n) {
    print(n);
    indent++;
    n.i.accept(this);
    n.e.accept(this);
    indent--;
  }

  // Identifier i;
  // Exp e1,e2;
  public void visit(ArrayAssign n) {
    print(n);
    indent++;
    n.i.accept(this);
    n.e1.accept(this);
    n.e2.accept(this);
    indent--;
  }

  // Exp e1,e2;
  public void visit(And n) {
    print(n);
    indent++;
    n.e1.accept(this);
    n.e2.accept(this);
    indent--;
  }

  // Exp e1,e2;
  public void visit(LessThan n) {
    print(n);
    n.e1.accept(this);
    n.e2.accept(this);
    indent--;
  }

  // Exp e1,e2;
  public void visit(Plus n) {
    print(n);
    indent++;
    n.e1.accept(this);
    n.e2.accept(this);
    indent--;
  }

  // Exp e1,e2;
  public void visit(Minus n) {
    print(n);
    indent++;
    n.e1.accept(this);
    n.e2.accept(this);
    indent--;
  }

  // Exp e1,e2;
  public void visit(Times n) {
    print(n);
    indent++;
    n.e1.accept(this);
    n.e2.accept(this);
    indent--;
  }

  // Exp e1,e2;
  public void visit(ArrayLookup n) {
    print(n);
    indent++;
    n.e1.accept(this);
    n.e2.accept(this);
    indent--;
  }

  // Exp e;
  public void visit(ArrayLength n) {
    print(n);
    indent++;
    n.e.accept(this);
    indent--;
  }

  // Exp e;
  // Identifier i;
  // ExpList el;
  public void visit(Call n) {
    print(n);
    indent++;
    n.e.accept(this);
    n.i.accept(this);
    for ( int i = 0; i < n.el.size(); i++ ) {
        n.el.get(i).accept(this);
    }
    indent--;
  }

  // int i;
  public void visit(IntegerLiteral n) {
    print(n, n.i + "");
  }

  public void visit(True n) {
    print(n);
  }

  public void visit(False n) {
    print(n);
  }

  // String s;
  public void visit(IdentifierExp n) {
    print(n, n.s);
  }

  public void visit(This n) {
    print(n);
  }

  // Exp e;
  public void visit(NewArray n) {
    print(n);
    indent++;
    n.e.accept(this);
    indent--;
  }

  // Identifier i;
  public void visit(NewObject n) {
    print(n, n.i.s);
  }

  // Exp e;
  public void visit(Not n) {
    print(n);
    indent++;
    n.e.accept(this);
    indent--;
  }

  // String s;
  public void visit(Identifier n) {
    print(n, n.s);
  }
}
