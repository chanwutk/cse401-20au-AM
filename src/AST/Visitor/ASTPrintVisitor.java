package AST.Visitor;

import AST.*;

// Sample print visitor from MiniJava web site with small modifications for UW CSE.
// HP 10/11

public class ASTPrintVisitor implements Visitor {

  private int indent = 0;
  
  private boolean accept_ln = true;

  private void print(String s) {
    System.out.print(s);
    accept_ln = true;
  }

  private void ln() {
    if (accept_ln) {
      System.out.println();
    }
    accept_ln = false;
  }

  private void indent() {
    for (int i = 0; i < indent; i++) {
      print("  ");
    }
  }

  private void space() {
    print(" ");
  }

  private void line(ASTNode node) {
    print("(line " + node.line_number + ")");
  }

  private void name(ASTNode node) {
    print(node.getClass().getSimpleName());
  }
  
  private void findent() {
    ln();
    indent++;
  }

  private void bindent() {
    indent--;
  }

  // MainClass m;
  // ClassDeclList cl;
  public void visit(Program n) {
    indent();
    name(n);
    findent();
      n.m.accept(this); ln();
      for (int i = 0; i < n.cl.size(); i++) {
        n.cl.get(i).accept(this); ln();
      }
    bindent();
  }

  // Identifier i1,i2;
  // Statement s;
  public void visit(MainClass n) {
    indent(); name(n); space(); n.i1.accept(this); space(); line(n);
    findent();
      print("args: "); n.i2.accept(this); ln();
      n.s.accept(this); ln();
    bindent();
  }

  // Identifier i;
  // VarDeclList vl;
  // MethodDeclList ml;
  public void visit(ClassDeclSimple n) {
    indent(); name(n); space(); n.i.accept(this); space(); line(n);
    findent();
      for (int i = 0; i < n.vl.size(); i++) {
        n.vl.get(i).accept(this); ln();
      }
      for (int i = 0; i < n.ml.size(); i++) {
        n.ml.get(i).accept(this); ln();
      }
    bindent();
  }

  // Identifier i;
  // Identifier j;
  // VarDeclList vl;
  // MethodDeclList ml;
  public void visit(ClassDeclExtends n) {
    indent(); name(n); space(); n.i.accept(this);
    print(" extends "); n.j.accept(this); space(); line(n);
    findent();
      for (int i = 0; i < n.vl.size(); i++) {
        n.vl.get(i).accept(this); ln();
      }
      for (int i = 0; i < n.ml.size(); i++) {
        n.ml.get(i).accept(this); ln();
      }
    bindent();
  }

  // Type t;
  // Identifier i;
  public void visit(VarDecl n) {
    indent(); name(n); space(); line(n);
    findent();
      indent(); n.t.accept(this); space(); n.i.accept(this); ln();
    bindent();
  }

  // Type t;
  // Identifier i;
  // FormalList fl;
  // VarDeclList vl;
  // StatementList sl;
  // Exp e;
  public void visit(MethodDecl n) {
    indent(); name(n); space(); n.i.accept(this); space(); line(n);
    findent();
    n.t.accept(this);
      indent(); print("return type: "); n.t.accept(this); ln();
      indent(); print("parameters:");
      findent();
        for (int i = 0; i < n.fl.size(); i++) {
          indent(); n.fl.get(i).accept(this); ln();
        }
      bindent();
      for (int i = 0; i < n.vl.size(); i++) {
        n.vl.get(i).accept(this); ln();
      }
      for (int i = 0; i < n.sl.size(); i++) {
        n.sl.get(i).accept(this); ln();
      }
      print("return "); line(n.e);
      findent();
        n.e.accept(this);
      bindent();
    bindent();
  }

  // Type t;
  // Identifier i;
  public void visit(Formal n) {
    n.t.accept(this); space(); n.i.accept(this);
  }

  public void visit(IntArrayType n) {
    print("int[]");
  }

  public void visit(BooleanType n) {
    print("boolean");
  }

  public void visit(IntegerType n) {
    print("int");
  }

  // String s;
  public void visit(IdentifierType n) {
    print("id(" + n.s + ")");
  }

  // StatementList sl;
  public void visit(Block n) {
    indent(); name(n); space(); line(n);
    findent();
    for (int i = 0; i < n.sl.size(); i++) {
      n.sl.get(i).accept(this); ln();
    }
    bindent();
  }

