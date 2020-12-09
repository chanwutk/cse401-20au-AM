package AST;

import AST.Visitor.Visitor;
import java_cup.runtime.ComplexSymbolFactory.Location;

public class TryCatch extends Statement {
  public StatementList s1,s2;
  public Identifier i;

  public TryCatch(StatementList as1, Identifier ai, StatementList as2, Location pos) {
    super(pos);
    s1=as1; s2=as2;
    i=ai;
  }

  public void accept(Visitor v) {
    v.visit(this);
  }
}

