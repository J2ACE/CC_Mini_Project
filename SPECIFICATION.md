# CodeInsight: Intelligent Static Code Analyzer with Optimization Suggestions
## Comprehensive Engineering Architecture & Implementation Blueprint

---

### A. Executive Project Summary

**CodeInsight** is an automated static code analysis and classical compiler optimization engine designed for educational demonstration and practical static analysis in academic and developer tooling environments. Built specifically to showcase core principles of **Compiler Construction**, CodeInsight accepts programs written in **MiniLang** (a deterministic, typed, C-like imperative programming language) and processes them through an end-to-end multi-stage compiler frontend and analysis pipeline.

Unlike basic linters that rely on regular expressions or heuristic text matching, CodeInsight executes genuine lexical analysis (DFA-based tokenization), LL(*)/ALL(*) syntax analysis (ANTLR 4), Abstract Syntax Tree (AST) synthesis, scoped hierarchical symbol resolution, multi-pass semantic verification, Control Flow Graph (CFG) construction with Basic Block partitioning, iterative bit-vector Data-Flow Analysis (DFA), and classical intermediate-representation optimizations.

The application is architected as a lightweight, modular system consisting of:
1. A **Java 17/21 & Spring Boot** backend encapsulating the core compiler engine, AST visitors, data-flow solvers, and REST endpoints.
2. A **React + TypeScript + Vite** web dashboard featuring Monaco Editor, interactive Graphviz CFG diagrams, expandable AST hierarchies, symbol table viewers, and side-by-side optimization diff previews.
3. **Zero database dependencies** to guarantee instantaneous zero-config deployment during project evaluation and laboratory demonstrations.

---

### B. High-Level Architecture & Pipeline Flow

The CodeInsight pipeline follows a strictly unidirectional, non-circular pipeline:

```
[ Raw MiniLang Source Code ]
           │
           ▼
┌─────────────────────────────────────────────────────────────┐
│ 1. LEXICAL ANALYSIS (ANTLR4 MiniLangLexer)                   │
│    • DFA-based tokenization                                 │
│    • Token stream generation                                │
│    • Lexical error listener & character validation          │
└──────────────────────────┬──────────────────────────────────┘
                           │ Tokens + Lexical Diagnostics
                           ▼
┌─────────────────────────────────────────────────────────────┐
│ 2. SYNTAX ANALYSIS (ANTLR4 MiniLangParser)                  │
│    • Concrete Syntax Tree (ParseTree) generation            │
│    • Descriptive syntax error recovery & reporting          │
└──────────────────────────┬──────────────────────────────────┘
                           │ ParseTree
                           ▼
┌─────────────────────────────────────────────────────────────┐
│ 3. AST CONSTRUCTION (ASTBuilderVisitor)                     │
│    • Custom decoupled AST nodes                             │
│    • Accurate source coordinates (line, col, offset, length)│
└──────────────────────────┬──────────────────────────────────┘
                           │ Abstract Syntax Tree (AST)
                           ▼
┌─────────────────────────────────────────────────────────────┐
│ 4. SYMBOL TABLE & SCOPE RESOLUTION (ScopeResolverVisitor)   │
│    • Hierarchical scoped symbol table (Global, Func, Block) │
│    • Declaration tracking, type binding, shadow detection   │
└──────────────────────────┬──────────────────────────────────┘
                           │ Annotated AST + Symbol Table
                           ▼
┌─────────────────────────────────────────────────────────────┐
│ 5. SEMANTIC ANALYSIS (SemanticAnalyzerVisitor)              │
│    • Static type checking & assignment compatibility        │
│    • Function signature matching & return validation        │
│    • Definite assignment / uninitialized access detection   │
└──────────────────────────┬──────────────────────────────────┘
                           │ Validated AST + Symbol Table
                           ▼
┌─────────────────────────────────────────────────────────────┐
│ 6. CONTROL FLOW GRAPH (CFGBuilder)                          │
│    • Basic block leader detection                           │
│    • Entry/Exit blocks, branch/loop/fallthrough edge typing │
│    • Graphviz DOT & JSON graph representation               │
└──────────────────────────┬──────────────────────────────────┘
                           │ CFG (per function)
                           ▼
┌─────────────────────────────────────────────────────────────┐
│ 7. DATA-FLOW ANALYSIS ENGINE (FixedPointDataFlowSolver)     │
│    • Reaching Definitions (Forward, ∪)                      │
│    • Live Variable Analysis (Backward, ∪)                   │
│    • Available Expressions (Forward, ∩)                     │
│    • Constant Propagation Lattice                           │
└──────────────────────────┬──────────────────────────────────┘
                           │ Data-Flow In/Out Sets per Block
                           ▼
┌─────────────────────────────────────────────────────────────┐
│ 8. STATIC ANALYSIS ENGINE (StaticAnalysisEngine)            │
│    • Unused variables (STA001)                              │
│    • Uninitialized reads (STA002)                           │
│    • Unreachable dead code (STA003)                         │
│    • Redundant assignments (STA004)                         │
│    • Suspicious conditions (STA005)                         │
└──────────────────────────┬──────────────────────────────────┘
                           │ Code Quality Diagnostics
                           ▼
┌─────────────────────────────────────────────────────────────┐
│ 9. OPTIMIZATION ENGINE (OptimizationEngine)                 │
│    • Constant Folding (OPT001)                              │
│    • Constant Propagation (OPT002)                          │
│    • Algebraic Simplification (OPT003)                      │
│    • Common Subexpression Elimination (OPT004)              │
│    • Strength Reduction (OPT005)                            │
│    • Loop-Invariant Code Motion (OPT006)                    │
└──────────────────────────┬──────────────────────────────────┘
                           │ Optimization Diagnostics & AST Transforms
                           ▼
┌─────────────────────────────────────────────────────────────┐
│ 10. DIAGNOSTIC AGGREGATION & REPORTING (DiagnosticService)  │
│     • Unified Diagnostic Model                              │
│     • Code diff generator (original vs optimized)           │
│     • Complexity and token metrics calculation              │
└──────────────────────────┬──────────────────────────────────┘
                           │ JSON Analysis Response
                           ▼
┌─────────────────────────────────────────────────────────────┐
│ 11. REST API (AnalysisController)                           │
│     • POST /api/analyze                                     │
│     • GET  /api/samples                                     │
│     • GET  /api/rules                                       │
└──────────────────────────┬──────────────────────────────────┘
                           │ HTTP JSON
                           ▼
┌─────────────────────────────────────────────────────────────┐
│ 12. REACT + TYPESCRIPT FRONTEND (Monaco + Graphviz + D3)    │
│     • Code editor with inline squiggles & gutter markers    │
│     • Interactive CFG visualizer & AST tree browser         │
│     • Symbol table inspect modal                            │
│     • Side-by-side diff preview & suggestion patcher        │
└─────────────────────────────────────────────────────────────┘
```

---

### C. Complete Module Breakdown

| Module | Core Class / Package | Primary Purpose | Inputs | Outputs |
|---|---|---|---|---|
| **Lexer & Parser** | `com.codeinsight.compiler.frontend.parser` | Tokenize and parse MiniLang source code using ANTLR 4 runtime | Raw source string | ANTLR `ParseTree`, Lexer/Parser error list |
| **AST Hierarchy** | `com.codeinsight.compiler.ast.node.*` | Immutable object model representing language nodes with coordinate tracking | None (Structural Model) | Strong-typed AST nodes |
| **AST Builder** | `com.codeinsight.compiler.ast.builder.ASTBuilderVisitor` | Transforms ANTLR parse tree into custom `ASTNode` tree | ANTLR `ParseTree` | Root `ProgramNode` |
| **Symbol Table** | `com.codeinsight.compiler.symbol.*` | Maintains nested scopes, symbol metadata, read/write counters | `ProgramNode` | `SymbolTable` hierarchy |
| **Semantic Analyzer** | `com.codeinsight.compiler.semantic.SemanticAnalyzer` | Validates types, function calls, return paths, and variable initialization | AST, `SymbolTable` | Semantic diagnostics (Errors & Warnings) |
| **CFG Engine** | `com.codeinsight.compiler.cfg.*` | Builds directed control flow graphs of basic blocks per function | AST Functions | `ControlFlowGraph` (Nodes, Edges, DOT, JSON) |
| **Data-Flow Solver** | `com.codeinsight.compiler.dataflow.*` | Solves generic bit-vector dataflow equations with fixed-point iteration | `ControlFlowGraph` | $IN[B]$ and $OUT[B]$ sets per basic block |
| **Static Analyzer** | `com.codeinsight.compiler.analyzer.StaticAnalysisEngine` | Executes semantic inspection and CFG checks for dead/unreachable code | AST, CFG, Dataflow | Static analysis diagnostics (`STAxxx`) |
| **Optimization Engine** | `com.codeinsight.compiler.optimizer.*` | Identifies classical compiler optimization opportunities and generates rewrites | AST, CFG, Dataflow | Optimization diagnostics (`OPTxxx`) with code diffs |
| **Report & Metrics** | `com.codeinsight.compiler.service.AnalysisService` | Orchestrates pipeline, computes statistics, produces unified payload | Source string | `AnalysisResponseDto` |
| **Web API** | `com.codeinsight.web.controller.AnalysisController` | Exposes Spring Boot REST endpoints | HTTP Request | HTTP JSON response |
| **Web Frontend** | `frontend/src/*` | Interactive UI with Monaco Editor, Graphviz SVG, AST Tree, Diff views | User edits & API data | Visual analyzer experience |

