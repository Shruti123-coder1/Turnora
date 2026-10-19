import { useCallback, useEffect, useState } from 'react';
import { Link } from 'react-router-dom';
import { servicesApi } from '../../api';
import { errorMessage } from '../../api/axiosConfig';
import Loader from '../../components/Loader';
import useStomp from '../../hooks/useStomp';

export default function Home() {
  const [offices, setOffices] = useState(null);
  const [error, setError] = useState('');

  const load = useCallback(() => {
    servicesApi
      .list()
      .then((list) => {
        setOffices(list);
        setError('');
      })
      .catch((e) => setError(errorMessage(e)));
  }, []);

  useEffect(load, [load]);
  useStomp('/topic/board', load);

  return (
    <div className="home-page grid grid-cols-1 items-center gap-10 py-6 sm:py-10 lg:min-h-[calc(100vh-13rem)] lg:grid-cols-[minmax(0,0.9fr)_minmax(38rem,1.1fr)] lg:gap-16 lg:py-8">
      <section className="flex min-w-0 flex-col justify-center text-left lg:pl-4">
        <div>
          <h1 className="text-5xl font-extrabold leading-[1.04] tracking-tight sm:text-6xl xl:text-7xl">
            <span className="block">Take a token.</span>
            <span className="block text-teal">Keep your turn.</span>
          </h1>
          <p className="mt-6 max-w-xl text-lg leading-relaxed text-muted">
            Join the line for any campus office from your phone, watch your place move up live, and get a message when you are next.
          </p>
          <div className="mt-8 flex flex-col gap-3 sm:flex-row">
            <Link to="/join" className="btn h-12 rounded-xl bg-teal px-6 text-base text-white shadow-sm transition hover:bg-teal-dark focus-visible:outline focus-visible:outline-2 focus-visible:outline-offset-2 focus-visible:outline-teal">Join a queue</Link>
            <Link to="/track" className="btn h-12 rounded-xl border border-line bg-white px-6 text-base text-ink transition hover:bg-gray-50 focus-visible:outline focus-visible:outline-2 focus-visible:outline-offset-2 focus-visible:outline-teal">Track a token</Link>
          </div>
          <p className="mt-5 flex items-center gap-2 text-sm text-muted"><span className="h-2 w-2 rounded-full bg-emerald-500 animate-pulse" aria-hidden="true" />Live queue updates</p>
        </div>
      </section>

      <section className="card w-full overflow-hidden rounded-xl border-line/90 shadow-[0_8px_24px_rgba(22,33,31,0.05)]" aria-label="Offices and current queue">
        <h2 className="border-b border-line px-5 py-4 text-lg font-bold">Offices right now</h2>
        {error && <p className="p-5 text-sm text-alert">{error}</p>}
        {!offices && !error && <div className="px-5"><Loader /></div>}
        {offices && offices.length === 0 && <p className="p-5 text-sm text-muted">No office is open right now.</p>}
        <ul className="divide-y divide-line">
          {offices?.map((o) => (
            <li key={o.id} className="flex min-w-0 items-center gap-3 px-4 py-3 sm:gap-4 sm:px-5">
              <span className="grid h-10 w-10 shrink-0 place-items-center rounded-md bg-signal font-display text-lg font-bold text-teal-dark">
                {o.prefix}
              </span>
              <div className="min-w-0 flex-1">
                <p className="truncate font-medium" title={o.name}>{o.name}</p>
                <p className="truncate text-sm text-muted" title={o.description}>{o.description}</p>
              </div>
              <div className="shrink-0 text-right text-sm">
                <p className="font-display text-xl font-bold tabular-nums">{o.waitingCount}</p>
                <p className="text-muted">waiting</p>
              </div>
              <Link to={`/join?service=${o.id}`} className="btn btn-ghost shrink-0 px-3 sm:px-4">Join</Link>
            </li>
          ))}
        </ul>
      </section>
    </div>
  );
}
