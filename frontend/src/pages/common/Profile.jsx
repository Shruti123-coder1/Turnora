import { useState } from 'react';
import { authApi } from '../../api';
import { errorMessage } from '../../api/axiosConfig';
import { useAuth } from '../../context/AuthContext';
import { labelOf } from '../../utils/formatTime';

const EyeIcon = ({ open }) => (
  <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2" className="h-5 w-5" aria-hidden="true">
    {open ? <><path d="M2 12s3.5-7 10-7 10 7 10 7-3.5 7-10 7-10-7-10-7Z" /><circle cx="12" cy="12" r="3" /></> : <><path d="m3 3 18 18" /><path d="M10.6 10.6a2 2 0 0 0 2.8 2.8" /><path d="M9.9 4.2A10.8 10.8 0 0 1 12 4c6.5 0 10 8 10 8a18.2 18.2 0 0 1-3.2 4.2M6.6 6.6C3.8 8.5 2 12 2 12s3.5 8 10 8a9.8 9.8 0 0 0 3.1-.5" /></>}
  </svg>
);

const Notice = ({ error, success }) => {
  if (!error && !success) return null;
  return <p role={error ? 'alert' : 'status'} className={`rounded-lg px-3 py-2 text-sm ${error ? 'bg-rose-50 text-alert' : 'bg-sky-50 text-sky-800'}`}>{error || success}</p>;
};

export default function Profile() {
  const { user, refreshUser } = useAuth();
  const isStudent = user?.role === 'STUDENT';
  const [editing, setEditing] = useState(false);
  const [profile, setProfile] = useState(() => ({ fullName: user?.fullName || '', phone: user?.phone || '', prn: user?.prn || '' }));
  const [profileError, setProfileError] = useState('');
  const [profileSuccess, setProfileSuccess] = useState('');
  const [profileBusy, setProfileBusy] = useState(false);
  const [password, setPassword] = useState({ currentPassword: '', newPassword: '', confirmPassword: '' });
  const [passwordError, setPasswordError] = useState('');
  const [passwordSuccess, setPasswordSuccess] = useState('');
  const [passwordBusy, setPasswordBusy] = useState(false);
  const [showPasswords, setShowPasswords] = useState(false);

  const initials = (user?.fullName || 'User').split(/\s+/).filter(Boolean).slice(0, 2).map((part) => part[0]).join('').toUpperCase();
  const updateProfileField = (event) => setProfile((current) => ({ ...current, [event.target.name]: event.target.value }));
  const updatePasswordField = (event) => setPassword((current) => ({ ...current, [event.target.name]: event.target.value }));

  const cancelEdit = () => {
    setProfile({ fullName: user?.fullName || '', phone: user?.phone || '', prn: user?.prn || '' });
    setProfileError('');
    setEditing(false);
  };

  const saveProfile = async (event) => {
    event.preventDefault();
    setProfileError('');
    setProfileSuccess('');
    setProfileBusy(true);
    try {
      await authApi.updateProfile(profile);
      await refreshUser();
      setProfileSuccess('Personal details saved.');
      setEditing(false);
    } catch (err) {
      setProfileError(errorMessage(err));
    } finally {
      setProfileBusy(false);
    }
  };

  const savePassword = async (event) => {
    event.preventDefault();
    setPasswordError('');
    setPasswordSuccess('');
    if (password.newPassword !== password.confirmPassword) {
      setPasswordError('New password and confirmation must match.');
      return;
    }
    setPasswordBusy(true);
    try {
      const response = await authApi.changePassword({ currentPassword: password.currentPassword, newPassword: password.newPassword });
      setPasswordSuccess(response.message || 'Password changed.');
      setPassword({ currentPassword: '', newPassword: '', confirmPassword: '' });
    } catch (err) {
      setPasswordError(errorMessage(err));
    } finally {
      setPasswordBusy(false);
    }
  };

  return (
    <div className="mx-auto max-w-4xl space-y-6">
      <div className="card flex flex-col gap-4 p-5 sm:flex-row sm:items-center sm:p-6">
        <span className="grid h-16 w-16 place-items-center rounded-full bg-sky-100 text-xl font-bold text-sky-800">{initials}</span>
        <div>
          <h1 className="page-heading">{user?.fullName}</h1>
          <p className="mt-1 text-sm text-muted">{user?.email}</p>
          <span className="mt-3 inline-flex rounded-full bg-sky-100 px-2.5 py-1 text-xs font-bold text-sky-800">{labelOf(user?.role)}</span>
        </div>
      </div>

      <section className="card p-5 sm:p-6">
        <div className="mb-5 flex items-center justify-between gap-3">
          <div><h2 className="text-xl font-bold">Personal details</h2><p className="mt-1 text-sm text-muted">Keep your contact information current.</p></div>
          {!editing && <button type="button" className="btn btn-primary" onClick={() => { setProfileSuccess(''); setEditing(true); }}>Edit</button>}
        </div>
        <Notice error={profileError} success={profileSuccess} />
        <form className="mt-4 space-y-4" onSubmit={saveProfile}>
          <div className="grid gap-4 sm:grid-cols-2">
            <div><label className="label" htmlFor="fullName">Full name</label><input id="fullName" name="fullName" className="input" value={profile.fullName} onChange={updateProfileField} disabled={!editing} required maxLength="100" /></div>
            <div><label className="label" htmlFor="phone">Phone</label><input id="phone" name="phone" type="tel" inputMode="numeric" className="input" value={profile.phone} onChange={updateProfileField} disabled={!editing} required pattern="[0-9]{10}" maxLength="10" /></div>
            {isStudent && <div><label className="label" htmlFor="prn">PRN</label><input id="prn" name="prn" className="input" value={profile.prn} onChange={updateProfileField} disabled={!editing} maxLength="30" /></div>}
            <div><label className="label" htmlFor="email">Email</label><input id="email" className="input bg-sky-50" value={user?.email || ''} readOnly /></div>
          </div>
          {editing && <div className="flex flex-wrap gap-3"><button className="btn btn-primary" disabled={profileBusy}>{profileBusy ? 'Saving…' : 'Save'}</button><button type="button" className="btn btn-ghost" onClick={cancelEdit} disabled={profileBusy}>Cancel</button></div>}
        </form>
      </section>

      <section className="card p-5 sm:p-6">
        <h2 className="text-xl font-bold">Change password</h2>
        <p className="mt-1 text-sm text-muted">Use a new password with 6 to 72 characters.</p>
        <form className="mt-5 max-w-xl space-y-4" onSubmit={savePassword}>
          <Notice error={passwordError} success={passwordSuccess} />
          {['currentPassword', 'newPassword', 'confirmPassword'].map((field) => {
            const labels = { currentPassword: 'Current password', newPassword: 'New password', confirmPassword: 'Confirm new password' };
            return <div key={field}><label className="label" htmlFor={field}>{labels[field]}</label><div className="relative"><input id={field} name={field} type={showPasswords ? 'text' : 'password'} className="input pr-12" value={password[field]} onChange={updatePasswordField} autoComplete={field === 'currentPassword' ? 'current-password' : 'new-password'} required minLength="6" maxLength="72" /><button type="button" className="absolute inset-y-0 right-0 grid w-11 place-items-center text-muted hover:text-sky-700" onClick={() => setShowPasswords((shown) => !shown)} aria-label={showPasswords ? 'Hide passwords' : 'Show passwords'}><EyeIcon open={showPasswords} /></button></div></div>;
          })}
          <button className="btn btn-primary" disabled={passwordBusy}>{passwordBusy ? 'Changing…' : 'Change password'}</button>
        </form>
      </section>
    </div>
  );
}