---

### D. Complete Feature List

1. **Multi-Stage Compiler Diagnostics**:
   - Distinction between Lexical, Syntax, Semantic, Quality, and Optimization findings.
   - Precise line number, column offset, source snippet, and highlight length.
2. **Interactive Syntax Tree (AST) Visualization**:
   - Expandable tree view of the custom AST.
   - Node-level metadata inspection (type, operator, scope level, declared symbols).
3. **Control Flow Graph (CFG) Rendering**:
   - Automatic decomposition of MiniLang code into Basic Blocks.
   - Visual graph rendering with labeled branching edges: True (green), False (red), Unconditional (blue), Loop-back (purple), Return (amber).
   - Highlighting of unreachable basic blocks.
4. **Symbol Table Inspector**:
   - Scope hierarchy navigator (Global $\rightarrow$ Function $\rightarrow$ Nested Blocks).
   - Variables tracked with Type, Declaration Coordinate, Read/Write Counts, Initialized State, and Shadowing status.
5. **Static Code Quality Checks**:
   - Unused variable detection (`STA001`).
   - Uninitialized variable usage detection (`STA002`).
   - Unreachable code detection via CFG graph reachability (`STA003`).
   - Dead / Redundant store detection via Live Variable Analysis (`STA004`).
   - Tautological / Invariant condition detection (`STA005`).
6. **Classical Compiler Optimizations**:
   - **Constant Folding (`OPT001`)**: Arithmetic, boolean, and relational compile-time constant evaluation.
   - **Constant Propagation (`OPT002`)**: Forward propagation of known invariant assignments.
   - **Algebraic Simplification (`OPT003`)**: Identities ($x+0$, $x \times 1$, $x \times 0$, $x \land \text{true}$, etc.).
   - **Common Subexpression Elimination (`OPT004`)**: Detection of redundant identical subtrees using Available Expressions.
   - **Strength Reduction (`OPT005`)**: Replacement of expensive operations with cheaper counterparts ($x \times 2 \rightarrow x \ll 1$ or $x + x$, $x / 2 \rightarrow x \gg 1$).
   - **Loop-Invariant Code Motion (`OPT006`)**: Hoisting loop invariants outside `while` and `for` bodies.
7. **Actionable Code Modification Suggestions**:
   - Single-click side-by-side Diff view showing original expression vs. optimized code snippet.
   - One-click "Apply Optimization" button directly in Monaco Editor.
8. **Exportable Evaluation Reports**:
   - Comprehensive summary metrics: Token count, Cyclomatic complexity, Issue density, AST depth.
   - Export report as JSON or formatted printable HTML/PDF.

---

### E. Technology Stack

| Layer | Technology | Version | Rationale |
|---|---|---|---|
| **Core Language** | Java (OpenJDK) | 17 LTS / 21 LTS | Robust object-oriented compiler infrastructure, type-safety, pattern matching |
| **Parser Generator** | ANTLR 4 | 4.13.1 | Industry standard LL(*) parser generator; generates clean Java visitor APIs |
| **Backend Framework** | Spring Boot | 3.2.x | Lightweight REST controller, zero-boilerplate DI, production-ready JSON handling |
| **Build System** | Maven | 3.9.x | Deterministic dependency management, integrated `antlr4-maven-plugin` |
| **Testing** | JUnit 5 & AssertJ | 5.10.x | Comprehensive unit testing and fluent AST/CFG assertions |
| **Frontend Framework** | React + TypeScript | React 18 / TS 5.x | Strongly typed modern component architecture |
| **Frontend Bundler** | Vite | 5.x | Instant HMR, minimal build footprint |
| **Code Editor** | Monaco Editor | `@monaco-editor/react` | VS Code editor experience, native line squiggles, diff editor support |
| **Graph Visualization** | Graphviz WASM | `@hpcc-js/wasm` | Native client-side Graphviz DOT rendering without server-side graphviz binaries |
| **Tree Visualizer** | React Flow / Lucide | Modern UI icons | Clean collapsible tree view for AST inspection |
| **Persistence** | *None (In-memory)* | N/A | Strictly deterministic static analysis; avoids DB setup hassles for evaluators |

---

### F. MiniLang Language Specification

#### 1. Lexical Grammar & Tokens
- **Keywords**: `int`, `float`, `char`, `bool`, `if`, `else`, `while`, `for`, `return`, `true`, `false`, `void`
- **Identifiers**: Regex `[a-zA-Z_][a-zA-Z0-9_]*`
- **Integer Literals**: Regex `[0-9]+`
- **Float Literals**: Regex `[0-9]+\.[0-9]+([eE][+-]?[0-9]+)?`
- **Character Literals**: Regex `'([^'\\]|\\.)'`
- **Boolean Literals**: `true`, `false`
- **Operators**:
  - Arithmetic: `+`, `-`, `*`, `/`, `%`
  - Relational: `<`, `<=`, `>`, `>=`, `==`, `!=`
  - Logical: `&&`, `||`, `!`
  - Assignment: `=`
  - Bitwise (for Strength Reduction target): `<<`, `>>`
- **Punctuation**: `;`, `,`, `(`, `)`, `{`, `}`
- **Comments**: Single-line `// ... \n` and Multi-line `/* ... */` (skipped via hidden channel)

#### 2. Type System
- Primitive types: `int`, `float`, `char`, `bool`, `void` (function returns only).
- Strongly typed: No implicit narrowing conversions (e.g. `float` to `int` is a semantic error).
- Explicit promotion allowed: `int` can be promoted to `float` in binary arithmetic (`int + float -> float`).
- Boolean context required for conditions in `if`, `while`, and `for`.

#### 3. Syntactic Structure
- A MiniLang program is a sequence of **Global Variable Declarations** and **Function Definitions**.
- Functions must contain a return type, parameter list, and a block body.
- Statements include:
  - Variable declarations with optional initializers: `int a = 10;`
  - Assignments: `a = b + 5;`
  - Control flow: `if (cond) { ... } else { ... }`, `while (cond) { ... }`, `for (init; cond; step) { ... }`
  - Function calls as statements: `print(x);`
  - Return statements: `return expr;` or `return;`
  - Nested block scopes: `{ int inner = 1; }`

---

### G. Grammar Specification (ANTLR 4)

File: `src/main/antlr4/com/codeinsight/compiler/grammar/MiniLang.g4`

