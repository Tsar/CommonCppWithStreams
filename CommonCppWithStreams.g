grammar CommonCppWithStreams;

options {
    output = AST;
    backtrack = true;
}

tokens {
    BLOCK;
    FUNCTION;
    ARGUMENTS;
    CALL;
}

@header {
}

@members {

private static class CompilationError {
    private int lineNumber;
    private String message;

    public CompilationError(int lineNumber, String message) {
        this.lineNumber = lineNumber;
        this.message = message;
    }

    public int getLineNumber() {
        return lineNumber;
    }

    public String getMessage() {
        return message;
    }
}

private boolean compiledOK;
private List<CompilationError> compilationErrors;
private SymbolTable symbolTable;
private FuncArgs fa;

private void init() {
    compiledOK = true;
    compilationErrors = new ArrayList<CompilationError>();
    symbolTable = new SymbolTable(this);
}

public void addCompilationError(int lineNumber, String message) {
    compiledOK = false;
    compilationErrors.add(new CompilationError(lineNumber, message));
}

private void finish() {
    if (!compiledOK) {
        for (CompilationError ce : compilationErrors) {
            System.out.format("error at line \%d: \%s\n", ce.getLineNumber(), ce.getMessage());
        }
    }
    symbolTable._debug_output();
}

public DataType getVariableType(String typeStr, int lineNumber) {
    if (typeStr.equals("int")) {
        return DataType.INT_VARIABLE;
    } else if (typeStr.equals("bool")) {
        return DataType.BOOL_VARIABLE;
    } else {
        addCompilationError(lineNumber, "Variable can not have type '" + typeStr + "'");
        return null;
    }
}

public DataType getFunctionType(String typeStr, int lineNumber) {
    if (typeStr.equals("int")) {
        return DataType.INT_FUNCTION;
    } else if (typeStr.equals("bool")) {
        return DataType.BOOL_FUNCTION;
    } else if (typeStr.equals("void")) {
        return DataType.VOID_FUNCTION;
    } else {
        addCompilationError(lineNumber, "Function can not have type '" + typeStr + "'");
        return null;
    }
}

}

s             : (variables_def | function_def)* EOF!;

variables_def : TYPE n1=NAME ('=' expr)? (',' n2=NAME ('=' expr)?)* ';';
function_args : (((t1=TYPE n1=NAME (',' t2=TYPE n2=NAME)*) | (t3=TYPE n3=NAME '=' expr)) (',' t4=TYPE n4=NAME '=' expr)*)? -> ^(ARGUMENTS ^(NAME TYPE)*);
function_def  : ft=TYPE fn=NAME '(' function_args ')' block -> ^(FUNCTION ^(NAME TYPE) function_args block);

block         : '{' statement* '}' -> ^(BLOCK statement*);
statement     : variables_def | expr ';'! | RETURN^ expr? ';'! | for_ | while_ | if_ | stream_read | stream_write | BREAK ';' | CONTINUE ';' | block | ';';
for_          : FOR^ '(' expr? ';' expr? ';' expr? ')' statement;
while_        : WHILE^ '('! expr ')'! statement | DO statement WHILE '(' expr ')' ';';
if_           : IF^ '('! expr ')'! statement (options {greedy=true;} : ELSE! statement)?;
function_call : NAME '(' (expr (',' expr)*)? ')' -> ^(CALL NAME ^(ARGUMENTS expr*));
stream_func   : STREAM_FUNC '(' ')';
stream_f_func : STREAM_F_FUNC '(' FILE_NAME_STR ')';

expr          : expr2 (('=' | '*=' | '/=' | '%=' | '+=' | '-=' | '>>=' | '<<=' | '&=' | '^=' | '|=')^ expr2)*;
expr2         : expr3 ('||'^ expr3)*;
expr3         : expr4 ('&&'^ expr4)*;
expr4         : expr5 ('|'^ expr5)*;
expr5         : expr6 ('^'^ expr6)*;
expr6         : expr7 ('&'^ expr7)*;
expr7         : expr8 (('==' | '!=')^ expr8)*;
expr8         : expr9 (('<=' | '>=' | '<' | '>')^ expr9)*;
expr9         : expr10 (('<<' | '>>')^ expr10)*;
expr10        : expr11 (('+' | '-')^ expr11)*;
expr11        : expr12 (('*' | '/' | '%')^ expr12)*;
expr12        : ('++' | '--' | '!' | '~' | '+' | '-')? expr13;
expr13        : expr14 ('++' | '--')?;
expr14        : '(' expr ')' | NAME | NUMBER | BOOL_VALUE | stream_func | stream_f_func | function_call;

stream_read   : NAME ('>>' NAME)+ ';';
stream_write  : NAME ('<<' expr)+ ';';

FOR           : 'for';
IF            : 'if';
ELSE          : 'else';
WHILE         : 'while';
DO            : 'do';
BREAK         : 'break';
CONTINUE      : 'continue';
RETURN        : 'return';
TYPE          : 'int' | 'bool' | 'void';
BOOL_VALUE    : 'false' | 'true';
STREAM_FUNC   : 'InputStream' | 'OutputStream';
STREAM_F_FUNC : 'InputFileStream' | 'OutputFileStream' | 'InputBinaryFileStream' | 'OutputBinaryFileStream';
NUMBER        : ('0'..'9')+;
NAME          : ('A'..'Z' | 'a'..'z' | '_') ('A'..'Z' | 'a'..'z' | '_' | '0'..'9')*;
FILE_NAME_STR : '".*?"';
WS            : (' ' | '\t' | '\r' | '\n') {$channel=HIDDEN;};
