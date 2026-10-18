import { useCallback, useEffect, useState } from 'react';
import { Link } from 'react-router-dom';
import { ticketApi } from '../../api';
import { errorMessage } from '../../api/axiosConfig';
import ConfirmModal from '../../components/ConfirmModal';
import Loader from '../../components/Loader';
import StatCard from '../../components/StatCard';
import TokenCard from '../../components/TokenCard';
import { useAuth } from '../../context/AuthContext';
import useStomp from '../../hooks/useStomp';
import { LIVE_STATUSES } from '../../utils/constants';

export default function StudentDashboard() {
  const { user } = useAuth();
  const [tickets, setTickets] = useState(null);
  const [error, setError] = useState('');
  const [toCancel, setToCancel] = useState(null);
  const [busy, setBusy] = useState(false);

  const load = useCallback(() => {
    ticketApi.mine().then(setTickets).catch((e) => setError(errorMessage(e)));
  }, []);

  useEffect(load, [load]);
  useStomp([`/topic/user/${user.id}`, '/topic/board'], load);

  const cancel = async () => {
    setBusy(true);
    try {
      await ticketApi.cancel(toCancel.id);
      setToCancel(null);
      load();
    } catch (e) {
      setError(errorMessage(e));
      setToCancel(null);
    } finally {
      setBusy(false);
    }
  };

  if (!tickets && !error) return <Loader />;
  const active = (tickets || []).filter((t) => LIVE_STATUSES.includes(t.status));
  const completed = (tickets || []).filter((t) => t.status === 'COMPLETED').length;

  return (
    <div className="space-y-8">
      <div>
        <h1 className="text-3xl font-bold">Hello, {user.fullName.split(' ')[0]}</h1>
        <p className="mt-1 text-muted">Your live tokens are shown below and update on their own.</p>
      </div>
      {error && <p role="alert" className="text-sm text-alert">{error}</p>}

      <div className="grid gap-4 sm:grid-cols-3">
        <StatCard label="Active tokens" value={active.length} />
        <StatCard label="Completed" value={completed} />
        <StatCard label="All tokens" value={tickets?.length ?? 0} />
      </div>

      {active.length === 0 ? (
        <div className="card p-8 text-center">
          <p className="font-medium">You have no active token.</p>
          <p className="mt-1 text-sm text-muted">Pick an office and take a token to join its line.</p>
          <Link to="/join" className="btn btn-primary mt-4">Join a queue</Link>
        </div>
      ) : (
        <div className="grid gap-6 md:grid-cols-2">
          {active.map((t) => <TokenCard key={t.id} ticket={t} onCancel={setToCancel} />)}
        </div>
      )}

      <ConfirmModal
        open={Boolean(toCancel)}
        title="Cancel this token?"
        message={toCancel ? `Token ${toCancel.tokenNumber} will be removed from the queue. You will lose your place.` : ''}
        confirmText="Cancel token"
        danger
        busy={busy}
        onConfirm={cancel}
        onCancel={() => setToCancel(null)}
      />
    </div>
  );
}