package AST.Visitor;

import java.util.HashMap;
import java.util.LinkedHashMap;

import AST.*;
import IO.Asm;
import Symbols.ClassType;
import Symbols.SymbolTable;
import Symbols.SymbolTable.VarLocation.Type;
import static IO.Error.numErrors;

public class VtableVisitor extends AbstractVisitor {
	public VtableVisitor(SymbolTable symbols) {
		this.symbols = symbols;
	}

	public void visit(Program n) {
		if (numErrors == 0) {
			var vtables = new HashMap<ClassType, LinkedHashMap<String, String>>();
			symbols.allClasses().forEach(cls -> putVtables(cls, vtables));
			Asm.rodata();
			vtables.entrySet().stream().forEach(e -> {
				var cls = e.getKey();
				Asm.label(Asm.vtable(cls.name));
				Asm.field(cls.base == null ? Asm.lit(0) : Asm.vtable(cls.base.name));
				e.getValue().values().forEach(m -> Asm.field(m));
			});
			n.cl.stream().forEach(cd -> cd.accept(this));
		}
	}

	private void visitClassDecl(ClassDecl n) {
		ClassType t = (ClassType) symbols.getClass(n.i);
		// At least vtable
		int baseSize = t.base == null ? Asm.WS : t.base.size;
		t.size = n.vl.size() * Asm.WS + baseSize;
		symbols = symbols.enterClassScope(n.i.s);
		resetLocation(Type.FIELD, baseSize);
		n.vl.stream().forEach(vd -> vd.accept(this));
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
		resetLocation(Type.ARG, -Asm.ARGS.size() * Asm.WS);
		n.fl.stream().forEach(f -> f.accept(this));
		resetLocation(Type.LOCAL, 0);
		n.vl.stream().forEach(vd -> vd.accept(this));
		symbols = symbols.exitScope();
	}

	private void visitVar(String v) {
		var loc = symbols.getVariableLocation(v);
		loc.offset = offset;
		loc.type = type;
		offset += Asm.WS;
	}

	public void visit(VarDecl n) {
		visitVar(n.i.s);
	}

	public void visit(Formal n) {
		visitVar(n.i.s);
	}

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
			int offset = Asm.WS;
			for (var m : cls.methods.keySet()) {
				vtable.put(m, Asm.method(cls.name, m));
				cls.offset.put(m, offset);
				offset += Asm.WS;
			}
			vtables.put(cls, vtable);
		}
	}
}
