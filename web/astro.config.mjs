import {defineConfig} from 'astro/config';
import tailwindcss from '@tailwindcss/vite';

export default defineConfig({
    site: 'https://litebridgedb.org',
    vite: {
        plugins: [tailwindcss()],
    },
});
