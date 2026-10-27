export interface SourcePosition {
  line: number;
  column: number;
  startIndex: number;
  stopIndex: number;
  length: number;
}

export interface Diagnostic {
  ruleId: string;
  category: 'LEXICAL' | 'SYNTAX' | 'SEMANTIC' | 'STATIC_ANALYSIS' | 'OPTIMIZATION';
  severity: 'ERROR' | 'WARNING' | 'OPTIMIZATION' | 'INFO';
  message: string;
  explanation: string;
  suggestion: string;
  line: number;
  column: number;
  length: number;
  sourceSnippet: string;
  replacementSnippet: string;
  safetyLevel: string;
}

export interface TokenDto {
  index?: number;
  category?: string;
  type: string;
  text: string;
  line: number;
  column: number;
}

export interface TransformationDto {
  type: string;
  line: number;
  description: string;
}

export interface CFGEdgeDto {
  sourceId: string;
  targetId: string;
  type: 'FALLTHROUGH' | 'BRANCH_TRUE' | 'BRANCH_FALSE' | 'LOOP_BACK' | 'RETURN_EDGE';
}

export interface BasicBlockDto {
  id: string;
  statements: any[];
  unreachable: boolean;
  successors: string[];
  predecessors: string[];
}

export interface ControlFlowGraphDto {
  functionName: string;
  dotRepresentation: string;
  blocks: BasicBlockDto[];
}

export interface SymbolDto {
  name: string;
  type: string;
  kind: string;
  declarationPosition: SourcePosition;
  initialized: boolean;
  readCount: number;
  writeCount: number;
  shadowed: boolean;
}

export interface ScopeDto {
  name: string;
  type: string;
  level: number;
  symbols: SymbolDto[];
  children: ScopeDto[];
}

export interface AnalysisResponse {
  success: boolean;
  metrics: {
    totalLines: number;
    sourceLines?: number;
    commentLines?: number;
    blankLines?: number;
    totalTokens: number;
    keywordCount?: number;
    identifierCount?: number;
    literalCount?: number;
    operatorCount?: number;
    statementCount?: number;
    variableDeclarationCount?: number;
    conditionalBranchCount?: number;
    loopConstructCount?: number;
    maxNestingDepth?: number;
    decisionPoints?: number;
    cyclomaticComplexity: number;
    astDepth: number;
    astNodeCount?: number;
    errorCount: number;
    warningCount: number;
    optimizationCount: number;
    executionTimeMs?: number;
    healthScore?: number;
    healthRating?: string;
    deductions?: {
      errors: number;
      warnings: number;
      optimizations: number;
      complexity: number;
      nestingDepth: number;
    };
    correctness?: number;
    codeQuality?: number;
    optimizationPotential?: number;
    maintainability?: number;
  };
  tokens: TokenDto[];
  ast: any;
  symbolTable: ScopeDto;
  controlFlowGraphs: ControlFlowGraphDto[];
  diagnostics: Diagnostic[];
  optimizedCode?: string;
  transformationsApplied?: TransformationDto[];
}

export interface SampleProgram {
  id: string;
  name: string;
  description: string;
  sourceCode: string;
}
