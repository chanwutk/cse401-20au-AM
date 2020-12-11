package AST;

import AST.Visitor.Visitor;
import java_cup.runtime.ComplexSymbolFactory.Location;

public class Catch extends ASTNode {
  public Formal f;
  public StatementList s;
  public boolean error = false;

  public Catch(Formal af, StatementList as, Location pos) {
    super(pos);
    s = as;
    f = af;
  }

  public void accept(Visitor v) {
    v.visit(this);
  }
}