```antlr
grammar MiniLang;

// ==========================================
// PARSER RULES
// ==========================================

program
    : (globalDecl | functionDecl)* EOF
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
    : INT_TYPE
    | FLOAT_TYPE
    | CHAR_TYPE
    | BOOL_TYPE
    | VOID_TYPE
    ;

block
    : '{' statement* '}'
    ;

statement
    : block                                         # BlockStmt
    | varDecl                                       # VarDeclStmt
    | assignment ';'                                # AssignStmt
    | ifStatement                                   # IfStmt
    | whileStatement                                # WhileStmt
    | forStatement                                  # ForStmt
    | returnStatement ';'                           # ReturnStmt
    | expression ';'                                # ExprStmt
    | ';'                                           # EmptyStmt
    ;

varDecl
    : typeType ID ('=' expression)? ';'
    ;

assignment
    : ID '=' expression
    ;

ifStatement
    : IF '(' expression ')' statement (ELSE statement)?
    ;

whileStatement
    : WHILE '(' expression ')' statement
    ;

forStatement
    : FOR '(' forInit? ';' expression? ';' forUpdate? ')' statement
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

returnStatement
    : RETURN expression?
    ;

// Expression hierarchy with explicit precedence
expression
    : expression '||' expression                   # LogicalOrExpr
    | expression '&&' expression                   # LogicalAndExpr
    | expression ('==' | '!=') expression          # EqualityExpr
    | expression ('<' | '<=' | '>' | '>=') expression # RelationalExpr
    | expression ('+' | '-') expression            # AdditiveExpr
    | expression ('*' | '/' | '%') expression      # MultiplicativeExpr
    | ('!' | '+' | '-') expression                 # UnaryExpr
    | '(' expression ')'                           # ParenExpr
    | ID '(' argList? ')'                          # FunctionCallExpr
    | ID                                           # VariableExpr
    | literal                                      # LiteralExpr
    ;

argList
    : expression (',' expression)*
    ;

literal
    : INT_LITERAL                                   # IntLiteral
    | FLOAT_LITERAL                                 # FloatLiteral
    | CHAR_LITERAL                                  # CharLiteral
    | BOOL_LITERAL                                  # BoolLiteral
    ;

// ==========================================
// LEXER RULES
// ==========================================

// Keywords
INT_TYPE   : 'int';
FLOAT_TYPE : 'float';
CHAR_TYPE  : 'char';
BOOL_TYPE  : 'bool';
VOID_TYPE  : 'void';
IF         : 'if';
ELSE       : 'else';
WHILE      : 'while';
FOR        : 'for';
RETURN     : 'return';
BOOL_LITERAL : 'true' | 'false';

// Literals
INT_LITERAL   : [0-9]+;
FLOAT_LITERAL : [0-9]+ '.' [0-9]+ ([eE] [+-]? [0-9]+)?;
CHAR_LITERAL  : '\'' (~['\\\r\n] | '\\' [abfnrtv\\'"]) '\'';

// Identifiers
ID : [a-zA-Z_] [a-zA-Z0-9_]*;

// Comments & Whitespace
LINE_COMMENT  : '//' ~[\r\n]* -> channel(HIDDEN);
BLOCK_COMMENT : '/*' .*? '*/' -> channel(HIDDEN);
WS            : [ \t\r\n]+    -> channel(HIDDEN);

// Unrecognized characters (Lexical Error Trap)
UNRECOGNIZED  : . ;
```

---

### H. Abstract Syntax Tree (AST) Architecture

The AST is completely decoupled from ANTLR runtime classes. Every node inherits from `ASTNode` and retains precise source file coordinates.

#### 1. Core Class Hierarchy

```
ASTNode (abstract)
│  ├── SourcePosition (line, column, charOffset, length)
│  └── accept(ASTVisitor<R, C> visitor, C context)
│
├── ProgramNode (List<GlobalDeclNode>, List<FunctionNode>)
├── FunctionNode (Type returnType, String name, List<ParamNode>, BlockNode body)
├── ParamNode (Type type, String name)
│
├── StatementNode (abstract)
│   ├── BlockNode (List<StatementNode> statements)
│   ├── VarDeclNode (Type type, String name, Optional<ExpressionNode> init)
│   ├── AssignNode (String targetVar, ExpressionNode value)
│   ├── IfNode (ExpressionNode condition, StatementNode thenBranch, Optional<StatementNode> elseBranch)
│   ├── WhileNode (ExpressionNode condition, StatementNode body)
│   ├── ForNode (Optional<ASTNode> init, Optional<ExpressionNode> condition, Optional<ASTNode> update, StatementNode body)
│   ├── ReturnNode (Optional<ExpressionNode> value)
│   └── ExprStmtNode (ExpressionNode expr)
│
└── ExpressionNode (abstract)
    ├── Type inferredType
    ├── BinaryExpr (Operator op, ExpressionNode left, ExpressionNode right)
    ├── UnaryExpr (Operator op, ExpressionNode operand)
    ├── LiteralNode (Type type, Object rawValue)
    ├── VarRefNode (String variableName)
    └── CallNode (String functionName, List<ExpressionNode> arguments)
```

#### 2. Source Position Retention
```java
public class SourcePosition {
    private final int line;
    private final int column;
    private final int startIndex;
    private final int stopIndex;
    // constructors, getters, toString
}
```

#### 3. Visitor Pattern Contract
```java
public interface ASTVisitor<R, C> {
    R visit(ProgramNode node, C context);
    R visit(FunctionNode node, C context);
    R visit(ParamNode node, C context);
    R visit(BlockNode node, C context);
    R visit(VarDeclNode node, C context);
    R visit(AssignNode node, C context);
    R visit(IfNode node, C context);
    R visit(WhileNode node, C context);
    R visit(ForNode node, C context);
    R visit(ReturnNode node, C context);
    R visit(ExprStmtNode node, C context);
    R visit(BinaryExpr node, C context);
    R visit(UnaryExpr node, C context);
    R visit(LiteralNode node, C context);
    R visit(VarRefNode node, C context);
    R visit(CallNode node, C context);
}
```

---

### I. Symbol Table & Scope Management

#### 1. Data Structure Design
The symbol table is structured as a Lexical Scope Tree:
- Each scope maintains a `Map<String, Symbol> symbols`.
- Each scope contains an enclosing parent pointer `Scope parentScope` (null for Global Scope).
- Each scope holds a list of children: `List<Scope> childScopes`.

```java
public enum SymbolKind {
    VARIABLE, PARAMETER, FUNCTION
}

public class Symbol {
    private final String name;
    private final Type type;
    private final SymbolKind kind;
    private final Scope scope;
    private final SourcePosition declarationPosition;
    private boolean initialized;
    private int readCount;
    private int writeCount;
    private final List<SourcePosition> references = new ArrayList<>();
    // getters, setters, incrementers
}

public class Scope {
    private final Scope parent;
    private final ScopeType type; // GLOBAL, FUNCTION, BLOCK
    private final Map<String, Symbol> symbols = new LinkedHashMap<>();
    private final List<Scope> children = new ArrayList<>();
    private final int scopeLevel;
    // define(Symbol), resolve(String), resolveLocally(String)
}
```

#### 2. Scope Resolution Rules
- **Lookup (`resolve`)**: Checks `symbols.get(name)`. If not present, recursively calls `parent.resolve(name)`.
- **Local Lookup (`resolveLocally`)**: Checks only the immediate scope. Used to detect duplicate definitions in the same scope.
- **Shadowing**: If `resolveLocally` is null but `parent.resolve` succeeds, a warning (`SEM002`) is emitted indicating that an outer variable is shadowed.
- **Reference Tracking**: Every AST variable reference increments `readCount` and appends its `SourcePosition`. Assignments increment `writeCount` and mark `initialized = true`.

---

### J. Semantic Analysis & Type System Rules

Semantic checks are executed by `SemanticAnalyzerVisitor` traversing the AST:

| Rule ID | Severity | Category | Condition / Detection |
|---|---|---|---|
| **SEM001** | ERROR | Undeclared Variable | Identifier referenced in expression or assignment target without being present in current or enclosing scopes. |
| **SEM002** | WARNING | Variable Shadowing | Variable declared in local/block scope with the exact same identifier as an active variable in an enclosing scope. |
| **SEM003** | ERROR | Duplicate Declaration | Variable or parameter declared more than once within the exact same lexical scope. |
| **SEM004** | ERROR | Type Mismatch in Assignment | Variable assigned a value whose evaluated type does not match the variable's declared type (e.g. `int x = 3.14;`). |
| **SEM005** | ERROR | Non-Boolean Condition | Expression in `if (...)`, `while (...)`, or `for (; ... ;)` condition does not evaluate to `bool`. |
| **SEM006** | ERROR | Incompatible Binary Operands | Invalid operand types (e.g., `int + bool`, `char * float`, logical `&&` on integers). |
| **SEM007** | ERROR | Function Not Declared | Call expression references a function name not found in the global symbol table. |
| **SEM008** | ERROR | Function Arity Mismatch | Number of arguments passed to function does not match declared parameter count. |
| **SEM009** | ERROR | Function Argument Type Mismatch | Passed argument type cannot be assigned to declared parameter type. |
| **SEM010** | ERROR | Missing / Invalid Return Type | Non-void function has execution paths without a `return` statement, or returns expression with mismatched type. |
| **SEM011** | WARNING | Use Before Initialization | Variable read before any definite assignment on the current path (tracked via conservative static walk). |

