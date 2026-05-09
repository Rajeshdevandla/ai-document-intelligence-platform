/** @type {import('tailwindcss').Config} */
module.exports = {
  content: ['./src/**/*.{ts,tsx,js,jsx}', './public/index.html'],
  theme: {
    extend: {
      colors: {
        gray: {
          950: '#030712',
        },
      },
    },
  },
  plugins: [],
};
