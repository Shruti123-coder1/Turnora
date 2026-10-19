import { Link, useNavigate } from 'react-router-dom';
import { useAuth } from '../context/AuthContext';
import { labelOf } from '../utils/formatTime';
import { ROLE_HOME } from '../utils/constants';
import NotificationBell from './NotificationBell';

export default function Topbar({ onMenu, sidebarExpanded }) {
  const { user, logout } = useAuth();
  const navigate = useNavigate();

  const signOut = () => {
    logout();
    navigate('/', { replace: true });
  };
  const initials = (user?.fullName || 'User').split(/\s+/).filter(Boolean).slice(0, 2).map((part) => part[0]).join('').toUpperCase();
  const roleLabel = labelOf(user?.role);
  const profileLabel = `${user?.fullName || 'User'} – ${roleLabel}`;

  return (
    <header className="sticky top-0 z-20 flex items-center border-b border-line/80 bg-white/95 px-4 py-3 backdrop-blur md:px-8">
      <button type="button" className="btn btn-ghost -ml-2 h-10 w-10 shrink-0 px-0 md:hidden" onClick={onMenu} aria-label="Toggle navigation menu" aria-expanded={sidebarExpanded}>
        <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2" className="h-5 w-5" aria-hidden="true">
          <path d="M4 6h16M4 12h16M4 18h16" />
        </svg>
        <span className="sr-only">Menu</span>
      </button>
      <div className="ml-auto flex items-center gap-2 sm:gap-3">
        <Link to="/profile" className="hidden rounded-full transition hover:bg-sky-50 md:grid" aria-label={profileLabel} title={profileLabel}>
          <span className="grid h-9 w-9 place-items-center rounded-full bg-sky-100 text-xs font-bold text-sky-800">{initials}</span>
        </Link>
        {user && <NotificationBell to={`${ROLE_HOME[user.role]}/notifications`} />}
        <Link to="/profile" className="grid h-10 w-10 place-items-center rounded-full bg-sky-100 text-xs font-bold text-sky-800 transition hover:bg-sky-200 md:hidden" aria-label={profileLabel} title={profileLabel}>{initials}</Link>
        <button type="button" className="btn btn-ghost" onClick={signOut}>
          Log out
        </button>
      </div>
    </header>
  );
}
