import { NavLink } from 'react-router-dom';
import { MENU } from '../config/menuConfig';
import { useAuth } from '../context/AuthContext';

export default function Sidebar({ open, onClose }) {
  const { user } = useAuth();
  const items = MENU[user?.role] || [];

  return (
    <>
      {open && <div className="fixed inset-0 z-30 bg-ink/40 md:hidden" onClick={onClose} aria-hidden="true" />}
      <aside
        className={`fixed inset-y-0 left-0 z-40 flex w-72 flex-col border-r border-line/80 bg-white p-4 shadow-xl shadow-ink/10 transition-transform md:static md:w-64 md:translate-x-0 md:shadow-none ${
          open ? 'translate-x-0' : '-translate-x-full'
        }`}
      >
        <div className="mb-8 flex items-center gap-2 px-2">
          <span className="grid h-9 w-9 place-items-center rounded-lg bg-signal font-display font-bold text-teal-dark shadow-sm">T</span>
          <span className="font-display text-xl font-bold">Turnora</span>
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
