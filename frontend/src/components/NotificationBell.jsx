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
    <Link to="/student/notifications" className="btn btn-ghost relative h-10 w-10 px-0" aria-label={`Notifications, ${unread} unread`}>
      <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2" className="h-5 w-5" aria-hidden="true">
        <path d="M18 8a6 6 0 0 0-12 0c0 7-3 7-3 9h18c0-2-3-2-3-9" />
        <path d="M10 21h4" />
      </svg>
      {unread > 0 && (
        <span className="absolute -right-1 -top-1 min-w-5 rounded-full bg-teal px-1.5 text-center text-xs font-bold tabular-nums text-white">{unread}</span>
      )}
    </Link>
  );
}
