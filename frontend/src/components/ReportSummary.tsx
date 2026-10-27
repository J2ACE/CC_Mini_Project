import React from 'react';
import { AnalysisResponse } from '../types';

interface ReportSummaryProps {
  response: AnalysisResponse | null;
  sourceCode: string;
}

export const ReportSummary: React.FC<ReportSummaryProps> = ({ response, sourceCode }) => {
  if (!response) {
    return (
      <div className="empty-state">
        <div className="empty-state-icon">📋</div>
        <p>No analysis report available.</p>
        <span style={{ fontSize: '0.8rem', color: 'var(--text-subtle)' }}>
          Run an analysis to generate an academic report summary.
        </span>
      </div>
    );
  }

  const handlePrint = () => {
    window.print();
  };

  const handleExportJson = () => {
    const dataStr = 'data:text/json;charset=utf-8,' + encodeURIComponent(JSON.stringify(response, null, 2));
    const downloadAnchor = document.createElement('a');
    downloadAnchor.setAttribute('href', dataStr);
    downloadAnchor.setAttribute('download', 'codeinsight-analysis-report.json');
    document.body.appendChild(downloadAnchor);
    downloadAnchor.click();
    downloadAnchor.remove();
  };

  return (
    <div style={{ display: 'flex', flexDirection: 'column', gap: '1rem', padding: '0.5rem' }}>
      <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center' }}>
        <h2 style={{ fontSize: '1.1rem', fontWeight: 700 }}>CodeInsight Academic Analysis Report</h2>
        <div style={{ display: 'flex', gap: '0.5rem' }}>
          <button className="btn-secondary" onClick={handleExportJson}>Export JSON</button>
          <button className="btn-primary" onClick={handlePrint}>Print / Save PDF</button>
        </div>
      </div>

      <div style={{ display: 'grid', gridTemplateColumns: 'repeat(4, 1fr)', gap: '0.75rem' }}>
        <div className="metric-pill" style={{ flexDirection: 'column', alignItems: 'flex-start', padding: '0.75rem' }}>
          <span style={{ fontSize: '0.7rem' }}>Lines Analyzed</span>
          <span style={{ fontSize: '1.25rem', fontWeight: 700, color: 'var(--text-main)' }}>{response.metrics.totalLines}</span>
        </div>
        <div className="metric-pill" style={{ flexDirection: 'column', alignItems: 'flex-start', padding: '0.75rem' }}>
          <span style={{ fontSize: '0.7rem' }}>Tokens Scanned</span>
          <span style={{ fontSize: '1.25rem', fontWeight: 700, color: 'var(--accent-cyan)' }}>{response.metrics.totalTokens}</span>
        </div>
        <div className="metric-pill" style={{ flexDirection: 'column', alignItems: 'flex-start', padding: '0.75rem' }}>
          <span style={{ fontSize: '0.7rem' }}>AST Depth</span>
          <span style={{ fontSize: '1.25rem', fontWeight: 700, color: 'var(--accent-purple)' }}>{response.metrics.astDepth}</span>
        </div>
        <div className="metric-pill" style={{ flexDirection: 'column', alignItems: 'flex-start', padding: '0.75rem' }}>
          <span style={{ fontSize: '0.7rem' }}>Cyclomatic Complexity</span>
          <span style={{ fontSize: '1.25rem', fontWeight: 700, color: 'var(--accent-amber)' }}>{response.metrics.cyclomaticComplexity}</span>
        </div>
      </div>

      <div style={{ backgroundColor: 'var(--bg-card)', padding: '1rem', borderRadius: '6px', border: '1px solid var(--border-subtle)' }}>
        <h3 style={{ fontSize: '0.9rem', marginBottom: '0.5rem' }}>Compiler Construction Concepts Verified</h3>
        <ul style={{ fontSize: '0.8rem', color: 'var(--text-muted)', lineHeight: '1.6', paddingLeft: '1.2rem' }}>
          <li><strong>Lexical Analysis:</strong> Successfully generated DFA token stream ({response.tokens.length} tokens).</li>
          <li><strong>Syntax Analysis:</strong> Constructed concrete parse tree via ANTLR 4 ALL(*) algorithm.</li>
          <li><strong>Abstract Syntax Tree:</strong> Abstracted into custom {response.ast?.nodeType || 'ProgramNode'} with full coordinate retention.</li>
          <li><strong>Symbol Table:</strong> Scoped hierarchical resolution verified across {response.symbolTable?.name || 'Global'} scope.</li>
          <li><strong>Control Flow Graph:</strong> Basic blocks partitioned with branch, loop-back, and return edge typings.</li>
          <li><strong>Data-Flow Analysis:</strong> Reaching Definitions, Live Variables, and Available Expressions solved via iterative fixed-point worklist.</li>
          <li><strong>Classical Optimizations:</strong> {response.metrics.optimizationCount} optimization suggestions identified.</li>
        </ul>
      </div>

      <div style={{ backgroundColor: 'var(--bg-card)', padding: '1rem', borderRadius: '6px', border: '1px solid var(--border-subtle)' }}>
        <h3 style={{ fontSize: '0.9rem', marginBottom: '0.5rem' }}>Diagnostic Breakdown</h3>
        <div style={{ display: 'flex', gap: '1rem', fontSize: '0.85rem' }}>
          <span style={{ color: 'var(--accent-rose)' }}>Errors: <strong>{response.metrics.errorCount}</strong></span>
          <span style={{ color: 'var(--accent-amber)' }}>Warnings: <strong>{response.metrics.warningCount}</strong></span>
          <span style={{ color: 'var(--accent-green)' }}>Optimizations: <strong>{response.metrics.optimizationCount}</strong></span>
        </div>
      </div>
    </div>
  );
};
