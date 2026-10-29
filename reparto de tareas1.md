# UTP+objects — reparto de tareas, RF y guía de integración

## Objetivo

Organizar el desarrollo de la aplicación por capacidades de negocio. Cada RF consolidado representa una pantalla principal o un flujo claramente identificable. Los 38 RF iniciales se agrupan para evitar que cada botón o campo sea tratado como una pantalla independiente.

## Roles oficiales

- **Personal registrador:** documenta hallazgos, registra pérdidas comunicadas presencialmente y consulta el seguimiento de los casos que originó.
- **Administrador SAE:** confirma la recepción física, administra la custodia, consulta el inventario, verifica reclamaciones, autoriza o rechaza devoluciones y administra el acceso de los registradores.

## RF funcionales consolidados

| Código | Requisito funcional | Pantalla o flujo | Rol principal |
|---|---|---|---|
| RF-01 | El sistema debe permitir autenticar al usuario y dirigirlo según el rol autorizado. | Inicio de sesión | Ambos |
| RF-02 | El sistema debe permitir al Personal registrador documentar un hallazgo con características, lugar, fecha, hora, contacto del hallador y evidencia disponible. | Documentar hallazgo | Personal registrador |
| RF-03 | El sistema debe permitir al Administrador SAE confirmar la recepción física, generar el código de custodia y registrar la ubicación del objeto. | Confirmar recepción y custodia | Administrador SAE |
| RF-04 | El sistema debe permitir al Administrador SAE consultar, filtrar y actualizar el inventario y la ubicación de los objetos bajo custodia. | Consultar inventario | Administrador SAE |
| RF-05 | El sistema debe permitir al Personal registrador registrar o reanudar un reporte de pérdida con descripción, lugar aproximado y datos de contacto. | Registrar o reanudar pérdida | Personal registrador |
| RF-06 | El sistema debe permitir consultar posibles coincidencias mediante filtros y conservar el resultado del caso. | Buscar coincidencias | Personal registrador |
| RF-07 | El sistema debe permitir al Administrador SAE revisar una reclamación, comparar la evidencia aportada, formular preguntas y registrar la decisión de verificación. | Verificar reclamación | Administrador SAE |
| RF-08 | El sistema debe permitir al Administrador SAE confirmar la devolución física, capturar la firma y actualizar los estados del objeto y de la reclamación. | Confirmar devolución | Administrador SAE |
| RF-09 | El sistema debe permitir al Administrador SAE consultar el historial de entregas realizadas y sus estados. | Historial de entregas | Administrador SAE |
| RF-10 | El sistema debe permitir al Administrador SAE generar y consultar el acta de entrega con receptor, DNI, correo, fecha, administrador responsable y firma capturada. | Acta de entrega | Administrador SAE |
| RF-11 | El sistema debe permitir al Administrador SAE habilitar, deshabilitar y restablecer el acceso de los registradores. | Administrar accesos | Administrador SAE |
| RF-12 | El sistema debe permitir consultar avisos y estados de los casos relacionados con el usuario autenticado. | Seguimiento y avisos | Ambos |

### Reglas de trazabilidad

- Los RF-P1 del proceso de hallazgos se trazan principalmente a RF-02, RF-03 y RF-04.
- Los RF-P2 del proceso de pérdidas se trazan principalmente a RF-05 y RF-06.
- Los RF-P3 del proceso de reclamación y devolución se trazan principalmente a RF-07, RF-08, RF-09 y RF-10.
- Los RF transversales se trazan a RF-01, RF-11 y RF-12.
- Los criterios de aceptación detallados de los 38 RF originales se conservan como criterios internos de estos 12 RF consolidados.

## Reglas de negocio comunes

1. La aplicación inicia directamente en Inicio de sesión; no existe una pantalla independiente de selección de rol.
2. Los nombres oficiales de rol son `PERSONAL_REGISTRADOR` y `ADMINISTRADOR_SAE`.
3. Un hallazgo reportado no se considera disponible hasta que el Administrador SAE confirme su recepción física.
4. El Personal registrador no puede autorizar ni ejecutar devoluciones.
5. La devolución es responsabilidad exclusiva del Administrador SAE.
6. El estado físico del objeto y el estado de la reclamación se mantienen separados.
7. La firma es una firma capturada en pantalla; no se implementan biometría, RENIEC, QR, hash ni firma certificada.
8. Las coincidencias son sugerencias y no autorizan por sí mismas la entrega.
9. Se conservan las reglas de custodia, ubicación en caja o anaquel y registro de responsables.
10. Se mantiene el diseño visual actual: tarjetas, colores UTP, iconografía, formularios y navegación inferior.

