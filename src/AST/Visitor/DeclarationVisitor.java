package AST.Visitor;

import java.util.ArrayList;
import java.util.ArrayDeque;
import java.util.HashMap;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.stream.Collectors;

import AST.*;
import IO.Error;
import Symbols.BaseType;
import Symbols.ClassType;
import Symbols.Signature;
import Symbols.SymbolTable;
import Symbols.Type;
import Symbols.SymbolTable.SymbolException;

public class DeclarationVisitor extends AbstractVisitor {

  private SymbolTable symbols;

  public DeclarationVisitor(SymbolTable table) {
    this.symbols = table;
  }

  public void visit(Program n) {
    n.m.accept(this);
    n.cl.reset(toposort(n.cl));
    n.cl.stream().forEach(cd -> cd.accept(this));
    n.cl.stream().forEach(cd -> {
      ClassType type = (ClassType) symbols.getClass(cd.i);
      symbols = symbols.enterClassScope(cd.i.s);
      cd.vl.stream().forEach(vd -> vd.accept(this));
      cd.ml.stream().forEach(md -> {
        type.putMethod(md.i.s, methodDeclToSignature(md));
        md.accept(this);
      });
      symbols = symbols.exitScope();
    });
  }

  public void visit(MainClass n) {
    try {
      symbols.putClass(n.i1, new ClassType(n.i1.s));
    } catch (SymbolException e) {
      n.error = true;
    }
  }

  private void visit(ClassDecl n, ClassType type) {
    try {
      symbols.putClass(n.i, type);
    } catch (SymbolException e) {
      n.error = true;
    }
  }

  public void visit(ClassDeclSimple n) {
    visit(n, new ClassType(n.i.s));
  }

  public void visit(ClassDeclExtends n) {
    // this cannot fail because toposort guarantees base is already declared
    ClassType base = (ClassType) symbols.getClass(n.j);
    visit(n, new ClassType(n.i.s, base));
  }

  public void visit(MethodDecl n) {
    try {
      symbols.putMethod(n.i, methodDeclToSignature(n));
      symbols = symbols.enterMethodScope(n.i.s);
      n.fl.stream().forEach(f -> f.accept(this));
      n.vl.stream().forEach(vd -> vd.accept(this));
      symbols = symbols.exitScope();
    } catch (SymbolException e) {
      n.error = true;
    }
  }

  public void visit(VarDecl n) {
    try {
      symbols.putVariable(n.i, varDeclToType(n.t));
    } catch (SymbolException e) {
      n.error = true;
    }
  }

  public void visit(Formal n) {
    try {
      symbols.putVariable(n.i, varDeclToType(n.t));
    } catch (SymbolException e) {
      n.error = true;
    }
  }

  private List<ClassDecl> toposort(ClassDeclList cl) {
    Map<String, ClassDecl> classes = new HashMap<>();
    Map<ClassDecl, Collection<ClassDecl>> requiredBy = new HashMap<>();

    cl.stream().forEach(cd -> {
      if (classes.containsKey(cd.i.s)) {
        Error.errorDuplicateClass(cd.i.line_number, cd.i.s);
        cd.error = true;
      } else {
        classes.put(cd.i.s, cd);
        requiredBy.put(cd, new ArrayList<>());
      }
    });

    Queue<ClassDecl> resolved = new ArrayDeque<>();
    for (int i = 0; i < cl.size(); i++) {
      ClassDecl cd = cl.get(i);
      if (!cd.error) {
        if (cd instanceof ClassDeclExtends) {
          ClassDeclExtends cde = ((ClassDeclExtends) cd);
          ClassDecl base = classes.get(cde.j.s);
          if (base == null) {
            Error.errorNoSymbol(cd.line_number, "class", cde.j.s);
            ClassDeclSimple replacement = new ClassDeclSimple(cd.i, cd.vl, cd.ml, null);
            replacement.line_number = cd.line_number;
            cl.set(i, replacement);
            requiredBy.put(replacement, requiredBy.get(cd));
            requiredBy.remove(cd);
            resolved.add(replacement);
          } else {
            requiredBy.get(base).add(cd);
          }
        } else {
          resolved.add(cd);
        }
      }
    }

    List<ClassDecl> ret = new ArrayList<>();
    while (!resolved.isEmpty()) {
      ClassDecl cd = resolved.remove();
      requiredBy.get(cd).forEach(derived -> resolved.add(derived));
      ret.add(cd);
      requiredBy.remove(cd);
    }

    if (!requiredBy.isEmpty()) {
      // report cyclic inheritance in the source code order
      cl.stream().forEach(cd -> {
        if (requiredBy.containsKey(cd)) {
          Error.errorCyclicInheritance(cd.line_number, cd.i.s);
          resolved.add(cd);
          while (!resolved.isEmpty()) {
            ClassDecl r = resolved.remove();
            r.error = true;
            try {
              symbols.putClass(r.i, BaseType.UNKNOWN);
            } catch (SymbolException e) {
              // r has already been marked error
            }
            requiredBy.get(r).stream().filter(derived -> derived != cd).forEach(derived -> resolved.add(derived));
            requiredBy.remove(r);
          }
        }
      });
    }

    return ret;
  }

  private Type varDeclToType(AST.Type t) {
    if (t instanceof BooleanType) {
      return BaseType.BOOLEAN;
    } else if (t instanceof IntegerType) {
      return BaseType.INT;
    } else if (t instanceof IntArrayType) {
      return BaseType.ARRAY;
    } else if (t instanceof IdentifierType) {
      return symbols.getClass(((IdentifierType) t).s, t.line_number);
    } else {
      // this would be compiler internal bug
      throw new IllegalArgumentException();
    }
  }

  private Signature methodDeclToSignature(MethodDecl n) {
    Symbols.Type ret = varDeclToType(n.t);
    List<Symbols.Type> params = n.fl.stream().map(f -> varDeclToType(f.t)).collect(Collectors.toList());
    return new Signature(n.i.s, ret, params);
  }
}
