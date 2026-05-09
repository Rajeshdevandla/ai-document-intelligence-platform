import React from 'react';
import { useQuery } from '@tanstack/react-query';
import {
  LineChart, Line, BarChart, Bar, PieChart, Pie, Cell,
  XAxis, YAxis, CartesianGrid, Tooltip, Legend, ResponsiveContainer
} from 'recharts';
import { getDashboardStats } from '../services/api';
import { DashboardStats } from '../types';

const COLORS = ['#3b82f6', '#8b5cf6', '#10b981', '#f59e0b', '#ef4444', '#06b6d4'];

const KpiCard: React.FC<{
  label: string; value: string | number; sub?: string; color?: string;
}> = ({ label, value, sub, color = 'blue' }) => {
  const colorMap: Record<string, string> = {
    blue: 'text-blue-400', green: 'text-green-400',
    purple: 'text-purple-400', yellow: 'text-yellow-400',
  };
  return (
    <div className="bg-gray-900 border border-gray-800 rounded-xl p-5">
      <p className="text-gray-400 text-sm mb-1">{label}</p>
      <p className={`text-3xl font-bold ${colorMap[color] || 'text-white'}`}>{value}</p>
      {sub && <p className="text-gray-600 text-xs mt-1">{sub}</p>}
    </div>
  );
};

// Mock data for demo when API isn't connected
const mockStats: DashboardStats = {
  totalDocuments: 2847,
  documentsToday: 143,
  avgProcessingTimeMs: 4200,
  avgConfidenceScore: 0.947,
  documentsWithAnomalies: 38,
  errorRate: 2.3,
  documentsByType: { invoice: 1240, receipt: 680, contract: 410, report: 317, letter: 200 },
  documentsByStatus: { EXTRACTED: 2650, PROCESSING: 120, FAILED: 77 },
  dailyTrend: [
    { date: '2026-05-02', count: 380, avgConfidence: 0.94 },
    { date: '2026-05-03', count: 420, avgConfidence: 0.945 },
    { date: '2026-05-04', count: 390, avgConfidence: 0.943 },
    { date: '2026-05-05', count: 460, avgConfidence: 0.948 },
    { date: '2026-05-06', count: 410, avgConfidence: 0.946 },
    { date: '2026-05-07', count: 444, avgConfidence: 0.95 },
    { date: '2026-05-08', count: 343, avgConfidence: 0.947 },
  ],
  llmModelStats: [
    { provider: 'GPT-4o', count: 1820, avgConfidence: 0.962, avgProcessingTimeMs: 3800 },
    { provider: 'Claude 3', count: 740, avgConfidence: 0.955, avgProcessingTimeMs: 4100 },
    { provider: 'Gemini', count: 287, avgConfidence: 0.941, avgProcessingTimeMs: 3600 },
  ],
};

