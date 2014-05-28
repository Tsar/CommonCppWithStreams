package components;

import gen.CommonCppWithStreamsLexer;

import org.antlr.runtime.tree.Tree;

import base.AsmWriter;
import base.CodeProvider;
import base.ErrorsCollector;
import base.SymbolTable;

public class If implements CodeProvider {
	private Expression cond;
	private Statement st1;
	private Statement st2;

	public If(Tree tree, ErrorsCollector ec, SymbolTable st) {
		assert(tree.getType() == CommonCppWithStreamsLexer.IF);
		assert(tree.getChildCount() == 2 || tree.getChildCount() == 3);

		cond = new Expression(tree.getChild(0), ec, st);
		st1 = new Statement(tree.getChild(1), ec, st);
		st2 = (tree.getChildCount() == 3) ? new Statement(tree.getChild(2), ec, st) : null;
	}

	public void writeAsmCode(AsmWriter w) {
		int uid = w.genNewUId();

		if (st2 != null) {
			w.c("jmp _if_" + uid + "_cond");
			w.l("_if_" + uid + "_else_part");
			st2.writeAsmCode(w);
			w.c("jmp _if_" + uid + "_end");
			w.l("_if_" + uid + "_cond");
		}

		cond.writeAsmCode(w);
		w.pop("eax");
		w.c("test eax, eax");
		w.c("jz _if_" + uid + ((st2 != null) ? "_else_part" : "_end"));

		st1.writeAsmCode(w);

		w.l("_if_" + uid + "_end");
	}
}
