grammar CommonCppWithStreams;

@header {
}

@members {
}

s             : (variables_def | function_def)* EOF;
variables_def : TYPE NAME ('=' expr)? (',' NAME ('=' expr)?)* ';';
function_def  : TYPE NAME '(' (((TYPE NAME (',' TYPE NAME)*) | (TYPE NAME '=' expr)) (',' TYPE NAME '=' expr)*)? ')' block;

block         : '{' statement* '}';
statement     : variables_def | expr ';' | RETURN expr? ';' | for_ | while_ | if_ | BREAK ';' | CONTINUE ';' | block | ';';
for_          : FOR '(' expr? ';' expr? ';' expr? ')' statement;
while_        : WHILE '(' expr ')' statement | DO statement WHILE '(' expr ')' ';';
if_           : IF '(' expr ')' statement (options {greedy=true;} : ELSE statement)?;
function_call : NAME '(' (expr (',' expr)*)? ')';
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
expr14        : '(' expr ')' | NAME | NUMBER | BOOL_VALUE | function_call;

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
NUMBER        : ('0'..'9')+;
NAME          : ('A'..'Z' | 'a'..'z' | '_') ('A'..'Z' | 'a'..'z' | '_' | '0'..'9')*;
WS            : (' ' | '\t' | '\r' | '\n') {$channel=HIDDEN;};

// для stream'ов сделать функцию наподобие FileStream('file'), чтобы их создавать
