import React from 'react';
import { Outlet, NavLink, useNavigate } from 'react-router-dom';

const Layout: React.FC = () => {
  const navigate = useNavigate();

  const logout = () => {
    localStorage.removeItem('jwt_token');
    navigate('/login');
  };

  const navClass = ({ isActive }: { isActive: boolean }) =>
    `px-4 py-2 rounded-md text-sm font-medium transition-colors ${
      isActive
        ? 'bg-blue-600 text-white'
        : 'text-gray-300 hover:bg-gray-700 hover:text-white'
    }`;

  return (
    <div className="min-h-screen bg-gray-950 text-gray-100 flex flex-col">
      <nav className="bg-gray-900 border-b border-gray-800 px-6 py-3">
        <div className="max-w-7xl mx-auto flex items-center justify-between">
          <div className="flex items-center gap-3">
            <div className="w-8 h-8 bg-blue-600 rounded-lg flex items-center justify-center font-bold text-white text-sm">
              AI
            </div>
            <span className="font-bold text-lg text-white">DocIQ</span>
            <span className="text-gray-500 text-xs ml-1">Document Intelligence</span>
          </div>
          <div className="flex items-center gap-2">
            <NavLink to="/upload"    className={navClass}>Upload</NavLink>
            <NavLink to="/dashboard" className={navClass}>Dashboard</NavLink>
            <button
              onClick={logout}
              className="ml-4 px-3 py-1.5 text-sm text-gray-400 hover:text-red-400 transition-colors"
            >
              Logout
            </button>
          </div>
        </div>
      </nav>

      <main className="flex-1 max-w-7xl mx-auto w-full px-6 py-8">
        <Outlet />
      </main>

      <footer className="border-t border-gray-800 py-4 text-center text-gray-600 text-sm">
        DocIQ — AI Document Intelligence Platform &nbsp;|&nbsp; Built by{' '}
        <a
          href="https://rajeshdevandla.github.io"
          className="text-blue-500 hover:text-blue-400"
          target="_blank"
          rel="noopener noreferrer"
        >
          Rajesh Kumar
        </a>
      </footer>
    </div>
  );
};

export default Layout;
