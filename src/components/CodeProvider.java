package components;

import java.io.PrintWriter;

public interface CodeProvider {
	public void writeCppCode(PrintWriter w);
	public void writeAsmCode(AsmWriter w);
}
