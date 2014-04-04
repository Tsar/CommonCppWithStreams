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

s             : {init();} (variables_def | function_def)* EOF! {finish();};

variables_def : TYPE n1=NAME ('=' expr {symbolTable.declareVariable($n1.text, $TYPE.text, true, $n1.getLine());} | {symbolTable.declareVariable($n1.text, $TYPE.text, false, $n1.getLine());}) (',' n2=NAME ('=' expr {symbolTable.declareVariable($n2.text, $TYPE.text, true, $n2.getLine());} | {symbolTable.declareVariable($n2.text, $TYPE.text, false, $n2.getLine());}))* ';';
function_args : {fa = new FuncArgs(this);} (((t1=TYPE n1=NAME {fa.add($n1.text, $t1.text, false, $n1.getLine());} (',' t2=TYPE n2=NAME {fa.add($n2.text, $t2.text, false, $n2.getLine());})*) | (t3=TYPE n3=NAME '=' expr {fa.add($n3.text, $t3.text, true, $n3.getLine());})) (',' t4=TYPE n4=NAME '=' expr {fa.add($n4.text, $t4.text, true, $n4.getLine());})*)? -> ^(ARGUMENTS ^(NAME TYPE)*);
function_def  : ft=TYPE fn=NAME '(' function_args ')' {symbolTable.declareFunction($fn.text, $ft.text, fa, $fn.getLine());} block -> ^(FUNCTION ^(NAME TYPE) function_args block);

block         : '{' {symbolTable.blockStarted();} statement* '}' {symbolTable.blockFinished();} -> ^(BLOCK statement*);
statement     : variables_def | expr ';'! | RETURN^ expr? ';'! | for_ | while_ | if_ | stream_read | stream_write | BREAK ';' | CONTINUE ';' | block | ';';
for_          : FOR^ '(' expr? ';' expr? ';' expr? ')' statement;
while_        : WHILE^ '('! expr ')'! statement | DO statement WHILE '(' expr ')' ';';
if_           : IF^ '('! expr ')'! statement (options {greedy=true;} : ELSE! statement)?;
function_call : NAME '(' (expr (',' expr)*)? ')' {symbolTable.checkFunctionCall($NAME.text, $NAME.getLine());} -> ^(CALL NAME ^(ARGUMENTS expr*));
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
expr14        : '(' expr ')' | NAME {symbolTable.checkVariableDeclared($NAME.text, $NAME.getLine());} | NUMBER | BOOL_VALUE | stream_func | stream_f_func | function_call;

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
