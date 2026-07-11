/** @type {import('tailwindcss').Config} */
export default {
  content: ['./src/**/*.{astro,html,js,jsx,md,mdx,svelte,ts,tsx,vue}'],
  darkMode: 'class',
  theme: {
    extend: {
      colors: {
        primary: '#0B2E4E',
        accent: '#4FD1F9',
        light: {
          bg: '#F8FAFC',
          text: '#0B2E4E',
        },
        dark: {
          bg: '#020617',
          text: '#F8FAFC',
        },
      },
      fontFamily: {
        sans: ['Inter', 'sans-serif'],
      },
    },
  },
  plugins: [],
};
