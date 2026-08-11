import { defineConfig } from 'vite';
import react from '@vitejs/plugin-react';

// https://vite.dev/config/
export default defineConfig(async () => {
  const plugins = [react()];
  if (!process.env.NO_CLOUDFLARE) {
    try {
      const { cloudflare } = await import("@cloudflare/vite-plugin");
      plugins.push(cloudflare());
    } catch (e) {
      console.warn("Could not load Cloudflare plugin, running without it.");
    }
  }
  return {
    plugins,
  };
});