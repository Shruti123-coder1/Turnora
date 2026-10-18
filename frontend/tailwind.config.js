/** @type {import('tailwindcss').Config} */
export default {
  content: ['./index.html', './src/**/*.{js,jsx}'],
  theme: {
    extend: {
      colors: {
        ink: '#16211F',
        paper: '#F5F6F2',
        line: '#DADFD8',
        muted: '#5D6B67',
        teal: { DEFAULT: '#0D5C58', dark: '#08403D', soft: '#DCEBE8' },
        signal: { DEFAULT: '#F2B705', soft: '#FBEFC2' },
        alert: '#B42318',
      },
      fontFamily: {
        display: ['"Bricolage Grotesque"', 'ui-sans-serif', 'system-ui', 'sans-serif'],
        body: ['"IBM Plex Sans"', 'ui-sans-serif', 'system-ui', 'sans-serif'],
      },
    },
  },
  plugins: [],
};