---

### K. Control Flow Graph (CFG) Design

The CFG is built on a per-function basis using standard Basic Block leader election.

#### 1. Basic Block Anatomy
A `BasicBlock` is a maximal linear sequence of program instructions with:
- Exactly **one entry point** (the first instruction).
- Exactly **one exit point** (the last instruction).
- No internal branches (if the first instruction executes, all subsequent instructions execute in order).

```java
public class BasicBlock {
    private final String id; // e.g. "B0", "B1", "ENTRY", "EXIT"
    private final List<ASTNode> instructions = new ArrayList<>();
    private final List<CFGEdge> incomingEdges = new ArrayList<>();
    private final List<CFGEdge> outgoingEdges = new ArrayList<>();
    // Data-flow sets attached to block
    private final Set<Definition> genReaching = new HashSet<>();
    private final Set<Definition> killReaching = new HashSet<>();
    private final Set<Variable> genLive = new HashSet<>();
    private final Set<Variable> killLive = new HashSet<>();
}
```

#### 2. Edge Classification
```java
public enum EdgeType {
    FALLTHROUGH,   // Unconditional sequential jump
    BRANCH_TRUE,   // Condition evaluated to true
    BRANCH_FALSE,  // Condition evaluated to false
    LOOP_BACK,     // Backward edge from loop body to loop header
    RETURN_EDGE    // Jump from return statement directly to EXIT block
}
```

#### 3. Construction Algorithm
For each `FunctionNode`:
1. Create `ENTRY` block and `EXIT` block.
2. Initialize current block as `ENTRY`.
3. Process statements recursively:
   - **Linear statements (VarDecl, Assign, ExprStmt)**: Append to current block.
   - **`IfNode(cond, thenStmt, elseStmt)`**:
     - Finalize condition block with outgoing `BRANCH_TRUE` to `thenBlock` and `BRANCH_FALSE` to `elseBlock` (or `joinBlock` if no else).
     - Traverse `thenStmt` and wire termination to `joinBlock`.
     - Traverse `elseStmt` and wire termination to `joinBlock`.
     - Continue construction from `joinBlock`.
   - **`WhileNode(cond, body)`**:
     - Wire current block to `headerBlock`.
     - `headerBlock` has `BRANCH_TRUE` to `bodyBlock` and `BRANCH_FALSE` to `afterBlock`.
     - At end of `bodyBlock`, add `LOOP_BACK` edge to `headerBlock`.
     - Continue construction from `afterBlock`.
   - **`ForNode(init, cond, step, body)`**:
     - Append `init` to current block.
     - Fallthrough to `conditionBlock`.
     - Branch true to `bodyBlock`, branch false to `exitBlock`.
     - At end of `bodyBlock`, fallthrough to `stepBlock`.
     - Wire `stepBlock` back to `conditionBlock` via `LOOP_BACK`.
   - **`ReturnNode`**:
     - Append `return` statement to current block.
     - Add `RETURN_EDGE` directly to `EXIT` block.
     - Terminate current block (subsequent statements in current block are unreachable).
4. Run DFS/BFS reachability from `ENTRY`. Any block not visited is tagged `isUnreachable = true`.

---

### L. Data-Flow Analysis Framework

The framework implements classical monotone iterative fixed-point data-flow equations using bit-vectors and sets over the CFG basic blocks.

#### 1. Reaching Definitions Analysis (Forward, Any-Path $\cup$)
- **Purpose**: Determines which variable assignments may reach a given program point without an intervening reassignment.
- **Domain**: Set of all definitions $d: v = e$ in the function.
- **Equations**:
  $$\text{IN}[B] = \bigcup_{P \in \text{Pred}(B)} \text{OUT}[P]$$
  $$\text{OUT}[B] = \text{GEN}[B] \cup (\text{IN}[B] \setminus \text{KILL}[B])$$
  - $\text{GEN}[B]$: Definitions generated in $B$ that are not overwritten later in $B$.
  - $\text{KILL}[B]$: All definitions anywhere in the program for variables assigned in $B$.
- **Boundary Condition**: $\text{OUT}[\text{ENTRY}] = \emptyset$, $\text{OUT}[B] = \emptyset$ initially.
- **Application**: Feeds Constant Propagation and Redundant Store elimination.

#### 2. Live Variable Analysis (Backward, Any-Path $\cup$)
- **Purpose**: Determines if a variable's current value will potentially be read in the future before being overwritten.
- **Domain**: Set of all variables in scope.
- **Equations**:
  $$\text{OUT}[B] = \bigcup_{S \in \text{Succ}(B)} \text{IN}[S]$$
  $$\text{IN}[B] = \text{GEN}[B] \cup (\text{OUT}[B] \setminus \text{KILL}[B])$$
  - $\text{GEN}[B]$ (Use): Variables read in $B$ prior to any definition in $B$.
  - $\text{KILL}[B]$ (Def): Variables defined/assigned in $B$.
- **Boundary Condition**: $\text{IN}[\text{EXIT}] = \emptyset$, $\text{IN}[B] = \emptyset$ initially.
- **Application**: Detects dead assignments (assigning to a variable not in $\text{OUT}[B]$ and not read) and completely unused variables.

#### 3. Available Expressions Analysis (Forward, All-Path $\cap$)
- **Purpose**: Determines if an expression has already been computed and its operands have not been modified since that computation.
- **Domain**: Set of all non-trivial binary/unary expressions in the function.
- **Equations**:
  $$\text{IN}[B] = \bigcap_{P \in \text{Pred}(B)} \text{OUT}[P]$$
  $$\text{OUT}[B] = \text{GEN}[B] \cup (\text{IN}[B] \setminus \text{KILL}[B])$$
  - $\text{GEN}[B]$: Expressions evaluated in $B$ whose operands are not subsequently redefined in $B$.
  - $\text{KILL}[B]$: Any expression containing an operand variable that is assigned in $B$.
- **Boundary Condition**: $\text{OUT}[\text{ENTRY}] = \emptyset$; for all other $B$, $\text{OUT}[B] = \mathcal{U}$ (Universal set of all expressions).
- **Application**: Powers Common Subexpression Elimination (CSE).

#### 4. Generic Fixed-Point Solver Implementation
```java
public class FixedPointWorklistSolver<T> {
    public void solveForward(ControlFlowGraph cfg, DataFlowFramework<T> framework) {
        Queue<BasicBlock> worklist = new LinkedList<>(cfg.getBlocks());
        while (!worklist.isEmpty()) {
            BasicBlock block = worklist.poll();
            Set<T> newIn = framework.confluence(block.getPredecessors());
            block.setIn(newIn);
            Set<T> newOut = framework.transfer(block, newIn);
            if (!newOut.equals(block.getOut())) {
                block.setOut(newOut);
                for (BasicBlock succ : block.getSuccessors()) {
                    if (!worklist.contains(succ)) worklist.add(succ);
                }
            }
        }
    }
}
```

---

### M. Static Code Analysis Rules

| Rule ID | Name | Severity | Detection Mechanism | Example Code | Expected Finding |
|---|---|---|---|---|---|
| **STA001** | Unused Variable | WARNING | AST Symbol Table query: `Symbol.getReadCount() == 0` for variable or parameter. | `int unused = 42;` | "Variable 'unused' is declared but never read." |
| **STA002** | Uninitialized Variable Read | ERROR | Reaching Definitions / Definite Assignment check: Variable read with no reaching definition from entry. | `int x; int y = x + 1;` | "Variable 'x' is read before being initialized." |
| **STA003** | Unreachable Code | WARNING | CFG Graph Reachability: Basic block has `inDegree == 0` and is not `ENTRY`, or statements placed immediately after `return`. | `return a; int b = 10;` | "Unreachable statement detected; will never execute." |
| **STA004** | Dead / Redundant Assignment | WARNING | Live Variable Analysis: Assignment $v = \text{expr}$ where $v \notin \text{LiveOut}(\text{stmt})$ and $v$ is never read before next write. | `x = 5; x = 10;` | "Assignment to 'x' is redundant because it is overwritten before being read." |
| **STA005** | Invariant / Tautological Condition | WARNING | AST Expression Evaluation: Condition in `if`/`while` statically reduces to constant literal `true` or `false`, or `x == x`. | `if (10 > 5)` or `if (a == a)` | "Condition is tautological and always evaluates to true." |

