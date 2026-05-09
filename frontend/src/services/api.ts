import axios, { AxiosInstance } from 'axios';
import { DashboardStats, ExtractionResult, UploadResponse } from '../types';

const uploadServiceClient: AxiosInstance = axios.create({
  baseURL: process.env.REACT_APP_UPLOAD_SERVICE_URL || 'http://localhost:8081',
  timeout: 30000,
});

const analyticsServiceClient: AxiosInstance = axios.create({
  baseURL: process.env.REACT_APP_ANALYTICS_SERVICE_URL || 'http://localhost:8084',
  timeout: 15000,
});

const extractionServiceClient: AxiosInstance = axios.create({
  baseURL: process.env.REACT_APP_EXTRACTION_SERVICE_URL || 'http://localhost:8083',
  timeout: 60000,
});

// JWT interceptor
const addAuthInterceptor = (client: AxiosInstance) => {
  client.interceptors.request.use((config) => {
    const token = localStorage.getItem('jwt_token');
    if (token) {
      config.headers.Authorization = `Bearer ${token}`;
    }
    return config;
  });
};

[uploadServiceClient, analyticsServiceClient, extractionServiceClient].forEach(addAuthInterceptor);

// Upload Service APIs
export const requestUploadUrl = async (
  fileName: string,
  contentType: string,
  fileSize: number
): Promise<UploadResponse> => {
  const response = await uploadServiceClient.post('/api/v1/documents/upload-url', {
    fileName,
    contentType,
    fileSize,
  });
  return response.data;
};

export const uploadFileToS3 = async (
  presignedUrl: string,
  file: File,
  onProgress?: (progress: number) => void
): Promise<void> => {
  await axios.put(presignedUrl, file, {
    headers: { 'Content-Type': file.type },
    onUploadProgress: (event) => {
      if (event.total && onProgress) {
        onProgress(Math.round((event.loaded / event.total) * 100));
      }
    },
  });
};

export const getDocumentStatus = async (documentId: string) => {
  const response = await uploadServiceClient.get(
    `/api/v1/documents/${documentId}/status`
  );
  return response.data;
};

// Analytics Service APIs
export const getDashboardStats = async (): Promise<DashboardStats> => {
  const response = await analyticsServiceClient.get('/api/v1/analytics/dashboard');
  return response.data;
};

// Extraction Service APIs
export const extractDocument = async (
  documentId: string,
  cleanedText: string,
  llmProvider: string = 'openai'
): Promise<ExtractionResult> => {
  const response = await extractionServiceClient.post('/api/v1/extract', {
    documentId,
    cleanedText,
    llmProvider,
  });
  return response.data;
};
