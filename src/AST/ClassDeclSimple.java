package AST;

import AST.Visitor.Visitor;
import java_cup.runtime.ComplexSymbolFactory.Location;

public class ClassDeclSimple extends ClassDecl {
  public boolean ex;
  public ClassDeclSimple(Identifier ai, boolean aex, VarDeclList avl, MethodDeclList aml,
                         Location pos) {
    super(pos);
    i=ai; ex=aex; vl=avl; ml=aml;
  }

  public void accept(Visitor v) {
    v.visit(this);
  }
}
