import React from 'react';
import { ScopeDto, SymbolDto } from '../types';

interface SymbolTableViewProps {
  rootScope: ScopeDto | null;
}

export const SymbolTableView: React.FC<SymbolTableViewProps> = ({ rootScope }) => {
  if (!rootScope) {
    return (
      <div className="empty-state">
        <div className="empty-state-icon">📖</div>
        <p>No Symbol Table available.</p>
        <span style={{ fontSize: '0.8rem', color: 'var(--text-subtle)' }}>
          Run static analysis to inspect lexical scopes and identifiers.
        </span>
      </div>
    );
  }

  const allSymbols: { scopeName: string; level: number; symbol: SymbolDto }[] = [];

  function traverseScope(s: ScopeDto) {
    if (s.symbols) {
      for (const sym of s.symbols) {
        allSymbols.push({ scopeName: s.name, level: s.level, symbol: sym });
      }
    }
    if (s.children) {
      for (const c of s.children) {
        traverseScope(c);
      }
    }
  }

  traverseScope(rootScope);

  return (
    <div className="symbol-table-container">
      <table>
        <thead>
          <tr>
            <th>Identifier</th>
            <th>Type</th>
            <th>Kind</th>
            <th>Scope</th>
            <th>Declared At</th>
            <th>Reads</th>
            <th>Writes</th>
            <th>State</th>
          </tr>
        </thead>
        <tbody>
          {allSymbols.map((item, idx) => (
            <tr key={idx}>
              <td style={{ fontFamily: 'var(--font-mono)', fontWeight: 600, color: 'var(--accent-cyan)' }}>
                {item.symbol.name}
              </td>
              <td style={{ fontFamily: 'var(--font-mono)', color: 'var(--accent-purple)' }}>
                {item.symbol.type}
              </td>
              <td>{item.symbol.kind}</td>
              <td style={{ color: 'var(--text-muted)' }}>
                {item.scopeName} (L{item.level})
              </td>
              <td style={{ fontFamily: 'var(--font-mono)' }}>
                {item.symbol.declarationPosition ? `L${item.symbol.declarationPosition.line}:${item.symbol.declarationPosition.column}` : '-'}
              </td>
              <td style={{ textAlign: 'center' }}>{item.symbol.readCount}</td>
              <td style={{ textAlign: 'center' }}>{item.symbol.writeCount}</td>
              <td>
                {item.symbol.shadowed && (
                  <span style={{ fontSize: '0.7rem', color: 'var(--accent-amber)', marginRight: '6px' }}>
                    [SHADOWED]
                  </span>
                )}
                {item.symbol.initialized ? (
                  <span style={{ fontSize: '0.7rem', color: 'var(--accent-green)' }}>
                    INIT
                  </span>
                ) : (
                  <span style={{ fontSize: '0.7rem', color: 'var(--accent-rose)' }}>
                    UNINIT
                  </span>
                )}
              </td>
            </tr>
          ))}
        </tbody>
      </table>
    </div>
  );
};
