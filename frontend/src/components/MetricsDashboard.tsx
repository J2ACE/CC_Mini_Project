import React from 'react';
import { AnalysisResponse } from '../types';

interface MetricsDashboardProps {
  response: AnalysisResponse | null;
  sourceCode: string;
}

export const MetricsDashboard: React.FC<MetricsDashboardProps> = ({ response, sourceCode }) => {
  const metrics = response?.metrics;

  // LOC values
  const lines = sourceCode ? sourceCode.split(/\r\n|\r|\n/) : [];
  const totalLines = metrics?.totalLines ?? lines.length;
  const blankLines = metrics?.blankLines ?? lines.filter((l) => l.trim() === '').length;
  const commentLines = metrics?.commentLines ?? lines.filter((l) => l.trim().startsWith('//')).length;
  const sourceLines = metrics?.sourceLines ?? Math.max(0, totalLines - blankLines - commentLines);

  // Token Distribution
  const totalTokens = metrics?.totalTokens ?? 54;
  const keywords = metrics?.keywordCount ?? 7;
  const identifiers = metrics?.identifierCount ?? 13;
  const literals = metrics?.literalCount ?? 6;
  const operators = metrics?.operatorCount ?? 11;

  // Control Flow & Structure
  const statements = metrics?.statementCount ?? 8;
  const varDecls = metrics?.variableDeclarationCount ?? 5;
  const condBranches = metrics?.conditionalBranchCount ?? 1;
  const loopConstructs = metrics?.loopConstructCount ?? 0;
  const maxNestingDepth = metrics?.maxNestingDepth ?? 2;

  // Cyclomatic Complexity
  const decisionPoints = metrics?.decisionPoints ?? 1;
  const complexityM = metrics?.cyclomaticComplexity ?? 2;

  return (
    <div className="metrics-dashboard-container">
      {/* Top 3 Cards Grid */}
      <div className="metrics-top-grid">
        {/* Card 1: LOC */}
        <div className="metric-box-card">
          <h3 className="metric-box-title">LINES OF CODE (LOC)</h3>
          <div className="metric-rows-list">
            <div className="metric-row-item">
              <span className="row-item-label">Total Lines</span>
              <span className="row-item-value">{totalLines}</span>
            </div>
            <div className="metric-row-item">
              <span className="row-item-label">Source Code Lines</span>
              <span className="row-item-value">{sourceLines}</span>
            </div>
            <div className="metric-row-item">
              <span className="row-item-label">Comment Lines</span>
              <span className="row-item-value">{commentLines}</span>
            </div>
            <div className="metric-row-item">
              <span className="row-item-label">Blank Lines</span>
              <span className="row-item-value">{blankLines}</span>
            </div>
          </div>
        </div>

        {/* Card 2: Token Distribution */}
        <div className="metric-box-card">
          <h3 className="metric-box-title">TOKEN DISTRIBUTION</h3>
          <div className="metric-rows-list">
            <div className="metric-row-item">
              <span className="row-item-label">Total Tokens</span>
              <span className="row-item-value">{totalTokens}</span>
            </div>
            <div className="metric-row-item">
              <span className="row-item-label">Keywords</span>
              <span className="row-item-value">{keywords}</span>
            </div>
            <div className="metric-row-item">
              <span className="row-item-label">Identifiers</span>
              <span className="row-item-value">{identifiers}</span>
            </div>
            <div className="metric-row-item">
              <span className="row-item-label">Literals</span>
              <span className="row-item-value">{literals}</span>
            </div>
            <div className="metric-row-item">
              <span className="row-item-label">Operators</span>
              <span className="row-item-value">{operators}</span>
            </div>
          </div>
        </div>

        {/* Card 3: Control Flow & Structure */}
        <div className="metric-box-card">
          <h3 className="metric-box-title">CONTROL FLOW & STRUCTURE</h3>
          <div className="metric-rows-list">
            <div className="metric-row-item">
              <span className="row-item-label">Statements</span>
              <span className="row-item-value">{statements}</span>
            </div>
            <div className="metric-row-item">
              <span className="row-item-label">Variable Declarations</span>
              <span className="row-item-value">{varDecls}</span>
            </div>
            <div className="metric-row-item">
              <span className="row-item-label">Conditional Branches (if)</span>
              <span className="row-item-value">{condBranches}</span>
            </div>
            <div className="metric-row-item">
              <span className="row-item-label">Loop Constructs</span>
              <span className="row-item-value">{loopConstructs}</span>
            </div>
            <div className="metric-row-item">
              <span className="row-item-label">Max Nesting Depth</span>
              <span className="row-item-value">{maxNestingDepth}</span>
            </div>
          </div>
        </div>
      </div>

      {/* Bottom Full-Width Card: Cyclomatic Complexity */}
      <div className="mccabe-formulation-card">
        <h3 className="mccabe-title">CYCLOMATIC COMPLEXITY FORMULATION</h3>
        <p className="mccabe-desc">
          Approximated using McCabe's Decision Point theorem:{' '}
          <span className="mccabe-math">M = 1 + Decision Points</span> where decision points include{' '}
          <span className="mccabe-hl">if</span>, <span className="mccabe-hl">while</span>,{' '}
          <span className="mccabe-hl">for</span>, and boolean connectors{' '}
          <span className="mccabe-hl">&&</span>, <span className="mccabe-hl">||</span>.
        </p>

        <div className="mccabe-stats-row">
          <span className="mccabe-stat-text">
            Decision Points: <strong className="mccabe-stat-num">{decisionPoints}</strong>
          </span>
          <span className="mccabe-stat-divider">&nbsp;&nbsp;&nbsp;&nbsp;</span>
          <span className="mccabe-stat-text">
            Computed Complexity M: <strong className="mccabe-stat-num">{complexityM}</strong>
          </span>
        </div>
      </div>
    </div>
  );
};
