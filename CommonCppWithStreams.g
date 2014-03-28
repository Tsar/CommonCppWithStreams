grammar CommonCppWithStreams;

@header {
}

@members {
}

s             : (variables_def | function_def)* EOF;
type          : TYPE_MODIFIER* TYPE | TYPE_MODIFIER+;
variables_def : type NAME ('=' expr)? (',' NAME ('=' expr)?)* ';';
function_def  : type NAME '(' (((type NAME (',' type NAME)*) | (type NAME '=' expr)) (',' type NAME '=' expr)*)? ')' (';' | codescope);

codescope     : '{' codeline* '}';
codeline      : variables_def | expr ';' | RETURN expr ';' | while_;
//if_           : IF '(' expr ')' (codeline | codescope | ';') (ELSE (codeline | codescope | ';'))?;
while_        : WHILE '(' expr ')' (codeline | codescope | ';') | DO codescope WHILE '(' expr ')' ';';
function_call : NAME '(' (expr (',' expr)*)? ')';
//expr          : expr2 (',' expr2)*;
expr          : expr3 ('=' expr3 | ('*=' | '/=' | '%=' | '+=' | '-=' | '>>=' | '<<=' | '&=' | '^=' | '|=') expr3 | '?' expr3 ':' expr3)*;
expr3         : expr4 ('||' expr4)*;
expr4         : expr5 ('&&' expr5)*;
expr5         : expr6 ('|' expr6)*;
expr6         : expr7 ('^' expr7)*;
expr7         : expr8 ('&' expr8)*;
expr8         : expr9 ('==' expr9 | '!=' expr9)*;
expr9         : expr10 (('<=' | '>=' | '<' | '>') expr10)*;
expr10        : expr11 ('<<' expr11 | '>>' expr11)*;
expr11        : expr12 ('+' expr12 | '-' expr12)*;
expr12        : expr13 ('*' expr13 | '/' expr13 | '%' expr13)*;
expr13        : ('++' | '--' | '!' | '~' | '+' | '-')? expr14;
expr14        : expr15 ('++' | '--')?;
expr15        : '(' expr ')' | NAME | NUMBER | function_call;

IF            : 'if';
ELSE          : 'else';
WHILE         : 'while';
DO            : 'do';
RETURN        : 'return';
TYPE_MODIFIER : 'signed' | 'unsigned' | 'long' | 'short';
TYPE          : 'int' | 'char' | 'bool' | 'void';
NUMBER        : ('0'..'9')+;
NAME          : ('A'..'Z' | 'a'..'z' | '_') ('A'..'Z' | 'a'..'z' | '_' | '0'..'9')*;
WS            : (' ' | '\t' | '\r' | '\n') {$channel=HIDDEN;};
