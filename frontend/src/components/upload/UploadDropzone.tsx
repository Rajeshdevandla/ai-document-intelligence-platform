import React, { useCallback } from "react";
import { useDropzone } from "react-dropzone";

interface Props {
  onFileSelected: (file: File) => void;
  disabled?: boolean;
}

const ACCEPTED_TYPES = {
  "application/pdf": [".pdf"],
  "image/png": [".png"],
  "image/jpeg": [".jpg", ".jpeg"],
};

const MAX_SIZE = 50 * 1024 * 1024; // 50 MB

const UploadDropzone: React.FC<Props> = ({ onFileSelected, disabled }) => {
  const onDrop = useCallback(
    (acceptedFiles: File[]) => {
      if (acceptedFiles.length > 0) {
        onFileSelected(acceptedFiles[0]);
      }
    },
    [onFileSelected]
  );

  const { getRootProps, getInputProps, isDragActive, isDragReject } = useDropzone({
    onDrop,
    accept: ACCEPTED_TYPES,
    maxSize: MAX_SIZE,
    multiple: false,
    disabled,
  });

  const borderColor = isDragReject
    ? "border-red-400 bg-red-50"
    : isDragActive
    ? "border-blue-400 bg-blue-50"
    : "border-gray-300 hover:border-blue-400 hover:bg-blue-50";

  return (
    <div
      {...getRootProps()}
      className={`flex flex-col items-center justify-center w-full h-64 border-2 border-dashed rounded-xl cursor-pointer transition-all duration-200 ${borderColor} ${
        disabled ? "opacity-50 cursor-not-allowed" : ""
      }`}
    >
      <input {...getInputProps()} />
      <div className="text-6xl mb-4">
        {isDragReject ? "❌" : isDragActive ? "📥" : "📄"}
      </div>
      <p className="text-xl font-semibold text-gray-700">
        {isDragActive ? "Drop your document here" : "Drag & drop your document"}
      </p>
      <p className="text-sm text-gray-500 mt-2">
        PDF, PNG, JPG supported — max 50 MB
      </p>
      <button
        type="button"
        className="mt-4 px-6 py-2 bg-blue-600 text-white rounded-lg hover:bg-blue-700 transition-colors font-medium"
      >
        Browse Files
      </button>
    </div>
  );
};

export default UploadDropzone;
