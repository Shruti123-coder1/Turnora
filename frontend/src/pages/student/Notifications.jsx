import { useCallback, useEffect, useState } from 'react';
import { notificationApi } from '../../api';
import { errorMessage } from '../../api/axiosConfig';
import Loader from '../../components/Loader';
import { useAuth } from '../../context/AuthContext';
import useStomp from '../../hooks/useStomp';
import { formatDateTime } from '../../utils/formatTime';

export default function Notifications() {
  const { user } = useAuth();
  const [items, setItems] = useState(null);
  const [error, setError] = useState('');

  const load = useCallback(() => {
    notificationApi.list().then(setItems).catch((e) => setError(errorMessage(e)));
  }, []);

  useEffect(load, [load]);
  useStomp(`/topic/user/${user.id}`, load);

  const markRead = async (id) => {
    try {
      await notificationApi.markRead(id);
      load();
    } catch (e) {
      setError(errorMessage(e));
    }
  };

  const markAll = async () => {
    await Promise.all(items.filter((n) => !n.read).map((n) => notificationApi.markRead(n.id).catch(() => null)));
    load();
  };

  const unread = (items || []).filter((n) => !n.read).length;

  return (
    <div className="mx-auto max-w-3xl space-y-6">
      <div className="flex items-center justify-between gap-3">
        <h1 className="page-heading">Notifications</h1>
        {unread > 0 && <button type="button" className="btn btn-ghost" onClick={markAll}>Mark all as read</button>}
      </div>
      {error && <p role="alert" className="text-sm text-alert">{error}</p>}
      {!items && !error && <Loader />}
      {items && items.length === 0 && <div className="card p-8 text-center text-muted">Nothing here yet. Updates about your tokens will appear here.</div>}

      <ul className="space-y-3">
        {items?.map((n) => (
          <li key={n.id} className={`card flex flex-col gap-3 p-4 sm:flex-row sm:items-start sm:justify-between sm:gap-4 ${n.read ? '' : 'border-teal bg-teal-soft/40'}`}>
            <div>
              <p className="text-sm">{n.message}</p>
              <p className="mt-1 text-xs text-muted">
                {n.tokenNumber && <span className="mr-2 font-medium">{n.tokenNumber}</span>}
                {formatDateTime(n.createdAt)}
              </p>
            </div>
            {!n.read && <button type="button" className="btn btn-ghost shrink-0" onClick={() => markRead(n.id)}>Mark as read</button>}
          </li>
        ))}
      </ul>
    </div>
  );
}
