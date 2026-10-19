import { Link, useNavigate } from 'react-router-dom';
import { useAuth } from '../context/AuthContext';
import { labelOf } from '../utils/formatTime';
import NotificationBell from './NotificationBell';

export default function Topbar({ onMenu }) {
  const { user, logout } = useAuth();
  const navigate = useNavigate();

  const signOut = () => {
    logout();
    navigate('/login');
  };
  const initials = (user?.fullName || 'User').split(/\s+/).filter(Boolean).slice(0, 2).map((part) => part[0]).join('').toUpperCase();
  const roleLabel = labelOf(user?.role);
  const nameMatchesRole = user?.fullName?.trim().toLowerCase() === roleLabel.toLowerCase();

  return (
    <header className="sticky top-0 z-20 flex items-center justify-between gap-3 border-b border-line/80 bg-white/95 px-4 py-3 backdrop-blur md:px-8">
      <button type="button" className="btn btn-ghost px-3 md:hidden" onClick={onMenu} aria-label="Open navigation menu">
        <span aria-hidden="true">☰</span><span className="sr-only">Menu</span>
      </button>
      <Link to="/profile" className="hidden items-center gap-2 rounded-lg px-2 py-1 transition hover:bg-sky-50 md:flex" aria-label="Open profile">
        <span className="grid h-9 w-9 place-items-center rounded-full bg-sky-100 text-xs font-bold text-sky-800">{initials}</span>
        <span className="text-sm">
          {!nameMatchesRole && <span className="block font-medium">{user?.fullName}</span>}
          <span className={`inline-flex rounded-full bg-sky-100 px-2 py-0.5 text-xs font-semibold text-sky-700 ${nameMatchesRole ? '' : 'mt-0.5'}`}>{roleLabel}</span>
        </span>
      </Link>
      <div className="flex items-center gap-2">
        {user?.role === 'STUDENT' && <NotificationBell />}
        <Link to="/profile" className="grid h-10 w-10 place-items-center rounded-full bg-sky-100 text-xs font-bold text-sky-800 transition hover:bg-sky-200 md:hidden" aria-label="Open profile">{initials}</Link>
        <button type="button" className="btn btn-ghost" onClick={signOut}>
          Log out
        </button>
      </div>
    </header>
  );
}
