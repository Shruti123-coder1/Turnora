export default function Loader({ full = false, label = 'Loading' }) {
  const spinner = (
    <div role="status" className="flex items-center gap-3 text-sm text-muted">
      <span className="h-5 w-5 animate-spin rounded-full border-2 border-line border-t-teal" />
      {label}
    </div>
  );
  return full ? <div className="flex min-h-screen items-center justify-center">{spinner}</div> : <div className="py-10">{spinner}</div>;
}