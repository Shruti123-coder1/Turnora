import { useEffect, useState } from 'react';
import { Link, useSearchParams } from 'react-router-dom';
import { servicesApi, ticketApi } from '../../api';
import { errorMessage } from '../../api/axiosConfig';
import Loader from '../../components/Loader';
import TokenCard from '../../components/TokenCard';
import { useAuth } from '../../context/AuthContext';
import { LAST_TOKEN_KEY, PRIORITIES } from '../../utils/constants';

export default function JoinQueue() {
  const { user } = useAuth();
  const [params] = useSearchParams();
  const [offices, setOffices] = useState(null);
  const [serviceId, setServiceId] = useState(params.get('service') || '');
  const [priority, setPriority] = useState('NORMAL');
  const [name, setName] = useState('');
  const [phone, setPhone] = useState('');
  const [error, setError] = useState('');
  const [busy, setBusy] = useState(false);
  const [ticket, setTicket] = useState(null);

  const isStudent = user?.role === 'STUDENT';
  const isStaff = user?.role === 'STAFF' || user?.role === 'ADMIN';
  const priorities = isStaff ? [...PRIORITIES, { value: 'URGENT', label: 'Urgent' }] : PRIORITIES;

  useEffect(() => {
    servicesApi.list().then(setOffices).catch((e) => setError(errorMessage(e)));
  }, []);

  const submit = async (e) => {
    e.preventDefault();
    if (!serviceId) {
      setError('Select an office first.');
      return;
    }
    setBusy(true);
    setError('');
    try {
      const created = await ticketApi.create({
        serviceId: Number(serviceId),
        priorityType: priority,
        customerName: name.trim() || undefined,
        phone: phone.trim() || undefined,
      });
      localStorage.setItem(LAST_TOKEN_KEY, created.tokenNumber);
      setTicket(created);
    } catch (err) {
      setError(errorMessage(err));
    } finally {
      setBusy(false);
    }
  };

  if (ticket) {
    return (
      <div className="mx-auto w-full max-w-2xl">
        <h1 className="mb-4 page-heading">You are in the queue</h1>
        <TokenCard ticket={ticket} />
        <div className="mt-5 flex flex-wrap gap-3">
          <Link to={`/track/${ticket.tokenNumber}`} className="btn btn-primary min-h-12 px-5">Watch live status</Link>
          <button type="button" className="btn btn-ghost" onClick={() => { setTicket(null); setName(''); setPhone(''); }}>
            Take another token
          </button>
        </div>
      </div>
    );
  }

  return (
    <div className="mx-auto w-full max-w-5xl">
      <h1 className="page-heading">Join a queue</h1>
      <p className="page-subtitle">Choose an office to reserve your place in line.</p>
      <form onSubmit={submit} className="card mt-6 space-y-7 p-5 sm:p-7 lg:p-8">
        <fieldset>
          <legend className="label">Office</legend>
          {!offices && !error && <Loader />}
          <div className="grid gap-3 sm:grid-cols-2 xl:grid-cols-3">
            {offices?.map((o) => (
              <label
                key={o.id}
                className={`card flex min-h-20 cursor-pointer items-center gap-3 p-4 transition hover:border-teal/40 hover:shadow-sm ${String(o.id) === String(serviceId) ? 'border-teal ring-2 ring-teal/30' : ''}`}
              >
                <input type="radio" name="service" className="sr-only" value={o.id} checked={String(o.id) === String(serviceId)} onChange={() => setServiceId(String(o.id))} />
                <span className="grid h-10 w-10 shrink-0 place-items-center rounded-lg bg-signal font-display font-bold text-teal-dark">{o.prefix}</span>
                <span className="min-w-0">
                  <span className="block truncate text-sm font-medium">{o.name}</span>
                  <span className="block text-xs text-muted">{o.waitingCount} waiting</span>
                </span>
              </label>
            ))}
          </div>
        </fieldset>

        <div>
          <label className="label" htmlFor="priority">Priority</label>
          <select id="priority" className="input min-h-12 text-base" value={priority} onChange={(e) => setPriority(e.target.value)}>
            {priorities.map((p) => <option key={p.value} value={p.value}>{p.label}</option>)}
          </select>
        </div>

        <div className="grid gap-4 sm:grid-cols-2">
          <div>
            <label className="label" htmlFor="name">Name</label>
            <input id="name" className="input min-h-12 text-base" value={name} onChange={(e) => setName(e.target.value)} placeholder={isStudent ? 'Using your account name' : ''} required={!isStudent} />
          </div>
          <div>
            <label className="label" htmlFor="phone">Phone (10 digits)</label>
            <input id="phone" type="tel" className="input min-h-12 text-base" value={phone} onChange={(e) => setPhone(e.target.value)} placeholder={isStudent ? 'Using your account phone' : ''} required={!isStudent} />
          </div>
        </div>

        {error && <p role="alert" className="text-sm text-alert">{error}</p>}
        <button className="btn btn-primary min-h-12 px-6 text-base" disabled={busy}>{busy ? 'Getting your token' : 'Get my token'}</button>
      </form>
    </div>
  );
}
