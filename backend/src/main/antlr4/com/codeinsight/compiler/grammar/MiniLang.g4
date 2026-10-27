grammar MiniLang;

// ==========================================
// PARSER RULES
// ==========================================

program
    : (functionDecl | globalDecl | statement)* EOF
    ;

globalDecl
    : varDecl
    ;

functionDecl
    : typeType ID '(' paramList? ')' block
    ;

paramList
    : param (',' param)*
    ;

param
    : typeType ID
    ;

typeType
    : INT_TYPE   # IntType
    | FLOAT_TYPE # FloatType
    | CHAR_TYPE  # CharType
    | BOOL_TYPE  # BoolType
    | VOID_TYPE  # VoidType
    ;

block
    : '{' statement* '}'
    ;

statement
    : block                                             # BlockStmt
    | varDecl                                           # VarDeclStmt
    | assignment ';'                                    # AssignStmt
    | IF '(' expression ')' statement (ELSE statement)? # IfStmt
    | WHILE '(' expression ')' statement                # WhileStmt
    | FOR '(' forInit? ';' expression? ';' forUpdate? ')' statement # ForStmt
    | RETURN expression? ';'                            # ReturnStmt
    | expression ';'                                    # ExprStmt
    | ';'                                               # EmptyStmt
    ;

varDecl
    : typeType ID ('=' expression)? ';'
    ;

assignment
    : ID '=' expression
    ;

forInit
    : varDeclNoSemi
    | assignment
    ;

varDeclNoSemi
    : typeType ID ('=' expression)?
    ;

forUpdate
    : assignment
    | expression
    ;

expression
    : expression '||' expression                       # LogicalOrExpr
    | expression '&&' expression                       # LogicalAndExpr
    | expression ('==' | '!=') expression              # EqualityExpr
    | expression ('<' | '<=' | '>' | '>=') expression  # RelationalExpr
    | expression ('+' | '-') expression                # AdditiveExpr
    | expression ('*' | '/' | '%') expression          # MultiplicativeExpr
    | ('!' | '+' | '-') expression                     # UnaryExpr
    | '(' expression ')'                               # ParenExpr
    | ID '(' argList? ')'                              # CallExpr
    | ID                                               # VarRefExpr
    | literal                                          # LiteralExpr
    ;

argList
    : expression (',' expression)*
    ;

literal
    : INT_LITERAL    # IntLit
    | FLOAT_LITERAL  # FloatLit
    | CHAR_LITERAL   # CharLit
    | BOOL_LITERAL   # BoolLit
    ;

// ==========================================
// LEXER RULES
// ==========================================

INT_TYPE     : 'int';
FLOAT_TYPE   : 'float';
CHAR_TYPE    : 'char';
BOOL_TYPE    : 'bool';
VOID_TYPE    : 'void';
IF           : 'if';
ELSE         : 'else';
WHILE        : 'while';
FOR          : 'for';
RETURN       : 'return';
BOOL_LITERAL : 'true' | 'false';

INT_LITERAL   : [0-9]+;
FLOAT_LITERAL : [0-9]+ '.' [0-9]+ ([eE] [+-]? [0-9]+)?;
CHAR_LITERAL  : '\'' (~['\\\r\n] | '\\' [abfnrtv\\'"]) '\'';
ID            : [a-zA-Z_] [a-zA-Z0-9_]*;

LINE_COMMENT  : '//' ~[\r\n]* -> channel(HIDDEN);
BLOCK_COMMENT : '/*' .*? '*/' -> channel(HIDDEN);
WS            : [ \t\r\n]+    -> channel(HIDDEN);

UNRECOGNIZED  : . ;
