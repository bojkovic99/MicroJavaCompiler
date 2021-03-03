
package rs.ac.bg.etf.pp1;

import java_cup.runtime.Symbol;

%%

%{

	// ukljucivanje informacije o poziciji tokena
	private Symbol new_symbol(int type) {
		return new Symbol(type, yyline+1, yycolumn);
	}
	
	// ukljucivanje informacije o poziciji tokena
	private Symbol new_symbol(int type, Object value) {
		return new Symbol(type, yyline+1, yycolumn, value);
	}

%}

%cup
%line
%column

%xstate COMMENT

%eofval{
	return new_symbol(sym.EOF);
%eofval}

%%

" " 	{ }
"\b" 	{ }
"\t" 	{ }
"\r\n" 	{ }
"\f" 	{ }
/*
	public static final int LETTER = 31;


	public static final int CHARCONST = 34;

	public static final int BOOLCONST = 36;

	
	
	// Constants
	public static final int NUMBER = 5;
*/
"program"   { return new_symbol(sym.PROG, yytext());}
"print" 	{ return new_symbol(sym.PRINT, yytext()); }
"return" 	{ return new_symbol(sym.RETURN, yytext()); }
"void" 		{ return new_symbol(sym.VOID, yytext()); }
"break"  	{ return new_symbol(sym.BREAK, yytext()); }
"class"		{ return new_symbol(sym.CLASS, yytext()); }
"enum"		{ return new_symbol(sym.ENUM, yytext()); }
"else"		{ return new_symbol(sym.ELSE, yytext()); }
"const"		{ return new_symbol(sym.CONST, yytext()); }
"if"		{ return new_symbol(sym.IF, yytext()); }
"switch"	{ return new_symbol(sym.SWITCH, yytext()); }
"do"		{ return new_symbol(sym.DO, yytext()); }
"while"		{ return new_symbol(sym.WHILE, yytext()); }
"new"		{ return new_symbol(sym.NEW, yytext()); }
"read"		{ return new_symbol(sym.READ, yytext()); }

"extends"	{ return new_symbol(sym.EXTENDS, yytext()); }
"continue"	{ return new_symbol(sym.CONTINUE, yytext()); }
"case"  	{ return new_symbol(sym.CASE, yytext()); }
"+" 		{ return new_symbol(sym.PLUS, yytext()); }
"=" 		{ return new_symbol(sym.EQUAL, yytext()); }
";" 		{ return new_symbol(sym.SEMI, yytext()); }
"," 		{ return new_symbol(sym.COMMA, yytext()); }
"(" 		{ return new_symbol(sym.LPAREN, yytext()); }
")" 		{ return new_symbol(sym.RPAREN, yytext()); }
"{" 		{ return new_symbol(sym.LBRACE, yytext()); }
"}"			{ return new_symbol(sym.RBRACE, yytext()); }
"-"			{ return new_symbol(sym.MINUS, yytext()); }
"*"			{ return new_symbol(sym.ZVEZDICA, yytext()); }
"/"			{ return new_symbol(sym.PODELJENO, yytext()); }
"%"			{ return new_symbol(sym.PROCENAT, yytext()); }
"=="		{ return new_symbol(sym.JEDNAKOJEDNAKO, yytext()); }
"!="		{ return new_symbol(sym.RAZLICITO, yytext()); }
">"			{ return new_symbol(sym.VECE, yytext()); }
">="		{ return new_symbol(sym.VECEJEDNAKO, yytext()); }
"<"			{ return new_symbol(sym.MANJE, yytext()); }
"<="		{ return new_symbol(sym.MANJEJEDNAKO, yytext()); }
"&&"		{ return new_symbol(sym.LOGICKOI, yytext()); }
"||"		{ return new_symbol(sym.LOGICKOILI, yytext()); }
"++"		{ return new_symbol(sym.PLUSPLUS, yytext()); }
"--"		{ return new_symbol(sym.MINUSMINUS, yytext()); }
"."			{ return new_symbol(sym.TACKA, yytext()); }
"["			{ return new_symbol(sym.LUGLASTA, yytext()); }
"]"			{ return new_symbol(sym.DUGLASTA, yytext()); }
"?"			{ return new_symbol(sym.PITANJE, yytext()); }
":"			{ return new_symbol(sym.DVOTACKA, yytext()); }

"//" {yybegin(COMMENT);}
<COMMENT> . {yybegin(COMMENT);}
<COMMENT> "\r\n" { yybegin(YYINITIAL); }
("true"|"false") 	{return new_symbol (sym.BOOLCONST, yytext()); }
[0-9]+  { return new_symbol(sym.NUMBER, new Integer (yytext())); }

([a-z]|[A-Z])[a-z|A-Z|0-9|_]* 	{return new_symbol (sym.IDENT, yytext()); }
"'"."'" 	{ return new_symbol (sym.CHARCONST, yytext()); }

. { System.err.println("Leksicka greska ("+yytext()+") u liniji "+(yyline+1)); }










