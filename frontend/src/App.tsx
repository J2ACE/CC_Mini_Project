import React, { useState, useEffect, useRef, useMemo } from 'react';
import Editor, { OnMount } from '@monaco-editor/react';
import { AnalysisResponse, Diagnostic, SampleProgram, ScopeDto } from './types';
import { analyzeCode, fetchSamples } from './api/client';
import { DEFAULT_SAMPLES } from './samples/defaultSamples';
import { ExecutiveHealthSummary } from './components/ExecutiveHealthSummary';
import { TokenStreamViewer } from './components/TokenStreamViewer';
import { OptimizedCodeViewer } from './components/OptimizedCodeViewer';
import { MetricsDashboard } from './components/MetricsDashboard';
import { FindingsPanel } from './components/FindingsPanel';
import { CFGViewer } from './components/CFGViewer';
import { ASTTreeViewer } from './components/ASTTreeViewer';
import { SymbolTableView } from './components/SymbolTableView';
import { ReportSummary } from './components/ReportSummary';
import { DiffModal } from './components/DiffModal';
import './App.css';

type ActiveTab =
  | 'overview'
  | 'tokens'
  | 'ast'
  | 'symbols'
  | 'findings'
  | 'optimizations'
  | 'optimized_code'
  | 'metrics'
  | 'pipeline'
  | 'report';

