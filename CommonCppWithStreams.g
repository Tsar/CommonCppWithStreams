grammar CommonCppWithStreams;

@header {
}

@members {
}

s : {System.out.println("#1");} SCOPE_START {System.out.println("#2");} SCOPE_END {System.out.println("#3");};

SCOPE_START : '{';

SCOPE_END   : '}';
