import StatusBadge from './StatusBadge';
import { formatWait, labelOf } from '../utils/formatTime';

function message(t) {
  switch (t.status) {
    case 'WAITING':
      return t.position ? `You are number ${t.position} in line.` : 'You are in the queue.';
    case 'CALLED':
      return `Please go to ${t.counterName || 'the counter'} now.`;
    case 'SERVING':
      return `You are being served at ${t.counterName || 'the counter'}.`;
    case 'COMPLETED':
      return 'This token is completed.';
    case 'NO_SHOW':
      return 'You were not at the counter when called. Join the queue again if you still need help.';
    case 'CANCELLED':
      return 'This token was cancelled.';
    default:
      return '';
  }
}

// A queue token shaped like a paper ticket. onCancel is optional (shows a cancel button for waiting tokens).
export default function TokenCard({ ticket, onCancel }) {
  const waiting = ticket.status === 'WAITING';
  return (
    <article className="card relative overflow-hidden">
      <div className="flex items-start justify-between gap-3 p-5">
        <div>
          <p className="text-sm text-muted">{ticket.serviceName}</p>
          <p className="mt-1 font-display text-5xl font-bold tabular-nums">{ticket.tokenNumber}</p>
          {ticket.customerName && <p className="mt-1 text-sm">{ticket.customerName}</p>}
          {ticket.priorityType && ticket.priorityType !== 'NORMAL' && (
            <p className="mt-1 text-xs text-muted">{labelOf(ticket.priorityType)} priority</p>
          )}
        </div>
        <StatusBadge status={ticket.status} />
      </div>

      <div className="relative">
        <div className="border-t-2 border-dashed border-line" />
        <span className="absolute -left-3 -top-3 h-6 w-6 rounded-full border border-line bg-paper" />
        <span className="absolute -right-3 -top-3 h-6 w-6 rounded-full border border-line bg-paper" />
      </div>

      <div className="p-5">
        {waiting && (
          <dl className="grid grid-cols-3 gap-3 text-sm">
            <div>
              <dt className="text-muted">Position</dt>
              <dd className="font-display text-xl font-bold tabular-nums">{ticket.position ?? '-'}</dd>
            </div>
            <div>
              <dt className="text-muted">People ahead</dt>
              <dd className="font-display text-xl font-bold tabular-nums">{ticket.peopleAhead ?? '-'}</dd>
            </div>
            <div>
              <dt className="text-muted">Est. wait</dt>
              <dd className="font-display text-xl font-bold">{formatWait(ticket.estimatedWaitMinutes)}</dd>
            </div>
          </dl>
        )}
        <p className={`text-sm ${waiting ? 'mt-3' : ''}`}>{message(ticket)}</p>
        {waiting && onCancel && (
          <button type="button" className="btn btn-ghost mt-4" onClick={() => onCancel(ticket)}>
            Cancel token
          </button>
        )}
      </div>
    </article>
  );
}