package AST.Visitor;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Queue;

import AST.*;
import Symbols.BaseType;
import Symbols.ClassType;
import Symbols.Signature;
import Symbols.SymbolTable;
import Symbols.Type;
import Info.Info;

public class RegisterClassesVisitor implements Visitor {

  private SymbolTable table;
  private Signature retsign;

  public RegisterClassesVisitor(SymbolTable table) {
    this.table = table;
  }

  private List<ClassDecl> toposort(ClassDeclList cl) {
    Map<String, ClassDecl> namemap = new HashMap<>();
    Map<String, Integer> indegrees = new HashMap<>();
    Map<String, List<String>> graph = new HashMap<>();
    int len = cl.size();

    for (int i = 0; i < len; i++) {
      ClassDecl cd = cl.get(i);
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
    }

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
    sorted_cl.forEach(cd -> {
      if (cd instanceof ClassDeclSimple) {
        ClassDeclSimple cs = (ClassDeclSimple) cd;
        table.putClass(cs.i.s, new ClassType(cs.i.s, null));
      } else if (cd instanceof ClassDeclExtends) {
        ClassDeclExtends ce = (ClassDeclExtends) cd;
        Type base = table.getClass(ce.j);
        if (base instanceof ClassType) {
          table.putClass(ce.i.s, new ClassType(ce.i.s, (ClassType) base));
        } else {
          System.err.printf("%s:%d: error: extending class does not exist", Info.file, ce.line_number);
          System.err.printf("  symbol:   class %s\n", ce.i.s);
          System.err.printf("  symbol:  parent %s\n", ce.j.s);
          System.err.printf("  location: class %s\n", Info.currentClass);
        }
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
    List<Symbols.Type> params = new ArrayList<>();
    int len = n.fl.size();
    for (int i = 0; i < len; i++) {
      Formal f = n.fl.get(i);
      params.add(astToBase(f.t));
    }
    return new Signature(ret, params);
  }

  private void visitClassDecl(ClassDecl n) {
    Type type = table.getClass(n.i);
    if (type instanceof ClassType) {
      ClassType ctype = (ClassType) type;
      int len = n.vl.size();
      for (int i = 0; i < len; i++) {
        VarDecl vd = n.vl.get(i);
        ctype.fields.put(vd.i.s, astToBase(vd.t));
      }

      len = n.ml.size();
      for (int i = 0; i < len; i++) {
        MethodDecl md = n.ml.get(i);
        ctype.methods.put(md.i.s, methodDeclToSignature(md));
      }
    }
    // else: unknown -> error is already printed form table.getClass();
  }

  public void visit(ClassDeclSimple n) {
    visitClassDecl(n);
  }

  public void visit(ClassDeclExtends n) {
    visitClassDecl(n);
  }

  public void visit(VarDecl n) {}
  public void visit(MethodDecl n) {}
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