---

### N. Optimization Engine Specification

The Optimization Engine identifies classical compiler optimizations, calculates replacement expressions, and generates line-anchored code diffs:

#### 1. Constant Folding (`OPT001`)
- **Principle**: Evaluate operations on literal constants at compile time rather than runtime.
- **Rule**: $c_1 \text{ op } c_2 \longrightarrow c_3$ where $c_1, c_2$ are literals.
- **Example**:
  - *Before*: `int seconds = 60 * 60 * 24;`
  - *After*: `int seconds = 86400;`
- **Suggestion**: Replace `60 * 60 * 24` with `86400`.

#### 2. Constant Propagation (`OPT002`)
- **Principle**: Replace variable references with their known constant values if only a single constant definition reaches the point of use.
- **Rule**: Given reaching definition $d: x = c$ and no conflicting definitions reaching statement $s: y = x + 2$, substitute $x \rightarrow c$.
- **Example**:
  - *Before*: `int a = 10; int b = a + 5;`
  - *After*: `int b = 10 + 5;` (which then folds to `15`).
- **Suggestion**: Replace variable 'a' with constant '10'.

#### 3. Algebraic Simplification (`OPT003`)
- **Principle**: Exploit algebraic identities to eliminate redundant computation.
- **Rules**:
  - $x + 0 \rightarrow x$, $0 + x \rightarrow x$
  - $x - 0 \rightarrow x$
  - $x \times 1 \rightarrow x$, $1 \times x \rightarrow x$
  - $x \times 0 \rightarrow 0$, $0 \times x \rightarrow 0$
  - $x / 1 \rightarrow x$
  - $x \land \text{true} \rightarrow x$, $x \lor \text{false} \rightarrow x$
  - $x \land \text{false} \rightarrow \text{false}$, $x \lor \text{true} \rightarrow \text{true}$
  - $!(!x) \rightarrow x$
- **Example**:
  - *Before*: `int total = price * 1 + 0;`
  - *After*: `int total = price;`

#### 4. Common Subexpression Elimination (CSE) (`OPT004`)
- **Principle**: Avoid recomputing an identical expression if its value is already available from an earlier calculation.
- **Rule**: If expression $e = (a + b)$ is in $\text{AvailableIn}(s)$, and operands $a, b$ were not modified, reuse previous evaluation.
- **Example**:
  - *Before*: `int x = a * b + 2; int y = a * b * 4;`
  - *After*: `int t1 = a * b; int x = t1 + 2; int y = t1 * 4;`
- **Suggestion**: Common subexpression `a * b` detected multiple times.

#### 5. Strength Reduction (`OPT005`)
- **Principle**: Replace computationally expensive operations (multiplication, division) with cheaper equivalent operations (shifts, additions).
- **Rules**:
  - $x \times 2 \rightarrow x \ll 1$ (or $x + x$)
  - $x \times 2^k \rightarrow x \ll k$
  - $x / 2 \rightarrow x \gg 1$ (for positive integers)
- **Example**:
  - *Before*: `int area = width * 2;`
  - *After*: `int area = width << 1;` (or `width + width`)
- **Suggestion**: Replace multiplication by power of 2 with bitwise left shift `width << 1`.

#### 6. Loop-Invariant Code Motion (LICM) (`OPT006`)
- **Principle**: Hoist expressions computed inside loops whose operand values do not change across iterations outside the loop header.
- **Rule**: In loop $L$, if expression $e$ in body has operands with definitions strictly outside $L$, move $e$ to loop pre-header.
- **Example**:
  - *Before*:
    ```c
    while (i < n) {
        int factor = x * y + 3; // x and y never modified in loop
        sum = sum + i * factor;
        i = i + 1;
    }
    ```
  - *After*:
    ```c
    int factor = x * y + 3; // Hoisted before loop
    while (i < n) {
        sum = sum + i * factor;
        i = i + 1;
    }
    ```
- **Suggestion**: Expression `x * y + 3` is loop-invariant; hoist computation before loop header.

---

### O. Diagnostic Model Schema

The diagnostic engine generates a standardized, serialized `Diagnostic` object consumed by the frontend editor.

```json
{
  "ruleId": "OPT001",
  "category": "OPTIMIZATION",
  "severity": "OPTIMIZATION",
  "message": "Constant expression detected: 10 + 20",
  "explanation": "The binary expression contains only compile-time constant integer literals. Evaluating this expression at compile time eliminates redundant runtime arithmetic instructions.",
  "suggestion": "Replace '10 + 20' with '30'.",
  "line": 4,
  "column": 13,
  "length": 7,
  "sourceSnippet": "10 + 20",
  "replacementSnippet": "30",
  "safetyLevel": "SAFE"
}
```

#### Diagnostic Severity Levels
- `ERROR`: Program cannot compile (Lexical, Syntax, or Semantic type error).
- `WARNING`: Program compiles, but exhibits high-risk static defects (uninitialized reads, shadowing, unused declarations).
- `OPTIMIZATION`: Performance enhancement suggestion accompanied by a concrete code transformation.

---

### P. REST API Design

The Spring Boot backend exposes clean stateless REST endpoints under `/api`.

#### 1. Endpoints Overview

| Method | Endpoint | Description | Request Body | Response Body |
|---|---|---|---|---|
| `POST` | `/api/analyze` | Complete static analysis run | `AnalyzeRequestDto` | `AnalysisResponseDto` |
| `GET` | `/api/samples` | List of preloaded test programs | *None* | `List<SampleProgramDto>` |
| `GET` | `/api/rules` | Metadata catalog of supported rules | *None* | `List<RuleDefinitionDto>` |
| `GET` | `/api/health` | Backend status and engine version | *None* | `Map<String, String>` |

#### 2. Detailed Contract: `POST /api/analyze`

##### Request JSON:
```json
{
  "sourceCode": "int main() {\n    int a = 10 + 20;\n    int b = a * 2;\n    int unused = 99;\n    return b;\n}"
}
```

##### Response JSON:
```json
{
  "success": true,
  "metrics": {
    "totalLines": 6,
    "totalTokens": 28,
    "astDepth": 5,
    "cyclomaticComplexity": 1,
    "errorCount": 0,
    "warningCount": 1,
    "optimizationCount": 2
  },
  "tokens": [
    { "type": "INT_TYPE", "text": "int", "line": 1, "column": 0 },
    { "type": "ID", "text": "main", "line": 1, "column": 4 },
    { "type": "LPAREN", "text": "(", "line": 1, "column": 8 },
    { "type": "RPAREN", "text": ")", "line": 1, "column": 9 }
  ],
  "ast": {
    "nodeType": "ProgramNode",
    "functions": [
      {
        "nodeType": "FunctionNode",
        "name": "main",
        "returnType": "int",
        "parameters": [],
        "body": {
          "nodeType": "BlockNode",
          "statements": [
            {
              "nodeType": "VarDeclNode",
              "type": "int",
              "variable": "a",
              "initializer": {
                "nodeType": "BinaryExpr",
                "operator": "+",
                "left": { "nodeType": "LiteralNode", "value": 10 },
                "right": { "nodeType": "LiteralNode", "value": 20 }
              }
            }
          ]
        }
      }
    ]
  },
  "symbolTable": {
    "name": "Global",
    "symbols": [
      { "name": "main", "type": "int", "kind": "FUNCTION" }
    ],
    "children": [
      {
        "name": "Function_main",
        "symbols": [
          { "name": "a", "type": "int", "kind": "VARIABLE", "readCount": 1, "writeCount": 1 },
          { "name": "b", "type": "int", "kind": "VARIABLE", "readCount": 1, "writeCount": 1 },
          { "name": "unused", "type": "int", "kind": "VARIABLE", "readCount": 0, "writeCount": 1 }
        ]
      }
    ]
  },
  "controlFlowGraphs": [
    {
      "functionName": "main",
      "dotRepresentation": "digraph CFG_main { ... }",
      "blocks": [
        {
          "id": "ENTRY",
          "statements": [],
          "successors": ["B0"]
        },
        {
          "id": "B0",
          "statements": ["int a = 10 + 20;", "int b = a * 2;", "int unused = 99;", "return b;"],
          "successors": ["EXIT"]
        },
        {
          "id": "EXIT",
          "statements": [],
          "successors": []
        }
      ]
    }
  ],
  "diagnostics": [
    {
      "ruleId": "OPT001",
      "category": "OPTIMIZATION",
      "severity": "OPTIMIZATION",
      "message": "Constant expression detected: 10 + 20",
      "explanation": "Can be computed at compile time.",
      "suggestion": "Replace with '30'",
      "line": 2,
      "column": 12,
      "length": 7,
      "sourceSnippet": "10 + 20",
      "replacementSnippet": "30",
      "safetyLevel": "SAFE"
    },
    {
      "ruleId": "OPT005",
      "category": "OPTIMIZATION",
      "severity": "OPTIMIZATION",
      "message": "Strength reduction: a * 2",
      "explanation": "Multiplication by 2 can be replaced with bitwise shift.",
      "suggestion": "Replace with 'a << 1'",
      "line": 3,
      "column": 12,
      "length": 5,
      "sourceSnippet": "a * 2",
      "replacementSnippet": "a << 1",
      "safetyLevel": "SAFE"
    },
    {
      "ruleId": "STA001",
      "category": "STATIC_ANALYSIS",
      "severity": "WARNING",
      "message": "Unused variable 'unused'",
      "explanation": "Variable is declared but never referenced.",
      "suggestion": "Remove unused declaration.",
      "line": 4,
      "column": 4,
      "length": 16,
      "sourceSnippet": "int unused = 99;",
      "replacementSnippet": "",
      "safetyLevel": "SAFE"
    }
  ]
}
```

