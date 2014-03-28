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
//expr          : expr2 (COMMA expr2)*;
expr          : expr3 (EQ expr3 | COMP_ASSIGN expr3 | '?' expr3 ':' expr3)*;
expr3         : expr4 ('||' expr4)*;
expr4         : expr5 ('&&' expr5)*;
expr5         : expr6 ('|' expr6)*;
expr6         : expr7 ('^' expr7)*;
expr7         : expr8 ('&' expr8)*;
expr8         : expr9 ('==' expr9 | '!=' expr9)*;
expr9         : expr10 (COMPARISON_OP expr10)*;
expr10        : expr11 ('<<' expr11 | '>>' expr11)*;
expr11        : expr12 ('+' expr12 | '-' expr12)*;
expr12        : expr13 ('*' expr13 | '/' expr13 | '%' expr13)*;
expr13        : PREFIX_OP? expr14;
expr14        : expr15 ('++' | '--')?;
expr15        : LB expr RB | NAME | NUMBER | function_call;

SCOPE_START   : '{';
SCOPE_END     : '}';
LB            : '(';
RB            : ')';
EQ            : '=';
COMP_ASSIGN   : '*=' | '/=' | '%=' | '+=' | '-=' | '>>=' | '<<=' | '&=' | '^=' | '|=';
PREFIX_OP     : '+' | '-' | '++' | '--' | '!' | '~';
COMPARISON_OP : '<' | '>' | '<=' | '>=';
COMMA         : ',';
SEMICOLON     : ';';
RETURN        : 'return';
TYPE_MODIFIER : 'signed' | 'unsigned' | 'long' | 'short';
TYPE          : 'int' | 'char' | 'bool' | 'void';
NUMBER        : ('0'..'9')+;
NAME          : ('A'..'Z' | 'a'..'z' | '_') ('A'..'Z' | 'a'..'z' | '_' | '0'..'9')*;
WS            : (' ' | '\t' | '\r' | '\n') {$channel=HIDDEN;};
