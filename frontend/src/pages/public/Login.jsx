import { useState } from 'react';
import { Link, useLocation, useNavigate } from 'react-router-dom';
import { errorMessage } from '../../api/axiosConfig';
import { useAuth } from '../../context/AuthContext';
import { ROLE_HOME } from '../../utils/constants';

export default function Login() {
  const { login } = useAuth();
  const navigate = useNavigate();
  const location = useLocation();
  const [form, setForm] = useState({ email: '', password: '' });
  const [error, setError] = useState('');
  const [busy, setBusy] = useState(false);

  const change = (e) => setForm({ ...form, [e.target.name]: e.target.value });

  const submit = async (e) => {
    e.preventDefault();
    setBusy(true);
    setError('');
    try {
      const user = await login(form.email.trim(), form.password);
      navigate(location.state?.from || ROLE_HOME[user.role] || '/', { replace: true });
    } catch (err) {
      setError(errorMessage(err, 'Login failed. Check your email and password.'));
    } finally {
      setBusy(false);
    }
  };

  return (
    <div className="mx-auto max-w-md">
      <h1 className="text-3xl font-bold">Log in</h1>
      <form onSubmit={submit} className="card mt-6 space-y-4 p-6">
        <div>
          <label className="label" htmlFor="email">Email</label>
          <input id="email" name="email" type="email" className="input" value={form.email} onChange={change} autoComplete="email" required />
        </div>
        <div>
          <label className="label" htmlFor="password">Password</label>
          <input id="password" name="password" type="password" className="input" value={form.password} onChange={change} autoComplete="current-password" required />
        </div>
        {error && <p role="alert" className="text-sm text-alert">{error}</p>}
        <button className="btn btn-primary w-full" disabled={busy}>{busy ? 'Logging in' : 'Log in'}</button>
        <p className="text-center text-sm text-muted">
          New student? <Link to="/register" className="font-medium text-teal underline">Create an account</Link>
        </p>
      </form>
    </div>
  );
}