  // Exp e;
  // Statement s1,s2;
  public void visit(If n) {
    indent(); name(n); space(); line(n);
    findent();
      indent(); print("condition:");
      findent();
        n.e.accept(this); ln();
      bindent();
      indent(); print("then:");
      findent();
        n.s1.accept(this); ln();
      bindent();
      indent(); print("else:");
      findent();
        n.s2.accept(this); ln();
      bindent();
    bindent();
  }

  // Exp e;
  // Statement s;
  public void visit(While n) {
    indent(); name(n); space(); line(n);
    findent();
      indent(); print("condition:");
      findent();
        n.e.accept(this); ln();
      bindent();
      indent(); print("do:");
      findent();
        n.s.accept(this); ln();
      bindent();
    bindent();
  }

  // Exp e;
  public void visit(Print n) {
    indent(); name(n); space(); line(n);
    findent();
      n.e.accept(this); ln();
    bindent();
  }

  // Identifier i;
  // Exp e;
  public void visit(Assign n) {
    indent(); name(n); space(); line(n);
    findent();
      indent(); print("to: "); n.i.accept(this); ln();
      indent(); print("exp:");
      findent();
        n.e.accept(this); ln();
      bindent();
    bindent();
  }

  // Identifier i;
  // Exp e1,e2;
  public void visit(ArrayAssign n) {
    indent(); name(n); space(); line(n);
    findent();
      indent(); print("array: "); n.i.accept(this); ln();
      indent(); print("index:");
      findent();
        n.e1.accept(this);
      bindent();
      indent(); print("exp:");
      findent();
        n.e2.accept(this); ln();
      bindent();
    bindent();
  }
  
  private void binary(String op, Exp e1, Exp e2) {
    indent(); print("(");
    findent();
      e1.accept(this); ln();
    bindent();
    indent(); print(")"); ln();
    indent(); print(op); ln();
    indent(); print("("); ln();
    findent();
      e2.accept(this); ln();
    bindent();
    indent(); print("(");
  }

  // Exp e1,e2;
  public void visit(And n) {
    binary("&&", n.e1, n.e2);
  }

  // Exp e1,e2;
  public void visit(LessThan n) {
    binary("<", n.e1, n.e2);
  }

  // Exp e1,e2;
  public void visit(Plus n) {
    binary("+", n.e1, n.e2);
  }

  // Exp e1,e2;
  public void visit(Minus n) {
    binary("-", n.e1, n.e2);
  }

  // Exp e1,e2;
  public void visit(Times n) {
    binary("*", n.e1, n.e2);
  }

  // Exp e1,e2;
  public void visit(ArrayLookup n) {
    indent(); name(n); space(); line(n);
    findent();
      indent(); print("array:"); ln();
      findent();
        n.e1.accept(this); ln();
      bindent();
      indent(); print("index:"); ln();
      findent();
        n.e2.accept(this); ln();
      bindent();
    bindent();
  }

  // Exp e;
  public void visit(ArrayLength n) {
    indent(); name(n); space(); line(n);
    findent();
      n.e.accept(this); ln();
    bindent();
  }

  // Exp e;
  // Identifier i;
  // ExpList el;
  public void visit(Call n) {
    indent(); name(n); space(); line(n);
    findent();
      indent(); print("object:"); ln();
      findent();
        n.e.accept(this); ln();
      bindent();
      indent(); print("method:"); ln();
      findent();
        n.i.accept(this); ln();
      bindent();
      indent(); print("parameters:"); ln();
      findent();
        for ( int i = 0; i < n.el.size(); i++ ) {
          indent(); print("param " + i + ":"); ln();
          findent();
            n.el.get(i).accept(this); ln();
          bindent();
        }
      bindent();
    bindent();
  }

  // int i;
  public void visit(IntegerLiteral n) {
    indent(); print(n.i + "");
  }

  public void visit(True n) {
    indent(); print("true");
  }

  public void visit(False n) {
    indent(); print("false");
  }

  // String s;
  public void visit(IdentifierExp n) {
    indent(); print("id(" + n.s + ")");
  }

  public void visit(This n) {
    indent(); print("this");
  }

  // Exp e;
  public void visit(NewArray n) {
    indent(); name(n);
    findent();
      indent(); print("size:"); ln();
      findent();
        n.e.accept(this); ln();
      bindent();
    bindent();
  }

  // Identifier i;
  public void visit(NewObject n) {
    indent(); name(n);
    findent();
      indent(); print("type: "); print(n.i.s); ln();
    bindent();
  }

  // Exp e;
  public void visit(Not n) {
    indent(); print("! (");
    findent();
      n.e.accept(this); ln();
    bindent();
    indent(); print(")"); ln();
  }

  // String s;
  public void visit(Identifier n) {
    print(n.s);
  }
}
