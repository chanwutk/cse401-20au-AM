package AST.Visitor;

import java.util.HashMap;
import java.util.LinkedHashMap;

import AST.*;
import IO.Asm;
import Symbols.ClassType;
import Symbols.SymbolTable;
import Symbols.SymbolTable.VarLocation.Type;
import static IO.Error.numErrors;

public class AllocationVisitor extends AbstractVisitor {
	public AllocationVisitor(SymbolTable symbols) {
		this.symbols = symbols;
	}

	public void visit(Program n) {
		if (numErrors == 0) {
			Asm.rodata();
			Asm.label(Asm.ARRAYINDEXOUTOFBOUND_MSG);
			Asm.fieldString("ArrayIndexOutOfBoundException: Index %d out of bounds for length %d\\n");
			Asm.label(Asm.NULLPOINTER_MSG);
			Asm.fieldString("NullPointerException\\n");
			Asm.label(Asm.EXCEPTION_MSG);
			Asm.fieldString("Exception\\n");
			var vtables = new HashMap<ClassType, LinkedHashMap<String, String>>();
			symbols.allClasses().forEach(cls -> putVtables(cls, vtables));
			vtables.entrySet().stream().forEach(e -> {
				var cls = e.getKey();
				Asm.label(Asm.vtable(cls.name));
				Asm.field(cls.base == null ? "0" : Asm.vtable(cls.base.name));
				e.getValue().values().forEach(m -> Asm.field(m));
			});
			n.m.accept(this);
			n.cl.stream().forEach(cd -> cd.accept(this));
		}
	}

	public void visit(MainClass n) {
		symbols = symbols.enterClassScope(n.i1.s);
		resetLocation(Type.RBP, -1);
		n.s.accept(this);
		symbols = symbols.exitScope();
	}

	private void visitClassDecl(ClassDecl n) {
		ClassType t = (ClassType) symbols.getClass(n.i);
		// At least vtable
		int baseSize = t.base == null ? 1 : t.base.size;
		t.size = n.vl.size() + baseSize;
		symbols = symbols.enterClassScope(n.i.s);
		resetLocation(Type.THIS, baseSize);
		n.vl.stream().forEach(vd -> {
			vd.accept(this);
			offset++;
		});
		n.ml.stream().forEach(md -> md.accept(this));
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
		int numRegArgs = Asm.numRegArgs(n.fl.size());

		// register arguments
		resetLocation(Type.RBP, -2); // rdi
		for (int i = 0; i < numRegArgs; i++) {
			n.fl.get(i).accept(this);
			offset--;
		}

		// stack arguments
		resetLocation(Type.RBP, 2); // saved rbp and ret addr
		for (int i = numRegArgs; i < n.fl.size(); i++) {
			n.fl.get(i).accept(this);
			offset++;
		}

		// locals
		resetLocation(Type.RBP, -2 - numRegArgs);
		n.vl.stream().forEach(vd -> {
			vd.accept(this);
			offset--;
		});

		// try-catch
		n.sl.stream().forEach(s -> s.accept(this));

		symbols = symbols.exitScope();
	}

	private void visitVar(String v) {
		var loc = symbols.getVariableLocation(v);
		loc.offset = offset;
		loc.type = type;
	}

	public void visit(VarDecl n) {
		visitVar(n.i.s);
	}

	public void visit(Formal n) {
		visitVar(n.i.s);
	}

	public void visit(Try n) {
		n.s.stream().forEach(s -> s.accept(this));
		n.c.stream().forEach(c -> c.accept(this));
	}

	public void visit(Catch n) {
		symbols = symbols.enterCatchScope(n);
		visitVar(n.f.i.s);
		offset--;
		n.s.stream().forEach(s -> s.accept(this));
		offset++;
		symbols = symbols.exitScope();
	}

	public void visit(Block n) {
		n.sl.stream().forEach(s -> s.accept(this));
	}

	public void visit(If n) {
		n.s1.accept(this);
		n.s2.accept(this);
	}

	public void visit(While n) {
		n.s.accept(this);
	}

	public void visit(Throw n) {}
	public void visit(Print n) {}
	public void visit(Assign n) {}
	public void visit(ArrayAssign n) {}

	private SymbolTable symbols;
	private int offset;
	private Type type;

	private void resetLocation(Type type, int offset) {
		this.offset = offset;
		this.type = type;
	}

	private static void putVtables(ClassType cls, HashMap<ClassType, LinkedHashMap<String, String>> vtables) {
		if (!vtables.containsKey(cls)) {
			LinkedHashMap<String, String> vtable;
			if (cls.base != null) {
				putVtables(cls.base, vtables);
				vtable = new LinkedHashMap<>(vtables.get(cls.base));
			} else {
				vtable = new LinkedHashMap<>();
			}
			// at least super pointer
			int offset = 1;
			for (var m : cls.methods.keySet()) {
				vtable.put(m, Asm.method(cls.name, m));
				cls.offset.put(m, offset);
				offset++;
			}
			vtables.put(cls, vtable);
		}
	}
}