## Reparto de tareas

### Integrante 1 — NOE líder e integración

NOE es responsable de la arquitectura, autenticación y entrega integrada.

**Base ya iniciada:**

- La aplicación inicia en Login.
- Los roles son `PERSONAL_REGISTRADOR` y `ADMINISTRADOR_SAE`.
- La selección de rol ya no es una pantalla independiente.
- El Login dirige al flujo correspondiente.
- El flujo inicial de hallazgo ya enlaza con la confirmación de recepción y custodia.

**Archivos principales:**

- `LoginRole.kt`
- `DemoAuthRepository.kt`
- `LoginViewModel.kt`
- `LoginActivity.kt`
- `AndroidManifest.xml`
- navegación principal

**Responsabilidades pendientes:**

- Revisar la navegación entre módulos.
- Integrar los layouts de los demás integrantes.
- Resolver nombres de recursos y paquetes.
- Ejecutar pruebas de navegación y validación.
- Verificar que cada pantalla tenga un RF asignado.

### Integrante 2 — hallazgos y custodia (integrante por escoger)

Implementar RF-02 y RF-03.

**Flujo:**

1. El Personal registrador completa descripción, lugar, fecha, hora, contacto del hallador y evidencia.
2. El sistema guarda el caso como `Hallazgo reportado`.
3. El Administrador SAE revisa el caso y confirma la recepción física.
4. El sistema genera el código de custodia y permite elegir caja o anaquel.
5. El caso pasa a `Bajo custodia`.

**No implementar:**

- No entregar objetos.
- No crear cuentas.
- No usar QR ni biometría.

### Integrante 3 — pérdidas y coincidencias (integrante por escoger)

Implementar RF-05 y RF-06.

**Flujo:**

1. El Personal registrador registra la pérdida comunicada presencialmente.
2. El sistema conserva descripción, ubicación aproximada y contacto.
3. El usuario puede reanudar el reporte pendiente.
4. El sistema permite filtrar por pabellón, piso, aula, categoría, color, marca y modelo.
5. Las coincidencias son sugerencias; no autorizan una entrega.

**Estados:** `Sin coincidencia`, `Coincidencia sugerida` y `En seguimiento`.

**No implementar:**

- No contactar ni entregar objetos directamente al solicitante.

### Integrante 4 — verificación y devolución (integrante por escoger)

Implementar RF-07, RF-08, RF-09 y RF-10.

**Flujo:**

1. El Administrador SAE revisa la reclamación y la evidencia.
2. Formula preguntas sobre características no publicadas.
3. Decide `Aprobada`, `Pendiente de verificación` o `Rechazada`.
4. Si procede, captura la firma en pantalla y confirma la entrega física.
5. El sistema actualiza el objeto, la reclamación y el historial.
6. Se consulta el acta con DNI, correo, fecha y firma capturada.

**No implementar:**

- No usar biometría.
- No usar RENIEC.
- No usar QR.
- No usar hash.
- No usar firma certificada.

## Criterios de terminado para cualquier pantalla

- El título y el rol coinciden con esta guía.
- Los campos obligatorios tienen validación visible.
- Los botones muestran un resultado comprensible.
- Los estados del caso se actualizan en la interfaz.
- La pantalla no contiene textos de demostración que contradigan el flujo.
- La navegación de regreso y continuación funciona.
- El integrante documenta cualquier dato simulado o pendiente de backend.

## Estado de preparación del equipo

La base de integración de NOE ya está iniciada: la aplicación comienza en Login, utiliza los roles oficiales, dirige cada rol a su flujo y cuenta con el flujo inicial de documentación de hallazgo y confirmación de recepción.

Los integrantes 2, 3 y 4 ya pueden comenzar sus módulos siguiendo esta guía. Antes de unir cambios, cada integrante debe:

1. Elegir la tarea que asumirá y escribir su nombre en este documento.
2. Trabajar únicamente en las pantallas y archivos de su RF asignado.
3. Mantener los nombres de roles, estados y reglas de negocio definidos aquí.
4. Probar su navegación localmente y entregar una breve lista de archivos modificados.

La parte de NOE no está cerrada al cien por ciento hasta integrar y probar los tres módulos del equipo. Después de recibirlos, NOE realizará la integración final, resolverá conflictos de recursos y comprobará la navegación completa.

## Modelo de entidades y repositorios

