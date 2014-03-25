grammar CommonCppWithStreams;

@header {
}

@members {
}

s             : (variables_def | function_def)* EOF;
type          : TYPE_MODIFIER* TYPE | TYPE_MODIFIER+;
variables_def : type NAME (EQ expr)? (COMMA NAME (EQ expr)?)* SEMICOLON;
function_def  : type NAME LB (((type NAME (COMMA type NAME)*) | (type NAME EQ expr)) (COMMA type NAME EQ expr)*)? RB (SEMICOLON | codescope);

codescope     : SCOPE_START codeline* SCOPE_END;
codeline      : variables_def | expr SEMICOLON | RETURN expr SEMICOLON;
function_call : NAME LB (expr (COMMA expr)*)? RB;
expr          : LB expr RB | NUMBER | NAME | function_call | assignment;
assignment    : NAME EQ expr;

SCOPE_START   : '{';
SCOPE_END     : '}';
LB            : '(';
RB            : ')';
EQ            : '=';
COMMA         : ',';
SEMICOLON     : ';';
BINARY_OP_P0  : '+' | '-';
BINARY_OP_P1  : '*' | '/';
UNARY_OP      : '!' | '-';
RETURN        : 'return';
TYPE_MODIFIER : 'signed' | 'unsigned' | 'long' | 'short';
TYPE          : 'int' | 'char' | 'bool' | 'void';
NUMBER        : ('0'..'9')+;
NAME          : ('A'..'Z' | 'a'..'z' | '_') ('A'..'Z' | 'a'..'z' | '_' | '0'..'9')*;
WS            : (' ' | '\t' | '\r' | '\n') {$channel=HIDDEN;};
