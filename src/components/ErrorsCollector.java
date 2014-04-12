package components;

import java.io.PrintStream;

public class ErrorsCollector {
	private PrintStream printStream;

	private int errorsCount;
	private int warningsCount;

	public ErrorsCollector() {
		printStream = System.err;
		errorsCount = 0;
		warningsCount = 0;
	}
	
	public ErrorsCollector(PrintStream printStream) {
		this.printStream = printStream;
		errorsCount = 0;
		warningsCount = 0;
	}
	
	public void check(boolean condition, int lineNumber, String errorMessage) {
		if (!condition) {
			printStream.println(lineNumber + ": error: " + errorMessage);
			++errorsCount;
		}
	}

	public void warnIfNot(boolean condition, int lineNumber, String warningMessage) {
		if (!condition) {
			printStream.println(lineNumber + ": warning: " + warningMessage);
			++warningsCount;
		}
	}

	public void fatalError(String message) {
		printStream.println("fatal error: " + message);
		++errorsCount;
	}

	public int getErrorsCount() {
		return errorsCount;
	}
	
	public int getWarningsCount() {
		return warningsCount;
	}
}
