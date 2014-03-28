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
codeline      : variables_def | expr ';' | RETURN expr ';' | for_ | while_ | if_ | codescope | ';';
for_          : FOR '(' expr? ';' expr? ';' expr? ')' codeline;
while_        : WHILE '(' expr ')' codeline | DO codeline WHILE '(' expr ')' ';';
if_           : IF '(' expr ')' codeline (options {greedy=true;} : ELSE codeline)?;
function_call : NAME '(' (expr (',' expr)*)? ')';
expr          : expr2 (('=' | '*=' | '/=' | '%=' | '+=' | '-=' | '>>=' | '<<=' | '&=' | '^=' | '|=') expr2 | '?' expr2 ':' expr2)*;
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
expr14        : '(' expr ')' | NAME | NUMBER | function_call;

FOR           : 'for';
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
