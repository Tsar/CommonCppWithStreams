grammar CommonCppWithStreams;

@header {
}

@members {
}

s             : (variables_def | function_def)*;
variables_def : TYPE NAME (EQ expr)? (COMMA NAME (EQ expr)?)* SEMICOLON;
function_def  : TYPE NAME LB (((TYPE NAME (COMMA TYPE NAME)*) | (TYPE NAME EQ expr)) (COMMA TYPE NAME EQ expr)*)? RB (SEMICOLON | codescope);

codescope     : SCOPE_START codeline* SCOPE_END;
codeline      : variables_def | expr SEMICOLON | RETURN expr SEMICOLON;
function_call : NAME LB (expr (COMMA expr)*)? RB;
expr          : LB expr RB | NUMBER | NAME | function_call;

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
TYPE          : 'int' | 'char' | 'long' | 'bool' | 'void';
NUMBER        : ('0'..'9')+;
NAME          : ('A'..'Z' | 'a'..'z' | '_') ('A'..'Z' | 'a'..'z' | '_' | '0'..'9')*;
WS            : (' ' | '\t' | '\r' | '\n') {$channel=HIDDEN;};
