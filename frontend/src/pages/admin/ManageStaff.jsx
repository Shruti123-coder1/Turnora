import { useCallback, useEffect, useState } from 'react';
import { adminApi } from '../../api';
import { errorMessage } from '../../api/axiosConfig';
import { Modal } from '../../components/ConfirmModal';
import Loader from '../../components/Loader';

const EMPTY = { fullName: '', email: '', phone: '', password: '' };

export default function ManageStaff() {
  const [list, setList] = useState(null);
  const [open, setOpen] = useState(false);
  const [form, setForm] = useState(EMPTY);
  const [error, setError] = useState('');
  const [formError, setFormError] = useState('');
  const [busy, setBusy] = useState(false);

  const load = useCallback(() => {
    adminApi.users().then(setList).catch((e) => setError(errorMessage(e)));
  }, []);
  useEffect(load, [load]);

  const save = async (e) => {
    e.preventDefault();
    setBusy(true);
    setFormError('');
    try {
      await adminApi.createUser({ ...form, fullName: form.fullName.trim(), email: form.email.trim(), phone: form.phone.trim() });
      setOpen(false);
      setForm(EMPTY);
      load();
    } catch (err) {
      setFormError(errorMessage(err));
    } finally {
      setBusy(false);
    }
  };

  const toggle = async (u) => {
    try {
      await adminApi.setUserActive(u.id, !u.active);
      load();
    } catch (err) {
      setError(errorMessage(err));
    }
  };

  const field = (name, label, type = 'text') => (
    <div>
      <label className="label" htmlFor={`st-${name}`}>{label}</label>
      <input id={`st-${name}`} type={type} className="input" value={form[name]} onChange={(e) => setForm({ ...form, [name]: e.target.value })} required />
    </div>
  );

  return (
    <div className="space-y-6">
      <div className="flex items-center justify-between gap-3">
        <h1 className="text-3xl font-bold">Staff</h1>
        <button type="button" className="btn btn-primary" onClick={() => { setFormError(''); setOpen(true); }}>Add staff member</button>
      </div>
      {error && <p role="alert" className="text-sm text-alert">{error}</p>}
      {!list && !error && <Loader />}

      {list && (
        <div className="card overflow-x-auto">
          <table className="w-full min-w-[640px]">
            <thead className="border-b border-line"><tr><th className="th">Name</th><th className="th">Email</th><th className="th">Phone</th><th className="th">Counter</th><th className="th">Status</th><th className="th" /></tr></thead>
            <tbody className="divide-y divide-line">
              {list.map((u) => (
                <tr key={u.id}>
                  <td className="td font-medium">{u.fullName}</td><td className="td">{u.email}</td><td className="td">{u.phone}</td>
                  <td className="td">{u.counterName || <span className="text-muted">Not assigned</span>}</td>
                  <td className="td">{u.active ? 'Active' : 'Disabled'}</td>
                  <td className="td text-right"><button type="button" className="btn btn-ghost" onClick={() => toggle(u)}>{u.active ? 'Disable' : 'Enable'}</button></td>
                </tr>
              ))}
            </tbody>
          </table>
        </div>
      )}

      <Modal open={open} title="Add staff member" onClose={() => setOpen(false)}>
        <form onSubmit={save} className="space-y-4">
          {field('fullName', 'Full name')}
          {field('email', 'Email', 'email')}
          {field('phone', 'Phone (10 digits)', 'tel')}
          {field('password', 'Password (6 or more characters)', 'password')}
          {formError && <p role="alert" className="text-sm text-alert">{formError}</p>}
          <div className="flex justify-end gap-2">
            <button type="button" className="btn btn-ghost" onClick={() => setOpen(false)}>Cancel</button>
            <button className="btn btn-primary" disabled={busy}>Create staff account</button>
          </div>
        </form>
      </Modal>
    </div>
  );
}