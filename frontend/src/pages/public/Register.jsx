import { useState } from 'react';
import { Link, useNavigate } from 'react-router-dom';
import { errorMessage } from '../../api/axiosConfig';
import { useAuth } from '../../context/AuthContext';

const FIELDS = [
  { name: 'fullName', label: 'Full name', type: 'text', auto: 'name' },
  { name: 'email', label: 'Email', type: 'email', auto: 'email' },
  { name: 'prn', label: 'PRN', type: 'text', auto: 'off' },
  { name: 'phone', label: 'Phone number (10 digits)', type: 'tel', auto: 'tel' },
  { name: 'password', label: 'Password (6 or more characters)', type: 'password', auto: 'new-password' },
];

export default function Register() {
  const { register } = useAuth();
  const navigate = useNavigate();
  const [form, setForm] = useState({ fullName: '', email: '', prn: '', phone: '', password: '' });
  const [error, setError] = useState('');
  const [busy, setBusy] = useState(false);

  const change = (e) => setForm({ ...form, [e.target.name]: e.target.value });

  const submit = async (e) => {
    e.preventDefault();
    setBusy(true);
    setError('');
    try {
      await register({ ...form, fullName: form.fullName.trim(), email: form.email.trim(), prn: form.prn.trim(), phone: form.phone.trim() });
      navigate('/student', { replace: true });
    } catch (err) {
      setError(errorMessage(err, 'Could not create the account.'));
    } finally {
      setBusy(false);
    }
  };

  return (
    <div className="mx-auto max-w-md">
      <h1 className="page-heading">Create your account</h1>
      <p className="page-subtitle">Get a simpler way to manage your campus visits.</p>
      <form onSubmit={submit} className="card mt-6 space-y-4 p-5 sm:p-6">
        {FIELDS.map((f) => (
          <div key={f.name}>
            <label className="label" htmlFor={f.name}>{f.label}</label>
            <input id={f.name} name={f.name} type={f.type} className="input" value={form[f.name]} onChange={change} autoComplete={f.auto} required />
          </div>
        ))}
        {error && <p role="alert" className="text-sm text-alert">{error}</p>}
        <button className="btn btn-primary w-full" disabled={busy}>{busy ? 'Creating account' : 'Create account'}</button>
        <p className="text-center text-sm text-muted">
          Already registered? <Link to="/login" className="font-medium text-teal underline">Log in</Link>
        </p>
      </form>
    </div>
  );
}
