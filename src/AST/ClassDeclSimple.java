package AST;

import AST.Visitor.Visitor;
import java_cup.runtime.ComplexSymbolFactory.Location;

public class ClassDeclSimple extends ClassDecl {
  public ClassDeclSimple(Identifier ai, VarDeclList avl, MethodDeclList aml,
                         Location pos) {
    super(pos);
    i=ai; vl=avl; ml=aml;
  }

  public void accept(Visitor v) {
    v.visit(this);
  }
}
