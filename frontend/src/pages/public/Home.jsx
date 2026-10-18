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
    <div className="grid gap-10 lg:grid-cols-[1fr_1.15fr] lg:items-start">
      <section className="pt-4">
        <h1 className="text-4xl font-bold leading-tight md:text-5xl">Take a token. Keep your turn.</h1>
        <p className="mt-4 max-w-md text-lg text-muted">
          Join the line for any campus office from your phone, watch your place move up live, and get a message when you are next.
        </p>
        <div className="mt-6 flex flex-wrap gap-3">
          <Link to="/join" className="btn btn-primary">Join a queue</Link>
          <Link to="/track" className="btn btn-ghost">Track a token</Link>
        </div>
      </section>

      <section className="card overflow-hidden" aria-label="Offices and current queue">
        <h2 className="border-b border-line px-5 py-3 text-lg font-bold">Offices right now</h2>
        {error && <p className="p-5 text-sm text-alert">{error}</p>}
        {!offices && !error && <div className="px-5"><Loader /></div>}
        {offices && offices.length === 0 && <p className="p-5 text-sm text-muted">No office is open right now.</p>}
        <ul className="divide-y divide-line">
          {offices?.map((o) => (
            <li key={o.id} className="flex items-center gap-4 px-5 py-3">
              <span className="grid h-10 w-10 shrink-0 place-items-center rounded-md bg-signal font-display text-lg font-bold text-teal-dark">
                {o.prefix}
              </span>
              <div className="min-w-0 flex-1">
                <p className="truncate font-medium">{o.name}</p>
                <p className="truncate text-sm text-muted">{o.description}</p>
              </div>
              <div className="text-right text-sm">
                <p className="font-display text-xl font-bold tabular-nums">{o.waitingCount}</p>
                <p className="text-muted">waiting</p>
              </div>
              <Link to={`/join?service=${o.id}`} className="btn btn-ghost">Join</Link>
            </li>
          ))}
        </ul>
      </section>
    </div>
  );
}