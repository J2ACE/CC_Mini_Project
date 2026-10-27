import { AnalysisResponse, SampleProgram } from '../types';

export async function analyzeCode(sourceCode: string): Promise<AnalysisResponse> {
  const response = await fetch('/api/analyze', {
    method: 'POST',
    headers: { 'Content-Type': 'application/json' },
    body: JSON.stringify({ sourceCode })
  });

  if (!response.ok) {
    throw new Error(`Analysis failed with status: ${response.status}`);
  }

  return response.json();
}

export async function fetchSamples(): Promise<SampleProgram[]> {
  try {
    const response = await fetch('/api/samples');
    if (!response.ok) throw new Error('Failed to load samples');
    return response.json();
  } catch (err) {
    console.warn('Backend samples unavailable, using fallback:', err);
    return [];
  }
}
