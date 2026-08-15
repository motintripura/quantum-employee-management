import { render, screen } from '@testing-library/react';
import App from './App';

test('renders the login page for unauthenticated users', () => {
  render(<App />);
  expect(screen.getByText(/sign in to your account/i)).toBeInTheDocument();
});