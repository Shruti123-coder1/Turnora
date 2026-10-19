import { useCallback, useEffect, useState } from 'react';
import { adminApi } from '../../api';
import { errorMessage } from '../../api/axiosConfig';
import Loader from '../../components/Loader';
import StatCard from '../../components/StatCard';
import useStomp from '../../hooks/useStomp';
import { formatWait, todayISO } from '../../utils/formatTime';

export default function AdminDashboard() {
  const [date, setDate] = useState(todayISO());
  const [data, setData] = useState(null);
  const [error, setError] = useState('');

  const load = useCallback(() => {
    adminApi.analytics(date).then((d) => { setData(d); setError(''); }).catch((e) => setError(errorMessage(e)));
  }, [date]);

  useEffect(() => { setData(null); load(); }, [load]);
  useStomp(date === todayISO() ? '/topic/board' : null, load);

  const maxHour = Math.max(1, ...(data?.tokensByHour || []).map((h) => h.tokens));
  const busiest = data?.tokensByHour?.reduce((a, b) => (b.tokens > a.tokens ? b : a), { hour: 0, tokens: 0 });

  return (
    <div className="space-y-8">
      <div className="flex flex-wrap items-end justify-between gap-4">
        <h1 className="page-heading">Overview</h1>
        <div>
          <label className="label" htmlFor="date">Date</label>
          <input id="date" type="date" className="input" value={date} max={todayISO()} onChange={(e) => e.target.value && setDate(e.target.value)} />
        </div>
      </div>
      {error && <p role="alert" className="text-sm text-alert">{error}</p>}
      {!data && !error && <Loader />}

      {data && (
        <>
          <div className="grid grid-cols-1 gap-4 sm:grid-cols-2 lg:grid-cols-4">
            <StatCard label="Tokens issued" value={data.totalTokens} />
            <StatCard label="Waiting now" value={data.waiting} hint={`${data.inProgress} in progress`} />
            <StatCard label="Completed" value={data.completed} hint={`${data.noShow} no show, ${data.cancelled} cancelled`} />
            <StatCard label="Average wait" value={formatWait(data.avgWaitMinutes)} hint={`Service ${formatWait(data.avgServiceMinutes)}`} />
          </div>

          <section className="card p-5">
            <div className="flex flex-wrap items-baseline justify-between gap-2">
              <h2 className="text-lg font-bold">Tokens by hour</h2>
              {busiest?.tokens > 0 && <p className="text-sm text-muted">Busiest hour: {String(busiest.hour).padStart(2, '0')}:00 with {busiest.tokens} tokens</p>}
            </div>
            <div className="mt-4 flex h-40 items-end gap-1" role="img" aria-label="Bar chart of tokens issued in each hour of the day">
              {data.tokensByHour.map((h) => (
                <div key={h.hour} className="flex h-full flex-1 flex-col justify-end" title={`${h.hour}:00 - ${h.tokens} tokens`}>
                  <div className={`w-full rounded-t ${h.tokens === busiest?.tokens && h.tokens > 0 ? 'bg-signal' : 'bg-teal'}`} style={{ height: `${(h.tokens / maxHour) * 100}%`, minHeight: h.tokens ? 4 : 0 }} />
                </div>
              ))}
            </div>
            <div className="mt-1 flex gap-1 text-[10px] text-muted">
              {data.tokensByHour.map((h) => <span key={h.hour} className="flex-1 text-center">{h.hour % 3 === 0 ? h.hour : ''}</span>)}
            </div>
          </section>

          <div className="grid gap-6 lg:grid-cols-2">
            <section className="card overflow-x-auto">
              <h2 className="px-4 pt-4 text-lg font-bold">Offices</h2>
              <table className="mt-2 w-full min-w-[420px]">
                <thead className="border-b border-line"><tr><th className="th">Office</th><th className="th">Tokens</th><th className="th">Done</th><th className="th">Avg wait</th><th className="th">Avg service</th></tr></thead>
                <tbody className="divide-y divide-line">
                  {data.offices.map((o) => (
                    <tr key={o.serviceId}>
                      <td className="td">{o.prefix} - {o.name}</td><td className="td tabular-nums">{o.tickets}</td><td className="td tabular-nums">{o.completed}</td>
                      <td className="td">{formatWait(o.avgWaitMinutes)}</td><td className="td">{formatWait(o.avgServiceMinutes)}</td>
                    </tr>
                  ))}
                </tbody>
              </table>
            </section>
            <section className="card overflow-x-auto">
              <h2 className="px-4 pt-4 text-lg font-bold">Counters</h2>
              <table className="mt-2 w-full min-w-[320px]">
                <thead className="border-b border-line"><tr><th className="th">Counter</th><th className="th">Served</th><th className="th">Avg service</th></tr></thead>
                <tbody className="divide-y divide-line">
                  {data.counters.map((c) => (
                    <tr key={c.counterId}><td className="td">{c.name}</td><td className="td tabular-nums">{c.served}</td><td className="td">{formatWait(c.avgServiceMinutes)}</td></tr>
                  ))}
                </tbody>
              </table>
            </section>
          </div>
        </>
      )}
    </div>
  );
}
