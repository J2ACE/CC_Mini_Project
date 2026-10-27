import React, { useState } from 'react';

interface ASTTreeViewerProps {
  ast: any;
}

const TreeNode: React.FC<{ name: string; value: any; depth?: number }> = ({ name, value, depth = 0 }) => {
  const [collapsed, setCollapsed] = useState(depth > 2);

  if (value === null || value === undefined) return null;

  const isObject = typeof value === 'object';
  const isArray = Array.isArray(value);

  if (!isObject) {
    return (
      <div style={{ marginLeft: `${depth * 14}px`, padding: '2px 0' }}>
        <span style={{ color: 'var(--text-muted)' }}>{name}: </span>
        <span style={{ color: 'var(--accent-cyan)' }}>{String(value)}</span>
      </div>
    );
  }

  const keys = Object.keys(value).filter(k => k !== 'position');
  const nodeType = value.nodeType || (isArray ? `[${value.length}]` : '');

  return (
    <div style={{ marginLeft: `${depth * 12}px`, marginTop: '2px' }}>
      <div
        className="ast-node-header"
        onClick={() => setCollapsed(!collapsed)}
        style={{ userSelect: 'none' }}
      >
        <span style={{ fontSize: '0.7rem', color: 'var(--text-muted)' }}>
          {collapsed ? '▶' : '▼'}
        </span>
        <span style={{ color: 'var(--text-main)', fontWeight: 500 }}>{name}</span>
        {nodeType && (
          <span style={{ color: 'var(--accent-purple)', fontSize: '0.75rem', fontWeight: 600 }}>
            ({nodeType})
          </span>
        )}
        {value.position && (
          <span style={{ color: 'var(--text-subtle)', fontSize: '0.7rem' }}>
            L{value.position.line}:{value.position.column}
          </span>
        )}
      </div>

      {!collapsed && (
        <div style={{ borderLeft: '1px solid var(--border-subtle)', marginLeft: '6px', paddingLeft: '8px' }}>
          {isArray ? (
            value.map((item: any, idx: number) => (
              <TreeNode key={idx} name={`[${idx}]`} value={item} depth={depth + 1} />
            ))
          ) : (
            keys.map((k) => (
              <TreeNode key={k} name={k} value={value[k]} depth={depth + 1} />
            ))
          )}
        </div>
      )}
    </div>
  );
};

export const ASTTreeViewer: React.FC<ASTTreeViewerProps> = ({ ast }) => {
  if (!ast) {
    return (
      <div className="empty-state">
        <div className="empty-state-icon">🌲</div>
        <p>No Abstract Syntax Tree generated yet.</p>
        <span style={{ fontSize: '0.8rem', color: 'var(--text-subtle)' }}>
          Click "Analyze" to parse your source code and construct the AST.
        </span>
      </div>
    );
  }

  return (
    <div style={{ fontFamily: 'var(--font-mono)', fontSize: '0.8rem', overflowX: 'auto', padding: '0.5rem' }}>
      <TreeNode name="Program" value={ast} depth={0} />
    </div>
  );
};
