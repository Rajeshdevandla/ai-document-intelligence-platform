export interface Document {
  documentId: string;
  fileName: string;
  contentType: string;
  fileSize: number;
  s3Key: string;
  status: DocumentStatus;
  uploadedBy?: string;
  createdAt: string;
  updatedAt: string;
  processingNotes?: string;
}

export type DocumentStatus =
  | 'PENDING'
  | 'PROCESSING'
  | 'OCR_COMPLETE'
  | 'EXTRACTED'
  | 'FAILED';

export interface UploadResponse {
  documentId: string;
  presignedUrl: string;
  s3Key: string;
  expiresInSeconds: number;
  statusCheckUrl: string;
  message: string;
}

export interface ExtractionResult {
  documentId: string;
  documentType: string;
  entities: Entity[];
  dates: DateField[];
  totals: Record<string, number | string>;
  lineItems: LineItem[];
  summary: string;
  anomalies: string[];
  llmProvider: string;
  llmModel: string;
  confidence: number;
  tokensUsed: number;
  processingTimeMs: number;
  extractedAt: string;
}

export interface Entity {
  type: string;
  value: string;
  confidence: number;
}

export interface DateField {
  label: string;
  value: string;
  normalized: string;
}

export interface LineItem {
  description: string;
  quantity: number;
  unitPrice: number;
  total: number;
}

export interface DashboardStats {
  totalDocuments: number;
  documentsToday: number;
  avgProcessingTimeMs: number;
  avgConfidenceScore: number;
  documentsWithAnomalies: number;
  errorRate: number;
  documentsByType: Record<string, number>;
  documentsByStatus: Record<string, number>;
  dailyTrend: TrendPoint[];
  llmModelStats: LlmModelStat[];
}

export interface TrendPoint {
  date: string;
  count: number;
  avgConfidence: number;
}

export interface LlmModelStat {
  provider: string;
  count: number;
  avgConfidence: number;
  avgProcessingTimeMs: number;
}

export interface UploadState {
  status: 'idle' | 'requesting-url' | 'uploading' | 'complete' | 'error';
  documentId?: string;
  progress: number;
  errorMessage?: string;
}
