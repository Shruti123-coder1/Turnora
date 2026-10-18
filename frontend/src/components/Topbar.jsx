import { useNavigate } from 'react-router-dom';
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

  return (
    <header className="flex items-center justify-between gap-3 border-b border-line bg-white px-4 py-3 md:px-8">
      <button type="button" className="btn btn-ghost md:hidden" onClick={onMenu} aria-label="Open menu">
        Menu
      </button>
      <div className="hidden text-sm md:block">
        <span className="font-medium">{user?.fullName}</span>
        <span className="ml-2 rounded-full bg-teal-soft px-2 py-0.5 text-xs text-teal-dark">{labelOf(user?.role)}</span>
      </div>
      <div className="flex items-center gap-2">
        {user?.role === 'STUDENT' && <NotificationBell />}
        <button type="button" className="btn btn-ghost" onClick={signOut}>
          Log out
        </button>
      </div>
    </header>
  );
}