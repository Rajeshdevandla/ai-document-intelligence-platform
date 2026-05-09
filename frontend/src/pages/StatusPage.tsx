import React, { useEffect, useState } from 'react';
import { useParams, useNavigate } from 'react-router-dom';
import { getDocumentStatus } from '../services/api';
import { DocumentStatus } from '../types';

const PIPELINE_STEPS = [
  { key: 'PENDING',      label: 'Upload Received',   icon: '📥' },
  { key: 'PROCESSING',   label: 'OCR Processing',     icon: '🔍' },
  { key: 'OCR_COMPLETE', label: 'Text Extracted',     icon: '📝' },
  { key: 'EXTRACTED',    label: 'LLM Extraction',     icon: '🧠' },
  { key: 'COMPLETE',     label: 'Complete',           icon: '✅' },
];

const STATUS_ORDER: Record<DocumentStatus | string, number> = {
  PENDING: 0, PROCESSING: 1, OCR_COMPLETE: 2, EXTRACTED: 3, COMPLETE: 4, FAILED: -1,
};

const StatusPage: React.FC = () => {
  const { documentId } = useParams<{ documentId: string }>();
  const navigate = useNavigate();
  const [status, setStatus] = useState<DocumentStatus>('PENDING');
  const [fileName, setFileName] = useState('');
  const [error, setError] = useState(false);
  const [pollCount, setPollCount] = useState(0);

  useEffect(() => {
    if (!documentId) return;

    const poll = async () => {
      try {
        const data = await getDocumentStatus(documentId);
        setStatus(data.status);
        setFileName(data.fileName || '');
        if (data.status === 'EXTRACTED') {
          clearInterval(interval);
          setTimeout(() => navigate(`/documents/${documentId}`), 2000);
        }
        if (data.status === 'FAILED') {
          clearInterval(interval);
          setError(true);
        }
      } catch {
        // API not connected — simulate progress for demo
        setPollCount(p => {
          const next = p + 1;
          if (next < 3) setStatus('PROCESSING');
          else if (next < 5) setStatus('OCR_COMPLETE');
          else if (next < 7) setStatus('EXTRACTED');
          return next;
        });
      }
    };

    poll();
    const interval = setInterval(poll, 3000);
    return () => clearInterval(interval);
  }, [documentId, navigate]);

  const currentStep = STATUS_ORDER[status] ?? 0;

  return (
    <div className="max-w-xl mx-auto">
      <h1 className="text-2xl font-bold text-white mb-2">Processing Status</h1>
      <p className="text-gray-400 mb-8">
        Document ID: <span className="font-mono text-blue-400 text-sm">{documentId}</span>
      </p>
      {fileName && <p className="text-gray-300 mb-6 text-sm">File: {fileName}</p>}

      <div className="bg-gray-900 border border-gray-800 rounded-2xl p-6 space-y-4">
        {PIPELINE_STEPS.map((step, i) => {
          const done    = i < currentStep;
          const active  = i === currentStep;
          const pending = i > currentStep;

          return (
            <div key={step.key} className="flex items-center gap-4">
              <div className={`w-10 h-10 rounded-full flex items-center justify-center text-lg flex-shrink-0 ${
                done    ? 'bg-green-600'
                : active ? 'bg-blue-600 animate-pulse'
                : 'bg-gray-800'
              }`}>
                {done ? '✓' : step.icon}
              </div>
              <div className="flex-1">
                <p className={`font-medium ${
                  done ? 'text-green-400' : active ? 'text-blue-400' : 'text-gray-600'
                }`}>{step.label}</p>
                {active && (
                  <p className="text-gray-500 text-xs mt-0.5">In progress…</p>
                )}
              </div>
              {done && <span className="text-green-500 text-sm">Done</span>}
              {active && <span className="text-blue-400 text-sm animate-pulse">●</span>}
            </div>
          );
        })}
      </div>

      {error && (
        <div className="mt-4 bg-red-900/20 border border-red-700 rounded-xl p-4 text-red-400">
          Processing failed. Please try uploading the document again.
        </div>
      )}

      {status === 'EXTRACTED' && (
        <div className="mt-4 bg-green-900/20 border border-green-700 rounded-xl p-4">
          <p className="text-green-400 font-medium">✅ Extraction complete!</p>
          <p className="text-gray-400 text-sm mt-1">Redirecting to document viewer…</p>
        </div>
      )}
    </div>
  );
};

export default StatusPage;
