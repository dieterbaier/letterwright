import { resolve } from 'node:path';
import react from '@vitejs/plugin-react';
import UnoCSS from 'unocss/vite';
import { defineConfig } from 'vitest/config';

export default defineConfig({
  plugins: [react(), UnoCSS()],
  build: {
    lib: {
      entry: resolve(import.meta.dirname, 'src/index.ts'),
      formats: ['es'],
      fileName: 'letterwright',
      cssFileName: 'letterwright',
    },
    rolldownOptions: {
      // The host provides React (peer dependency).
      external: ['react', 'react-dom', 'react/jsx-runtime'],
    },
  },
  test: {
    environment: 'jsdom',
    include: ['src/**/*.test.{ts,tsx}'],
    coverage: {
      provider: 'v8',
      include: ['src/**/*.{ts,tsx}'],
      // Test helpers are not part of the shipped package.
      exclude: ['src/**/*.test.{ts,tsx}', 'src/test/**'],
      reporter: ['text', 'html'],
      // ADR-011: at least 80 % line and branch coverage per package.
      thresholds: {
        lines: 80,
        branches: 80,
      },
    },
  },
});
