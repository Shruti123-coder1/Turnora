import { useCallback, useEffect, useState } from 'react';
import { Link } from 'react-router-dom';
import { notificationApi } from '../api';
import { useAuth } from '../context/AuthContext';
import useStomp from '../hooks/useStomp';

export default function NotificationBell() {
  const { user } = useAuth();
  const [unread, setUnread] = useState(0);

  const load = useCallback(() => {
    notificationApi
      .list()
      .then((list) => setUnread(list.filter((n) => !n.read).length))
      .catch(() => {});
  }, []);

  useEffect(load, [load]);
  useStomp(user ? `/topic/user/${user.id}` : null, load);

  return (
    <Link to="/student/notifications" className="btn btn-ghost relative" aria-label={`Notifications, ${unread} unread`}>
      Notifications
      {unread > 0 && (
        <span className="rounded-full bg-signal px-1.5 text-xs font-bold tabular-nums text-ink">{unread}</span>
      )}
    </Link>
  );
}