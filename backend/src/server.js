import 'dotenv/config';
import express from 'express';
import cors from 'cors';
import morgan from 'morgan';
import { createClient } from '@supabase/supabase-js';

const app = express();
app.use(cors());
app.use(express.json({ limit: '2mb' }));
app.use(morgan('dev'));

const supabaseUrl = process.env.SUPABASE_URL;
const serviceKey = process.env.SUPABASE_SERVICE_ROLE_KEY;
if (!supabaseUrl || !serviceKey) throw new Error('Configura SUPABASE_URL y SUPABASE_SERVICE_ROLE_KEY en .env');
const supabase = createClient(supabaseUrl, serviceKey, { auth: { persistSession: false } });

app.get('/api/salud', (_req, res) => res.json({ ok: true, servicio: 'UTP+objects API' }));

function fail(res, error) {
  console.error(error);
  return res.status(400).json({ ok: false, mensaje: error.message ?? 'Error de datos' });
}

app.get('/api/objetos', async (req, res) => {
  let query = supabase.from('objetos_encontrados').select('*').order('creado_en', { ascending: false });
  if (req.query.estado) query = query.eq('estado', req.query.estado);
  const { data, error } = await query;
  if (error) return fail(res, error);
  return res.json({ ok: true, datos: data });
});

app.post('/api/objetos', async (req, res) => {
  const { data, error } = await supabase.from('objetos_encontrados').insert(req.body).select().single();
  if (error) return fail(res, error);
  return res.status(201).json({ ok: true, datos: data });
});

app.get('/api/reportes-perdida', async (_req, res) => {
  const { data, error } = await supabase.from('reportes_perdida').select('*').order('creado_en', { ascending: false });
  if (error) return fail(res, error);
  return res.json({ ok: true, datos: data });
});

app.post('/api/reportes-perdida', async (req, res) => {
  const { data, error } = await supabase.from('reportes_perdida').insert(req.body).select().single();
  if (error) return fail(res, error);
  return res.status(201).json({ ok: true, datos: data });
});

app.get('/api/reclamaciones', async (_req, res) => {
  const { data, error } = await supabase.from('reclamaciones').select('*').order('creado_en', { ascending: false });
  if (error) return fail(res, error);
  return res.json({ ok: true, datos: data });
});

app.patch('/api/reclamaciones/:id', async (req, res) => {
  const { data, error } = await supabase.from('reclamaciones').update(req.body).eq('id', req.params.id).select().single();
  if (error) return fail(res, error);
  return res.json({ ok: true, datos: data });
});

app.get('/api/entregas', async (_req, res) => {
  const { data, error } = await supabase.from('entregas').select('*').order('entregado_en', { ascending: false });
  if (error) return fail(res, error);
  return res.json({ ok: true, datos: data });
});

app.get('/api/avisos/:usuarioId', async (req, res) => {
  const { data, error } = await supabase.from('avisos').select('*').eq('usuario_id', req.params.usuarioId).order('creado_en', { ascending: false });
  if (error) return fail(res, error);
  return res.json({ ok: true, datos: data });
});

app.listen(Number(process.env.PORT || 3000), () => console.log(`UTP+objects API escuchando en puerto ${process.env.PORT || 3000}`));
