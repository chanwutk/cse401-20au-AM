package AST;

import java.util.ArrayList;
import java.util.List;

import AST.Visitor.Visitor;
import java_cup.runtime.ComplexSymbolFactory.Location;

public class Try extends Statement {
  public StatementList s;
  public List<Catch> c = new ArrayList<>();

  public Try(StatementList as, Location pos) {
    super(pos);
    s=as;
  }

  public void accept(Visitor v) {
    v.visit(this);
  }
}
