package components;

import java.util.List;

import gen.CommonCppWithStreamsLexer;

import org.antlr.runtime.tree.Tree;

import base.AsmWriter;
import base.CodeProvider;
import base.ErrorsCollector;
import base.SymbolTable;

public class Continue implements CodeProvider {
	private int blocksUnderCycle;

	public Continue(Tree tree, ErrorsCollector ec, SymbolTable st) {
		assert(tree.getType() == CommonCppWithStreamsLexer.CONTINUE);
		assert(tree.getChildCount() == 0);

		blocksUnderCycle = 0;
		Tree p = tree.getParent();
		while (p != null && p.getType() != CommonCppWithStreamsLexer.WHILE && p.getType() != CommonCppWithStreamsLexer.FOR) {
			if (p.getType() == CommonCppWithStreamsLexer.BLOCK)
				++blocksUnderCycle;
			p = p.getParent();
		}
		if (p == null) {
			ec.check(false, tree.getLine(), "'continue' not in any cycle");
			return;
		}
	}

	public void writeAsmCode(AsmWriter w) {
		assert(w.getBlockList().size() >= blocksUnderCycle);

		if (blocksUnderCycle > 0) {
			int copyOfSP = w.getSP();
			List<Block> blockList = w.getBlockList();
			for (int i = blockList.size() - 1; i >= blockList.size() - blocksUnderCycle; --i) {
				blockList.get(i).writeAsmCodeToCleanLocalVariables(w);
			}
			w.setSP(copyOfSP);
		}

		w.c("jmp _loop_" + w.getLastLoopUId() + "_start");
	}
}
