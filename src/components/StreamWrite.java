package components;

import java.util.ArrayList;
import java.util.List;

import gen.CommonCppWithStreamsLexer;

import org.antlr.runtime.tree.Tree;

import base.AsmWriter;
import base.AsmWriter.AsmFunction;
import base.CodeProvider;
import base.ErrorsCollector;
import base.SymbolTable;
import base.Type;
import base.TypeChecker;

public class StreamWrite implements CodeProvider {
	private VarDef streamDef;
	private List<Expression> exprs;

	public StreamWrite(Tree tree, ErrorsCollector ec, SymbolTable st) {
		assert(tree.getType() == CommonCppWithStreamsLexer.STREAM_WRITE);
		assert(tree.getChildCount() == 1);

		String name = tree.getChild(0).getText();
		int lineNumber = tree.getChild(0).getLine();
		streamDef = st.referenceVariableAndGetVarDef(name, true, lineNumber);
		if (streamDef != null && streamDef.getType() != Type.OSTREAM) {
			ec.check(false, lineNumber, "Variable '" + name + "' is not an output stream");
			return;
		}

		exprs = new ArrayList<Expression>();
		for (int i = 0; i < tree.getChild(0).getChildCount(); ++i) {
			Expression expr = new Expression(tree.getChild(0).getChild(i), ec, st);
			ec.check(TypeChecker.isIntOrBool(expr), tree.getChild(0).getChild(i).getLine(), "Can not write anything except for int or bool");
			exprs.add(expr);
		}
	}

	public void writeAsmCode(AsmWriter w) {
		assert(exprs != null);

		w.c("mov eax, " + w.varAddr(streamDef));
		w.c("call get_W_descriptor_into_ebp_and_mode_into_esi");

		for (int i = 0; i < exprs.size(); ++i) {
			Expression expr = exprs.get(i);
			expr.writeAsmCode(w);
			w.pop("eax");
			w.push("edx");
			switch (expr.getType()) {
				case INT:
					w.setUsed(AsmFunction.WRITE_INT);
					w.c("call write_int_from_eax");
					break;
				case BOOL:
					w.setUsed(AsmFunction.WRITE_BOOL);
					w.c("call write_bool_from_eax");
					break;
				default:
					assert(false);
					break;
			}
			w.c("call write_" + ((i == exprs.size() - 1) ? "endl" : "space") + "_if_text_mode");
			w.pop("edx");
		}

		w.c("call close_by_descriptor_in_ebp");
	}
}
