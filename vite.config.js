import { defineConfig } from "vite";
import react from "@vitejs/plugin-react";

export default defineConfig({
  base: "/interceptor/",
  plugins: [react()],
  server: {
    port: 5173,
    proxy: {
      "/api": {
        target: "https://travian-interceptor-backend-latest.onrender.com",
        changeOrigin: true,
        secure: true,
        rewrite: (path) => path.replace(/^/api/, "/api"),
      },
    },
  },
});
