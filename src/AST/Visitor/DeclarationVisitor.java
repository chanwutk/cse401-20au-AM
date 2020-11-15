package AST.Visitor;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.stream.Collectors;

import AST.*;
import Symbols.BaseType;
import Symbols.ClassType;
import Symbols.Signature;
import Symbols.SymbolTable;
import Symbols.Type;
import Info.Info;

public class DeclarationVisitor implements Visitor {

  private SymbolTable symbols;

  public DeclarationVisitor(SymbolTable table) {
    this.symbols = table;
  }

  private List<ClassDecl> toposort(ClassDeclList cl) {
    Map<String, ClassDecl> namemap = new HashMap<>();
    Map<String, Integer> indegrees = new HashMap<>();
    Map<String, List<String>> graph = new HashMap<>();

    cl.stream().forEach(cd -> {
      String name = cd.i.s;
      namemap.put(name, cd);
      if (!graph.containsKey(name)) graph.put(name, new ArrayList<>());

      if (cd instanceof ClassDeclExtends) {
        String base = ((ClassDeclExtends) cd).j.s;
        if (!indegrees.containsKey(base)) indegrees.put(base, 0);
        if (!indegrees.containsKey(name)) indegrees.put(name, 0);
        indegrees.put(name, indegrees.get(name) + 1);

        if (!graph.containsKey(base)) graph.put(base, new ArrayList<>());
        graph.get(base).add(name);
      }
    });

    Queue<String> queue = new LinkedList<>();
    indegrees.forEach((name, indegree) -> {
      if (indegree != 0) {
        queue.add(name);
      }
    });

    List<ClassDecl> ret = new ArrayList<>();
    while(!queue.isEmpty()) {
      String head = queue.remove();
      ret.add(namemap.get(head));

      List<String> children = graph.get(head);
      if (children != null) {
        children.forEach(child -> {
          int indegree = indegrees.get(child) - 1;
          indegrees.put(child, indegree);
          if (indegree == 0) queue.add(child);
        });
      }
    }

    return ret;
  }

  public void visit(Program n) {
    ClassDeclList cl = n.cl;
    List<ClassDecl> sorted_cl = toposort(cl);
    Map<Boolean, List<ClassDecl>> partition = sorted_cl.stream()
      .collect(Collectors.partitioningBy(cd -> cd instanceof ClassDeclSimple));
    
    partition.get(true).stream().map(cd -> (ClassDeclSimple) cd).forEach(cs -> {
      if (symbols.containsClass(cs.i.s)) {
        System.err.printf("%s:%d: error: class already exist", Info.file, cs.line_number);
        System.err.printf("  symbol:    class %s\n", cs.i.s);
        System.err.printf("  location:  class %s\n", Info.currentClass);
        Info.numErrors++;
      } else {
        symbols.putClass(cs.i.s, new ClassType(cs.i.s));
      }
    });

    partition.get(false).stream().map(cd -> (ClassDeclExtends) cd).forEach(ce -> {
      Type base = symbols.getClass(ce.j);
      if (base instanceof ClassType) {
        if (symbols.containsClass(ce.i.s)) {
          System.err.printf("%s:%d: error: class already exist", Info.file, ce.line_number);
          System.err.printf("  symbol:    class %s\n", ce.i.s);
          System.err.printf("  location:  class %s\n", Info.currentClass);
        Info.numErrors++;
        } else {
          symbols.putClass(ce.i.s, new ClassType(ce.i.s, (ClassType) base));
        }
      } else {
        System.err.printf("%s:%d: error: extending class does not exist", Info.file, ce.line_number);
        System.err.printf("  symbol:   class %s\n", ce.i.s);
        System.err.printf("  symbol:  parent %s\n", ce.j.s);
        System.err.printf("  location: class %s\n", Info.currentClass);
        Info.numErrors++;
        symbols.putClass(ce.i.s, new ClassType(ce.i.s));
      }
    });

    sorted_cl.forEach(cd -> cd.accept(this));
  }

  public void visit(MainClass n) {}