const DashboardPage: React.FC = () => {
  const { data: stats, isLoading } = useQuery({
    queryKey: ['dashboardStats'],
    queryFn: getDashboardStats,
    refetchInterval: 30000,
    retry: false,
  });

  const s = stats || mockStats;

  const byTypeData = Object.entries(s.documentsByType).map(([name, value]) => ({
    name: name.charAt(0).toUpperCase() + name.slice(1), value,
  }));

  return (
    <div className="space-y-6">
      <div className="flex items-center justify-between">
        <div>
          <h1 className="text-2xl font-bold text-white">Analytics Dashboard</h1>
          <p className="text-gray-400 text-sm mt-1">Real-time document processing metrics</p>
        </div>
        {isLoading && (
          <span className="text-gray-500 text-sm animate-pulse">Refreshing…</span>
        )}
        {!stats && (
          <span className="bg-yellow-900/30 border border-yellow-700 text-yellow-400 text-xs px-3 py-1 rounded-full">
            Demo mode — connect API for live data
          </span>
        )}
      </div>

      {/* KPI Cards */}
      <div className="grid grid-cols-2 lg:grid-cols-4 gap-4">
        <KpiCard label="Total Documents" value={s.totalDocuments.toLocaleString()} color="blue" />
        <KpiCard label="Processed Today" value={s.documentsToday} color="green" />
        <KpiCard
          label="Avg Processing Time"
          value={`${(s.avgProcessingTimeMs / 1000).toFixed(1)}s`}
          color="purple"
        />
        <KpiCard
          label="Avg Confidence"
          value={`${(s.avgConfidenceScore * 100).toFixed(1)}%`}
          color="yellow"
        />
      </div>

      <div className="grid grid-cols-1 lg:grid-cols-3 gap-6">
        {/* 7-day trend */}
        <div className="lg:col-span-2 bg-gray-900 border border-gray-800 rounded-xl p-5">
          <h2 className="text-white font-semibold mb-4">7-Day Processing Trend</h2>
          <ResponsiveContainer width="100%" height={220}>
            <LineChart data={s.dailyTrend}>
              <CartesianGrid strokeDasharray="3 3" stroke="#374151" />
              <XAxis dataKey="date" tick={{ fill: '#9ca3af', fontSize: 11 }}
                tickFormatter={d => d.slice(5)} />
              <YAxis tick={{ fill: '#9ca3af', fontSize: 11 }} />
              <Tooltip
                contentStyle={{ background: '#111827', border: '1px solid #374151', borderRadius: 8 }}
                labelStyle={{ color: '#fff' }}
              />
              <Line type="monotone" dataKey="count" stroke="#3b82f6" strokeWidth={2}
                dot={{ r: 4, fill: '#3b82f6' }} name="Documents" />
            </LineChart>
          </ResponsiveContainer>
        </div>

        {/* Document type pie */}
        <div className="bg-gray-900 border border-gray-800 rounded-xl p-5">
          <h2 className="text-white font-semibold mb-4">Document Types</h2>
          <ResponsiveContainer width="100%" height={180}>
            <PieChart>
              <Pie data={byTypeData} cx="50%" cy="50%" outerRadius={70}
                dataKey="value" label={({ name, percent }) =>
                  `${name} ${(percent * 100).toFixed(0)}%`
                }
                labelLine={false}
              >
                {byTypeData.map((_, i) => (
                  <Cell key={i} fill={COLORS[i % COLORS.length]} />
                ))}
              </Pie>
              <Tooltip
                contentStyle={{ background: '#111827', border: '1px solid #374151', borderRadius: 8 }}
              />
            </PieChart>
          </ResponsiveContainer>
        </div>
      </div>

      {/* LLM Model Stats */}
      <div className="bg-gray-900 border border-gray-800 rounded-xl p-5">
        <h2 className="text-white font-semibold mb-4">LLM Provider Performance</h2>
        <ResponsiveContainer width="100%" height={200}>
          <BarChart data={s.llmModelStats}>
            <CartesianGrid strokeDasharray="3 3" stroke="#374151" />
            <XAxis dataKey="provider" tick={{ fill: '#9ca3af', fontSize: 12 }} />
            <YAxis tick={{ fill: '#9ca3af', fontSize: 12 }} />
            <Tooltip
              contentStyle={{ background: '#111827', border: '1px solid #374151', borderRadius: 8 }}
            />
            <Legend wrapperStyle={{ color: '#9ca3af', fontSize: 12 }} />
            <Bar dataKey="count" fill="#3b82f6" name="Documents Processed" radius={[4, 4, 0, 0]} />
          </BarChart>
        </ResponsiveContainer>
      </div>

      {/* Stats footer */}
      <div className="grid grid-cols-3 gap-4">
        <div className="bg-gray-900 border border-gray-800 rounded-xl p-4 text-center">
          <p className="text-2xl font-bold text-red-400">{s.errorRate}%</p>
          <p className="text-gray-400 text-sm">Error Rate</p>
        </div>
        <div className="bg-gray-900 border border-gray-800 rounded-xl p-4 text-center">
          <p className="text-2xl font-bold text-yellow-400">{s.documentsWithAnomalies}</p>
          <p className="text-gray-400 text-sm">Anomalies Detected</p>
        </div>
        <div className="bg-gray-900 border border-gray-800 rounded-xl p-4 text-center">
          <p className="text-2xl font-bold text-green-400">
            {s.documentsByStatus['EXTRACTED']?.toLocaleString() || 0}
          </p>
          <p className="text-gray-400 text-sm">Successfully Extracted</p>
        </div>
      </div>
    </div>
  );
};

export default DashboardPage;
