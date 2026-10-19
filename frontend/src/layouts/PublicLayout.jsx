import { Link, NavLink, Outlet, useLocation, useNavigate } from 'react-router-dom';
import { useAuth } from '../context/AuthContext';
import { ROLE_HOME } from '../utils/constants';

const link = ({ isActive }) =>
  `rounded-lg px-3 py-2 text-sm font-semibold transition ${isActive ? 'bg-teal-soft text-teal-dark' : 'text-muted hover:bg-teal-soft hover:text-teal-dark'}`;

export default function PublicLayout() {
  const { user, logout } = useAuth();
  const navigate = useNavigate();
  const { pathname } = useLocation();
  const isHome = pathname === '/';

  return (
    <div className={`flex min-h-screen flex-col ${isHome ? 'bg-[#EEF8FD]' : 'bg-sky-50'}`}>
      <header className="sticky top-0 z-20 border-b border-line/80 bg-white/95 backdrop-blur">
        <div className="mx-auto flex w-[90%] max-w-[1600px] flex-wrap items-center gap-x-6 gap-y-3 py-3 lg:grid lg:grid-cols-[1fr_auto_1fr] lg:flex-nowrap">
          <Link to="/" className="flex shrink-0 items-center gap-2">
            <span className="grid h-9 w-9 place-items-center rounded-lg bg-signal font-display font-bold text-teal-dark shadow-sm">T</span>
            <span className="font-display text-xl font-bold">Turnora</span>
          </Link>
          <nav className="order-3 flex w-full flex-wrap items-center gap-1 sm:order-none sm:w-auto lg:flex-nowrap lg:gap-2" aria-label="Primary navigation">
            <NavLink to="/" end className={link}>Offices</NavLink>
            <NavLink to="/join" className={link}>Join a queue</NavLink>
            <NavLink to="/track" className={link}>Track a token</NavLink>
            <NavLink to="/board" className={link}>Display board</NavLink>
          </nav>
          <div className="ml-auto flex shrink-0 items-center gap-2 lg:ml-0 lg:justify-self-end">
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
      <main className={`surface-grid mx-auto w-[90%] max-w-[1600px] flex-1 ${isHome ? 'py-6 lg:py-8' : 'py-8 lg:py-14'}`}>
        <Outlet />
      </main>
      <footer className="border-t border-line bg-white py-5 text-center text-sm text-muted">Turnora · Smart queue management for campus offices</footer>
    </div>
  );
}
