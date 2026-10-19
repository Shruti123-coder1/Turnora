export const API_URL = import.meta.env.VITE_API_URL || 'http://localhost:8080';

export const TOKEN_KEY = 'turnora_token';
export const USER_KEY = 'turnora_user';
export const LAST_TOKEN_KEY = 'turnora_last_token';

export const ROLE_HOME = { STUDENT: '/student', STAFF: '/staff', ADMIN: '/admin' };

export const STATUS_META = {
  WAITING: { label: 'Waiting', cls: 'bg-sky-100 text-sky-900' },
  CALLED: { label: 'Called', cls: 'bg-sky-100 text-sky-900' },
  SERVING: { label: 'Being served', cls: 'bg-sky-200 text-sky-900' },
  COMPLETED: { label: 'Completed', cls: 'bg-emerald-100 text-emerald-900' },
  NO_SHOW: { label: 'No show', cls: 'bg-rose-100 text-rose-900' },
  CANCELLED: { label: 'Cancelled', cls: 'bg-stone-200 text-stone-700' },
};

export const LIVE_STATUSES = ['WAITING', 'CALLED', 'SERVING'];

export const PRIORITIES = [
  { value: 'NORMAL', label: 'Regular' },
  { value: 'SENIOR_CITIZEN', label: 'Senior citizen' },
  { value: 'DIFFERENTLY_ABLED', label: 'Differently abled' },
];

export const COUNTER_STATUSES = ['ACTIVE', 'PAUSED', 'OFFLINE'];
