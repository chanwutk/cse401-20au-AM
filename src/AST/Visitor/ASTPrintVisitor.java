package AST.Visitor;

import AST.*;

// Sample print visitor from MiniJava web site with small modifications for UW CSE.
// HP 10/11

public class ASTPrintVisitor implements Visitor {

  private int indent_level = 0;
  
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
    for (int i = 0; i < indent_level; i++) {
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
  
  private void inc_indent_level() {
    ln();
    indent_level++;
  }

  private void dec_indent_level() {
    indent_level--;
  }

  // MainClass m;
  // ClassDeclList cl;
  public void visit(Program n) {
    indent();
    name(n);
    inc_indent_level();
      n.m.accept(this); ln();
      for (int i = 0; i < n.cl.size(); i++) {
        n.cl.get(i).accept(this); ln();
      }
    dec_indent_level();
  }

  // Identifier i1,i2;
  // Statement s;
  public void visit(MainClass n) {
    indent(); name(n); space(); n.i1.accept(this); space(); line(n);
    inc_indent_level();
      print("args: "); n.i2.accept(this); ln();
      n.s.accept(this); ln();
    dec_indent_level();
  }

  // Identifier i;
  // VarDeclList vl;
  // MethodDeclList ml;
  public void visit(ClassDeclSimple n) {
    indent(); name(n); space(); n.i.accept(this); space(); line(n);
    inc_indent_level();
      for (int i = 0; i < n.vl.size(); i++) {
        n.vl.get(i).accept(this); ln();
      }
      for (int i = 0; i < n.ml.size(); i++) {
        n.ml.get(i).accept(this); ln();
      }
    dec_indent_level();
  }

  // Identifier i;
  // Identifier j;
  // VarDeclList vl;
  // MethodDeclList ml;
  public void visit(ClassDeclExtends n) {
    indent(); name(n); space(); n.i.accept(this);
    print(" extends "); n.j.accept(this); space(); line(n);
    inc_indent_level();
      for (int i = 0; i < n.vl.size(); i++) {
        n.vl.get(i).accept(this); ln();
      }
      for (int i = 0; i < n.ml.size(); i++) {
        n.ml.get(i).accept(this); ln();
      }
    dec_indent_level();
  }

  // Type t;
  // Identifier i;
  public void visit(VarDecl n) {
    indent(); name(n); space(); line(n);
    inc_indent_level();
      indent(); n.t.accept(this); space(); n.i.accept(this); ln();
    dec_indent_level();
  }

  // Type t;
  // Identifier i;
  // FormalList fl;
  // VarDeclList vl;
  // StatementList sl;
  // Exp e;
  public void visit(MethodDecl n) {
    indent(); name(n); space(); n.i.accept(this); space(); line(n);
    inc_indent_level();
    n.t.accept(this);
      indent(); print("return type: "); n.t.accept(this); ln();
      indent(); print("parameters:");
      inc_indent_level();
        for (int i = 0; i < n.fl.size(); i++) {
          indent(); n.fl.get(i).accept(this); ln();
        }
      dec_indent_level();
      for (int i = 0; i < n.vl.size(); i++) {
        n.vl.get(i).accept(this); ln();
      }
      for (int i = 0; i < n.sl.size(); i++) {
        n.sl.get(i).accept(this); ln();
      }
      print("return "); line(n.e);
      inc_indent_level();
        n.e.accept(this);
      dec_indent_level();
    dec_indent_level();
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
    inc_indent_level();
    for (int i = 0; i < n.sl.size(); i++) {
      n.sl.get(i).accept(this); ln();
    }
    dec_indent_level();
  }

  // Exp e;
  // Statement s1,s2;
  public void visit(If n) {
    indent(); name(n); space(); line(n);
    inc_indent_level();
      indent(); print("condition:");
      inc_indent_level();
        n.e.accept(this); ln();
      dec_indent_level();
      indent(); print("then:");
      inc_indent_level();
        n.s1.accept(this); ln();
      dec_indent_level();
      indent(); print("else:");
      inc_indent_level();
        n.s2.accept(this); ln();
      dec_indent_level();
    dec_indent_level();
  }

  // Exp e;
  // Statement s;
  public void visit(While n) {
    indent(); name(n); space(); line(n);
    inc_indent_level();
      indent(); print("condition:");
      inc_indent_level();
        n.e.accept(this); ln();
      dec_indent_level();
      indent(); print("do:");
      inc_indent_level();
        n.s.accept(this); ln();
      dec_indent_level();
    dec_indent_level();
  }

  // Exp e;
  public void visit(Print n) {
    indent(); name(n); space(); line(n);
    inc_indent_level();
      n.e.accept(this); ln();
    dec_indent_level();
  }

  // Identifier i;
  // Exp e;
  public void visit(Assign n) {
    indent(); name(n); space(); line(n);
    inc_indent_level();
      indent(); print("to: "); n.i.accept(this); ln();
      indent(); print("exp:");
      inc_indent_level();
        n.e.accept(this); ln();
      dec_indent_level();
    dec_indent_level();
  }

  // Identifier i;
  // Exp e1,e2;
  public void visit(ArrayAssign n) {
    indent(); name(n); space(); line(n);
    inc_indent_level();
      indent(); print("array: "); n.i.accept(this); ln();
      indent(); print("index:");
      inc_indent_level();
        n.e1.accept(this);
      dec_indent_level();
      indent(); print("exp:");
      inc_indent_level();
        n.e2.accept(this); ln();
      dec_indent_level();
    dec_indent_level();
  }
  
  private void binary(String op, Exp e1, Exp e2) {
    indent(); print("(");
    inc_indent_level();
      e1.accept(this); ln();
    dec_indent_level();
    indent(); print(")"); ln();
    indent(); print(op); ln();
    indent(); print("("); ln();
    inc_indent_level();
      e2.accept(this); ln();
    dec_indent_level();
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
    inc_indent_level();
      indent(); print("array:"); ln();
      inc_indent_level();
        n.e1.accept(this); ln();
      dec_indent_level();
      indent(); print("index:"); ln();
      inc_indent_level();
        n.e2.accept(this); ln();
      dec_indent_level();
    dec_indent_level();
  }

  // Exp e;
  public void visit(ArrayLength n) {
    indent(); name(n); space(); line(n);
    inc_indent_level();
      n.e.accept(this); ln();
    dec_indent_level();
  }

  // Exp e;
  // Identifier i;
  // ExpList el;
  public void visit(Call n) {
    indent(); name(n); space(); line(n);
    inc_indent_level();
      indent(); print("object:"); ln();
      inc_indent_level();
        n.e.accept(this); ln();
      dec_indent_level();
      indent(); print("method:"); ln();
      inc_indent_level();
        n.i.accept(this); ln();
      dec_indent_level();
      indent(); print("parameters:"); ln();
      inc_indent_level();
        for ( int i = 0; i < n.el.size(); i++ ) {
          indent(); print("param " + i + ":"); ln();
          inc_indent_level();
            n.el.get(i).accept(this); ln();
          dec_indent_level();
        }
      dec_indent_level();
    dec_indent_level();
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
    inc_indent_level();
      indent(); print("size:"); ln();
      inc_indent_level();
        n.e.accept(this); ln();
      dec_indent_level();
    dec_indent_level();
  }

  // Identifier i;
  public void visit(NewObject n) {
    indent(); name(n);
    inc_indent_level();
      indent(); print("type: "); print(n.i.s); ln();
    dec_indent_level();
  }

  // Exp e;
  public void visit(Not n) {
    indent(); print("! (");
    inc_indent_level();
      n.e.accept(this); ln();
    dec_indent_level();
    indent(); print(")"); ln();
  }

  // String s;
  public void visit(Identifier n) {
    print(n.s);
  }
}
