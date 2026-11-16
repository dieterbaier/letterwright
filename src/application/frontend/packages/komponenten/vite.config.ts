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
      fileName: 'standardschreiben',
      cssFileName: 'standardschreiben',
    },
    rolldownOptions: {
      // React stellt der Host bereit (Peer-Abhängigkeit).
      external: ['react', 'react-dom', 'react/jsx-runtime'],
    },
  },
  test: {
    environment: 'jsdom',
    include: ['src/**/*.test.{ts,tsx}'],
    coverage: {
      provider: 'v8',
      include: ['src/**/*.{ts,tsx}'],
      // Testhilfen gehören nicht zum ausgelieferten Paket.
      exclude: ['src/**/*.test.{ts,tsx}', 'src/test/**'],
      reporter: ['text', 'html'],
      // ADR-011: mindestens 80 % Zeilen- und Zweigabdeckung je Paket.
      thresholds: {
        lines: 80,
        branches: 80,
      },
    },
  },
});
