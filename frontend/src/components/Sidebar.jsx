import { NavLink } from 'react-router-dom';
import { MENU } from '../config/menuConfig';
import { useAuth } from '../context/AuthContext';

export default function Sidebar({ open, collapsed, onClose, onToggleCollapse }) {
  const { user } = useAuth();
  const items = MENU[user?.role] || [];

  return (
    <>
      {open && <div className="fixed inset-0 z-30 bg-ink/40 md:hidden" onClick={onClose} aria-hidden="true" />}
      <aside
        className={`fixed inset-y-0 left-0 z-40 flex w-72 flex-col border-r border-line/80 bg-white p-4 shadow-xl shadow-ink/10 transition-[transform,width,padding] duration-300 ease-in-out md:static md:translate-x-0 md:shadow-none ${
          open ? 'translate-x-0' : '-translate-x-full'
        } ${collapsed ? 'md:w-52 md:px-3' : 'md:w-64'}`}
      >
        <div className="mb-8 flex items-center justify-between gap-2">
          <div className="flex min-w-0 items-center gap-2">
            <span className="grid h-9 w-9 shrink-0 place-items-center rounded-lg bg-signal font-display font-bold text-teal-dark shadow-sm">T</span>
            <span className="truncate font-display text-xl font-bold">Turnora</span>
          </div>
          <button type="button" className="btn btn-ghost h-10 w-10 shrink-0 px-0 md:hidden" onClick={onClose} aria-label="Close navigation menu">
            <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2" className="h-5 w-5" aria-hidden="true">
              <path d="M4 6h16M4 12h16M4 18h16" />
            </svg>
          </button>
          <button type="button" className="btn btn-ghost hidden h-10 w-10 shrink-0 px-0 md:inline-flex" onClick={onToggleCollapse} aria-label="Toggle sidebar navigation" aria-expanded={!collapsed}>
            <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2" className="h-5 w-5" aria-hidden="true">
              <path d="M4 6h16M4 12h16M4 18h16" />
            </svg>
          </button>
        </div>
        <nav className="space-y-1" aria-label="Dashboard navigation">
          {items.map((item) => (
            <NavLink
              key={item.to + item.label}
              to={item.to}
              end={item.end}
              onClick={onClose}
              className={({ isActive }) =>
                `block rounded-lg px-3 py-2.5 text-sm font-semibold transition ${
                  isActive ? 'bg-teal text-white shadow-sm' : 'text-muted hover:bg-teal-soft hover:text-teal-dark'
                }`
              }
            >
              {item.label}
            </NavLink>
          ))}
        </nav>
      </aside>
    </>
  );
}
