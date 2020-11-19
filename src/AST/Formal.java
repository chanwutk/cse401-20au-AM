package AST;

import AST.Visitor.Visitor;
import java_cup.runtime.ComplexSymbolFactory.Location;

public class Formal extends ASTNode{
  public Type t;
  public Identifier i;
  // Error has been reported and visitors should ignore this node
  public boolean error = false;

  public Formal(Type at, Identifier ai, Location pos) {
    super(pos);
    t=at; i=ai;
  }

  public void accept(Visitor v) {
    v.visit(this);
  }
}
