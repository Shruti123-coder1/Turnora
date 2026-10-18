const parse = (v) => (v ? new Date(v) : null);

export const formatTime = (v) => {
  const d = parse(v);
  return d ? d.toLocaleTimeString('en-IN', { hour: 'numeric', minute: '2-digit' }) : '-';
};

export const formatDate = (v) => {
  const d = parse(v);
  return d ? d.toLocaleDateString('en-IN', { day: 'numeric', month: 'short', year: 'numeric' }) : '-';
};

export const formatDateTime = (v) => (v ? `${formatDate(v)}, ${formatTime(v)}` : '-');

export const formatWait = (minutes) => {
  if (minutes === null || minutes === undefined) return '-';
  if (minutes < 1) return 'Under 1 min';
  return `${Math.round(minutes)} min`;
};

// yyyy-mm-dd in local time (used by date inputs)
export const todayISO = () => new Date().toLocaleDateString('en-CA');

// SENIOR_CITIZEN -> Senior citizen
export const labelOf = (value) =>
  value ? value.replaceAll('_', ' ').toLowerCase().replace(/^\w/, (c) => c.toUpperCase()) : '';