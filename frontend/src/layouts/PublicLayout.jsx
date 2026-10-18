import { Link, NavLink, Outlet, useNavigate } from 'react-router-dom';
import { useAuth } from '../context/AuthContext';
import { ROLE_HOME } from '../utils/constants';

const link = ({ isActive }) =>
  `rounded-md px-3 py-2 text-sm font-medium ${isActive ? 'bg-teal-soft text-teal-dark' : 'hover:bg-teal-soft'}`;

export default function PublicLayout() {
  const { user, logout } = useAuth();
  const navigate = useNavigate();

  return (
    <div className="flex min-h-screen flex-col">
      <header className="border-b border-line bg-white">
        <div className="mx-auto flex max-w-6xl flex-wrap items-center justify-between gap-3 px-4 py-3">
          <Link to="/" className="flex items-center gap-2">
            <span className="grid h-8 w-8 place-items-center rounded-md bg-signal font-display font-bold text-teal-dark">T</span>
            <span className="font-display text-xl font-bold">Turnora</span>
          </Link>
          <nav className="flex flex-wrap items-center gap-1">
            <NavLink to="/" end className={link}>Offices</NavLink>
            <NavLink to="/join" className={link}>Join a queue</NavLink>
            <NavLink to="/track" className={link}>Track a token</NavLink>
            <NavLink to="/board" className={link}>Display board</NavLink>
          </nav>
          <div className="flex items-center gap-2">
            {user ? (
              <>
                <Link to={ROLE_HOME[user.role] || '/'} className="btn btn-primary">Dashboard</Link>
                <button type="button" className="btn btn-ghost" onClick={() => { logout(); navigate('/'); }}>Log out</button>
              </>
            ) : (
              <>
                <Link to="/login" className="btn btn-ghost">Log in</Link>
                <Link to="/register" className="btn btn-primary">Create account</Link>
              </>
            )}
          </div>
        </div>
      </header>
      <main className="mx-auto w-full max-w-6xl flex-1 px-4 py-8">
        <Outlet />
      </main>
      <footer className="border-t border-line py-4 text-center text-sm text-muted">Turnora - smart queue for campus offices</footer>
    </div>
  );
}