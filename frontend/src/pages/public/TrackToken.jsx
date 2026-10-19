import { useCallback, useEffect, useState } from 'react';
import { useNavigate, useParams } from 'react-router-dom';
import { ticketApi } from '../../api';
import { errorMessage } from '../../api/axiosConfig';
import Loader from '../../components/Loader';
import TokenCard from '../../components/TokenCard';
import useStomp from '../../hooks/useStomp';
import { LAST_TOKEN_KEY } from '../../utils/constants';

export default function TrackToken() {
  const { token } = useParams();
  const navigate = useNavigate();
  const [input, setInput] = useState(token || localStorage.getItem(LAST_TOKEN_KEY) || '');
  const [ticket, setTicket] = useState(null);
  const [error, setError] = useState('');
  const [loading, setLoading] = useState(false);

  const load = useCallback((value) => {
    setLoading(true);
    setError('');
    ticketApi
      .track(value)
      .then(setTicket)
      .catch((e) => {
        setTicket(null);
        setError(errorMessage(e));
      })
      .finally(() => setLoading(false));
  }, []);

  useEffect(() => {
    if (token) {
      setInput(token);
      load(token);
    } else {
      setTicket(null);
    }
  }, [token, load]);

  // Live position and status of this token
  useStomp(ticket ? `/topic/ticket/${ticket.tokenNumber}` : null, (msg) => {
    if (msg && typeof msg === 'object' && msg.tokenNumber) {
      setTicket((prev) => ({ ...prev, ...msg }));
    }
  });

  const submit = (e) => {
    e.preventDefault();
    const value = input.trim().toUpperCase();
    if (value) navigate(`/track/${value}`);
  };

  return (
    <div className="mx-auto w-full max-w-3xl">
      <h1 className="page-heading">Track a token</h1>
      <p className="page-subtitle">See your live position and service status in one place.</p>
      <form onSubmit={submit} className="card mt-6 flex gap-3 p-4 sm:p-5">
        <label htmlFor="token" className="sr-only">Token number</label>
        <input id="token" className="input min-h-12 min-w-0 uppercase text-base" placeholder="For example F-014" value={input} onChange={(e) => setInput(e.target.value)} />
        <button className="btn btn-primary min-h-12 shrink-0 px-5 text-base">Track</button>
      </form>
      <p className="mt-2 text-sm text-muted">Only tokens taken today can be tracked.</p>

      <div className="mt-7 mx-auto max-w-2xl">
        {loading && <Loader />}
        {error && <p role="alert" className="text-sm text-alert">{error}</p>}
        {ticket && !loading && <TokenCard ticket={ticket} />}
      </div>
    </div>
  );
}
