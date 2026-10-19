import { useCallback, useEffect, useState } from 'react';
import { Link } from 'react-router-dom';
import { ticketApi } from '../../api';
import { errorMessage } from '../../api/axiosConfig';
import ConfirmModal from '../../components/ConfirmModal';
import Loader from '../../components/Loader';
import StatusBadge from '../../components/StatusBadge';
import { useAuth } from '../../context/AuthContext';
import useStomp from '../../hooks/useStomp';
import { STATUS_META } from '../../utils/constants';
import { formatDateTime, formatWait } from '../../utils/formatTime';

export default function MyTokens() {
  const { user } = useAuth();
  const [tickets, setTickets] = useState(null);
  const [filter, setFilter] = useState('ALL');
  const [error, setError] = useState('');
  const [toCancel, setToCancel] = useState(null);
  const [busy, setBusy] = useState(false);

  const load = useCallback(() => {
    ticketApi.mine().then(setTickets).catch((e) => setError(errorMessage(e)));
  }, []);

  useEffect(load, [load]);
  useStomp(`/topic/user/${user.id}`, load);

  const cancel = async () => {
    setBusy(true);
    try {
      await ticketApi.cancel(toCancel.id);
      load();
    } catch (e) {
      setError(errorMessage(e));
    } finally {
      setToCancel(null);
      setBusy(false);
    }
  };

  const rows = (tickets || []).filter((t) => filter === 'ALL' || t.status === filter);

  return (
    <div className="space-y-6">
      <div className="flex flex-wrap items-center justify-between gap-3">
        <h1 className="page-heading">My tokens</h1>
        <div>
          <label htmlFor="filter" className="sr-only">Filter by status</label>
          <select id="filter" className="input" value={filter} onChange={(e) => setFilter(e.target.value)}>
            <option value="ALL">All statuses</option>
            {Object.entries(STATUS_META).map(([value, m]) => <option key={value} value={value}>{m.label}</option>)}
          </select>
        </div>
      </div>
      {error && <p role="alert" className="text-sm text-alert">{error}</p>}
      {!tickets && !error && <Loader />}

      {tickets && rows.length === 0 && (
        <div className="card p-8 text-center">
          <p className="font-medium">No tokens to show.</p>
          <Link to="/join" className="btn btn-primary mt-4">Join a queue</Link>
        </div>
      )}

      {rows.length > 0 && (
        <div className="table-shell">
          <table className="w-full min-w-[640px]">
            <thead className="border-b border-line">
              <tr>
                <th className="th">Token</th><th className="th">Office</th><th className="th">Status</th>
                <th className="th">Joined</th><th className="th">Est. wait</th><th className="th" />
              </tr>
            </thead>
            <tbody className="divide-y divide-line">
              {rows.map((t) => (
                <tr key={t.id}>
                  <td className="td font-display text-lg font-bold tabular-nums">{t.tokenNumber}</td>
                  <td className="td">{t.serviceName}</td>
                  <td className="td"><StatusBadge status={t.status} /></td>
                  <td className="td">{formatDateTime(t.createdAt)}</td>
                  <td className="td">{t.status === 'WAITING' ? formatWait(t.estimatedWaitMinutes) : '-'}</td>
                  <td className="td text-right">
                    {t.status === 'WAITING' && <button type="button" className="btn btn-ghost" onClick={() => setToCancel(t)}>Cancel</button>}
                  </td>
                </tr>
              ))}
            </tbody>
          </table>
        </div>
      )}

      <ConfirmModal
        open={Boolean(toCancel)}
        title="Cancel this token?"
        message={toCancel ? `Token ${toCancel.tokenNumber} will be removed from the queue.` : ''}
        confirmText="Cancel token"
        danger
        busy={busy}
        onConfirm={cancel}
        onCancel={() => setToCancel(null)}
      />
    </div>
  );
}
