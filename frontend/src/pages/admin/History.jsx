import { useCallback, useEffect, useState } from 'react';
import { adminApi } from '../../api';
import { errorMessage } from '../../api/axiosConfig';
import { Modal } from '../../components/ConfirmModal';
import Loader from '../../components/Loader';
import StatusBadge from '../../components/StatusBadge';
import { STATUS_META } from '../../utils/constants';
import { formatDateTime, formatTime, formatWait, labelOf, todayISO } from '../../utils/formatTime';

export default function History() {
  const [filters, setFilters] = useState({ from: todayISO(), to: todayISO(), serviceId: '', status: '', q: '' });
  const [applied, setApplied] = useState(filters);
  const [page, setPage] = useState(0);
  const [data, setData] = useState(null);
  const [offices, setOffices] = useState([]);
  const [events, setEvents] = useState(null); // { row, list }
  const [error, setError] = useState('');

  useEffect(() => { adminApi.services().then(setOffices).catch(() => {}); }, []);

  const load = useCallback(() => {
    const params = { page, size: 20 };
    Object.entries(applied).forEach(([k, v]) => { if (v) params[k] = v; });
    setError('');
    adminApi.history(params).then(setData).catch((e) => { setData(null); setError(errorMessage(e)); });
  }, [applied, page]);
  useEffect(load, [load]);

  const search = (e) => { e.preventDefault(); setPage(0); setApplied(filters); };
  const set = (name) => (e) => setFilters({ ...filters, [name]: e.target.value });

  const showEvents = async (row) => {
    setEvents({ row, list: null });
    try {
      setEvents({ row, list: await adminApi.events(row.id) });
    } catch (e) {
      setEvents(null);
      setError(errorMessage(e));
    }
  };

  return (
    <div className="space-y-6">
      <h1 className="page-heading">Queue history</h1>

      <form onSubmit={search} className="card grid gap-4 p-4 sm:grid-cols-2 lg:grid-cols-6">
        <div><label className="label" htmlFor="hf">From</label><input id="hf" type="date" className="input" value={filters.from} onChange={set('from')} /></div>
        <div><label className="label" htmlFor="ht">To</label><input id="ht" type="date" className="input" value={filters.to} onChange={set('to')} /></div>
        <div>
          <label className="label" htmlFor="hs">Office</label>
          <select id="hs" className="input" value={filters.serviceId} onChange={set('serviceId')}>
            <option value="">All offices</option>
            {offices.map((o) => <option key={o.id} value={o.id}>{o.name}</option>)}
          </select>
        </div>
        <div>
          <label className="label" htmlFor="hst">Status</label>
          <select id="hst" className="input" value={filters.status} onChange={set('status')}>
            <option value="">All statuses</option>
            {Object.entries(STATUS_META).map(([v, m]) => <option key={v} value={v}>{m.label}</option>)}
          </select>
        </div>
        <div><label className="label" htmlFor="hq">Token, name or phone</label><input id="hq" className="input" value={filters.q} onChange={set('q')} /></div>
        <div className="flex items-end"><button className="btn btn-primary w-full">Search</button></div>
      </form>

      {error && <p role="alert" className="text-sm text-alert">{error}</p>}
      {!data && !error && <Loader />}

      {data && (
        <>
          <p className="text-sm text-muted">{data.totalElements} tokens found</p>
          <div className="table-shell">
            <table className="w-full min-w-[820px]">
              <thead className="border-b border-line">
                <tr><th className="th">Token</th><th className="th">Office</th><th className="th">Customer</th><th className="th">Status</th><th className="th">Counter</th><th className="th">Joined</th><th className="th">Wait</th><th className="th">Service</th></tr>
              </thead>
              <tbody className="divide-y divide-line">
                {data.content.map((r) => (
                  <tr key={r.id} className="cursor-pointer hover:bg-teal-soft/40" onClick={() => showEvents(r)}>
                    <td className="td font-display text-lg font-bold tabular-nums">{r.tokenNumber}</td>
                    <td className="td">{r.serviceName}</td>
                    <td className="td">{r.customerName}<span className="block text-xs text-muted">{r.phone}</span></td>
                    <td className="td"><StatusBadge status={r.status} /></td>
                    <td className="td">{r.counterName || '-'}</td>
                    <td className="td">{formatDateTime(r.createdAt)}</td>
                    <td className="td">{formatWait(r.waitMinutes)}</td>
                    <td className="td">{formatWait(r.serviceMinutes)}</td>
                  </tr>
                ))}
                {data.content.length === 0 && <tr><td className="td text-muted" colSpan={8}>No tokens match these filters.</td></tr>}
              </tbody>
            </table>
          </div>
          <div className="flex items-center justify-between">
            <button type="button" className="btn btn-ghost" disabled={page === 0} onClick={() => setPage(page - 1)}>Previous</button>
            <span className="text-sm text-muted">Page {data.totalPages === 0 ? 0 : data.page + 1} of {data.totalPages}</span>
            <button type="button" className="btn btn-ghost" disabled={page + 1 >= data.totalPages} onClick={() => setPage(page + 1)}>Next</button>
          </div>
        </>
      )}

      <Modal open={Boolean(events)} title={events ? `Token ${events.row.tokenNumber} timeline` : ''} onClose={() => setEvents(null)}>
        {events && !events.list && <Loader />}
        {events?.list && (
          <ol className="divide-y divide-line">
            {events.list.map((ev) => (
              <li key={ev.id} className="flex items-start justify-between gap-4 py-2 text-sm">
                <div>
                  <p className="font-medium">{labelOf(ev.eventType)}</p>
                  <p className="text-muted">
                    {ev.fromStatus ? `${labelOf(ev.fromStatus)} to ` : ''}{labelOf(ev.toStatus)}
                    {ev.counterName ? ` at ${ev.counterName}` : ''}{ev.actorName ? ` by ${ev.actorName}` : ''}
                  </p>
                </div>
                <span className="shrink-0 text-muted">{formatTime(ev.createdAt)}</span>
              </li>
            ))}
          </ol>
        )}
        <div className="mt-4 flex justify-end"><button type="button" className="btn btn-ghost" onClick={() => setEvents(null)}>Close</button></div>
      </Modal>
    </div>
  );
}