---

### Q. Frontend Architecture & Design

The frontend is built using **React + TypeScript + Vite**, organized into modular, decoupled UI components with custom modern styling (dark mode, glassmorphism, accent badges).

#### 1. Component Hierarchy
```
App
├── Header (Logo, Sample Selector dropdown, Analyze button, Status indicator)
├── MetricCards (Total lines, Syntax errors, Warnings, Optimizations count)
├── MainLayout (Split view container)
│   ├── LeftPane (Editor Section)
│   │   ├── MonacoEditor (Source code editor with inline error squiggles)
│   │   └── DiffModal (Side-by-side Monaco Diff preview when suggestion clicked)
│   └── RightPane (Analysis Viewers with Tabs)
│       ├── TabNav (Findings | CFG Visualizer | AST Hierarchy | Symbol Table | Report)
│       ├── Tab: Findings
│       │   ├── FilterToolbar (All, Errors, Warnings, Optimizations)
│       │   └── DiagnosticList (Cards with line, message, explanation, Apply button)
│       ├── Tab: CFG Visualizer
│       │   ├── FunctionSelector (Dropdown to switch between functions)
│       │   └── GraphvizView (Renders SVG using @hpcc-js/wasm with zoom/pan)
│       ├── Tab: AST Explorer
│       │   └── CollapsibleTreeView (Recursive node viewer with coordinate details)
│       ├── Tab: Symbol Table
│       │   └── ScopeTable (Scope hierarchy, type, read/write counters)
│       └── Tab: Report Summary
│           ├── ComplexityStats (Cyclomatic complexity, AST depth)
│           └── ExportButtons (Export JSON, Print PDF summary)
└── Footer (Version info, compiler phase timings in milliseconds)
```

#### 2. Monaco Editor Integration
- Uses `@monaco-editor/react`.
- Custom language registration for `minilang` with keywords, types, and operator syntax highlighting.
- Markers synced directly from backend `diagnostics`:
  ```typescript
  const markers = diagnostics.map(d => ({
    startLineNumber: d.line,
    startColumn: d.column + 1,
    endLineNumber: d.line,
    endColumn: d.column + 1 + d.length,
    message: `${d.ruleId}: ${d.message}\n💡 ${d.suggestion}`,
    severity: d.severity === 'ERROR' ? MarkerSeverity.Error 
            : d.severity === 'WARNING' ? MarkerSeverity.Warning 
            : MarkerSeverity.Info
  }));
  monaco.editor.setModelMarkers(editor.getModel(), 'codeinsight', markers);
  ```

#### 3. Client-Side Graphviz Rendering
- Uses `@hpcc-js/wasm` to render DOT strings directly to an interactive SVG in the browser without any native backend graphviz installation.

---

### R. Complete Project Directory Structure

```
d:/CC_Mini_Project/
├── pom.xml                                   # Root Maven project descriptor
├── README.md                                 # Complete documentation & viva guide
├── SPECIFICATION.md                          # Master architectural specification
│
├── backend/                                  # Spring Boot Application
│   ├── pom.xml
│   └── src/
│       ├── main/
│       │   ├── antlr4/
│       │   │   └── com/codeinsight/compiler/grammar/
│       │   │       └── MiniLang.g4           # Combined ANTLR4 Lexer & Parser
│       │   ├── java/com/codeinsight/
│       │   │   ├── CodeInsightApplication.java
│       │   │   ├── compiler/
│       │   │   │   ├── ast/
│       │   │   │   │   ├── builder/
│       │   │   │   │   │   └── ASTBuilderVisitor.java
│       │   │   │   │   ├── node/
│       │   │   │   │   │   ├── ASTNode.java
│       │   │   │   │   │   ├── ProgramNode.java
│       │   │   │   │   │   ├── FunctionNode.java
│       │   │   │   │   │   ├── StatementNode.java
│       │   │   │   │   │   ├── VarDeclNode.java
│       │   │   │   │   │   ├── AssignNode.java
│       │   │   │   │   │   ├── IfNode.java
│       │   │   │   │   │   ├── WhileNode.java
│       │   │   │   │   │   ├── ForNode.java
│       │   │   │   │   │   ├── ReturnNode.java
│       │   │   │   │   │   ├── ExpressionNode.java
│       │   │   │   │   │   ├── BinaryExpr.java
│       │   │   │   │   │   ├── UnaryExpr.java
│       │   │   │   │   │   ├── LiteralNode.java
│       │   │   │   │   │   ├── VarRefNode.java
│       │   │   │   │   │   ├── CallNode.java
│       │   │   │   │   │   └── SourcePosition.java
│       │   │   │   │   └── visitor/
│       │   │   │   │       ├── ASTVisitor.java
│       │   │   │   │       └── BaseASTVisitor.java
│       │   │   │   ├── symbol/
│       │   │   │   │   ├── Type.java
│       │   │   │   │   ├── Symbol.java
│       │   │   │   │   ├── SymbolKind.java
│       │   │   │   │   ├── Scope.java
│       │   │   │   │   ├── ScopeType.java
│       │   │   │   │   └── SymbolTable.java
│       │   │   │   ├── semantic/
│       │   │   │   │   ├── TypeChecker.java
│       │   │   │   │   ├── ScopeResolverVisitor.java
│       │   │   │   │   └── SemanticAnalyzerVisitor.java
│       │   │   │   ├── cfg/
│       │   │   │   │   ├── BasicBlock.java
│       │   │   │   │   ├── CFGEdge.java
│       │   │   │   │   ├── EdgeType.java
│       │   │   │   │   ├── ControlFlowGraph.java
│       │   │   │   │   ├── CFGBuilder.java
│       │   │   │   │   └── DOTExporter.java
│       │   │   │   ├── dataflow/
│       │   │   │   │   ├── DataFlowFramework.java
│       │   │   │   │   ├── FixedPointSolver.java
│       │   │   │   │   ├── ReachingDefinitions.java
│       │   │   │   │   ├── LiveVariableAnalysis.java
│       │   │   │   │   └── AvailableExpressions.java
│       │   │   │   ├── analyzer/
│       │   │   │   │   ├── StaticAnalysisRule.java
│       │   │   │   │   ├── StaticAnalysisEngine.java
│       │   │   │   │   ├── UnusedVariableDetector.java
│       │   │   │   │   ├── UninitializedReadDetector.java
│       │   │   │   │   ├── UnreachableCodeDetector.java
│       │   │   │   │   └── RedundantAssignDetector.java
│       │   │   │   ├── optimizer/
│       │   │   │   │   ├── OptimizerRule.java
│       │   │   │   │   ├── OptimizationEngine.java
│       │   │   │   │   ├── ConstantFolder.java
│       │   │   │   │   ├── ConstantPropagator.java
│       │   │   │   │   ├── AlgebraicSimplifier.java
│       │   │   │   │   ├── CommonSubexpressionEliminator.java
│       │   │   │   │   ├── StrengthReducer.java
│       │   │   │   │   └── LoopInvariantMotion.java
│       │   │   │   ├── diagnostic/
│       │   │   │   │   ├── Diagnostic.java
│       │   │   │   │   ├── Severity.java
│       │   │   │   │   ├── Category.java
│       │   │   │   │   └── DiagnosticCollector.java
│       │   │   │   └── service/
│       │   │   │       ├── AnalysisService.java
│       │   │   │       └── SampleCodeService.java
│       │   │   └── web/
│       │   │       ├── controller/
│       │   │       │   └── AnalysisController.java
│       │   │       └── dto/
│       │   │           ├── AnalyzeRequestDto.java
│       │   │           ├── AnalysisResponseDto.java
│       │   │           └── DiagnosticDto.java
│       │   └── resources/
│       │       └── application.yml
│       └── test/java/com/codeinsight/
│           ├── LexerParserTest.java
│           ├── ASTBuilderTest.java
│           ├── SymbolTableTest.java
│           ├── SemanticAnalyzerTest.java
│           ├── CFGBuilderTest.java
│           ├── DataFlowTest.java
│           ├── StaticAnalysisTest.java
│           ├── OptimizerTest.java
│           └── IntegrationAnalysisTest.java
│
├── frontend/                                 # React + TypeScript Web App
│   ├── package.json
│   ├── tsconfig.json
│   ├── vite.config.ts
│   ├── index.html
│   └── src/
│       ├── main.tsx
│       ├── App.tsx
│       ├── App.css
│       ├── index.css
│       ├── api/
│       │   └── client.ts                     # Axios / Fetch client to Spring Boot
│       ├── types/
│       │   └── index.ts                      # Full TypeScript interfaces for API DTOs
│       ├── components/
│       │   ├── Header.tsx                    # Top navigation & Sample selector
│       │   ├── MetricBar.tsx                 # Metric badges
│       │   ├── CodeEditor.tsx                # Monaco Editor with markers
│       │   ├── DiffViewer.tsx                # Suggestion patch comparison
│       │   ├── FindingsPanel.tsx             # Diagnostics list with filters
│       │   ├── CFGViewer.tsx                 # Graphviz SVG viewer with pan/zoom
│       │   ├── ASTTreeViewer.tsx             # Collapsible AST hierarchy
│       │   ├── SymbolTableView.tsx           # Scoped Symbol Table inspector
│       │   └── ReportSummary.tsx             # Markdown / PDF summary preview
│       └── samples/
│           └── defaultSamples.ts             # Embedded MiniLang showcase demos
│
└── samples/                                  # Pure MiniLang test scripts
    ├── 01_valid_showcase.minilang
    ├── 02_constant_folding.minilang
    ├── 03_dead_unreachable_code.minilang
    ├── 04_semantic_type_errors.minilang
    ├── 05_common_subexpressions.minilang
    ├── 06_strength_reduction.minilang
    └── 07_loop_invariants.minilang
```

