import React from 'react';
import { useParams } from 'react-router-dom';

// Mock extraction result for demo
const MOCK_RESULT = {
  documentId: 'demo-doc-001',
  documentType: 'invoice',
  confidence: 0.962,
  llmProvider: 'openai',
  llmModel: 'gpt-4o',
  tokensUsed: 1247,
  processingTimeMs: 3820,
  summary:
    'Invoice #INV-2024-0892 from Acme Supplies Inc. to TechCorp Solutions for cloud infrastructure components. Total amount due: $12,450.00, due by January 31 2024.',
  entities: [
    { type: 'ORG',    value: 'Acme Supplies Inc.',    confidence: 0.98 },
    { type: 'ORG',    value: 'TechCorp Solutions',    confidence: 0.97 },
    { type: 'MONEY',  value: '$12,450.00',            confidence: 0.99 },
    { type: 'PERSON', value: 'John Smith',            confidence: 0.95 },
  ],
  dates: [
    { label: 'invoice_date', value: 'January 15, 2024',  normalized: '2024-01-15' },
    { label: 'due_date',     value: 'January 31, 2024',  normalized: '2024-01-31' },
  ],
  totals: { subtotal: 11250.00, tax: 1200.00, total: 12450.00, currency: 'USD' },
  lineItems: [
    { description: 'AWS EC2 Reserved Instances (12-month)',  quantity: 5,  unitPrice: 1800.00, total: 9000.00 },
    { description: 'S3 Storage — 10TB',                     quantity: 1,  unitPrice: 1500.00, total: 1500.00 },
    { description: 'CloudFront CDN Setup',                  quantity: 1,  unitPrice:  750.00, total:  750.00 },
  ],
  anomalies: [],
};

const ConfidenceBadge: React.FC<{ value: number }> = ({ value }) => {
  const pct = (value * 100).toFixed(1);
  const color = value > 0.9 ? 'green' : value > 0.7 ? 'yellow' : 'red';
  const colorMap: Record<string, string> = {
    green: 'bg-green-900/30 border-green-700 text-green-400',
    yellow: 'bg-yellow-900/30 border-yellow-700 text-yellow-400',
    red: 'bg-red-900/30 border-red-700 text-red-400',
  };
  return (
    <span className={`border text-xs px-2 py-0.5 rounded-full font-medium ${colorMap[color]}`}>
      {pct}% confidence
    </span>
  );
};

const DocumentViewerPage: React.FC = () => {
  const { documentId } = useParams<{ documentId: string }>();
  const r = MOCK_RESULT;

  return (
    <div className="max-w-4xl mx-auto space-y-6">
      {/* Header */}
      <div className="flex items-start justify-between">
        <div>
          <h1 className="text-2xl font-bold text-white">Extraction Result</h1>
          <p className="text-gray-400 text-sm mt-1 font-mono">{documentId || r.documentId}</p>
        </div>
        <div className="flex flex-col items-end gap-1">
          <ConfidenceBadge value={r.confidence} />
          <span className="text-gray-500 text-xs">{r.llmProvider} · {r.llmModel}</span>
        </div>
      </div>

      {/* Summary */}
      <div className="bg-gray-900 border border-gray-800 rounded-xl p-5">
        <h2 className="text-white font-semibold mb-2">Summary</h2>
        <p className="text-gray-300 leading-relaxed">{r.summary}</p>
        <div className="flex gap-4 mt-3 text-xs text-gray-500">
          <span>Type: <span className="text-blue-400 capitalize">{r.documentType}</span></span>
          <span>Tokens: <span className="text-purple-400">{r.tokensUsed.toLocaleString()}</span></span>
          <span>Time: <span className="text-green-400">{(r.processingTimeMs / 1000).toFixed(2)}s</span></span>
        </div>
      </div>

      <div className="grid grid-cols-1 md:grid-cols-2 gap-6">
        {/* Entities */}
        <div className="bg-gray-900 border border-gray-800 rounded-xl p-5">
          <h2 className="text-white font-semibold mb-3">Entities</h2>
          <div className="space-y-2">
            {r.entities.map((e, i) => (
              <div key={i} className="flex items-center justify-between py-1.5 border-b border-gray-800 last:border-0">
                <div>
                  <span className="text-xs text-gray-500 uppercase tracking-wide mr-2">{e.type}</span>
                  <span className="text-white text-sm">{e.value}</span>
                </div>
                <span className="text-gray-600 text-xs">{(e.confidence * 100).toFixed(0)}%</span>
              </div>
            ))}
          </div>
        </div>

        {/* Dates */}
        <div className="bg-gray-900 border border-gray-800 rounded-xl p-5">
          <h2 className="text-white font-semibold mb-3">Dates</h2>
          <div className="space-y-2">
            {r.dates.map((d, i) => (
              <div key={i} className="flex items-center justify-between py-1.5 border-b border-gray-800 last:border-0">
                <span className="text-gray-400 text-sm capitalize">{d.label.replace(/_/g, ' ')}</span>
                <span className="text-white text-sm font-mono">{d.normalized}</span>
              </div>
            ))}
          </div>

          <h2 className="text-white font-semibold mt-5 mb-3">Totals</h2>
          <div className="space-y-1">
            {Object.entries(r.totals).filter(([k]) => k !== 'currency').map(([k, v]) => (
              <div key={k} className="flex justify-between text-sm">
                <span className="text-gray-400 capitalize">{k}</span>
                <span className={`font-medium ${k === 'total' ? 'text-green-400' : 'text-white'}`}>
                  ${typeof v === 'number' ? v.toFixed(2) : v}
                </span>
              </div>
            ))}
          </div>
        </div>
      </div>

      {/* Line Items */}
      <div className="bg-gray-900 border border-gray-800 rounded-xl p-5">
        <h2 className="text-white font-semibold mb-3">Line Items</h2>
        <table className="w-full text-sm">
          <thead>
            <tr className="text-gray-500 border-b border-gray-800">
              <th className="text-left pb-2">Description</th>
              <th className="text-right pb-2">Qty</th>
              <th className="text-right pb-2">Unit Price</th>
              <th className="text-right pb-2">Total</th>
            </tr>
          </thead>
          <tbody>
            {r.lineItems.map((item, i) => (
              <tr key={i} className="border-b border-gray-800 last:border-0">
                <td className="py-2 text-gray-300">{item.description}</td>
                <td className="py-2 text-right text-gray-400">{item.quantity}</td>
                <td className="py-2 text-right text-gray-400">${item.unitPrice.toFixed(2)}</td>
                <td className="py-2 text-right text-white font-medium">${item.total.toFixed(2)}</td>
              </tr>
            ))}
          </tbody>
        </table>
      </div>

      {r.anomalies.length > 0 && (
        <div className="bg-red-900/20 border border-red-700 rounded-xl p-5">
          <h2 className="text-red-400 font-semibold mb-2">⚠️ Anomalies Detected</h2>
          <ul className="space-y-1">
            {r.anomalies.map((a, i) => <li key={i} className="text-red-300 text-sm">• {a}</li>)}
          </ul>
        </div>
      )}
    </div>
  );
};

export default DocumentViewerPage;
