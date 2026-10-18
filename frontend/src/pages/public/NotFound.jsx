import { Link } from 'react-router-dom';

export default function NotFound() {
  return (
    <div className="mx-auto max-w-md py-16 text-center">
      <p className="font-display text-6xl font-bold text-teal">404</p>
      <h1 className="mt-2 text-2xl font-bold">This page does not exist</h1>
      <p className="mt-2 text-muted">Check the address, or go back to the list of offices.</p>
      <Link to="/" className="btn btn-primary mt-6">Go to home</Link>
    </div>
  );
}