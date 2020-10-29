package AST;

import java_cup.runtime.ComplexSymbolFactory.Location;

abstract public class ASTNode {
  // Line number in source file.
  public int line_number;

  // Constructor
  public ASTNode(Location pos) {
    setLocation(pos);
  }

  public void setLocation(Location pos) {
    if (pos != null) {
      line_number = pos.getLine();
    } else {
      line_number = 0;
    }
  }
}
