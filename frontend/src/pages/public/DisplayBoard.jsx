import { useCallback, useEffect, useState } from 'react';
import { counterApi } from '../../api';
import useStomp from '../../hooks/useStomp';
import { formatTime } from '../../utils/formatTime';

export default function DisplayBoard() {
  const [board, setBoard] = useState({ counters: [], recentCalls: [] });
  const [now, setNow] = useState(new Date());
  const [offline, setOffline] = useState(false);

  const load = useCallback(() => {
    counterApi
      .board()
      .then((b) => {
        setBoard(b);
        setOffline(false);
      })
      .catch(() => setOffline(true));
  }, []);

  useEffect(() => {
    load();
    const poll = setInterval(load, 20000);
    const tick = setInterval(() => setNow(new Date()), 1000);
    return () => {
      clearInterval(poll);
      clearInterval(tick);
    };
  }, [load]);

  useStomp('/topic/board', load);

  return (
    <div className="min-h-screen overflow-x-hidden bg-sky-950 p-4 text-white sm:p-6 md:p-10">
      <header className="flex flex-col gap-3 sm:flex-row sm:items-end sm:justify-between">
        <div>
          <p className="font-display text-lg text-signal">Turnora</p>
          <h1 className="text-4xl font-bold md:text-6xl">Now serving</h1>
        </div>
        <p className="font-display text-3xl tabular-nums md:text-5xl">
          {now.toLocaleTimeString('en-IN', { hour: '2-digit', minute: '2-digit' })}
        </p>
      </header>
      {offline && <p className="mt-4 text-sm text-amber-300">Connection lost. Trying again.</p>}

      <div className="mt-8 grid gap-6 lg:grid-cols-[2fr_1fr]">
        <section className="grid gap-4 sm:grid-cols-2 xl:grid-cols-3">
          {board.counters.map((c) => {
            const active = c.status === 'ACTIVE';
            return (
            <div key={c.id} className="rounded-xl border border-white/15 bg-white/5 p-5 shadow-lg shadow-black/10">
                <div className="flex items-center justify-between text-sm text-white/70">
                  <span>{c.name}</span>
                  {!active && <span className="rounded bg-white/10 px-2 py-0.5 text-xs">{c.status === 'PAUSED' ? 'On a break' : 'Closed'}</span>}
                </div>
                <p className={`mt-3 font-display text-6xl font-bold tabular-nums ${c.currentToken ? 'text-signal' : 'text-white/25'}`}>
                  {c.currentToken || '-'}
                </p>
                <p className="mt-2 min-h-[1.25rem] truncate text-sm text-white/70">{c.currentToken ? c.serviceName : active ? 'Ready for the next token' : ''}</p>
              </div>
            );
          })}
          {board.counters.length === 0 && <p className="text-white/60">No counters are set up yet.</p>}
        </section>

        <aside className="rounded-xl border border-white/15 bg-white/5 p-5 shadow-lg shadow-black/10">
          <h2 className="text-xl font-bold">Recent calls</h2>
          <ul className="mt-3 divide-y divide-white/10">
            {board.recentCalls.map((r, i) => (
              <li key={`${r.token}-${i}`} className="flex items-center justify-between py-3">
                <span className="font-display text-2xl font-bold tabular-nums">{r.token}</span>
                <span className="text-right text-sm text-white/70">
                  {r.counterName}
                  <span className="block text-xs">{formatTime(r.calledAt)}</span>
                </span>
              </li>
            ))}
            {board.recentCalls.length === 0 && <li className="py-3 text-sm text-white/60">No token has been called yet today.</li>}
          </ul>
        </aside>
      </div>
    </div>
  );
}
