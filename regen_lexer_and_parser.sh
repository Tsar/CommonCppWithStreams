#!/bin/bash
rm CommonCppWithStreams.tokens CommonCppWithStreamsLexer.c CommonCppWithStreamsLexer.h CommonCppWithStreamsParser.c CommonCppWithStreamsParser.h
antlr3 CommonCppWithStreams.g
