package AST;

import java.util.List;
import java.util.stream.Stream;
import java.util.ArrayList;
import java_cup.runtime.ComplexSymbolFactory.Location;

public class FormalList extends ASTNode {
   private List<Formal> list;

   public FormalList(Location pos) {
      super(pos);
      list = new ArrayList<Formal>();
   }

   public void add(Formal n) {
      list.add(n);
   }

   public Formal get(int i)  {
      return list.get(i);
   }

   public int size() {
      return list.size();
   }

   public Stream<Formal> stream() {
      return list.stream().filter(f -> !f.error);
   }
}
