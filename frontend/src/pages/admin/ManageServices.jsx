import { useCallback, useEffect, useState } from 'react';
import { adminApi } from '../../api';
import { errorMessage } from '../../api/axiosConfig';
import { Modal } from '../../components/ConfirmModal';
import Loader from '../../components/Loader';

const EMPTY = { name: '', prefix: '', description: '', avgServiceMinutes: 5, active: true };

export default function ManageServices() {
  const [list, setList] = useState(null);
  const [editing, setEditing] = useState(null); // null = closed, {} = form
  const [form, setForm] = useState(EMPTY);
  const [error, setError] = useState('');
  const [formError, setFormError] = useState('');
  const [busy, setBusy] = useState(false);

  const load = useCallback(() => {
    adminApi.services().then(setList).catch((e) => setError(errorMessage(e)));
  }, []);
  useEffect(load, [load]);

  const openForm = (service) => {
    setFormError('');
    setForm(service ? { name: service.name, prefix: service.prefix, description: service.description || '', avgServiceMinutes: service.avgServiceMinutes, active: service.active } : EMPTY);
    setEditing(service || {});
  };

  const save = async (e) => {
    e.preventDefault();
    setBusy(true);
    setFormError('');
    const body = { ...form, prefix: form.prefix.trim().toUpperCase(), name: form.name.trim(), avgServiceMinutes: Number(form.avgServiceMinutes) };
    try {
      if (editing.id) await adminApi.updateService(editing.id, body);
      else await adminApi.createService(body);
      setEditing(null);
      load();
    } catch (err) {
      setFormError(errorMessage(err));
    } finally {
      setBusy(false);
    }
  };

  const toggle = async (s) => {
    try {
      await adminApi.updateService(s.id, { name: s.name, prefix: s.prefix, description: s.description, avgServiceMinutes: s.avgServiceMinutes, active: !s.active });
      load();
    } catch (err) {
      setError(errorMessage(err));
    }
  };

  return (
    <div className="space-y-6">
      <div className="flex items-center justify-between gap-3">
        <h1 className="page-heading">Offices</h1>
        <button type="button" className="btn btn-primary" onClick={() => openForm(null)}>Add office</button>
      </div>
      {error && <p role="alert" className="text-sm text-alert">{error}</p>}
      {!list && !error && <Loader />}

      {list && (
        <div className="table-shell">
          <table className="w-full min-w-[640px]">
            <thead className="border-b border-line"><tr><th className="th">Prefix</th><th className="th">Office</th><th className="th">Avg service</th><th className="th">Waiting</th><th className="th">Status</th><th className="th" /></tr></thead>
            <tbody className="divide-y divide-line">
              {list.map((s) => (
                <tr key={s.id}>
                  <td className="td font-display text-lg font-bold">{s.prefix}</td>
                  <td className="td"><p className="font-medium">{s.name}</p><p className="text-xs text-muted">{s.description}</p></td>
                  <td className="td">{s.avgServiceMinutes} min</td>
                  <td className="td tabular-nums">{s.waitingCount}</td>
                  <td className="td">{s.active ? 'Open' : 'Closed'}</td>
                  <td className="td space-x-2 text-right">
                    <button type="button" className="btn btn-ghost" onClick={() => openForm(s)}>Edit</button>
                    <button type="button" className="btn btn-ghost" onClick={() => toggle(s)}>{s.active ? 'Close' : 'Open'}</button>
                  </td>
                </tr>
              ))}
            </tbody>
          </table>
        </div>
      )}

      <Modal open={Boolean(editing)} title={editing?.id ? 'Edit office' : 'Add office'} onClose={() => setEditing(null)}>
        <form onSubmit={save} className="space-y-4">
          <div><label className="label" htmlFor="sname">Office name</label><input id="sname" className="input" value={form.name} onChange={(e) => setForm({ ...form, name: e.target.value })} required /></div>
          <div className="grid gap-4 sm:grid-cols-2">
            <div><label className="label" htmlFor="sprefix">Token prefix</label><input id="sprefix" className="input uppercase" maxLength={5} value={form.prefix} onChange={(e) => setForm({ ...form, prefix: e.target.value })} required /></div>
            <div><label className="label" htmlFor="savg">Avg minutes per person</label><input id="savg" type="number" min="1" max="120" step="0.5" className="input" value={form.avgServiceMinutes} onChange={(e) => setForm({ ...form, avgServiceMinutes: e.target.value })} required /></div>
          </div>
          <div><label className="label" htmlFor="sdesc">Description</label><input id="sdesc" className="input" value={form.description} onChange={(e) => setForm({ ...form, description: e.target.value })} /></div>
          <label className="flex items-center gap-2 text-sm"><input type="checkbox" checked={form.active} onChange={(e) => setForm({ ...form, active: e.target.checked })} /> Open for new tokens</label>
          {formError && <p role="alert" className="text-sm text-alert">{formError}</p>}
          <div className="flex justify-end gap-2">
            <button type="button" className="btn btn-ghost" onClick={() => setEditing(null)}>Cancel</button>
            <button className="btn btn-primary" disabled={busy}>Save office</button>
          </div>
        </form>
      </Modal>
    </div>
  );
}
