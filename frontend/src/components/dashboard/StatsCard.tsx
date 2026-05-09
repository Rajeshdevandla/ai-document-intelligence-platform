import React from "react";

interface Props {
  label: string;
  value: string | number;
  icon: string;
  color?: "blue" | "green" | "red" | "yellow" | "purple";
  subtitle?: string;
}

const colorMap = {
  blue:   "bg-blue-50 border-blue-200 text-blue-700",
  green:  "bg-green-50 border-green-200 text-green-700",
  red:    "bg-red-50 border-red-200 text-red-700",
  yellow: "bg-yellow-50 border-yellow-200 text-yellow-700",
  purple: "bg-purple-50 border-purple-200 text-purple-700",
};

const StatsCard: React.FC<Props> = ({ label, value, icon, color = "blue", subtitle }) => (
  <div className={`p-6 rounded-xl border ${colorMap[color]} shadow-sm`}>
    <div className="flex items-center justify-between mb-2">
      <span className="text-sm font-medium opacity-80">{label}</span>
      <span className="text-2xl">{icon}</span>
    </div>
    <div className="text-3xl font-bold">{value}</div>
    {subtitle && <div className="text-xs opacity-70 mt-1">{subtitle}</div>}
  </div>
);

export default StatsCard;
