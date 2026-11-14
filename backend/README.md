# UTP+objects API

Backend opcional Node.js/Express para centralizar operaciones sobre Supabase.

1. Copia `.env.example` a `.env`.
2. Completa `SUPABASE_SERVICE_ROLE_KEY` únicamente en el servidor.
3. Ejecuta `npm install` y `npm run dev`.

La aplicación Android nunca debe recibir la service-role key; para ella se usa la API REST de Supabase con la clave anon y sesión autenticada.