  private BaseType astToBase(AST.Type t) {
    if (t instanceof BooleanType) {
      return BaseType.BOOLEAN;
    } else if (t instanceof IntegerType) {
      return BaseType.INT;
    } else if (t instanceof IntArrayType) {
      return BaseType.ARRAY;
    } else {
      return BaseType.UNKNOWN;
    }
  }

  private Signature methodDeclToSignature(MethodDecl n) {
    Symbols.Type ret = astToBase(n.t);
    List<Symbols.Type> params = n.fl.stream()
      .map(f -> astToBase(f.t))
      .collect(Collectors.toList());
    return new Signature(ret, params);
  }

  private void visitClassDecl(ClassDecl n) {
    symbols = symbols.enterClassScope(n.i.s);

    Type type = symbols.getClass(n.i);
    if (type instanceof ClassType) {
      ClassType ctype = (ClassType) type;
      n.vl.stream().forEach(vd -> {
        if (symbols.containsVariable(vd.i.s)) {
          System.err.printf("%s:%d: error: variable already exist", Info.file, vd.line_number);
          System.err.printf("  symbol: variable %s\n", vd.i.s);
          System.err.printf("  location:  class %s\n", Info.currentClass);
          Info.numErrors++;
        } else {
          Type t = astToBase(vd.t);
          symbols.putVariable(vd.i.s, t);
          ctype.setField(vd.i.s, t);
        }
      });

      n.ml.stream().forEach(md -> {
        if (symbols.containsMethod(md.i.s)) {
          System.err.printf("%s:%d: error: method already exist", Info.file, md.line_number);
          System.err.printf("  symbol:  method %s\n", md.i.s);
          System.err.printf("  location: class %s\n", Info.currentClass);
          Info.numErrors++;
        } else {
          Signature s = methodDeclToSignature(md);
          symbols.putMethod(md.i.s, s);
          ctype.setMethod(md.i.s, s);
        }

        md.accept(this);
      });
    }
    // else: unknown -> error is already printed form table.getClass();

    symbols = symbols.exitScope();
  }

  public void visit(ClassDeclSimple n) {
    visitClassDecl(n);
  }

  public void visit(ClassDeclExtends n) {
    visitClassDecl(n);
  }

  public void visit(MethodDecl n) {
    symbols = symbols.enterMethodScope(n.i.s);
    
    n.fl.stream().forEach(f -> {
      if (symbols.containsVariable(f.i.s)) {
        System.err.printf("%s:%d: error: parameter already exist", Info.file, f.line_number);
        System.err.printf("  symbol: parameter %s\n", f.i.s);
        System.err.printf("  location:   class %s\n", Info.currentClass);
        Info.numErrors++;
      } else {
        symbols.putVariable(f.i.s, astToBase(f.t));
      }
    });

    n.vl.stream().forEach(vd -> {
      if (symbols.containsVariable(vd.i.s)) {
        System.err.printf("%s:%d: error: variable already exist", Info.file, vd.line_number);
        System.err.printf("  symbol: variable %s\n", vd.i.s);
        System.err.printf("  location:  class %s\n", Info.currentClass);
        Info.numErrors++;
      } else {
        symbols.putVariable(vd.i.s, astToBase(vd.t));
      }
    });

    symbols = symbols.exitScope();
  }

  public void visit(VarDecl n) {}
  public void visit(Formal n) {}
  public void visit(IntArrayType n) {}
  public void visit(BooleanType n) {}
  public void visit(IntegerType n) {}
  public void visit(IdentifierType n) {}
  public void visit(Block n) {}
  public void visit(If n) {}
  public void visit(While n) {}
  public void visit(Print n) {}
  public void visit(Assign n) {}
  public void visit(ArrayAssign n) {}
  public void visit(And n) {}
  public void visit(LessThan n) {}
  public void visit(Plus n) {}
  public void visit(Minus n) {}
  public void visit(Times n) {}
  public void visit(ArrayLookup n) {}
  public void visit(ArrayLength n) {}
  public void visit(Call n) {}
  public void visit(IntegerLiteral n) {}
  public void visit(True n) {}
  public void visit(False n) {}
  public void visit(IdentifierExp n) {}
  public void visit(This n) {}
  public void visit(NewArray n) {}
  public void visit(NewObject n) {}
  public void visit(Not n) {}
  public void visit(Identifier n) {}
}
