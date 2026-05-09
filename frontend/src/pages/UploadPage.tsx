import React, { useCallback, useState } from 'react';
import { useDropzone } from 'react-dropzone';
import { useNavigate } from 'react-router-dom';
import { useUpload } from '../hooks/useUpload';

const ACCEPTED_TYPES = {
  'application/pdf': ['.pdf'],
  'image/png': ['.png'],
  'image/jpeg': ['.jpg', '.jpeg'],
};

const MAX_SIZE = 50 * 1024 * 1024; // 50MB

const UploadPage: React.FC = () => {
  const navigate = useNavigate();
  const { state, uploadFile, reset } = useUpload();
  const [selectedFile, setSelectedFile] = useState<File | null>(null);

  const onDrop = useCallback((acceptedFiles: File[]) => {
    if (acceptedFiles.length > 0) {
      setSelectedFile(acceptedFiles[0]);
    }
  }, []);

  const { getRootProps, getInputProps, isDragActive, fileRejections } = useDropzone({
    onDrop,
    accept: ACCEPTED_TYPES,
    maxFiles: 1,
    maxSize: MAX_SIZE,
  });

  const handleUpload = async () => {
    if (!selectedFile) return;
    const docId = await uploadFile(selectedFile);
    if (docId) {
      setTimeout(() => navigate(`/status/${docId}`), 1500);
    }
  };

  const handleReset = () => {
    reset();
    setSelectedFile(null);
  };

  const formatSize = (bytes: number) =>
    bytes < 1024 * 1024
      ? `${(bytes / 1024).toFixed(1)} KB`
      : `${(bytes / (1024 * 1024)).toFixed(1)} MB`;

  return (
    <div className="max-w-2xl mx-auto">
      <h1 className="text-2xl font-bold text-white mb-2">Upload Document</h1>
      <p className="text-gray-400 mb-8">
        Upload PDFs or images to extract structured data using AI
      </p>

      {state.status === 'idle' || state.status === 'error' ? (
        <>
          <div
            {...getRootProps()}
            className={`border-2 border-dashed rounded-2xl p-12 text-center cursor-pointer transition-all ${
              isDragActive
                ? 'border-blue-500 bg-blue-950/30'
                : 'border-gray-700 hover:border-gray-500 bg-gray-900/50'
            }`}
          >
            <input {...getInputProps()} />
            <div className="text-5xl mb-4">📄</div>
            {isDragActive ? (
              <p className="text-blue-400 font-medium">Drop it here!</p>
            ) : (
              <>
                <p className="text-gray-200 font-medium mb-1">
                  Drag & drop a document here
                </p>
                <p className="text-gray-500 text-sm">or click to browse</p>
              </>
            )}
            <p className="text-gray-600 text-xs mt-4">PDF, PNG, JPG · Max 50 MB</p>
          </div>

          {fileRejections.length > 0 && (
            <p className="text-red-400 text-sm mt-2">
              {fileRejections[0].errors[0].message}
            </p>
          )}

          {selectedFile && (
            <div className="mt-4 bg-gray-900 border border-gray-800 rounded-xl p-4 flex items-center justify-between">
              <div>
                <p className="text-white font-medium">{selectedFile.name}</p>
                <p className="text-gray-500 text-sm">{formatSize(selectedFile.size)}</p>
              </div>
              <button
                onClick={handleUpload}
                className="bg-blue-600 hover:bg-blue-500 text-white px-6 py-2 rounded-lg font-medium transition-colors"
              >
                Upload & Process
              </button>
            </div>
          )}

          {state.status === 'error' && (
            <div className="mt-4 bg-red-900/20 border border-red-700 rounded-xl p-4">
              <p className="text-red-400">{state.errorMessage}</p>
              <button onClick={handleReset} className="text-red-300 text-sm underline mt-2">
                Try again
              </button>
            </div>
          )}
        </>
      ) : (
        <div className="bg-gray-900 border border-gray-800 rounded-2xl p-8">
          <p className="text-white font-medium mb-4">
            {state.status === 'requesting-url' && '⏳ Requesting upload URL…'}
            {state.status === 'uploading' && `📤 Uploading… ${state.progress}%`}
            {state.status === 'complete' && '✅ Upload complete! Redirecting to status page…'}
          </p>

          {state.status === 'uploading' && (
            <div className="w-full bg-gray-800 rounded-full h-2">
              <div
                className="bg-blue-500 h-2 rounded-full transition-all"
                style={{ width: `${state.progress}%` }}
              />
            </div>
          )}

          {state.status === 'complete' && state.documentId && (
            <p className="text-gray-400 text-sm mt-2">
              Document ID: <span className="text-blue-400 font-mono">{state.documentId}</span>
            </p>
          )}
        </div>
      )}

      <div className="grid grid-cols-3 gap-4 mt-8">
        {[
          { icon: '🔍', title: 'OCR Extraction', desc: 'AWS Textract + Tesseract' },
          { icon: '🧠', title: 'LLM Analysis', desc: 'GPT-4o · Claude 3 · Gemini' },
          { icon: '📊', title: 'Analytics', desc: 'Real-time metrics dashboard' },
        ].map(({ icon, title, desc }) => (
          <div key={title} className="bg-gray-900 border border-gray-800 rounded-xl p-4 text-center">
            <div className="text-2xl mb-2">{icon}</div>
            <p className="text-white text-sm font-medium">{title}</p>
            <p className="text-gray-500 text-xs mt-1">{desc}</p>
          </div>
        ))}
      </div>
    </div>
  );
};

export default UploadPage;
