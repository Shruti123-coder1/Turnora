import { STATUS_META } from '../utils/constants';
import { labelOf } from '../utils/formatTime';

export default function StatusBadge({ status }) {
  const meta = STATUS_META[status] || { label: labelOf(status), cls: 'bg-stone-200 text-stone-700' };
  return <span className={`inline-block rounded-full px-2.5 py-0.5 text-xs font-medium ${meta.cls}`}>{meta.label}</span>;
}