---

### S. Testing Strategy & Test Suites

#### 1. Unit Test Matrix
- **`LexerParserTest`**:
  - Test valid tokens (keywords, identifiers, literals, operators).
  - Verify syntax error listener captures unmatched braces, missing semicolons with exact line/column.
- **`ASTBuilderTest`**:
  - Assert that parse trees convert into exact nested AST structures without loss of operators or tokens.
- **`SymbolTableTest`**:
  - Verify variable resolution in nested blocks.
  - Verify shadowed variable detection in child scopes.
- **`SemanticAnalyzerTest`**:
  - Test type mismatch rejection (`int a = true;`).
  - Test undeclared variable read error (`x = 5;`).
  - Test return path completeness in conditional branches.
- **`CFGBuilderTest`**:
  - Verify basic block partitioning for `if-else` (4 blocks: Entry, Cond, Then, Else, Join).
  - Verify back-edge detection for `while` loops.
- **`DataFlowTest`**:
  - Verify Reaching Definitions set computation on simple sequential programs.
  - Verify Live Variable sets across branching blocks.
- **`OptimizerTest`**:
  - Test `10 + 20 * 2` folds to `50`.
  - Test `x * 1` simplifies to `x`.
  - Test redundant assignment `x = 1; x = 2;` flagged.
- **`IntegrationAnalysisTest`**:
  - End-to-end payload test: string in $\rightarrow$ full JSON out with diagnostics and CFG.

#### 2. Canonical MiniLang Test Program (Optimizer Showcase)

File: `samples/01_valid_showcase.minilang`
```c
int compute(int x, int y) {
    // 1. Constant Folding opportunity
    int base = 10 + 20 * 2;
    
    // 2. Algebraic Simplification opportunity
    int adjusted = x * 1 + 0;
    
    // 3. Redundant / Dead assignment
    int temp = 999;
    temp = base + adjusted;
    
    // 4. Strength Reduction opportunity
    int doubled = temp * 2;
    
    // 5. Common Subexpression Elimination opportunity
    int part1 = (x + y) * 3;
    int part2 = (x + y) + 7;
    
    // 6. Unused variable
    int unusedVar = 100;
    
    // 7. Loop invariant code motion opportunity
    int sum = 0;
    int i = 0;
    while (i < 10) {
        int invariantVal = x * 5 + 4; // Should be hoisted
        sum = sum + invariantVal + i;
        i = i + 1;
    }
    
    // 8. Tautological condition
    if (10 > 5) {
        return sum + doubled + part1 + part2;
    } else {
        // 9. Unreachable code
        int unreachableVal = 404;
        return 0;
    }
}
```

---

### T. Implementation Roadmap (Phases & Milestones)

| Phase | Milestone Name | Key Deliverables | Completion Criteria |
|---|---|---|---|
| **Phase 1** | **Project Setup & Scaffolding** | Maven root pom, Spring Boot backend structure, Vite frontend template | Both backend (`mvn compile`) and frontend (`npm run build`) build cleanly |
| **Phase 2** | **Grammar & Lexer/Parser** | `MiniLang.g4`, generated ANTLR classes, custom error listener | Lexer and Parser parse all sample `.minilang` files without unhandled exceptions |
| **Phase 3** | **AST Hierarchy & Builder** | All `ASTNode` subclasses, `SourcePosition`, `ASTBuilderVisitor` | Unit tests verify parse tree maps 100% to AST nodes with accurate coordinates |
| **Phase 4** | **Symbol Table & Scope Resolver** | `Scope`, `Symbol`, `Type`, `ScopeResolverVisitor` | Resolves variables across nested blocks; detects shadowing and duplicate declarations |
| **Phase 5** | **Semantic Analysis Engine** | `SemanticAnalyzerVisitor`, `TypeChecker` | Detects type mismatches, undeclared variables, missing return statements |
| **Phase 6** | **CFG Engine & Visualizer** | `BasicBlock`, `CFGEdge`, `CFGBuilder`, `DOTExporter` | Generates valid Graphviz DOT strings for all conditional and loop constructs |
| **Phase 7** | **Data-Flow Solver** | `ReachingDefinitions`, `LiveVariableAnalysis`, `AvailableExpressions` | IN/OUT bit-vector sets converge within max 10 iterations on test CFGs |
| **Phase 8** | **Static Analysis Rules** | Detectors for unused vars, uninitialized vars, unreachable code, dead stores | All `STA001` - `STA005` rules pass respective unit tests |
| **Phase 9** | **Optimization Engine** | Transformers for Constant Folding, Propagation, Simplification, CSE, Strength Reduction | All `OPT001` - `OPT006` rules generate valid replacement code snippets |
| **Phase 10** | **Backend REST API** | `AnalysisController`, `AnalysisService`, DTO contracts | `POST /api/analyze` returns comprehensive JSON payload in < 150ms |
| **Phase 11** | **React Frontend Core** | Monaco Editor setup, language tokenizer, marker sync | Editor displays MiniLang code with squiggles for reported diagnostics |
| **Phase 12** | **Interactive Visualizations** | Graphviz CFG renderer (`@hpcc-js/wasm`), AST Tree viewer, Symbol Table view | Visual CFG graphs render cleanly with interactive node inspects |
| **Phase 13** | **Suggestions & Diff Viewer** | Side-by-side diff modal, "Apply Suggestion" Monaco patcher | Clicking suggestion updates editor source code in real time |
| **Phase 14** | **Packaging & Demo Polish** | Preloaded sample switcher, printable report export, documentation | End-to-end manual walkthrough succeeds with zero console errors |

