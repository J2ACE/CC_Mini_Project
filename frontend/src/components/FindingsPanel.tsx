import React, { useState } from 'react';
import { Diagnostic } from '../types';

interface FindingsPanelProps {
  diagnostics: Diagnostic[];
  onSelectDiagnostic: (d: Diagnostic) => void;
  onOpenDiff: (d: Diagnostic) => void;
}

export const FindingsPanel: React.FC<FindingsPanelProps> = ({
  diagnostics,
  onSelectDiagnostic,
  onOpenDiff,
}) => {
  const [filter, setFilter] = useState<'ALL' | 'ERROR' | 'WARNING' | 'OPTIMIZATION'>('ALL');
  const [search, setSearch] = useState<string>('');

  const filtered = diagnostics.filter((d) => {
    if (filter !== 'ALL' && d.severity !== filter) return false;
    if (search.trim()) {
      const q = search.toLowerCase();
      return (
        d.message.toLowerCase().includes(q) ||
        d.ruleId.toLowerCase().includes(q) ||
        d.explanation.toLowerCase().includes(q)
      );
    }
    return true;
  });

  if (!diagnostics || diagnostics.length === 0) {
    return (
      <div className="empty-state">
        <div className="empty-state-icon">✨</div>
        <p>No diagnostics found.</p>
        <span style={{ fontSize: '0.8rem', color: 'var(--text-subtle)' }}>
          Code has zero syntax or semantic issues, or has not been analyzed yet.
        </span>
      </div>
    );
  }

  return (
    <div style={{ display: 'flex', gap: '0.75rem', height: '100%', flexDirection: 'column' }}>
      <div style={{ display: 'flex', gap: '0.5rem', alignItems: 'center', marginBottom: '0.25rem' }}>
        <div style={{ display: 'flex', gap: '4px', backgroundColor: 'var(--bg-primary)', padding: '2px', borderRadius: '6px' }}>
          {(['ALL', 'ERROR', 'WARNING', 'OPTIMIZATION'] as const).map((lvl) => (
            <button
              key={lvl}
              onClick={() => setFilter(lvl)}
              style={{
                fontSize: '0.75rem',
                padding: '4px 8px',
                borderRadius: '4px',
                color: filter === lvl ? '#fff' : 'var(--text-muted)',
                backgroundColor: filter === lvl ? 'var(--bg-surface)' : 'transparent',
                fontWeight: filter === lvl ? 600 : 400
              }}
            >
              {lvl}
            </button>
          ))}
        </div>

        <input
          type="text"
          placeholder="Filter by keyword or rule ID..."
          value={search}
          onChange={(e) => setSearch(e.target.value)}
          style={{
            flex: 1,
            backgroundColor: 'var(--bg-primary)',
            border: '1px solid var(--border-subtle)',
            borderRadius: '4px',
            color: 'var(--text-main)',
            fontSize: '0.75rem',
            padding: '4px 8px',
            outline: 'none'
          }}
        />
      </div>

      <div className="findings-list" style={{ overflowY: 'auto', flex: 1 }}>
        {filtered.map((d, idx) => (
          <div
            key={idx}
            className={`finding-card severity-${d.severity}`}
            onClick={() => onSelectDiagnostic(d)}
          >
            <div className="finding-header">
              <div className="finding-title-row">
                <span className="rule-id">{d.ruleId}</span>
                <span className="finding-message">{d.message}</span>
              </div>
              <span className="line-badge">Line {d.line}:{d.column}</span>
            </div>

            <div className="finding-explanation">{d.explanation}</div>

            <div className="finding-suggestion-box">
              <span className="suggestion-text">💡 {d.suggestion}</span>

              {d.severity === 'OPTIMIZATION' && d.replacementSnippet && (
                <button
                  className="apply-button"
                  onClick={(e) => {
                    e.stopPropagation();
                    onOpenDiff(d);
                  }}
                >
                  View Diff
                </button>
              )}
            </div>
          </div>
        ))}
      </div>
    </div>
  );
};