Para que la arquitectura sea coherente, cada entidad de negocio debe tener un repositorio responsable de consultar, guardar y actualizar sus datos. En esta primera versión se aplicará una relación uno a uno entre entidad y repositorio, tal como solicitó el profesor. Los repositorios aislarán a los ViewModel de la implementación local o remota.

| Entidad | Repositorio | Responsabilidad principal |
|---|---|---|
| `Usuario` | `UsuarioRepository` | Autenticación, rol y estado de la cuenta. |
| `ObjetoEncontrado` | `ObjetoEncontradoRepository` | Hallazgo, código, estado físico y custodia. |
| `ReportePerdida` | `ReportePerdidaRepository` | Registro, reanudación y cierre de pérdidas. |
| `Reclamacion` | `ReclamacionRepository` | Evidencia, preguntas y decisión del SAE. |
| `UbicacionAlmacenamiento` | `UbicacionRepository` | Caja, anaquel y movimientos de ubicación. |
| `Entrega` | `EntregaRepository` | Reserva, confirmación, recuperación y constancia. |
| `Evidencia` | `EvidenciaRepository` | Fotografías, características reservadas y sustento. |
| `PersonaAtendida` | `PersonaAtendidaRepository` | Datos de quien reporta, reclama o recibe. |
| `Aviso` | `AvisoRepository` | Avisos, destinatarios, estado y resultado de contacto. |
| `EventoAuditoria` | `AuditoriaRepository` | Usuario, fecha, operación, cambios y motivo. |

`LoginRole` se mantiene como enumeración de roles; no reemplaza a `Usuario`. Las entidades deben ubicarse en `model/entity`, las interfaces en `model/repository` y las implementaciones concretas en `model/local` o `model/remote`.

### Estado de la base compartida

NOE ya creó las diez entidades y sus diez interfaces de repositorio en el proyecto. Por ahora son contratos de dominio sin una base de datos real; cada integrante implementará o conectará el repositorio que necesite para su RF. No se deben crear entidades paralelas con nombres diferentes.

## Controles obligatorios

Cada pantalla debe incluir controles de interfaz y controles de negocio. No basta con mostrar un botón: el sistema debe impedir operaciones inválidas.

### Controles de entrada

- Validar campos obligatorios, formatos, longitudes y valores vacíos.
- Mostrar mensajes claros junto al campo que contiene el error.
- No permitir guardar una pérdida sin descripción y medio de contacto.
- No permitir confirmar custodia sin código y ubicación válida.
- No permitir confirmar devolución sin reclamación aprobada y firma capturada.

### Controles de rol y permisos

- El Personal registrador no puede confirmar custodia, revisar evidencia reservada ni entregar objetos.
- El Administrador SAE es el único que puede confirmar recepción, verificar reclamaciones, entregar objetos y administrar accesos.
- La pantalla debe ocultar acciones no autorizadas y el repositorio debe volver a validar el permiso.

### Controles de estado

- Un hallazgo reportado no se muestra como disponible hasta pasar a `Bajo custodia`.
- Una reclamación `Pendiente de verificación` no puede iniciar una devolución.
- Un objeto `Entregado` no puede volver a reservarse.
- Una operación incierta debe reanudar la misma operación, no crear un duplicado.

### Controles de integridad y auditoría

- No eliminar registros definitivamente desde la aplicación.
- Registrar usuario, fecha, estado anterior, estado nuevo y motivo en cada modificación relevante.
- Evitar doble envío al pulsar dos veces el mismo botón.
- Mantener separadas la evidencia reservada, la firma y la información visible para el registrador.

## Cómo debe trabajar cada integrante

Para cada RF asignado, el trabajo debe seguir este orden:

1. Identificar las entidades que utiliza la pantalla.
2. Crear o reutilizar esas entidades en `model/entity`.
3. Definir el repositorio correspondiente en `model/repository`.
4. Crear el ViewModel para el estado, validaciones y eventos de la pantalla.
5. Implementar la Activity y el layout conservando el diseño visual existente.
6. Aplicar los controles de entrada, rol, estado e integridad definidos en esta guía.
7. Probar los casos correctos y los casos rechazados.
8. Entregar una lista de archivos modificados y anotar cualquier dato simulado o pendiente de backend.

Ningún integrante debe crear nombres alternativos para las mismas entidades, estados o roles. Si necesita una entidad nueva, debe agregarla primero a esta tabla y crear su repositorio correspondiente para que NOE pueda integrarla sin duplicidades.