---

### U. Step-by-Step Viva Demo Script

When demonstrating the project to an academic examiner or faculty evaluator, execute this precise sequence:

1. **Launch the Application**:
   - Start backend: `mvn spring-boot:run` on port `8080`.
   - Start frontend: `npm run dev` on port `5173`.
   - Open browser at `http://localhost:5173`.
2. **Load the Showcase Program**:
   - Click the "Select Sample" dropdown in the header and choose **"Comprehensive Compiler Showcase"**.
   - Point out the MiniLang syntax in Monaco Editor (notice types, control flow, arithmetic).
3. **Trigger Static Analysis**:
   - Click the vibrant **"Analyze Code"** button.
   - Point to the **Metrics Bar**: Execution time (~45ms), Total Tokens (128), AST Depth (7), Warnings (2), Optimizations (6).
4. **Demonstrate Compiler Frontend Concepts**:
   - Click the **AST Explorer** tab. Expand `FunctionNode [compute] -> BlockNode -> VarDeclNode [base]`.
   - Explain: *"This demonstrates our custom Abstract Syntax Tree constructed via ANTLR 4 visitor, retaining exact source tokens and file coordinates."*
   - Click the **Symbol Table** tab. Highlight scopes: `Global` vs `Function_compute`.
   - Show how variable `unusedVar` has `writeCount = 1, readCount = 0`.
5. **Demonstrate Control Flow Analysis**:
   - Click the **CFG Visualizer** tab.
   - Show the generated Basic Blocks:
     - Header block leading into `BRANCH_TRUE` (green edge) and `BRANCH_FALSE` (red edge).
     - Loop back-edge (purple) from `while` loop body to condition block.
     - **Unreachable Block Highlighting**: Point to the amber block inside the `else` branch of `if (10 > 5)` tagged as unreachable dead code.
6. **Demonstrate Classical Optimizations & Diffs**:
   - Switch to the **Findings** tab.
   - Click **OPT001 (Constant Folding)**: Show finding `10 + 20 * 2` $\rightarrow$ `50`. Click "Show Diff" to reveal side-by-side Monaco diff comparison.
   - Click **OPT005 (Strength Reduction)**: Show `temp * 2` $\rightarrow$ `temp << 1`.
   - Click **OPT003 (Algebraic Simplification)**: Show `x * 1 + 0` $\rightarrow$ `x`.
   - Click **OPT006 (Loop-Invariant Code Motion)**: Show hoisting suggestion for `x * 5 + 4`.
7. **Demonstrate Interactive Code Patching**:
   - Click the **"Apply Suggestion"** button on the Constant Folding finding.
   - Show how Monaco Editor instantly updates `10 + 20 * 2` to `50` in the source text.
   - Re-analyze to prove that the diagnostic clears dynamically.
8. **Demonstrate Semantic Error Rejection**:
   - Type `int bad = "string";` or `bool flag = 10 + 5;` or `foo = 99;` into the editor.
   - Click Analyze. Show immediate red squiggle and **SEM001 / SEM004** error cards with exact line/column indicators.

---

### V. Syllabus & Viva Mapping

| Academic Subject Topic | Implementation in CodeInsight | Typical Examiner Viva Question | Model Answer |
|---|---|---|---|
| **Lexical Analysis (Scanner)** | `MiniLang.g4` (Lexer tokens, Regex, DFA simulation) | *"How does the lexer distinguish between identifiers and keywords?"* | In ANTLR4 / Lexer rules, keywords are declared before the general `ID` pattern. When an identical prefix matches, the first rule has priority, classifying it as a keyword rather than an identifier. |
| **Syntax Analysis (Parser)** | LL(*) recursive-descent parser generated by ANTLR4 | *"How does your grammar eliminate left-recursion in binary expressions?"* | MiniLang uses prioritized expression rules in ANTLR 4 which internally handles direct left-recursion by generating precedence-climbing parsing logic based on rule order. |
| **Abstract Syntax Tree (AST)** | Decoupled Node hierarchy (`ASTNode`, `BinaryExpr`, etc.) | *"Why do you build an AST instead of operating directly on the ParseTree?"* | ParseTrees contain concrete grammatical tokens (parentheses, commas, semicolons) that carry no semantic meaning. The AST is a condensed, abstract representation containing only operators, operands, and hierarchy, making transformations straightforward. |
| **Symbol Table** | Scoped hierarchy (`Scope`, `Symbol`, parent pointers) | *"How do you handle variable shadowing across blocks?"* | Each block creates a child `Scope` with a reference to its parent. Lookup traverses up the tree, but local insertion checks only the immediate scope. If an identifier exists in an ancestor scope, a shadowing warning is emitted. |
| **Semantic Analysis** | `TypeChecker`, assignment compatibility, return path check | *"What is the difference between a syntax error and a semantic error?"* | `int x = 5 + * 2;` violates grammatical production rules (Syntax Error). `int x = true + 5;` is grammatically valid according to the CFG, but violates the type-compatibility rules of the language (Semantic Error). |
| **Basic Block Partitioning** | `CFGBuilder` leader algorithm | *"What defines the leader of a Basic Block?"* | Three conditions: 1) The first statement of the function, 2) The target of any conditional or unconditional jump, 3) Any statement immediately following a jump or return. |
| **Data-Flow Analysis** | Fixed-point worklist iteration ($IN, OUT, GEN, KILL$) | *"Why is Reaching Definitions a union ($\cup$) confluence while Available Expressions is an intersection ($\cap$)?"* | Reaching Definitions checks if a definition *may* reach along *any* incoming execution path ($\cup$). Available Expressions checks if an expression *must* be valid along *all* incoming paths ($\cap$) to be safely reused. |
| **Constant Folding vs Propagation** | `ConstantFolder` vs `ConstantPropagator` | *"Explain the difference between Constant Folding and Constant Propagation."* | Constant Folding evaluates operations between compile-time constants (e.g. `2 + 3` $\rightarrow$ `5`). Constant Propagation replaces variables with their known constant values based on reaching definitions (e.g. `x = 5; y = x;` $\rightarrow$ `y = 5;`). |

---

### W. Known Limitations & Future Enhancements

#### Current Deliberate Scope Limitations
1. **Pointers and Dynamic Memory**: No raw pointers or `malloc`/`free` to keep static analysis deterministic and avoid pointer alias complexity in a mini-project.
2. **Arrays and Structs**: MiniLang currently supports scalar primitives (`int`, `float`, `char`, `bool`). Array bounds analysis is an ideal future extension.
3. **Inter-procedural Analysis**: Data-flow analysis is intra-procedural (per-function). Function calls are treated conservatively as potential variable modifications unless parameters are read-only.

#### Future Enhancements
- Inter-procedural call-graph analysis.
- Live WebAssembly compilation to execute and benchmark unoptimized vs optimized code in real time.
- SSA (Static Single Assignment) form intermediate representation.

---

### X. Exact Acceptance Criteria for Finished System

1. **Compilation**:
   - `mvn clean compile test` executes with zero compilation errors and all JUnit tests pass.
   - `npm run build` in `frontend/` builds production bundle without TypeScript errors.
2. **Static Execution Safety**:
   - No runtime compilation or `eval()` of user-submitted code; the backend performs 100% static analysis in memory.
3. **Diagnostic Correctness**:
   - Successfully detects all 5 static quality rules (`STA001`-`STA005`).
   - Successfully identifies all 6 classical compiler optimizations (`OPT001`-`OPT006`).
   - Accurately reports syntax and semantic errors with exact 1-indexed line and column numbers.
4. **CFG Correctness**:
   - Emits valid Graphviz DOT notation that parses and renders in the frontend without syntax errors.
   - Unreachable blocks are distinctly styled and labeled.
5. **Interactive UI**:
   - Monaco editor highlights code lines matching returned diagnostics.
   - Clicking an optimization suggestion displays a side-by-side diff comparing original code against suggested replacement.
   - Clicking "Apply Suggestion" updates the source buffer in Monaco Editor.
   - Sample programs can be loaded in one click from the UI header.
