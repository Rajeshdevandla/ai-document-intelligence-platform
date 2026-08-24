import '@testing-library/jest-dom';
import { render, screen } from '@testing-library/react';

import App from './App';


beforeEach(() => {
  localStorage.clear();
  window.history.pushState({}, '', '/');
});


test('redirects signed-out users to the login screen', async () => {
  render(<App />);

  expect(await screen.findByRole('heading', { name: 'Sign In' })).toBeInTheDocument();
  expect(screen.getByText('AI Document Intelligence Platform')).toBeInTheDocument();
  expect(screen.getByPlaceholderText('demo')).toBeRequired();
  expect(screen.getByPlaceholderText('demo123')).toBeRequired();
});
