import React, { useState, useMemo } from 'react';
import { TokenDto } from '../types';

interface TokenStreamViewerProps {
  tokens: TokenDto[];
}

export const TokenStreamViewer: React.FC<TokenStreamViewerProps> = ({ tokens }) => {
  const [searchQuery, setSearchQuery] = useState<string>('');
  const [selectedCategory, setSelectedCategory] = useState<string>('All Categories');

  const categories = useMemo(() => {
    const set = new Set<string>();
    tokens.forEach((t) => {
      if (t.category) set.add(t.category);
    });
    return ['All Categories', ...Array.from(set).sort()];
  }, [tokens]);

  const filteredTokens = useMemo(() => {
    return tokens.filter((t) => {
      const matchesCategory =
        selectedCategory === 'All Categories' || t.category === selectedCategory;
      const q = searchQuery.toLowerCase();
      const matchesSearch =
        !q ||
        t.text.toLowerCase().includes(q) ||
        t.type.toLowerCase().includes(q) ||
        (t.category && t.category.toLowerCase().includes(q));
      return matchesCategory && matchesSearch;
    });
  }, [tokens, selectedCategory, searchQuery]);

  const getCategoryBadgeClass = (category?: string) => {
    switch (category) {
      case 'Comment':
        return 'cat-badge-comment';
      case 'Keyword':
        return 'cat-badge-keyword';
      case 'Identifier':
        return 'cat-badge-identifier';
      case 'Assignment Operator':
        return 'cat-badge-assign';
      case 'Operator':
        return 'cat-badge-operator';
      case 'Literal':
        return 'cat-badge-literal';
      case 'Delimiter':
        return 'cat-badge-delimiter';
      default:
        return 'cat-badge-default';
    }
  };

  return (
    <div className="tokens-viewer-container">
      {/* Top Filter Bar */}
      <div className="tokens-filter-bar">
        <div className="tokens-search-wrapper">
          <span className="search-icon">🔍</span>
          <input
            type="text"
            className="tokens-search-input"
            placeholder="Search tokens by lexeme or type..."
            value={searchQuery}
            onChange={(e) => setSearchQuery(e.target.value)}
          />
        </div>

        <div className="tokens-category-wrapper">
          <select
            className="tokens-category-select"
            value={selectedCategory}
            onChange={(e) => setSelectedCategory(e.target.value)}
          >
            {categories.map((cat) => (
              <option key={cat} value={cat}>
                {cat}
              </option>
            ))}
          </select>
        </div>
      </div>

      {/* Table Section */}
      <div className="tokens-table-wrapper">
        <table className="tokens-table">
          <thead>
            <tr>
              <th style={{ width: '8%' }}>Index</th>
              <th style={{ width: '18%' }}>Category</th>
              <th style={{ width: '22%' }}>Token Type</th>
              <th style={{ width: '36%' }}>Lexeme</th>
              <th style={{ width: '8%' }}>Line</th>
              <th style={{ width: '8%' }}>Column</th>
            </tr>
          </thead>
          <tbody>
            {filteredTokens.length === 0 ? (
              <tr>
                <td colSpan={6} style={{ textAlign: 'center', padding: '2rem', color: 'var(--text-subtle)' }}>
                  No tokens matching current filter
                </td>
              </tr>
            ) : (
              filteredTokens.map((t, idx) => (
                <tr key={`${t.line}-${t.column}-${idx}`}>
                  <td className="token-index">{t.index ?? idx}</td>
                  <td>
                    <span className={`cat-badge ${getCategoryBadgeClass(t.category)}`}>
                      {t.category ?? 'Token'}
                    </span>
                  </td>
                  <td className="token-type">{t.type}</td>
                  <td className="token-lexeme" title={t.text}>
                    {t.text}
                  </td>
                  <td className="token-pos">{t.line}</td>
                  <td className="token-pos">{t.column}</td>
                </tr>
              ))
            )}
          </tbody>
        </table>
      </div>
    </div>
  );
};
