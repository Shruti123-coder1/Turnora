import { useCallback, useEffect, useState } from 'react';
import { counterApi, servicesApi } from '../../api';
import { errorMessage } from '../../api/axiosConfig';
import Loader from '../../components/Loader';
import StatusBadge from '../../components/StatusBadge';
import useStomp from '../../hooks/useStomp';
import { COUNTER_STATUSES } from '../../utils/constants';
import { labelOf } from '../../utils/formatTime';

export default function CounterScreen() {
  const [counters, setCounters] = useState(null);
  const [counterId, setCounterId] = useState('');
  const [state, setState] = useState(null);
  const [offices, setOffices] = useState([]);
  const [transferTo, setTransferTo] = useState('');
  const [error, setError] = useState('');
  const [busy, setBusy] = useState(false);

  useEffect(() => {
    counterApi
      .list()
      .then((list) => {
        setCounters(list);
        if (list.length) setCounterId(String(list[0].id));
      })
      .catch((e) => setError(errorMessage(e)));
    servicesApi.list().then(setOffices).catch(() => {});
  }, []);

  const loadState = useCallback(() => {
    if (!counterId) return;
    counterApi.state(counterId).then(setState).catch((e) => setError(errorMessage(e)));
  }, [counterId]);

  useEffect(() => {
    setState(null);
    loadState();
  }, [loadState]);
  useStomp('/topic/board', loadState);

  // Runs one counter action and shows the new state
  const run = async (action) => {
    setBusy(true);
    setError('');
    try {
      setState(await action());
    } catch (e) {
      setError(errorMessage(e));
    } finally {
      setBusy(false);
    }
  };

  if (!counters && !error) return <Loader />;
  if (counters && counters.length === 0) {
    return (
      <div className="card mx-auto max-w-md p-8 text-center">
        <h1 className="text-2xl font-bold">No counter assigned</h1>
        <p className="mt-2 text-muted">Ask the admin to assign a counter to your account.</p>
      </div>
    );
  }

  const current = state?.currentTicket;
  const id = Number(counterId);

  return (
    <div className="space-y-6">
      <div className="flex flex-wrap items-end justify-between gap-4">
        <div>
          <h1 className="text-3xl font-bold">{state?.counterName || 'Counter screen'}</h1>
          <p className="mt-1 text-muted">Call the next person, then mark the visit complete.</p>
        </div>
        <div className="flex flex-wrap gap-3">
          {counters?.length > 1 && (
            <div>
              <label className="label" htmlFor="counter">Counter</label>
              <select id="counter" className="input" value={counterId} onChange={(e) => setCounterId(e.target.value)}>
                {counters.map((c) => <option key={c.id} value={c.id}>{c.name}</option>)}
              </select>
            </div>
          )}
          <div>
            <label className="label" htmlFor="cstatus">Counter status</label>
            <select id="cstatus" className="input" value={state?.counterStatus || ''} disabled={busy || !state} onChange={(e) => run(() => counterApi.setStatus(id, e.target.value))}>
              {COUNTER_STATUSES.map((s) => <option key={s} value={s}>{labelOf(s)}</option>)}
            </select>
          </div>
        </div>
      </div>

      {error && <p role="alert" className="text-sm text-alert">{error}</p>}
      {!state && !error && <Loader />}

      {state && (
        <div className="grid gap-6 lg:grid-cols-[1.4fr_1fr]">
          <section className="card p-6">
            <h2 className="text-lg font-bold">Current token</h2>
            {current ? (
              <>
                <div className="mt-3 flex items-start justify-between gap-3">
                  <p className="font-display text-7xl font-bold tabular-nums">{current.tokenNumber}</p>
                  <StatusBadge status={current.status} />
                </div>
                <p className="mt-2 text-sm">{current.customerName} - {current.serviceName}</p>
                {current.priorityType !== 'NORMAL' && <p className="text-sm text-muted">{labelOf(current.priorityType)} priority</p>}

                <div className="mt-6 flex flex-wrap gap-3">
                  {current.status === 'CALLED' && (
                    <button type="button" className="btn btn-primary" disabled={busy} onClick={() => run(() => counterApi.start(id))}>Start serving</button>
                  )}
                  <button type="button" className="btn btn-primary" disabled={busy} onClick={() => run(() => counterApi.complete(id))}>Mark complete</button>
                  {current.status === 'CALLED' && (
                    <button type="button" className="btn btn-danger" disabled={busy} onClick={() => run(() => counterApi.noShow(id))}>No show</button>
                  )}
                </div>

                <div className="mt-6 flex flex-wrap items-end gap-3 border-t border-line pt-4">
                  <div className="min-w-[200px] flex-1">
                    <label className="label" htmlFor="transfer">Send to another office</label>
                    <select id="transfer" className="input" value={transferTo} onChange={(e) => setTransferTo(e.target.value)}>
                      <option value="">Select an office</option>
                      {offices.filter((o) => o.name !== current.serviceName).map((o) => <option key={o.id} value={o.id}>{o.name}</option>)}
                    </select>
                  </div>
                  <button type="button" className="btn btn-ghost" disabled={busy || !transferTo} onClick={() => run(() => counterApi.transfer(id, Number(transferTo))).then(() => setTransferTo(''))}>
                    Transfer
                  </button>
                </div>
              </>
            ) : (
              <div className="py-8 text-center">
                <p className="text-muted">No token at this counter.</p>
                <button type="button" className="btn btn-primary mt-4 px-8 py-3 text-base" disabled={busy || state.counterStatus !== 'ACTIVE'} onClick={() => run(() => counterApi.next(id))}>
                  Call next token
                </button>
                {state.counterStatus !== 'ACTIVE' && <p className="mt-2 text-sm text-muted">Set the counter to Active to call tokens.</p>}
              </div>
            )}
          </section>

          <section className="card p-6">
            <h2 className="text-lg font-bold">Waiting: <span className="tabular-nums">{state.waitingCount}</span></h2>
            {state.upNext.length === 0 ? (
              <p className="mt-3 text-sm text-muted">No one is waiting.</p>
            ) : (
              <ol className="mt-3 divide-y divide-line">
                {state.upNext.map((t) => (
                  <li key={t.id} className="flex items-center justify-between py-2">
                    <span className="font-display text-xl font-bold tabular-nums">{t.tokenNumber}</span>
                    <span className="text-right text-sm text-muted">
                      {t.customerName}
                      {t.priorityType !== 'NORMAL' && <span className="block text-xs">{labelOf(t.priorityType)}</span>}
                    </span>
                  </li>
                ))}
              </ol>
            )}
          </section>
        </div>
      )}
    </div>
  );
}