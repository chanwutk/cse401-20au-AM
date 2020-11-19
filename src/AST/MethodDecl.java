package AST;

import AST.Visitor.Visitor;
import java_cup.runtime.ComplexSymbolFactory.Location;

public class MethodDecl extends ASTNode {
  public Type t;
  public Identifier i;
  public FormalList fl;
  public VarDeclList vl;
  public StatementList sl;
  public Exp e;
  // Error has been reported and visitors should ignore this node
  public boolean error = false;

  public MethodDecl(Type at, Identifier ai, FormalList afl, VarDeclList avl,
                    StatementList asl, Exp ae, Location pos) {
    super(pos);
    t=at; i=ai; fl=afl; vl=avl; sl=asl; e=ae;
  }

  public void accept(Visitor v) {
    if (!error)
      v.visit(this);
  }
}
