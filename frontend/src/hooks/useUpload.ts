import { useState, useCallback } from 'react';
import { requestUploadUrl, uploadFileToS3 } from '../services/api';
import { UploadState } from '../types';

export const useUpload = () => {
  const [state, setState] = useState<UploadState>({
    status: 'idle',
    progress: 0,
  });

  const reset = useCallback(() => {
    setState({ status: 'idle', progress: 0 });
  }, []);

  const uploadFile = useCallback(async (file: File) => {
    try {
      // Step 1: Request presigned URL
      setState({ status: 'requesting-url', progress: 0 });
      const uploadResponse = await requestUploadUrl(file.name, file.type, file.size);

      // Step 2: Upload to S3
      setState({ status: 'uploading', progress: 0, documentId: uploadResponse.documentId });
      await uploadFileToS3(uploadResponse.presignedUrl, file, (progress) => {
        setState(prev => ({ ...prev, progress }));
      });

      // Step 3: Complete
      setState({
        status: 'complete',
        progress: 100,
        documentId: uploadResponse.documentId,
      });

      return uploadResponse.documentId;
    } catch (error: any) {
      setState({
        status: 'error',
        progress: 0,
        errorMessage: error.message || 'Upload failed. Please try again.',
      });
      throw error;
    }
  }, []);

  return { state, uploadFile, reset };
};
