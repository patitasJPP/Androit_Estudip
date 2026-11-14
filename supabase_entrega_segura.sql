-- Ejecutar una sola vez en SQL Editor de Supabase, después de supabase_schema_es.sql.
-- La entrega física queda registrada de forma atómica y solo por un Administrador SAE.

alter table public.reclamaciones drop constraint if exists reclamaciones_decision_check;
alter table public.reclamaciones add constraint reclamaciones_decision_check
  check (decision in ('PENDIENTE_VERIFICACION','APROBADA','RECHAZADA','ATENDIDA'));

create unique index if not exists una_entrega_confirmada_por_reclamacion
  on public.entregas(reclamacion_id) where estado = 'ENTREGADA';

create or replace function public.confirmar_entrega_segura(
  p_codigo_reclamacion text,
  p_codigo_entrega text,
  p_receptor text,
  p_dni_receptor text,
  p_correo_receptor text,
  p_firma_url text
)
returns uuid
language plpgsql
security definer
set search_path = public
as $$
declare
  v_reclamacion public.reclamaciones%rowtype;
  v_objeto public.objetos_encontrados%rowtype;
  v_entrega_id uuid;
begin
  if not exists (
    select 1 from public.usuarios
    where id = auth.uid() and rol = 'ADMINISTRADOR_SAE' and activo
  ) then
    raise exception 'Solo un Administrador SAE activo puede confirmar una entrega';
  end if;

  select * into v_reclamacion from public.reclamaciones
  where codigo_reclamacion = p_codigo_reclamacion for update;
  if not found then raise exception 'Reclamación no encontrada'; end if;

  select * into v_objeto from public.objetos_encontrados
  where id = v_reclamacion.objeto_encontrado_id for update;
  if not found then raise exception 'Objeto no encontrado'; end if;

  select id into v_entrega_id from public.entregas
  where codigo_entrega = p_codigo_entrega and reclamacion_id = v_reclamacion.id;
  if found then return v_entrega_id; end if;

  if v_reclamacion.decision <> 'APROBADA' then
    raise exception 'La reclamación debe estar aprobada';
  end if;
  if v_objeto.estado <> 'BAJO_CUSTODIA' then
    raise exception 'El objeto debe estar bajo custodia';
  end if;
  if exists (select 1 from public.entregas where reclamacion_id = v_reclamacion.id and estado = 'ENTREGADA') then
    raise exception 'Esta reclamación ya tiene una entrega confirmada';
  end if;
  if length(trim(p_receptor)) < 3 or p_dni_receptor !~ '^[0-9]{8}$'
     or position('@' in p_correo_receptor) < 2 or coalesce(p_firma_url, '') = '' then
    raise exception 'Datos del receptor o firma incompletos';
  end if;

  insert into public.entregas (
    codigo_entrega, reclamacion_id, objeto_encontrado_id,
    receptor, dni_receptor, correo_receptor, firma_url, entregado_por, estado
  ) values (
    p_codigo_entrega, v_reclamacion.id, v_objeto.id,
    trim(p_receptor), p_dni_receptor, p_correo_receptor,
    p_firma_url, auth.uid(), 'ENTREGADA'
  ) returning id into v_entrega_id;

  update public.objetos_encontrados set estado = 'ENTREGADO', actualizado_en = now()
  where id = v_objeto.id;
  update public.reclamaciones set decision = 'ATENDIDA', actualizado_en = now()
  where id = v_reclamacion.id;

  return v_entrega_id;
end;
$$;

revoke all on function public.confirmar_entrega_segura(text,text,text,text,text,text) from public;
grant execute on function public.confirmar_entrega_segura(text,text,text,text,text,text) to authenticated;

-- Mantiene el reporte de pérdida coherente con una entrega confirmada.
create or replace function public.marcar_reporte_recuperado()
returns trigger language plpgsql security definer set search_path = public as $$
begin
  if new.estado = 'ENTREGADA' then
    update public.reportes_perdida set estado = 'RECUPERADO', actualizado_en = now()
    where id = (select reporte_perdida_id from public.reclamaciones where id = new.reclamacion_id);
  end if;
  return new;
end;
$$;
drop trigger if exists entrega_recupera_reporte on public.entregas;
create trigger entrega_recupera_reporte after insert on public.entregas
for each row execute function public.marcar_reporte_recuperado();

drop policy if exists "Firma SAE autenticado" on storage.objects;
create policy "Firma SAE autenticado" on storage.objects
  for insert to authenticated
  with check (
    bucket_id = 'evidencias-utp-objects'
    and exists (
      select 1 from public.usuarios
      where id = auth.uid() and rol = 'ADMINISTRADOR_SAE' and activo
    )
  );

-- Cierra las políticas iniciales que permitían a cualquier cuenta autenticada
-- cambiar roles o marcar directamente un objeto como entregado.
create or replace function public.es_admin_sae()
returns boolean language sql stable security definer set search_path = public
as $$
  select exists (select 1 from public.usuarios
    where id = auth.uid() and rol = 'ADMINISTRADOR_SAE' and activo)
$$;
grant execute on function public.es_admin_sae() to authenticated;

drop policy if exists acceso_autenticado_usuarios on public.usuarios;
drop policy if exists usuarios_autenticados on public.usuarios;
drop policy if exists acceso_autenticado_objetos_encontrados on public.objetos_encontrados;
drop policy if exists objetos_autenticados on public.objetos_encontrados;
drop policy if exists acceso_autenticado_reportes_perdida on public.reportes_perdida;
drop policy if exists reportes_autenticados on public.reportes_perdida;
drop policy if exists acceso_autenticado_reclamaciones on public.reclamaciones;
drop policy if exists reclamaciones_autenticadas on public.reclamaciones;
drop policy if exists acceso_autenticado_entregas on public.entregas;
drop policy if exists entregas_autenticadas on public.entregas;

create policy usuarios_lectura on public.usuarios for select to authenticated
  using (id = auth.uid() or public.es_admin_sae());
create policy usuarios_edicion_admin on public.usuarios for update to authenticated
  using (public.es_admin_sae()) with check (public.es_admin_sae());

create policy objetos_lectura on public.objetos_encontrados for select to authenticated using (true);
create policy objetos_creacion on public.objetos_encontrados for insert to authenticated
  with check ((registrado_por = auth.uid() and estado = 'HALLAZGO_REPORTADO')
    or public.es_admin_sae());
create policy objetos_edicion_sae on public.objetos_encontrados for update to authenticated
  using (public.es_admin_sae()) with check (public.es_admin_sae());

create policy reportes_lectura on public.reportes_perdida for select to authenticated using (true);
create policy reportes_creacion on public.reportes_perdida for insert to authenticated
  with check (registrado_por = auth.uid());
create policy reportes_edicion on public.reportes_perdida for update to authenticated
  using (registrado_por = auth.uid() or public.es_admin_sae())
  with check (registrado_por = auth.uid() or public.es_admin_sae());

create policy reclamaciones_lectura on public.reclamaciones for select to authenticated using (true);
create policy reclamaciones_creacion on public.reclamaciones for insert to authenticated
  with check (decision = 'PENDIENTE_VERIFICACION');
create policy reclamaciones_edicion_sae on public.reclamaciones for update to authenticated
  using (public.es_admin_sae()) with check (public.es_admin_sae());

create policy entregas_lectura_sae on public.entregas for select to authenticated
  using (public.es_admin_sae());
