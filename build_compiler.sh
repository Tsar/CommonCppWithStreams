#!/bin/bash
./regen_lexer_and_parser.sh
CLASSPATH=/usr/share/java/antlr3.jar javac CommonCppWithStreamsCompiler.java CommonCppWithStreamsLexer.java CommonCppWithStreamsParser.java
