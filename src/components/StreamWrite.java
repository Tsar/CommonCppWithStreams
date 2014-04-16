package components;

import java.util.ArrayList;

import gen.CommonCppWithStreamsLexer;

import org.antlr.runtime.tree.Tree;

import base.AsmWriter;
import base.CodeProvider;
import base.ErrorsCollector;
import base.SymbolTable;
import base.Type;
import base.TypeChecker;

public class StreamWrite implements CodeProvider {
	private VarDef streamDef;
	private ArrayList<Expression> exprs;

	public StreamWrite(Tree tree, ErrorsCollector ec, SymbolTable st) {
		assert(tree.getType() == CommonCppWithStreamsLexer.STREAM_WRITE);
		assert(tree.getChildCount() == 1);

		String name = tree.getChild(0).getText();
		int lineNumber = tree.getChild(0).getLine();
		streamDef = st.referenceVariableAndGetVarDef(name, true, lineNumber);
		if (streamDef.getType() != Type.OSTREAM) {
			ec.check(false, lineNumber, "Variable '" + name + "' is not an output stream");
			return;
		}

		exprs = new ArrayList<Expression>();
		for (int i = 0; i < tree.getChild(0).getChildCount(); ++i) {
			Expression expr = new Expression(tree.getChild(0).getChild(i), ec, st);
			ec.check(TypeChecker.isIntOrBool(expr), tree.getChild(0).getChild(i).getLine(), "Can not output anything except for int or bool");
			exprs.add(expr);
		}
	}

	public void writeAsmCode(AsmWriter w) {
		assert(exprs != null);

		for (int i = 0; i < exprs.size(); ++i) {
			Expression expr = exprs.get(i);
			expr.writeAsmCode(w);
			w.pop("eax");
			w.push("edx");
			switch (expr.getType()) {
				case INT:
					w.c("call prepare_int_eax_to_write");
					break;
				case BOOL:
					w.c("call prepare_bool_eax_to_write");
					break;
				default:
					assert(false);
					break;
			}
			w.c("mov ebx, 0");  // TODO: fd
			w.c("mov eax, 4", "number of 'write' syscall");
		    w.c("int 80h");
			w.c("call write_" + ((i == exprs.size() - 1) ? "endl" : "space"));
			w.pop("edx");
		}
	}
}
