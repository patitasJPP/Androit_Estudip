-- RF-10: permite al Administrador SAE activo consultar las firmas de entregas.
-- El bucket sigue siendo privado; no se concede lectura a otros roles.
drop policy if exists "Firma SAE lectura acta" on storage.objects;
create policy "Firma SAE lectura acta" on storage.objects
  for select to authenticated
  using (
    bucket_id = 'evidencias-utp-objects'
    and name like 'firmas/%'
    and public.es_admin_sae()
  );
