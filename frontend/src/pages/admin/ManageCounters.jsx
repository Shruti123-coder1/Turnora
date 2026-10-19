import { useCallback, useEffect, useState } from 'react';
import { adminApi } from '../../api';
import { errorMessage } from '../../api/axiosConfig';
import ConfirmModal, { Modal } from '../../components/ConfirmModal';
import Loader from '../../components/Loader';
import { COUNTER_STATUSES } from '../../utils/constants';
import { labelOf } from '../../utils/formatTime';

const EMPTY = { name: '', status: 'ACTIVE', staffId: '', serviceIds: [] };
const uniqueById = (items) => Array.from(new Map(items.map((item) => [item.id, item])).values());

export default function ManageCounters() {
  const [counters, setCounters] = useState(null);
  const [offices, setOffices] = useState([]);
  const [staff, setStaff] = useState([]);
  const [editing, setEditing] = useState(null);
  const [form, setForm] = useState(EMPTY);
  const [toDelete, setToDelete] = useState(null);
  const [error, setError] = useState('');
  const [formError, setFormError] = useState('');
  const [busy, setBusy] = useState(false);

  const load = useCallback(() => {
    adminApi.counters().then((items) => setCounters(uniqueById(items))).catch((e) => setError(errorMessage(e)));
  }, []);

  useEffect(() => {
    load();
    adminApi.services().then(setOffices).catch(() => {});
    adminApi.users().then(setStaff).catch(() => {});
  }, [load]);

  const openForm = (c) => {
    setFormError('');
    setForm(c ? { name: c.name, status: c.status, staffId: c.staffId ?? '', serviceIds: c.serviceIds || [] } : EMPTY);
    setEditing(c || {});
  };

  const toggleOffice = (id) =>
    setForm((f) => ({ ...f, serviceIds: f.serviceIds.includes(id) ? f.serviceIds.filter((x) => x !== id) : [...f.serviceIds, id] }));

  const save = async (e) => {
    e.preventDefault();
    setBusy(true);
    setFormError('');
    const body = { name: form.name.trim(), status: form.status, staffId: form.staffId === '' ? null : Number(form.staffId), serviceIds: form.serviceIds };
    try {
      if (editing.id) await adminApi.updateCounter(editing.id, body);
      else await adminApi.createCounter(body);
      setEditing(null);
      load();
    } catch (err) {
      setFormError(errorMessage(err));
    } finally {
      setBusy(false);
    }
  };

  const remove = async () => {
    setBusy(true);
    try {
      await adminApi.deleteCounter(toDelete.id);
      load();
    } catch (err) {
      setError(errorMessage(err));
    } finally {
      setToDelete(null);
      setBusy(false);
    }
  };

  return (
    <div className="space-y-6">
      <div className="flex items-center justify-between gap-3">
        <h1 className="page-heading">Counters</h1>
        <button type="button" className="btn btn-primary" onClick={() => openForm(null)}>Add counter</button>
      </div>
      {error && <p role="alert" className="text-sm text-alert">{error}</p>}
      {!counters && !error && <Loader />}

      {counters && (
        <div className="table-shell">
          <table className="w-full min-w-[680px]">
            <thead className="border-b border-line"><tr><th className="th">Counter</th><th className="th">Status</th><th className="th">Staff</th><th className="th">Offices served</th><th className="th">Now</th><th className="th" /></tr></thead>
            <tbody className="divide-y divide-line">
              {counters.map((c) => (
                <tr key={c.id}>
                  <td className="td font-medium">{c.name}</td>
                  <td className="td">{labelOf(c.status)}</td>
                  <td className="td">{c.staffName || <span className="text-muted">Not assigned</span>}</td>
                  <td className="td text-sm">{(c.serviceNames || []).join(', ') || '-'}</td>
                  <td className="td font-display font-bold tabular-nums">{c.currentToken || '-'}</td>
                  <td className="td space-x-2 text-right">
                    <button type="button" className="btn btn-ghost" onClick={() => openForm(c)}>Edit</button>
                    <button type="button" className="btn btn-ghost" onClick={() => setToDelete(c)}>Remove</button>
                  </td>
                </tr>
              ))}
            </tbody>
          </table>
        </div>
      )}

      <Modal open={Boolean(editing)} title={editing?.id ? 'Edit counter' : 'Add counter'} onClose={() => setEditing(null)}>
        <form onSubmit={save} className="space-y-4">
          <div><label className="label" htmlFor="cname">Counter name</label><input id="cname" className="input" value={form.name} onChange={(e) => setForm({ ...form, name: e.target.value })} required /></div>
          <div className="grid gap-4 sm:grid-cols-2">
            <div>
              <label className="label" htmlFor="cstat">Status</label>
              <select id="cstat" className="input" value={form.status} onChange={(e) => setForm({ ...form, status: e.target.value })}>
                {COUNTER_STATUSES.map((s) => <option key={s} value={s}>{labelOf(s)}</option>)}
              </select>
            </div>
            <div>
              <label className="label" htmlFor="cstaff">Staff member</label>
              <select id="cstaff" className="input" value={form.staffId} onChange={(e) => setForm({ ...form, staffId: e.target.value })}>
                <option value="">Not assigned</option>
                {staff.filter((u) => u.active).map((u) => <option key={u.id} value={u.id}>{u.fullName}</option>)}
              </select>
            </div>
          </div>
          <fieldset>
            <legend className="label">Offices this counter serves</legend>
            <div className="grid gap-2 sm:grid-cols-2">
              {offices.map((o) => (
                <label key={o.id} className="flex items-center gap-2 text-sm">
                  <input type="checkbox" checked={form.serviceIds.includes(o.id)} onChange={() => toggleOffice(o.id)} /> {o.name}
                </label>
              ))}
            </div>
          </fieldset>
          {formError && <p role="alert" className="text-sm text-alert">{formError}</p>}
          <div className="flex justify-end gap-2">
            <button type="button" className="btn btn-ghost" onClick={() => setEditing(null)}>Cancel</button>
            <button className="btn btn-primary" disabled={busy}>Save counter</button>
          </div>
        </form>
      </Modal>

      <ConfirmModal open={Boolean(toDelete)} title="Remove this counter?" message={toDelete ? `${toDelete.name} will be deleted. A counter that has served tokens cannot be removed.` : ''} confirmText="Remove counter" danger busy={busy} onConfirm={remove} onCancel={() => setToDelete(null)} />
    </div>
  );
}
