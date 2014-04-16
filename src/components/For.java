package components;

import gen.CommonCppWithStreamsLexer;

import org.antlr.runtime.tree.Tree;

import base.AsmWriter;
import base.CodeProvider;
import base.ErrorsCollector;
import base.SymbolTable;
import base.UIdHolder;

public class For implements CodeProvider, UIdHolder {
	private Statement forInit;
	private Expression forCond;
	private Statement forAct;
	private Statement statement;

	private int uid;

	public For(Tree tree, ErrorsCollector ec, SymbolTable st) {
		assert(tree.getType() == CommonCppWithStreamsLexer.FOR);
		assert(tree.getChildCount() == 4);

		forInit = new Statement(tree.getChild(0), ec, st);
		forCond = new Expression(tree.getChild(1), ec, st);
		forAct = new Statement(tree.getChild(2), ec, st);
		statement = new Statement(tree.getChild(3), ec, st);
	}

	public void writeAsmCode(AsmWriter w) {
		uid = w.genNewUId();

		forInit.writeAsmCode(w);

		w.loopStart(this);

		w.l("_loop_" + uid + "_real_start");
		forCond.writeAsmCode(w);
		w.pop("eax");
		w.c("test eax, eax");
		w.c("jz _loop_" + uid + "_end");

		statement.writeAsmCode(w);

		w.t("Following label is not loop start; just named so for 'continue'");
		w.l("_loop_" + uid + "_start");
		forAct.writeAsmCode(w);

		w.c("jmp _loop_" + uid + "_real_start");

		w.l("_loop_" + uid + "_end");

		w.loopEnd();
	}

	public int getUId() {
		return uid;
	}
}