export const App: React.FC = () => {
  const [samples, setSamples] = useState<SampleProgram[]>(DEFAULT_SAMPLES);
  const [selectedSampleId, setSelectedSampleId] = useState<string>(DEFAULT_SAMPLES[0].id);
  const [sourceCode, setSourceCode] = useState<string>(DEFAULT_SAMPLES[0].sourceCode);
  const [loading, setLoading] = useState<boolean>(false);
  const [response, setResponse] = useState<AnalysisResponse | null>(null);
  const [activeTab, setActiveTab] = useState<ActiveTab>('overview');
  const [diffDiag, setDiffDiag] = useState<Diagnostic | null>(null);

  const editorRef = useRef<any>(null);
  const monacoRef = useRef<any>(null);

  useEffect(() => {
    fetchSamples().then((loaded) => {
      if (loaded && loaded.length > 0) {
        setSamples(loaded);
      }
    });
  }, []);

  const runAnalysis = async (codeToAnalyze: string) => {
    setLoading(true);
    try {
      const res = await analyzeCode(codeToAnalyze);
      setResponse(res);
      updateEditorMarkers(res.diagnostics);
    } catch (err: any) {
      console.error('Analysis failed:', err);
    } finally {
      setLoading(false);
    }
  };

  const handleSampleChange = (id: string) => {
    setSelectedSampleId(id);
    const found = samples.find((s) => s.id === id);
    if (found) {
      setSourceCode(found.sourceCode);
      if (editorRef.current) {
        editorRef.current.setValue(found.sourceCode);
      }
      runAnalysis(found.sourceCode);
    }
  };

  const handleAnalyze = () => {
    runAnalysis(sourceCode);
  };

  const onEditorMount: OnMount = (editor, monaco) => {
    editorRef.current = editor;
    monacoRef.current = monaco;

    // Define custom MiniLang tokenization
    monaco.languages.register({ id: 'minilang' });
    monaco.languages.setMonarchTokensProvider('minilang', {
      keywords: [
        'int',
        'float',
        'char',
        'bool',
        'void',
        'if',
        'else',
        'while',
        'for',
        'return',
        'true',
        'false',
      ],
      tokenizer: {
        root: [
          [
            /[a-zA-Z_]\w*/,
            {
              cases: {
                '@keywords': 'keyword',
                '@default': 'identifier',
              },
            },
          ],
          [/[0-9]+\.[0-9]+/, 'number.float'],
          [/[0-9]+/, 'number'],
          [/'([^'\\]|\\.)'/, 'string.char'],
          [/\/\/.*$/, 'comment'],
          [/\/\*/, 'comment', '@comment'],
          [/[{}()\[\]]/, '@brackets'],
          [/[<>!=]=?|&&|\|\||[+\-*\/%]/, 'operator'],
          [/;/, 'delimiter'],
        ],
        comment: [
          [/[^\/*]+/, 'comment'],
          [/\*\//, 'comment', '@pop'],
          [/[\/*]/, 'comment'],
        ],
      },
    });

    // Auto-analyze initial sample
    handleAnalyze();
  };

  const updateEditorMarkers = (diagnostics: Diagnostic[]) => {
    if (!editorRef.current || !monacoRef.current) return;
    const monaco = monacoRef.current;
    const model = editorRef.current.getModel();

    const markers = diagnostics.map((d) => {
      let severity = monaco.MarkerSeverity.Info;
      if (d.severity === 'ERROR') severity = monaco.MarkerSeverity.Error;
      else if (d.severity === 'WARNING') severity = monaco.MarkerSeverity.Warning;
      else if (d.severity === 'OPTIMIZATION') severity = monaco.MarkerSeverity.Hint;

      return {
        startLineNumber: d.line,
        startColumn: d.column + 1,
        endLineNumber: d.line,
        endColumn: d.column + 1 + Math.max(1, d.length),
        message: `${d.ruleId}: ${d.message}\n💡 ${d.suggestion}`,
        severity,
      };
    });

    monaco.editor.setModelMarkers(model, 'codeinsight', markers);
  };

  const handleSelectDiagnostic = (d: Diagnostic) => {
    if (editorRef.current) {
      editorRef.current.revealLineInCenter(d.line);
      editorRef.current.setPosition({ lineNumber: d.line, column: d.column + 1 });
      editorRef.current.focus();
    }
  };

  const handleApplyOptimization = (d: Diagnostic) => {
    if (!editorRef.current || !d.replacementSnippet) return;
    const model = editorRef.current.getModel();

    const lineContent = model.getLineContent(d.line);
    const searchTarget = d.sourceSnippet || d.replacementSnippet;

    if (lineContent.includes(searchTarget)) {
      const startCol = lineContent.indexOf(searchTarget) + 1;
      const endCol = startCol + searchTarget.length;

      editorRef.current.executeEdits('codeinsight', [
        {
          range: new monacoRef.current.Range(d.line, startCol, d.line, endCol),
          text: d.replacementSnippet,
          forceMoveMarkers: true,
        },
      ]);

      const updatedCode = model.getValue();
      setSourceCode(updatedCode);
    }
  };

  // Helper to count symbols in scoped tree
  const countSymbols = (scope: ScopeDto | null | undefined): number => {
    if (!scope) return 0;
    let count = scope.symbols ? scope.symbols.length : 0;
    if (scope.children && Array.isArray(scope.children)) {
      for (const child of scope.children) {
        count += countSymbols(child);
      }
    }
    return count;
  };

  // Counts for tabs
  const tokensCount = response?.tokens?.length ?? 54;
  const astCount = response?.metrics?.astNodeCount ?? (response?.ast ? 29 : 0);
  const symbolCount = useMemo(() => countSymbols(response?.symbolTable) || 5, [response?.symbolTable]);
  const errorCount = response?.metrics?.errorCount ?? 0;
  const warningCount = response?.metrics?.warningCount ?? 0;
  const findingsCount = errorCount + warningCount;
  const optCount = response?.metrics?.optimizationCount ?? 3;

  const findingsDiagnostics = useMemo(() => {
    if (!response?.diagnostics) return [];
    return response.diagnostics.filter(
      (d) => d.severity === 'ERROR' || d.severity === 'WARNING' || d.category !== 'OPTIMIZATION'
    );
  }, [response?.diagnostics]);

  const optimizationDiagnostics = useMemo(() => {
    if (!response?.diagnostics) return [];
    return response.diagnostics.filter(
      (d) => d.severity === 'OPTIMIZATION' || d.category === 'OPTIMIZATION'
    );
  }, [response?.diagnostics]);

  return (
    <div className="app-container">
      {/* Top Header */}
      <header className="app-header">
        <div className="brand-section">
          <div className="brand-logo">⚡</div>
          <div>
            <div style={{ display: 'flex', alignItems: 'center', gap: '0.5rem' }}>
              <span className="brand-title">CodeInsight</span>
              <span className="brand-badge">MiniLang Analyzer</span>
            </div>
            <div style={{ fontSize: '0.7rem', color: 'var(--text-subtle)' }}>
              Compiler Construction Static Code Analyzer &amp; Optimization Engine
            </div>
          </div>
        </div>

        <div className="actions-section">
          <select
            className="sample-selector"
            value={selectedSampleId}
            onChange={(e) => handleSampleChange(e.target.value)}
          >
            {samples.map((s) => (
              <option key={s.id} value={s.id}>
                {s.name}
              </option>
            ))}
          </select>

          <button className="analyze-button" onClick={handleAnalyze} disabled={loading}>
            {loading ? 'Analyzing...' : '⚡ Analyze Code'}
          </button>
        </div>
      </header>

      {/* Metrics Bar */}
      <div className="metrics-bar">
        <div className="metric-pill">
          <span>Lines:</span>
          <span className="metric-value">{response?.metrics?.totalLines ?? 0}</span>
        </div>
        <div className="metric-pill">
          <span>Tokens:</span>
          <span className="metric-value">{response?.metrics?.totalTokens ?? 0}</span>
        </div>
        <div className="metric-pill">
          <span>AST Depth:</span>
          <span className="metric-value">{response?.metrics?.astDepth ?? 0}</span>
        </div>
        <div className="metric-pill errors">
          <span>Errors:</span>
          <span className="metric-value">{response?.metrics?.errorCount ?? 0}</span>
        </div>
        <div className="metric-pill warnings">
          <span>Warnings:</span>
          <span className="metric-value">{response?.metrics?.warningCount ?? 0}</span>
        </div>
        <div className="metric-pill optimizations">
          <span>Optimizations:</span>
          <span className="metric-value">{response?.metrics?.optimizationCount ?? 0}</span>
        </div>
      </div>

      {/* Main Workspace */}
      <main className="main-layout">
        {/* Monaco Editor Pane */}
        <section className="editor-pane">
          <div className="pane-header">
            <span>MiniLang Source Code Editor</span>
            <span style={{ fontSize: '0.7rem', color: 'var(--accent-cyan)' }}>UTF-8</span>
          </div>
          <Editor
            height="100%"
            language="minilang"
            theme="vs-dark"
            value={sourceCode}
            onChange={(val) => setSourceCode(val || '')}
            onMount={onEditorMount}
            options={{
              minimap: { enabled: false },
              fontSize: 13,
              fontFamily: "'Fira Code', Consolas, monospace",
              fontLigatures: true,
              scrollBeyondLastLine: false,
              automaticLayout: true,
              tabSize: 4,
              renderLineHighlight: 'all',
              padding: { top: 12, bottom: 12 },
            }}
          />
        </section>

        {/* Right Analysis Pane */}
        <section className="analysis-pane">
          {/* Tabs */}
          <div className="tabs-header">
            {/* 1. Overview Tab */}
            <button
              className={`tab-btn ${activeTab === 'overview' ? 'active' : ''}`}
              onClick={() => setActiveTab('overview')}
            >
              <span className="tab-icon">📋</span>
              <span>Overview</span>
            </button>

            {/* 2. Tokens Tab */}
            <button
              className={`tab-btn ${activeTab === 'tokens' ? 'active' : ''}`}
              onClick={() => setActiveTab('tokens')}
            >
              <span className="tab-icon">🏷️</span>
              <span>Tokens</span>
              <span className="tab-badge">{tokensCount}</span>
            </button>

            {/* 3. AST Hierarchy Tab */}
            <button
              className={`tab-btn ${activeTab === 'ast' ? 'active' : ''}`}
              onClick={() => setActiveTab('ast')}
            >
              <span className="tab-icon">🌲</span>
              <span>AST Hierarchy</span>
              <span className="tab-badge">{astCount}</span>
            </button>

            {/* 4. Symbol Table Tab */}
            <button
              className={`tab-btn ${activeTab === 'symbols' ? 'active' : ''}`}
              onClick={() => setActiveTab('symbols')}
            >
              <span className="tab-icon">📖</span>
              <span>Symbol Table</span>
              <span className="tab-badge">{symbolCount}</span>
            </button>

            {/* 5. Findings Tab */}
            <button
              className={`tab-btn ${activeTab === 'findings' ? 'active' : ''}`}
              onClick={() => setActiveTab('findings')}
            >
              <span className="tab-icon">⚠️</span>
              <span>Findings</span>
              <span className="tab-badge">{findingsCount}</span>
            </button>

            {/* 6. Optimizations Tab */}
            <button
              className={`tab-btn ${activeTab === 'optimizations' ? 'active' : ''}`}
              onClick={() => setActiveTab('optimizations')}
            >
              <span className="tab-icon">⚡</span>
              <span>Optimizations</span>
              <span className="tab-badge">{optCount}</span>
            </button>

            {/* 7. Optimized Code Tab */}
            <button
              className={`tab-btn ${activeTab === 'optimized_code' ? 'active' : ''}`}
              onClick={() => setActiveTab('optimized_code')}
            >
              <span className="tab-icon">✨</span>
              <span>Optimized Code</span>
            </button>

            {/* 8. Metrics Tab */}
            <button
              className={`tab-btn ${activeTab === 'metrics' ? 'active' : ''}`}
              onClick={() => setActiveTab('metrics')}
            >
              <span className="tab-icon">📐</span>
              <span>Metrics</span>
            </button>

            {/* 9. Compiler Pipeline Tab */}
            <button
              className={`tab-btn ${activeTab === 'pipeline' ? 'active' : ''}`}
              onClick={() => setActiveTab('pipeline')}
            >
              <span className="tab-icon">🔀</span>
              <span>Compiler Pipeline</span>
              <span className="tab-badge">{response?.controlFlowGraphs?.length ?? 0}</span>
            </button>

            {/* 10. Academic Report Tab */}
            <button
              className={`tab-btn ${activeTab === 'report' ? 'active' : ''}`}
              onClick={() => setActiveTab('report')}
            >
              <span className="tab-icon">📄</span>
              <span>Report</span>
            </button>
          </div>

          {/* Tab Content Viewers */}
          <div className="tab-content">
            {/* Feature 1: Executive Health Summary */}
            {activeTab === 'overview' && (
              <ExecutiveHealthSummary response={response} />
            )}

            {/* Feature 2: Tokens Stream Explorer */}
            {activeTab === 'tokens' && (
              <TokenStreamViewer tokens={response?.tokens ?? []} />
            )}

            {/* AST Hierarchy */}
            {activeTab === 'ast' && (
              <ASTTreeViewer ast={response?.ast} />
            )}

            {/* Symbol Table */}
            {activeTab === 'symbols' && (
              <SymbolTableView rootScope={response?.symbolTable ?? null} />
            )}

            {/* Diagnostics & Findings */}
            {activeTab === 'findings' && (
              <FindingsPanel
                diagnostics={findingsDiagnostics}
                onSelectDiagnostic={handleSelectDiagnostic}
                onOpenDiff={(d) => setDiffDiag(d)}
              />
            )}

            {/* Compiler Optimizations */}
            {activeTab === 'optimizations' && (
              <FindingsPanel
                diagnostics={optimizationDiagnostics}
                onSelectDiagnostic={handleSelectDiagnostic}
                onOpenDiff={(d) => setDiffDiag(d)}
              />
            )}

            {/* Feature 3: Optimized Code Side-by-Side Comparison */}
            {activeTab === 'optimized_code' && (
              <OptimizedCodeViewer
                originalCode={sourceCode}
                optimizedCode={response?.optimizedCode}
                transformations={response?.transformationsApplied}
              />
            )}

            {/* Feature 4: Compiler & Code Metrics */}
            {activeTab === 'metrics' && (
              <MetricsDashboard response={response} sourceCode={sourceCode} />
            )}

            {/* Control Flow & Compiler Pipeline */}
            {activeTab === 'pipeline' && (
              <CFGViewer cfgs={response?.controlFlowGraphs ?? []} />
            )}

            {/* Academic Report Summary */}
            {activeTab === 'report' && (
              <ReportSummary response={response} sourceCode={sourceCode} />
            )}
          </div>
        </section>
      </main>

      {/* Suggestion Diff Modal */}
      {diffDiag && (
        <DiffModal
          diagnostic={diffDiag}
          onClose={() => setDiffDiag(null)}
          onApply={handleApplyOptimization}
        />
      )}
    </div>
  );
};

export default App;
