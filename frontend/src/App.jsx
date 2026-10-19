import { Route, Routes } from 'react-router-dom';
import PrivateRoute from './components/PrivateRoute';
import DashboardLayout from './layouts/DashboardLayout';
import PublicLayout from './layouts/PublicLayout';

import Home from './pages/public/Home';
import Login from './pages/public/Login';
import Register from './pages/public/Register';
import JoinQueue from './pages/public/JoinQueue';
import TrackToken from './pages/public/TrackToken';
import DisplayBoard from './pages/public/DisplayBoard';
import NotFound from './pages/public/NotFound';

import StudentDashboard from './pages/student/Dashboard';
import MyTokens from './pages/student/MyTokens';
import Notifications from './pages/student/Notifications';

import CounterScreen from './pages/staff/CounterScreen';

import AdminDashboard from './pages/admin/Dashboard';
import ManageServices from './pages/admin/ManageServices';
import ManageCounters from './pages/admin/ManageCounters';
import ManageStaff from './pages/admin/ManageStaff';
import History from './pages/admin/History';
import Profile from './pages/common/Profile';

export default function App() {
  return (
    <Routes>
      <Route element={<PublicLayout />}>
        <Route index element={<Home />} />
        <Route path="join" element={<JoinQueue />} />
        <Route path="track" element={<TrackToken />} />
        <Route path="track/:token" element={<TrackToken />} />
        <Route path="login" element={<Login />} />
        <Route path="register" element={<Register />} />
        <Route path="*" element={<NotFound />} />
      </Route>

      <Route path="board" element={<DisplayBoard />} />

      <Route element={<PrivateRoute roles={['STUDENT']} />}>
        <Route path="student" element={<DashboardLayout />}>
          <Route index element={<StudentDashboard />} />
          <Route path="tokens" element={<MyTokens />} />
          <Route path="notifications" element={<Notifications />} />
        </Route>
      </Route>

      <Route element={<PrivateRoute roles={['STAFF', 'ADMIN']} />}>
        <Route path="staff" element={<DashboardLayout />}>
          <Route index element={<CounterScreen />} />
          <Route path="notifications" element={<Notifications />} />
        </Route>
      </Route>

      <Route element={<PrivateRoute roles={['ADMIN']} />}>
        <Route path="admin" element={<DashboardLayout />}>
          <Route index element={<AdminDashboard />} />
          <Route path="services" element={<ManageServices />} />
          <Route path="counters" element={<ManageCounters />} />
          <Route path="staff" element={<ManageStaff />} />
          <Route path="notifications" element={<Notifications />} />
          <Route path="history" element={<History />} />
        </Route>
      </Route>

      <Route element={<PrivateRoute roles={['STUDENT', 'STAFF', 'ADMIN']} />}>
        <Route path="profile" element={<DashboardLayout />}>
          <Route index element={<Profile />} />
        </Route>
      </Route>
    </Routes>
  );
}
