import React, { useEffect, useState } from 'react';
import { ControlFlowGraphDto } from '../types';
import { Graphviz } from '@hpcc-js/wasm';

interface CFGViewerProps {
  cfgs: ControlFlowGraphDto[];
}

export const CFGViewer: React.FC<CFGViewerProps> = ({ cfgs }) => {
  const [selectedFnIndex, setSelectedFnIndex] = useState<number>(0);
  const [svgContent, setSvgContent] = useState<string>('');
  const [loading, setLoading] = useState<boolean>(false);
  const [error, setError] = useState<string | null>(null);

  const currentCfg = cfgs && cfgs.length > 0 ? cfgs[selectedFnIndex] : null;

  useEffect(() => {
    if (!currentCfg || !currentCfg.dotRepresentation) {
      setSvgContent('');
      return;
    }

    setLoading(true);
    setError(null);

    Graphviz.load().then(graphviz => {
      try {
        const svg = graphviz.dot(currentCfg.dotRepresentation);
        setSvgContent(svg);
      } catch (err: any) {
        setError(err.message || 'Error compiling DOT graph');
      } finally {
        setLoading(false);
      }
    }).catch(err => {
      setError('Could not initialize Graphviz engine: ' + err.message);
      setLoading(false);
    });
  }, [currentCfg]);

  if (!cfgs || cfgs.length === 0) {
    return (
      <div className="empty-state">
        <div className="empty-state-icon">📊</div>
        <p>No Control Flow Graphs available.</p>
        <span style={{ fontSize: '0.8rem', color: 'var(--text-subtle)' }}>
          Write a function in MiniLang and click "Analyze" to generate basic blocks and control flow edges.
        </span>
      </div>
    );
  }

  return (
    <div className="cfg-container">
      <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', marginBottom: '0.75rem' }}>
        <div style={{ display: 'flex', alignItems: 'center', gap: '0.5rem' }}>
          <span style={{ fontSize: '0.8rem', color: 'var(--text-muted)' }}>Function:</span>
          <select
            value={selectedFnIndex}
            onChange={(e) => setSelectedFnIndex(Number(e.target.value))}
            className="sample-selector"
          >
            {cfgs.map((c, idx) => (
              <option key={idx} value={idx}>{c.functionName}()</option>
            ))}
          </select>
        </div>

        <div style={{ display: 'flex', gap: '0.75rem', fontSize: '0.75rem' }}>
          <span style={{ display: 'flex', alignItems: 'center', gap: '4px' }}>
            <span style={{ width: 8, height: 8, borderRadius: '50%', background: '#a6e3a1' }}></span> True
          </span>
          <span style={{ display: 'flex', alignItems: 'center', gap: '4px' }}>
            <span style={{ width: 8, height: 8, borderRadius: '50%', background: '#f38ba8' }}></span> False
          </span>
          <span style={{ display: 'flex', alignItems: 'center', gap: '4px' }}>
            <span style={{ width: 8, height: 8, borderRadius: '50%', background: '#cba6f7' }}></span> Loop
          </span>
          <span style={{ display: 'flex', alignItems: 'center', gap: '4px' }}>
            <span style={{ width: 8, height: 8, borderRadius: '50%', background: '#fab387' }}></span> Return
          </span>
        </div>
      </div>

      {loading && <div style={{ textAlign: 'center', padding: '2rem', color: 'var(--text-muted)' }}>Rendering Graphviz CFG...</div>}
      {error && <div style={{ color: 'var(--accent-rose)', padding: '1rem' }}>{error}</div>}

      {!loading && !error && svgContent && (
        <div
          className="cfg-svg-wrapper"
          dangerouslySetInnerHTML={{ __html: svgContent }}
        />
      )}
    </div>
  );
};
