package AST;

import AST.Visitor.Visitor;
import java_cup.runtime.ComplexSymbolFactory.Location;

public abstract class ClassDecl extends ASTNode{
  public Identifier i;
  public VarDeclList vl;  
  public MethodDeclList ml;

  public ClassDecl(Location pos) {
    super(pos);
  }
  public abstract void accept(Visitor v);
}
