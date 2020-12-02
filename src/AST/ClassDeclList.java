package AST;

import java.util.List;
import java.util.stream.Stream;
import java.util.ArrayList;
import java_cup.runtime.ComplexSymbolFactory.Location;

public class ClassDeclList extends ASTNode{
   private List<ClassDecl> list;

   public ClassDeclList(Location pos) {
      super(pos);
      list = new ArrayList<ClassDecl>();
   }

   public void add(ClassDecl n) {
      list.add(n);
   }

   public ClassDecl get(int i)  {
      return list.get(i);
   }

   public void set(int i, ClassDecl n) {
      list.set(i, n);
   }

   public int size() {
      return list.size();
   }

   public void reset(List<ClassDecl> cl) {
      list = cl;
   }

   public Stream<ClassDecl> stream() {
      return list.stream().filter(cd -> !cd.error);
   }
}
