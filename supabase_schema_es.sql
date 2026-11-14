-- Esquema inicial de UTP+objects (Supabase/PostgreSQL)
-- Nombres de tablas y columnas en español.

create extension if not exists pgcrypto;

create table if not exists public.usuarios (
  id uuid primary key references auth.users(id) on delete cascade,
  nombre_completo text not null,
  nombre_usuario text,
  correo text unique,
  rol text not null check (rol in ('PERSONAL_REGISTRADOR','ADMINISTRADOR_SAE')),
  activo boolean not null default true,
  creado_en timestamptz not null default now(),
  actualizado_en timestamptz not null default now()
);

create table if not exists public.ubicaciones_custodia (
  id uuid primary key default gen_random_uuid(),
  codigo text not null unique,
  tipo text not null check (tipo in ('CAJA','ANAQUEL')),
  descripcion text,
  disponible boolean not null default true,
  creado_en timestamptz not null default now()
);

create table if not exists public.objetos_encontrados (
  id uuid primary key default gen_random_uuid(),
  codigo_custodia text unique,
  categoria text not null,
  descripcion text not null,
  caracteristicas_reservadas text,
  lugar_hallazgo text,
  fecha_hallazgo timestamptz not null default now(),
  contacto_hallador text,
  fotografia_url text,
  estado text not null default 'HALLAZGO_REPORTADO' check (estado in ('HALLAZGO_REPORTADO','BAJO_CUSTODIA','RECLAMADO','ENTREGADO','ANULADO')),
  ubicacion_id uuid references public.ubicaciones_custodia(id),
  registrado_por uuid not null references public.usuarios(id),
  recibido_por uuid references public.usuarios(id),
  recibido_en timestamptz,
  creado_en timestamptz not null default now(),
  actualizado_en timestamptz not null default now()
);

create table if not exists public.reportes_perdida (
  id uuid primary key default gen_random_uuid(),
  codigo_reporte text not null unique,
  persona_atendida text not null,
  identificador_persona text,
  contacto text not null,
  descripcion text not null,
  lugar_aproximado text,
  estado text not null default 'BUSCANDO' check (estado in ('BUSCANDO','COINCIDENCIA_SUGERIDA','RECUPERADO','CERRADO')),
  registrado_por uuid not null references public.usuarios(id),
  creado_en timestamptz not null default now(),
  actualizado_en timestamptz not null default now()
);

create table if not exists public.reclamaciones (
  id uuid primary key default gen_random_uuid(),
  codigo_reclamacion text not null unique,
  reporte_perdida_id uuid not null references public.reportes_perdida(id),
  objeto_encontrado_id uuid not null references public.objetos_encontrados(id),
  solicitante text not null,
  identificador_solicitante text,
  evidencia_aportada text,
  preguntas_verificacion text,
  decision text not null default 'PENDIENTE_VERIFICACION' check (decision in ('PENDIENTE_VERIFICACION','APROBADA','RECHAZADA')),
  motivo_decision text,
  revisada_por uuid references public.usuarios(id),
  revisada_en timestamptz,
  creado_en timestamptz not null default now(),
  actualizado_en timestamptz not null default now()
);

create table if not exists public.entregas (
  id uuid primary key default gen_random_uuid(),
  codigo_entrega text not null unique,
  reclamacion_id uuid not null references public.reclamaciones(id),
  objeto_encontrado_id uuid not null references public.objetos_encontrados(id),
  receptor text not null,
  dni_receptor text,
  correo_receptor text,
  firma_url text,
  entregado_por uuid not null references public.usuarios(id),
  entregado_en timestamptz not null default now(),
  estado text not null default 'ENTREGADA' check (estado in ('PENDIENTE','ENTREGADA','ANULADA')),
  creado_en timestamptz not null default now()
);

create table if not exists public.avisos (
  id uuid primary key default gen_random_uuid(),
  usuario_id uuid not null references public.usuarios(id) on delete cascade,
  tipo text not null,
  titulo text not null,
  mensaje text not null,
  leido boolean not null default false,
  referencia_tipo text,
  referencia_id uuid,
  creado_en timestamptz not null default now()
);

create table if not exists public.auditoria (
  id bigint generated always as identity primary key,
  usuario_id uuid references public.usuarios(id),
  accion text not null,
  entidad text not null,
  entidad_id uuid,
  detalle jsonb,
  creado_en timestamptz not null default now()
);

create index if not exists idx_objetos_estado on public.objetos_encontrados(estado);
create index if not exists idx_objetos_ubicacion on public.objetos_encontrados(ubicacion_id);
create index if not exists idx_reportes_estado on public.reportes_perdida(estado);
create index if not exists idx_reclamaciones_decision on public.reclamaciones(decision);
create index if not exists idx_avisos_usuario on public.avisos(usuario_id, leido);

-- Datos mínimos para ubicaciones físicas.
insert into public.ubicaciones_custodia (codigo, tipo, descripcion)
values ('CAJA-A01','CAJA','Caja principal de objetos recibidos'),
       ('ANAQUEL-B02','ANAQUEL','Anaquel de custodia B02')
on conflict (codigo) do nothing;

-- Bucket privado para fotos y firmas.
insert into storage.buckets (id, name, public)
values ('evidencias-utp-objects', 'evidencias-utp-objects', false)
on conflict (id) do nothing;

-- RLS: el backend/cliente autenticado solo opera con sesión válida.
alter table public.usuarios enable row level security;
alter table public.ubicaciones_custodia enable row level security;
alter table public.objetos_encontrados enable row level security;
alter table public.reportes_perdida enable row level security;
alter table public.reclamaciones enable row level security;
alter table public.entregas enable row level security;
alter table public.avisos enable row level security;
alter table public.auditoria enable row level security;

do $$
declare t text;
begin
  foreach t in array array['usuarios','ubicaciones_custodia','objetos_encontrados','reportes_perdida','reclamaciones','entregas','avisos','auditoria'] loop
    execute format('drop policy if exists acceso_autenticado_%s on public.%I', t, t);
    execute format('create policy acceso_autenticado_%s on public.%I for all to authenticated using (true) with check (true)', t, t);
  end loop;
end $$;

-- Habilita eventos Realtime para sincronización entre roles.
do $$ begin
  alter publication supabase_realtime add table public.objetos_encontrados;
exception when duplicate_object then null; end $$;
do $$ begin
  alter publication supabase_realtime add table public.reportes_perdida;
exception when duplicate_object then null; end $$;
do $$ begin
  alter publication supabase_realtime add table public.reclamaciones;
exception when duplicate_object then null; end $$;
do $$ begin
  alter publication supabase_realtime add table public.entregas;
exception when duplicate_object then null; end $$;
do $$ begin
  alter publication supabase_realtime add table public.avisos;
exception when duplicate_object then null; end $$;
