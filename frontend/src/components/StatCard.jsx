export default function StatCard({ label, value, hint }) {
  return (
    <div className="card group p-5 transition duration-200 hover:-translate-y-0.5 hover:border-teal/25 hover:shadow-md">
      <p className="text-sm font-medium text-muted">{label}</p>
      <p className="mt-2 font-display text-3xl font-bold tabular-nums text-teal-dark">{value}</p>
      {hint && <p className="mt-1 text-xs text-muted">{hint}</p>}
    </div>
  );
}
