grammar CommonCppWithStreams;

options {
    backtrack = true;
}

@header {
}

@members {
}

s             : (variables_def | function_def)* EOF;
variables_def : TYPE NAME ('=' expr)? (',' NAME ('=' expr)?)* ';';
function_def  : TYPE NAME '(' (((TYPE NAME (',' TYPE NAME)*) | (TYPE NAME '=' expr)) (',' TYPE NAME '=' expr)*)? ')' block;

block         : '{' statement* '}';
statement     : variables_def | expr ';' | RETURN expr? ';' | for_ | while_ | if_ | stream_read | stream_write | BREAK ';' | CONTINUE ';' | block | ';';
for_          : FOR '(' expr? ';' expr? ';' expr? ')' statement;
while_        : WHILE '(' expr ')' statement | DO statement WHILE '(' expr ')' ';';
if_           : IF '(' expr ')' statement (options {greedy=true;} : ELSE statement)?;
function_call : NAME '(' (expr (',' expr)*)? ')';
stream_func   : STREAM_FUNC '(' ')';
stream_f_func : STREAM_F_FUNC '(' FILE_NAME_STR ')';

expr          : expr2 (('=' | '*=' | '/=' | '%=' | '+=' | '-=' | '>>=' | '<<=' | '&=' | '^=' | '|=') expr2)*;
expr2         : expr3 ('||' expr3)*;
expr3         : expr4 ('&&' expr4)*;
expr4         : expr5 ('|' expr5)*;
expr5         : expr6 ('^' expr6)*;
expr6         : expr7 ('&' expr7)*;
expr7         : expr8 (('==' | '!=') expr8)*;
expr8         : expr9 (('<=' | '>=' | '<' | '>') expr9)*;
expr9         : expr10 (('<<' | '>>') expr10)*;
expr10        : expr11 (('+' | '-') expr11)*;
expr11        : expr12 (('*' | '/' | '%') expr12)*;
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
