package components;

import gen.CommonCppWithStreamsLexer;

import org.antlr.runtime.tree.Tree;

import base.AsmWriter;
import base.CodeProvider;
import base.ErrorsCollector;
import base.SymbolTable;

public class StreamRead implements CodeProvider {
	public StreamRead(Tree tree, ErrorsCollector ec, SymbolTable st) {
		assert(tree.getType() == CommonCppWithStreamsLexer.STREAM_READ);
		assert(tree.getChildCount() == 1);
		// TODO
	}

	public void writeAsmCode(AsmWriter w) {
		// TODO
	}
}
