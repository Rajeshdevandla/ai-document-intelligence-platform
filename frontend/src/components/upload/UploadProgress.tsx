import React from "react";
import { UploadState } from "../../hooks/useUpload";

interface Props {
  state: UploadState;
  progress: number;
  fileName: string;
  documentId: string | null;
  error: string | null;
  onViewStatus: () => void;
  onReset: () => void;
}

const UploadProgress: React.FC<Props> = ({
  state, progress, fileName, documentId, error, onViewStatus, onReset
}) => {
  const statusConfig = {
    idle: { icon: "", label: "" },
    "requesting-url": { icon: "🔗", label: "Preparing secure upload link…" },
    uploading: { icon: "⬆️", label: `Uploading ${fileName}…` },
    complete: { icon: "✅", label: "Upload complete! Processing started." },
    error: { icon: "❌", label: error || "Upload failed" },
  };

  const { icon, label } = statusConfig[state];

  return (
    <div className="mt-6 p-6 bg-white rounded-xl shadow-sm border border-gray-200">
      <div className="flex items-center gap-3 mb-4">
        <span className="text-2xl">{icon}</span>
        <span className="text-gray-700 font-medium">{label}</span>
      </div>

      {state === "uploading" && (
        <div className="w-full bg-gray-200 rounded-full h-2.5">
          <div
            className="bg-blue-600 h-2.5 rounded-full transition-all duration-300"
            style={{ width: `${progress}%` }}
          />
        </div>
      )}

      {state === "complete" && documentId && (
        <div className="mt-4 p-3 bg-green-50 rounded-lg">
          <p className="text-sm text-green-700">
            Document ID: <span className="font-mono font-semibold">{documentId}</span>
          </p>
          <div className="flex gap-3 mt-3">
            <button
              onClick={onViewStatus}
              className="px-4 py-2 bg-blue-600 text-white rounded-lg hover:bg-blue-700 transition-colors text-sm font-medium"
            >
              Track Processing →
            </button>
            <button
              onClick={onReset}
              className="px-4 py-2 border border-gray-300 rounded-lg hover:bg-gray-50 transition-colors text-sm"
            >
              Upload Another
            </button>
          </div>
        </div>
      )}

      {state === "error" && (
        <button
          onClick={onReset}
          className="mt-3 px-4 py-2 bg-red-600 text-white rounded-lg hover:bg-red-700 transition-colors text-sm"
        >
          Try Again
        </button>
      )}
    </div>
  );
};

export default UploadProgress;
