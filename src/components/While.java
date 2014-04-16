package components;

import gen.CommonCppWithStreamsLexer;

import org.antlr.runtime.tree.Tree;

import base.AsmWriter;
import base.CodeProvider;
import base.ErrorsCollector;
import base.SymbolTable;

public class While implements CodeProvider {
	private Expression cond;
	private Statement statement;
	
	public While(Tree tree, ErrorsCollector ec, SymbolTable st) {
		assert(tree.getType() == CommonCppWithStreamsLexer.WHILE);
		assert(tree.getChildCount() == 2);

		cond = new Expression(tree.getChild(0), ec, st);
		statement = new Statement(tree.getChild(1), ec, st);
	}

	public void writeAsmCode(AsmWriter w) {
		int uid = w.genNewUId();
		
		w.l("_while_" + uid + "_start");
		cond.writeAsmCode(w);
		w.pop("eax");
		w.c("test eax, eax");
		w.c("jz _while_" + uid + "_end");
		
		statement.writeAsmCode(w);
		w.c("jmp _while_" + uid + "_start");
		
		w.l("_while_" + uid + "_end");
	}
}
