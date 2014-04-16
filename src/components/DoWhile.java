package components;

import gen.CommonCppWithStreamsLexer;

import org.antlr.runtime.tree.Tree;

import base.AsmWriter;
import base.CodeProvider;
import base.ErrorsCollector;
import base.SymbolTable;
import base.UIdHolder;

public class DoWhile implements CodeProvider, UIdHolder {
	private Expression cond;
	private Statement statement;

	private int uid;
	
	public DoWhile(Tree tree, ErrorsCollector ec, SymbolTable st) {
		assert(tree.getType() == CommonCppWithStreamsLexer.DOWHILE);
		assert(tree.getChildCount() == 2);

		cond = new Expression(tree.getChild(0), ec, st);
		statement = new Statement(tree.getChild(1), ec, st);
	}

	public void writeAsmCode(AsmWriter w) {
		uid = w.genNewUId();

		w.loopStart(this);

		w.c("jmp _loop_" + uid + "_statement");

		w.l("_loop_" + uid + "_start");
		cond.writeAsmCode(w);
		w.pop("eax");
		w.c("test eax, eax");
		w.c("jz _loop_" + uid + "_end");

		w.l("_loop_" + uid + "_statement");
		statement.writeAsmCode(w);
		w.c("jmp _loop_" + uid + "_start");

		w.l("_loop_" + uid + "_end");

		w.loopEnd();
	}

	public int getUId() {
		return uid;
	}
}
