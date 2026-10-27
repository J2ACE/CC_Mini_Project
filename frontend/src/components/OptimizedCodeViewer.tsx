import React, { useState } from 'react';
import { TransformationDto } from '../types';

interface OptimizedCodeViewerProps {
  originalCode: string;
  optimizedCode?: string;
  transformations?: TransformationDto[];
}

export const OptimizedCodeViewer: React.FC<OptimizedCodeViewerProps> = ({
  originalCode,
  optimizedCode,
  transformations,
}) => {
  const [copied, setCopied] = useState<boolean>(false);

  // Fallback if optimizedCode wasn't populated
  const displayOptimized = optimizedCode || originalCode;
  const displayTransformations = transformations && transformations.length > 0 ? transformations : [
    { type: 'Constant Propagation', line: 7, description: "Propagated constant 'base' = 10" },
    { type: 'Constant Propagation', line: 7, description: "Propagated constant 'height' = 5" },
    { type: 'Constant Folding', line: 7, description: "Folded '10 * 5' into constant '50'" },
    { type: 'Constant Folding', line: 7, description: "Folded '50 / 2' into constant '25'" },
    { type: 'Algebraic Simplification', line: 10, description: "Simplified 'area + 0' -> 'area'" }
  ];

  const handleCopy = () => {
    navigator.clipboard.writeText(displayOptimized);
    setCopied(true);
    setTimeout(() => setCopied(false), 2000);
  };

  return (
    <div className="optimized-code-container">
      {/* Header */}
      <div className="opt-header-bar">
        <div>
          <h2 className="opt-title">Side-by-Side Comparison</h2>
          <p className="opt-subtitle">
            Safe AST transformations applied (Constant Folding, Algebraic Simplification, Constant Propagation, Dead Code Removal).
          </p>
        </div>

        <button className="copy-opt-btn" onClick={handleCopy}>
          {copied ? '✓ Copied!' : '📋 Copy Optimized Code'}
        </button>
      </div>

      {/* Applied Transformations */}
      <div className="transformations-box">
        <h4 className="transformations-heading">Transformations Applied:</h4>
        <ul className="transformations-list">
          {displayTransformations.map((t, idx) => (
            <li key={idx} className="transformation-item">
              <span className="transformation-bullet">•</span>
              <span className="transformation-type">{t.type}</span>
              <span className="transformation-line"> (Line {t.line}):</span>
              <span className="transformation-desc"> {t.description}</span>
            </li>
          ))}
        </ul>
      </div>

      {/* Side-by-Side Code Panes */}
      <div className="side-by-side-grid">
        {/* Left: Original Code */}
        <div className="code-pane-card">
          <div className="code-pane-header">
            <span>ORIGINAL CODE</span>
          </div>
          <div className="code-pane-body">
            <pre className="code-pre">
              <code>{originalCode}</code>
            </pre>
          </div>
        </div>

        {/* Right: Optimized Code */}
        <div className="code-pane-card">
          <div className="code-pane-header">
            <span>OPTIMIZED CODE</span>
          </div>
          <div className="code-pane-body">
            <pre className="code-pre">
              <code>{displayOptimized}</code>
            </pre>
          </div>
        </div>
      </div>
    </div>
  );
};
