import { defineConfig } from "vite";
import react from "@vitejs/plugin-react";
export default defineConfig({
    plugins: [react()],
    server: {
        port: 5173,
        proxy: {
            "/api": {
                target: process.env.BACKEND_URL || "https://travian-interceptor-backend-latest.onrender.com",
                changeOrigin: true,
                secure: true,
                rewrite: (path) => path.replace(/^\/api/, "/api"),
            },
        },
    },
});
