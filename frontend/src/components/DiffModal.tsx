import React from 'react';
import { Diagnostic } from '../types';

interface DiffModalProps {
  diagnostic: Diagnostic | null;
  onClose: () => void;
  onApply: (diag: Diagnostic) => void;
}

export const DiffModal: React.FC<DiffModalProps> = ({ diagnostic, onClose, onApply }) => {
  if (!diagnostic) return null;

  return (
    <div className="modal-overlay" onClick={onClose}>
      <div className="modal-card" onClick={(e) => e.stopPropagation()}>
        <div className="modal-header">
          <div style={{ display: 'flex', alignItems: 'center', gap: '0.5rem' }}>
            <span className="rule-id" style={{ backgroundColor: 'rgba(34, 197, 94, 0.2)', color: 'var(--accent-green)' }}>
              {diagnostic.ruleId}
            </span>
            <h3 style={{ fontSize: '1rem', fontWeight: 600 }}>{diagnostic.message}</h3>
          </div>
          <button onClick={onClose} style={{ color: 'var(--text-muted)', fontSize: '1.25rem' }}>×</button>
        </div>

        <div className="modal-body">
          <p style={{ fontSize: '0.85rem', color: 'var(--text-muted)' }}>
            {diagnostic.explanation}
          </p>

          <div className="diff-preview">
            <div className="diff-box orig">
              <div className="diff-label">Original Code (Line {diagnostic.line})</div>
              <code>{diagnostic.sourceSnippet || diagnostic.message}</code>
            </div>

            <div className="diff-box repl">
              <div className="diff-label">Optimized Transformation</div>
              <code style={{ color: 'var(--accent-green)' }}>
                {diagnostic.replacementSnippet || diagnostic.suggestion}
              </code>
            </div>
          </div>

          <div style={{ fontSize: '0.75rem', color: 'var(--text-subtle)', display: 'flex', gap: '1rem' }}>
            <span>Safety Classification: <strong>{diagnostic.safetyLevel}</strong></span>
            <span>Rule Category: <strong>{diagnostic.category}</strong></span>
          </div>
        </div>

        <div className="modal-footer">
          <button className="btn-secondary" onClick={onClose}>Close</button>
          {diagnostic.replacementSnippet && (
            <button
              className="btn-primary"
              onClick={() => {
                onApply(diagnostic);
                onClose();
              }}
            >
              Apply Transformation
            </button>
          )}
        </div>
      </div>
    </div>
  );
};
