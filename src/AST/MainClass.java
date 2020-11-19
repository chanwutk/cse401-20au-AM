package AST;

import AST.Visitor.Visitor;
import java_cup.runtime.ComplexSymbolFactory.Location;

public class MainClass extends ASTNode{
  public Identifier i1,i2;
  public Statement s;
  // Error has been reported and visitors should ignore this node
  public boolean error = false;

  public MainClass(Identifier ai1, Identifier ai2, Statement as,
                   Location pos) {
    super(pos);
    i1=ai1; i2=ai2; s=as;
  }

  public void accept(Visitor v) {
    if (!error)
      v.visit(this);
  }
}
