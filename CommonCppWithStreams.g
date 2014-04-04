grammar CommonCppWithStreams;

options {
    output = AST;
    backtrack = true;
}

tokens {
    BLOCK;
    VAR_DEF;
    FUNCTION;
    ARG;
    ARGS;
    CALL;
    DOWHILE;
    FOR_INIT;
    FOR_COND;
    FOR_ACT;
    POSTFIX_PP;
    POSTFIX_MM;
    STREAM_READ;
    STREAM_WRITE;
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

variable_def  : NAME '=' expr -> NAME expr | NAME;
variables_def : TYPE variable_def (',' variable_def)* ';' -> ^(VAR_DEF TYPE variable_def)+;

func_arg_nodv : TYPE NAME -> ^(ARG TYPE NAME);
func_arg_dv   : TYPE NAME '=' expr -> ^(ARG TYPE NAME expr);
function_args : (((func_arg_nodv (',' func_arg_nodv)*) | func_arg_dv) (',' func_arg_dv)*)? -> ^(ARGS func_arg_nodv* func_arg_dv*);
function_def  : TYPE NAME '(' function_args ')' block -> ^(FUNCTION TYPE NAME function_args block);

block         : '{' statement* '}' -> ^(BLOCK statement*);
statement     : variables_def | stream_read | stream_write | expr ';'! | RETURN^ expr? ';'! | for_ | while_ | if_ | BREAK ';'! | CONTINUE ';'! | block | ';' -> BLOCK;
for_init      : expr? -> ^(FOR_INIT expr?);
for_cond      : expr? -> ^(FOR_COND expr?);
for_act       : expr? -> ^(FOR_ACT expr?);
for_          : FOR^ '('! for_init ';'! for_cond ';'! for_act ')'! statement;
while_        : WHILE^ '('! expr ')'! statement | DO statement WHILE '(' expr ')' ';' -> ^(DOWHILE expr statement);
if_           : IF^ '('! expr ')'! statement (options {greedy=true;} : ELSE! statement)?;
function_call : NAME '(' (expr (',' expr)*)? ')' -> ^(CALL NAME ^(ARGS expr*));
stream_func   : STREAM_FUNC '('! ')'!;
stream_f_func : STREAM_F_FUNC^ '('! FILE_NAME_STR ')'!;

stream_read   : NAME ('>>' NAME)+ ';' -> ^(STREAM_READ ^(NAME NAME+));
stream_write  : NAME ('<<' expr10)+ ';' -> ^(STREAM_WRITE ^(NAME expr10+));

expr          : expr2 (('=' | '*=' | '/=' | '%=' | '+=' | '-=' | '>>=' | '<<=' | '&=' | '^=' | '|=')^ expr)*;
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
expr12        : ('++' | '--' | '!' | '~' | '+' | '-')^ expr12 | expr13;
expr13        : expr14 '++' -> ^(POSTFIX_PP expr14) | expr14 '--' -> ^(POSTFIX_MM expr14) | expr14;
expr14        : '('! expr ')'! | NAME | NUMBER | BOOL_VALUE | stream_func | stream_f_func | function_call;

FOR           : 'for';
IF            : 'if';
ELSE          : 'else';
WHILE         : 'while';
DO            : 'do';
BREAK         : 'break';
CONTINUE      : 'continue';
RETURN        : 'return';
TYPE          : 'int' | 'bool' | 'void' | 'Stream';
BOOL_VALUE    : 'false' | 'true';
STREAM_FUNC   : 'InputStream' | 'OutputStream';
STREAM_F_FUNC : 'InputFileStream' | 'OutputFileStream' | 'InputBinaryFileStream' | 'OutputBinaryFileStream';
NUMBER        : ('0'..'9')+;
NAME          : ('A'..'Z' | 'a'..'z' | '_') ('A'..'Z' | 'a'..'z' | '_' | '0'..'9')*;
FILE_NAME_STR : '"' ~('"')* '"';
COMMENT       : (('//' ~('\r' | '\n')*) | ('/*' .* '*/')) {$channel=HIDDEN;};
WS            : (' ' | '\t' | '\r' | '\n') {$channel=HIDDEN;};
