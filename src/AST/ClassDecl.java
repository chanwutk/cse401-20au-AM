package AST;

import AST.Visitor.Visitor;
import java_cup.runtime.ComplexSymbolFactory.Location;

public abstract class ClassDecl extends ASTNode{
  public Identifier i;
  public VarDeclList vl;
  public MethodDeclList ml;
  // Error has been reported and visitors should ignore this node
  public boolean error = false;

  public ClassDecl(Location pos) {
    super(pos);
  }
  public abstract void accept(Visitor v);
}
