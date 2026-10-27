# CodeInsight: Intelligent Static Code Analyzer with Optimization Suggestions

[![Java](https://img.shields.io/badge/Java-17%20LTS-ED8B00?style=for-the-badge&logo=openjdk&logoColor=white)](https://openjdk.org/)
[![Spring Boot](https://img.shields.io/badge/Spring_Boot-3.2.4-6DB33F?style=for-the-badge&logo=springboot&logoColor=white)](https://spring.io/projects/spring-boot)
[![ANTLR4](https://img.shields.io/badge/ANTLR-4.13.1-C9252D?style=for-the-badge&logo=antlr&logoColor=white)](https://www.antlr.org/)
[![React](https://img.shields.io/badge/React-18-61DAFB?style=for-the-badge&logo=react&logoColor=black)](https://react.dev/)
[![TypeScript](https://img.shields.io/badge/TypeScript-5.2-3178C6?style=for-the-badge&logo=typescript&logoColor=white)](https://www.typescriptlang.org/)
[![Vite](https://img.shields.io/badge/Vite-5.0-646CFF?style=for-the-badge&logo=vite&logoColor=white)](https://vitejs.dev/)

**CodeInsight** is an automated static source-code analyzer and classical compiler optimization engine engineered specifically for an undergraduate B.Tech Computer Engineering Capstone / Mini-Project in **Compiler Construction**.

Unlike naive linters that rely on regular expressions or simple heuristics, CodeInsight is a **true compiler frontend and static analysis engine**. It parses programs written in **MiniLang** (a deterministic, typed, C-like language subset) through an end-to-end multi-pass compilation pipeline: lexical scanning, concrete parsing, AST construction, hierarchical symbol resolution, semantic analysis, basic block Control Flow Graph (CFG) generation, iterative bit-vector Data-Flow Analysis (DFA), and classical compiler optimizations.

---

## 1. Compiler Construction Concepts Demonstrated

| Compiler Phase | Implementation in CodeInsight | Description |
|---|---|---|
| **Lexical Analysis (Scanning)** | ANTLR 4 `MiniLangLexer` | DFA-based tokenization; token streams, keyword priority, and lexical error recovery. |
| **Syntax Analysis (Parsing)** | ANTLR 4 `MiniLangParser` | ALL(*) recursive-descent parsing with operator precedence climbing and syntax diagnostics. |
| **Intermediate Representation (AST)** | Custom OOP AST Hierarchy | Decoupled AST (`ProgramNode`, `BinaryExpr`, etc.) retaining precise line/column coordinates. |
| **Symbol Table & Scoping** | Hierarchical `Scope` Tree | Scoped symbol tables (Global, Function, Block) tracking variable types, read/write counts, and shadowing. |
| **Semantic Analysis** | `TypeChecker` & Visitors | Enforces type system rules, function signatures, condition types, and return path completeness. |
| **Control Flow Analysis (CFG)** | `CFGBuilder` & `BasicBlock` | Basic Block leader election, edge classification (True, False, Loop-Back, Return), reachability analysis. |
| **Data-Flow Analysis (DFA)** | Iterative Fixed-Point Worklist | Classical bit-vector transfer equations for Reaching Definitions, Live Variables, and Available Expressions. |
| **Compiler Optimizations** | `OptimizationEngine` | Classical transformations: Constant Folding, Constant Propagation, Algebraic Simplification, CSE, Strength Reduction, and LICM. |

---

## 2. High-Level Architecture & Pipeline Flow

```
MiniLang Source Code
       │
       ▼
┌─────────────────────────────┐
│ 1. Lexical Analysis (ANTLR4)│ ──► Tokens (Keywords, IDs, Literals, Operators)
└──────────────┬──────────────┘
               ▼
┌─────────────────────────────┐
│ 2. Syntax Analysis (ALL*)   │ ──► Parse Tree + Syntax Diagnostics (SYN001)
└──────────────┬──────────────┘
               ▼
┌─────────────────────────────┐
│ 3. Custom AST Construction  │ ──► Decoupled AST with SourcePosition Coordinates
└──────────────┬──────────────┘
               ▼
┌─────────────────────────────┐
│ 4. Symbol Table Resolution  │ ──► Scopes (Global, Function, Block), Shadowing (SEM002)
└──────────────┬──────────────┘
               ▼
┌─────────────────────────────┐
│ 5. Semantic Analysis        │ ──► Type checking, Return paths, Arity matching (SEMxxx)
└──────────────┬──────────────┘
               ▼
┌─────────────────────────────┐
│ 6. CFG & Basic Blocks       │ ──► Leader detection, Graph reachability, Graphviz DOT
└──────────────┬──────────────┘
               ▼
┌─────────────────────────────┐
│ 7. Data-Flow Solvers        │ ──► Reaching Definitions (∪), Live Variables (∪), Avail Exprs (∩)
└──────────────┬──────────────┘
               ▼
┌─────────────────────────────┐
│ 8. Static Analysis Rules    │ ──► Unused vars, Dead stores, Unreachable blocks (STAxxx)
└──────────────┬──────────────┘
               ▼
┌─────────────────────────────┐
│ 9. Optimization Engine      │ ──► Constant Folding, CSE, Strength Reduction (OPTxxx)
└──────────────┬──────────────┘
               ▼
┌─────────────────────────────┐
│ 10. REST API & Web UI       │ ──► Spring Boot (8080) + React/Vite + Monaco + Graphviz SVG
└─────────────────────────────┘
```

---

## 3. MiniLang Language Specification

MiniLang is a statically typed imperative C-like programming language supporting:

- **Primitive Types**: `int`, `float`, `char`, `bool`, `void` (function returns).
- **Literals**: Integers (`42`), Floats (`3.14`), Characters (`'c'`), Booleans (`true`, `false`).
- **Operators**:
  - Arithmetic: `+`, `-`, `*`, `/`, `%`
  - Relational: `<`, `<=`, `>`, `>=`, `==`, `!=`
  - Logical: `&&`, `||`, `!`
  - Assignment: `=`
  - Target Shifts: `<<`, `>>`
- **Control Flow**:
  - `if (cond) { ... } else { ... }`
  - `while (cond) { ... }`
  - `for (init; cond; step) { ... }`
  - `return expr;` / `return;`
  - Nested block scoping: `{ ... }`
- **Functions**: Typed return, named parameters, local variables, nested scopes.

---

## 4. Diagnostic & Optimization Rules

### Static Quality Checks
| Rule ID | Category | Severity | Description |
|---|---|---|---|
| **STA001** | Static Analysis | WARNING | **Unused Variable**: Variable declared but never read along any execution path. |
| **STA002** | Static Analysis | ERROR | **Uninitialized Variable Read**: Variable read without prior reaching assignment. |
| **STA003** | Static Analysis | WARNING | **Unreachable Code**: CFG Basic block cannot be reached from function ENTRY. |
| **STA004** | Static Analysis | WARNING | **Redundant / Dead Store**: Variable assigned a value overwritten before being read. |
| **STA005** | Static Analysis | WARNING | **Tautological Condition**: Invariant boolean comparison (e.g. `10 > 5`, `a == a`). |

### Classical Compiler Optimizations
| Rule ID | Name | Safety | Transformation Example |
|---|---|---|---|
| **OPT001** | Constant Folding | SAFE | `60 * 60 * 24` $\longrightarrow$ `86400` |
| **OPT002** | Constant Propagation | SAFE | `int a = 10; return a + 5;` $\longrightarrow$ `return 10 + 5;` |
| **OPT003** | Algebraic Simplification | SAFE | `x * 1 + 0` $\longrightarrow$ `x`; `!(!x)` $\longrightarrow$ `x` |
| **OPT004** | Common Subexpression (CSE) | CONDITIONALLY_SAFE | `(a + b) * 3; ... (a + b) + 7;` $\longrightarrow$ reuse temporary variable |
| **OPT005** | Strength Reduction | SAFE | `x * 2` $\longrightarrow$ `x << 1`; `x / 4` $\longrightarrow$ `x >> 2` |
| **OPT006** | Loop-Invariant Code Motion | SAFE | Hoists expressions whose operands are never modified inside loop body |

---

## 5. Project Directory Structure

```
d:/CC_Mini_Project/
├── pom.xml                                   # Root Maven Parent POM
├── SPECIFICATION.md                          # Master Architectural Blueprint
├── README.md                                 # Full Project Documentation
│
├── backend/                                  # Spring Boot Application
│   ├── pom.xml
│   └── src/
│       ├── main/
│       │   ├── antlr4/com/codeinsight/compiler/grammar/
│       │   │   └── MiniLang.g4               # ANTLR4 Lexer & Parser grammar
│       │   ├── java/com/codeinsight/
│       │   │   ├── CodeInsightApplication.java
│       │   │   ├── compiler/
│       │   │   │   ├── ast/node/             # Decoupled custom AST nodes
│       │   │   │   ├── ast/builder/          # ASTBuilderVisitor.java
│       │   │   │   ├── ast/visitor/          # ASTVisitor.java & BaseASTVisitor.java
│       │   │   │   ├── symbol/               # Scope, Symbol, SymbolTable, Type
│       │   │   │   ├── semantic/             # TypeChecker, SemanticAnalyzerVisitor
│       │   │   │   ├── cfg/                  # BasicBlock, CFGEdge, CFGBuilder, DOTExporter
│       │   │   │   ├── dataflow/             # Solver, ReachingDefs, LiveVars, AvailExprs
│       │   │   │   ├── analyzer/             # STA001 - STA005 rule detectors
│       │   │   │   ├── optimizer/            # OPT001 - OPT006 optimization passes
│       │   │   │   ├── diagnostic/           # Unified Diagnostic models
│       │   │   │   └── service/              # AnalysisService.java & SampleCodeService.java
│       │   │   └── web/
│       │   │       ├── controller/           # AnalysisController.java
│       │   │       └── dto/                  # Request / Response JSON DTOs
│       │   └── resources/
│       │       └── application.yml
│       └── test/java/com/codeinsight/        # 9 Unit and Integration Test Suites
│
├── frontend/                                 # React + TypeScript + Vite Web App
│   ├── package.json
│   ├── tsconfig.json
│   ├── vite.config.ts
│   ├── index.html
│   └── src/
│       ├── main.tsx
│       ├── App.tsx
│       ├── App.css
│       ├── index.css
│       ├── api/client.ts                     # API client to Spring Boot
│       ├── types/index.ts                    # TypeScript contracts
│       ├── components/
│       │   ├── FindingsPanel.tsx             # Diagnostics filter & diff triggers
│       │   ├── CFGViewer.tsx                 # Interactive Graphviz WASM SVG renderer
│       │   ├── ASTTreeViewer.tsx             # Collapsible custom AST tree
│       │   ├── SymbolTableView.tsx           # Scoped Symbol Table inspector
│       │   ├── ReportSummary.tsx             # Academic printable report generator
│       │   └── DiffModal.tsx                 # Optimization diff preview modal
│       └── samples/defaultSamples.ts         # Embedded showcase programs
│
└── samples/                                  # Pure MiniLang test programs
    ├── 01_valid_showcase.minilang
    ├── 02_constant_folding.minilang
    ├── 03_dead_unreachable_code.minilang
    ├── 04_semantic_type_errors.minilang
    ├── 05_common_subexpressions.minilang
    ├── 06_strength_reduction.minilang
    └── 07_loop_invariants.minilang
```

---

## 6. How to Build & Run

### Prerequisites
- **Java 17+ LTS** (`java -version`)
- **Maven 3.9+** (`mvn -version`)
- **Node.js 18+ & npm** (`node -v`, `npm -v`)

---

### Step 1: Build & Run the Backend
```bash
# Navigate to backend directory
cd backend

# Compile ANTLR grammar and Java source
mvn clean compile

# Run all 14 automated unit & integration test suites
mvn test

# Start the Spring Boot REST API server
mvn spring-boot:run
```
*The backend starts at `http://localhost:8080`.*

---

### Step 2: Build & Run the Frontend
```bash
# Open a new terminal and navigate to frontend directory
cd frontend

# Install npm dependencies
npm install

# Start the local Vite development server
npm run dev
```
*The frontend dashboard opens at `http://localhost:5173`.*

---

### Step 3: Production Build (Optional)
```bash
# In frontend directory
npm run build
```

---

## 7. REST API Documentation

### `POST /api/analyze`
Executes complete static analysis pipeline on the provided source code.

**Request Body**:
```json
{
  "sourceCode": "int compute(int x) {\n    int a = 10 + 20 * 2;\n    return a * 2;\n}"
}
```

**Response Body**:
```json
{
  "success": true,
  "metrics": {
    "totalLines": 4,
    "totalTokens": 20,
    "astDepth": 5,
    "cyclomaticComplexity": 3,
    "errorCount": 0,
    "warningCount": 0,
    "optimizationCount": 2
  },
  "tokens": [...],
  "ast": { "nodeType": "ProgramNode", ... },
  "symbolTable": { "name": "Global", "children": [...] },
  "controlFlowGraphs": [
    {
      "functionName": "compute",
      "dotRepresentation": "digraph CFG_compute { ... }",
      "blocks": [...]
    }
  ],
  "diagnostics": [
    {
      "ruleId": "OPT001",
      "category": "OPTIMIZATION",
      "severity": "OPTIMIZATION",
      "message": "Constant expression detected: 10 + 20 * 2",
      "explanation": "Both operands are compile-time constants. Evaluating this expression at compile-time reduces CPU arithmetic instructions at runtime.",
      "suggestion": "Replace '10 + 20 * 2' with '50'.",
      "line": 2,
      "column": 12,
      "length": 11,
      "sourceSnippet": "10 + 20 * 2",
      "replacementSnippet": "50",
      "safetyLevel": "SAFE"
    },
    {
      "ruleId": "OPT005",
      "category": "OPTIMIZATION",
      "severity": "OPTIMIZATION",
      "message": "Strength reduction: 'a * 2'",
      "explanation": "Multiplication by power of 2 can be replaced with a single-cycle bitwise shift operation.",
      "suggestion": "Replace 'a * 2' with 'a << 1'.",
      "line": 3,
      "column": 11,
      "length": 5,
      "sourceSnippet": "a * 2",
      "replacementSnippet": "a << 1",
      "safetyLevel": "SAFE"
    }
  ]
}
```

---

## 8. College Viva & Examiner Demo Script

### Walkthrough Sequence:
1. **Open UI**: Open `http://localhost:5173` in any browser.
2. **Select Sample**: From the top dropdown, select **"Full Optimizer Showcase"**.
3. **Execute Analysis**: Click the **"⚡ Analyze Code"** button. Point out the sub-50ms execution speed and the Metrics Bar.
4. **Demonstrate AST Explorer**:
   - Open the **AST Explorer** tab.
   - Expand `FunctionNode [compute]` $\rightarrow$ `BlockNode` $\rightarrow$ `VarDeclNode [base]`.
   - Explain how concrete syntax tokens (parentheses, semicolons) were abstracted away while preserving source line coordinates.
5. **Demonstrate Symbol Table**:
   - Open the **Symbol Table** tab.
   - Point out variable `unusedVar`: `readCount = 0`, `writeCount = 1`.
   - Explain lexical scopes (`Global` vs `Function_compute` vs inner blocks).
6. **Demonstrate Control Flow Graph (CFG)**:
   - Open the **Control Flow Graph** tab.
   - Point out the basic blocks, green true edges, red false edges, and purple loop back-edges.
   - Highlight the **Unreachable Block** (`B_Dead / else` branch) detected by graph reachability analysis.
7. **Demonstrate Classical Optimizations**:
   - In the **Findings** tab, review:
     - `OPT001 (Constant Folding)`: `10 + 20 * 2` $\longrightarrow$ `50`.
     - `OPT003 (Algebraic Simplification)`: `x * 1 + 0` $\longrightarrow$ `x`.
     - `OPT005 (Strength Reduction)`: `temp * 2` $\longrightarrow$ `temp << 1`.
     - `OPT006 (Loop-Invariant Code Motion)`: `x * 5 + 4` hoisted outside loop.
   - Click **"View Diff"** on any finding to see the side-by-side comparison modal.
   - Click **"Apply Transformation"** to patch the code directly in Monaco Editor!

---

## 9. Academic Viva Questions & Model Answers

**Q1: How does your scanner distinguish keywords from identifiers?**  
*Answer*: In our ANTLR4 grammar (`MiniLang.g4`), keyword lexical rules (`INT_TYPE`, `IF`, etc.) are declared prior to the generic `ID` rule. Under maximal munch and first-rule priority, the lexer identifies reserved words as keyword tokens rather than identifiers.

**Q2: What is the difference between an AST and a Parse Tree?**  
*Answer*: A Parse Tree (CST) contains every grammatical token including punctuation (parentheses, commas, semicolons). An Abstract Syntax Tree (AST) condenses the program to only meaningful semantic nodes (operators and operands), making optimizations and code generation straightforward.

**Q3: How do you identify the leader of a Basic Block?**  
*Answer*: Leaders are identified by three criteria: 1) The first statement of a function, 2) The target of any conditional or unconditional jump, and 3) Any statement immediately following a jump or return.

**Q4: Why is Reaching Definitions a Union ($\cup$) confluence while Available Expressions is an Intersection ($\cap$)?**  
*Answer*: Reaching Definitions is a *may*-analysis (a definition reaches if it comes along *any* path), hence union. Available Expressions is a *must*-analysis (an expression is available only if it is calculated along *all* paths without modification), hence intersection.

---

## 10. Verification & Test Summary

Automated JUnit test results from `mvn test`:
```
[INFO] Running com.codeinsight.ASTBuilderTest            -> Tests run: 1, Failures: 0
[INFO] Running com.codeinsight.CFGBuilderTest            -> Tests run: 1, Failures: 0
[INFO] Running com.codeinsight.DataFlowTest              -> Tests run: 1, Failures: 0
[INFO] Running com.codeinsight.IntegrationAnalysisTest   -> Tests run: 1, Failures: 0
[INFO] Running com.codeinsight.LexerParserTest           -> Tests run: 2, Failures: 0
[INFO] Running com.codeinsight.OptimizerTest             -> Tests run: 3, Failures: 0
[INFO] Running com.codeinsight.SemanticAnalyzerTest      -> Tests run: 2, Failures: 0
[INFO] Running com.codeinsight.StaticAnalysisTest        -> Tests run: 2, Failures: 0
[INFO] Running com.codeinsight.SymbolTableTest           -> Tests run: 1, Failures: 0
[INFO] ------------------------------------------------------------------------
[INFO] BUILD SUCCESS (Total tests: 14, Failures: 0, Errors: 0)
```
