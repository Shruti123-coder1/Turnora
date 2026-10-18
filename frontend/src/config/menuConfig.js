export const MENU = {
  STUDENT: [
    { to: '/student', label: 'Overview', end: true },
    { to: '/student/tokens', label: 'My tokens' },
    { to: '/student/notifications', label: 'Notifications' },
    { to: '/join', label: 'Join a queue' },
  ],
  STAFF: [
    { to: '/staff', label: 'Counter screen', end: true },
    { to: '/board', label: 'Display board' },
  ],
  ADMIN: [
    { to: '/admin', label: 'Overview', end: true },
    { to: '/admin/services', label: 'Offices' },
    { to: '/admin/counters', label: 'Counters' },
    { to: '/admin/staff', label: 'Staff' },
    { to: '/admin/history', label: 'Queue history' },
    { to: '/staff', label: 'Counter screen' },
  ],
};