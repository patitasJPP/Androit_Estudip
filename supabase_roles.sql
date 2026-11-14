-- Roles de operación para las cuentas creadas en Supabase Auth.
-- No contiene contraseñas.
alter table public.usuarios drop constraint if exists usuarios_nombre_usuario_key;
insert into public.usuarios (id, nombre_completo, nombre_usuario, correo, rol)
values
 ('c29418c1-6371-4a96-864a-085dbb08343e', 'Personal registrador', 'jair', 'floresgames6@gmail.com', 'PERSONAL_REGISTRADOR'),
 ('e8ccfdb0-0843-4597-888d-509897994b80', 'Administrador SAE', 'jair', 'yalliconoe@gmail.com', 'ADMINISTRADOR_SAE')
on conflict (id) do update set
 nombre_completo = excluded.nombre_completo,
 correo = excluded.correo,
 rol = excluded.rol;
