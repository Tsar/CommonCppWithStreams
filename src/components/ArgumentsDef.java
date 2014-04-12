package components;

import gen.CommonCppWithStreamsLexer;

import java.util.ArrayList;

import org.antlr.runtime.tree.Tree;

public class ArgumentsDef implements CodeProvider {
	private ErrorsCollector ec;

	private ArrayList<VarDef> arguments;
	
	public ArgumentsDef(Tree tree, ErrorsCollector ec, SymbolTable st) {
		this.ec = ec;
		
		assert(tree.getType() == CommonCppWithStreamsLexer.ARGS);

		arguments = new ArrayList<VarDef>();
		for (int i = 0; i < tree.getChildCount(); ++i) {
			Tree child = tree.getChild(i);
			assert(child.getType() == CommonCppWithStreamsLexer.ARG);
			assert(child.getChildCount() == 2 || child.getChildCount() == 3);
			
			arguments.add(new VarDef(child, ec, st));
			for (VarDef arg : arguments) {
				st.setVariableInitialized(arg.getName());
			}
		}
	}

	public int count() {
		return arguments.size();
	}

	public void checkArgumentsAndFillDefaults(ArrayList<Expression> args, int lineNumber) {
		assert(count() >= args.size());

		int n = args.size();
		for (int i = 0; i < n; ++i) {
			ec.check(TypeChecker.canBeAssigned(arguments.get(i), args.get(i)), lineNumber, "passing '" + TypeConverter.typeToString(args.get(i)) + "' as argument number " + (i + 1) + ", '" + TypeConverter.typeToString(arguments.get(i)) + "' expected");
		}
		for (int i = n; i < count(); ++i) {
			if (!arguments.get(i).hasDefaultValue()) {
				ec.check(false, lineNumber, "argument number " + (i + 1) + " expected");
				continue;
			}
			args.add(arguments.get(i).getDefaultValue());
		}
	}

	public void writeAsmCode(AsmWriter w) {
	}
}
