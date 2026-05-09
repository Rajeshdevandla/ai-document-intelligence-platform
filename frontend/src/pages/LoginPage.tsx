import React, { useState } from 'react';
import { useNavigate } from 'react-router-dom';

const LoginPage: React.FC = () => {
  const navigate = useNavigate();
  const [credentials, setCredentials] = useState({ username: '', password: '' });
  const [error, setError] = useState('');
  const [loading, setLoading] = useState(false);

  const handleLogin = async (e: React.FormEvent) => {
    e.preventDefault();
    setLoading(true);
    setError('');

    // Demo login — replace with real auth API call
    await new Promise(r => setTimeout(r, 800));

    if (credentials.username === 'demo' && credentials.password === 'demo123') {
      const fakeJwt = 'eyJhbGciOiJIUzI1NiJ9.demo.signature';
      localStorage.setItem('jwt_token', fakeJwt);
      navigate('/upload');
    } else {
      setError('Invalid credentials. Use demo / demo123');
      setLoading(false);
    }
  };

  return (
    <div className="min-h-screen bg-gray-950 flex items-center justify-center px-4">
      <div className="w-full max-w-md">
        <div className="text-center mb-8">
          <div className="w-16 h-16 bg-blue-600 rounded-2xl flex items-center justify-center font-bold text-white text-2xl mx-auto mb-4">
            AI
          </div>
          <h1 className="text-3xl font-bold text-white">DocIQ</h1>
          <p className="text-gray-400 mt-1">AI Document Intelligence Platform</p>
        </div>

        <div className="bg-gray-900 rounded-2xl p-8 border border-gray-800">
          <h2 className="text-xl font-semibold text-white mb-6">Sign In</h2>
          {error && (
            <div className="bg-red-900/30 border border-red-700 rounded-lg p-3 mb-4 text-red-400 text-sm">
              {error}
            </div>
          )}
          <form onSubmit={handleLogin} className="space-y-4">
            <div>
              <label className="block text-sm font-medium text-gray-300 mb-1">Username</label>
              <input
                type="text"
                value={credentials.username}
                onChange={e => setCredentials(p => ({ ...p, username: e.target.value }))}
                placeholder="demo"
                className="w-full bg-gray-800 border border-gray-700 rounded-lg px-4 py-2.5 text-white placeholder-gray-500 focus:outline-none focus:ring-2 focus:ring-blue-500"
                required
              />
            </div>
            <div>
              <label className="block text-sm font-medium text-gray-300 mb-1">Password</label>
              <input
                type="password"
                value={credentials.password}
                onChange={e => setCredentials(p => ({ ...p, password: e.target.value }))}
                placeholder="demo123"
                className="w-full bg-gray-800 border border-gray-700 rounded-lg px-4 py-2.5 text-white placeholder-gray-500 focus:outline-none focus:ring-2 focus:ring-blue-500"
                required
              />
            </div>
            <button
              type="submit"
              disabled={loading}
              className="w-full bg-blue-600 hover:bg-blue-500 disabled:opacity-60 text-white font-semibold py-2.5 rounded-lg transition-colors"
            >
              {loading ? 'Signing in…' : 'Sign In'}
            </button>
          </form>
          <p className="text-gray-500 text-xs text-center mt-4">Demo credentials: demo / demo123</p>
        </div>
      </div>
    </div>
  );
};

export default LoginPage;
