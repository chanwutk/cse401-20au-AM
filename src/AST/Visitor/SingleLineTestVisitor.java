package AST.Visitor;

import AST.*;

public class SingleLineTestVisitor implements Visitor {
  private boolean singleline = true;

  public void clear() {singleline = true;}

  public boolean isSingleLine() {return singleline;}

  public void visit(And n) {
    n.e1.accept(this);
    if (singleline) {
      n.e2.accept(this);
    }
  }

  public void visit(LessThan n) {
    n.e1.accept(this);
    if (singleline) {
      n.e2.accept(this);
    }
  }

  public void visit(Plus n) {
    n.e1.accept(this);
    if (singleline) {
      n.e2.accept(this);
    }
  }

  public void visit(Minus n) {
    n.e1.accept(this);
    if (singleline) {
      n.e2.accept(this);
    }
  }

  public void visit(Times n) {
    n.e1.accept(this);
    if (singleline) {
      n.e2.accept(this);
    }
  }

  public void visit(Not n) {n.e.accept(this);}

  public void visit(IntegerLiteral n) {}
  public void visit(True n) {}
  public void visit(False n) {}
  public void visit(IdentifierExp n) {}
  public void visit(This n) {}

  public void visit(Identifier n) {}

  public void visit(Program n) {singleline = false;}
  public void visit(MainClass n) {singleline = false;}
  public void visit(ClassDeclSimple n) {singleline = false;}
  public void visit(ClassDeclExtends n) {singleline = false;}
  public void visit(VarDecl n) {singleline = false;}
  public void visit(MethodDecl n) {singleline = false;}
  public void visit(Formal n) {singleline = false;}
  public void visit(IntArrayType n) {singleline = false;}
  public void visit(BooleanType n) {singleline = false;}
  public void visit(IntegerType n) {singleline = false;}
  public void visit(IdentifierType n) {singleline = false;}
  public void visit(Block n) {singleline = false;}
  public void visit(If n) {singleline = false;}
  public void visit(TryCatch n) {singleline = false;}
  public void visit(While n) {singleline = false;}
  public void visit(Throw n) {singleline = false;}
  public void visit(Print n) {singleline = false;}
  public void visit(Assign n) {singleline = false;}
  public void visit(ArrayAssign n) {singleline = false;}
  public void visit(ArrayLookup n) {singleline = false;}
  public void visit(ArrayLength n) {singleline = false;}
  public void visit(Call n) {singleline = false;}
  public void visit(NewArray n) {singleline = false;}
  public void visit(NewObject n) {singleline = false;}
}
