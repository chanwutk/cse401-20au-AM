package AST;

import AST.Visitor.Visitor;
import java_cup.runtime.ComplexSymbolFactory.Location;

public class ClassDeclExtends extends ClassDecl {
  public Identifier j;
 
  public ClassDeclExtends(Identifier ai, Identifier aj, 
                          VarDeclList avl, MethodDeclList aml,
                          Location pos) {
    super(pos);
    i=ai; j=aj; vl=avl; ml=aml;
  }

  public void accept(Visitor v) {
    v.visit(this);
  }
}
