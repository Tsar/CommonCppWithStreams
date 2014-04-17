package components;

import java.util.ArrayList;
import java.util.List;

import gen.CommonCppWithStreamsLexer;

import org.antlr.runtime.tree.Tree;

import base.AsmWriter;
import base.CodeProvider;
import base.ErrorsCollector;
import base.SymbolTable;
import base.Type;
import base.TypeChecker;
import base.AsmWriter.AsmFunction;

public class StreamRead implements CodeProvider {
	private VarDef streamDef;
	private List<VarDef> vars;

	public StreamRead(Tree tree, ErrorsCollector ec, SymbolTable st) {
		assert(tree.getType() == CommonCppWithStreamsLexer.STREAM_READ);
		assert(tree.getChildCount() == 1);

		String name = tree.getChild(0).getText();
		int lineNumber = tree.getChild(0).getLine();
		streamDef = st.referenceVariableAndGetVarDef(name, true, lineNumber);
		if (streamDef.getType() != Type.ISTREAM) {
			ec.check(false, lineNumber, "Variable '" + name + "' is not an input stream");
			return;
		}

		vars = new ArrayList<VarDef>();
		for (int i = 0; i < tree.getChild(0).getChildCount(); ++i) {
			lineNumber = tree.getChild(0).getChild(i).getLine();
			VarDef varDef = st.referenceVariableAndGetVarDef(tree.getChild(0).getChild(i).getText(), false, lineNumber);
			ec.check(TypeChecker.isIntOrBool(varDef), lineNumber, "Can not read anything except for int or bool");
			st.setVariableInitialized(varDef.getName());
			vars.add(varDef);
		}
	}

	public void writeAsmCode(AsmWriter w) {
		assert(vars != null);

		w.c("mov eax, " + w.varAddr(streamDef));
		w.c("call get_R_descriptor_into_ebp_and_mode_into_esi");
		w.c("mov " + w.varAddr(streamDef) + ", eax");

		for (VarDef vd : vars) {
			w.push("edx");
			switch (vd.getType()) {
				case INT:
					w.setUsed(AsmFunction.READ_INT);
					w.c("call read_int_to_eax");
					break;
				case BOOL:
					w.setUsed(AsmFunction.READ_BOOL);
					w.c("call read_bool_to_eax");
					break;
				default:
					assert(false);
					break;
			}
			w.pop("edx");
			w.c("mov " + w.varAddr(vd) + ", eax");
		}
	}
}
