# Estructura del cliente Android

MVVM se usa como **patrón arquitectónico de presentación**. La separación visible en el código es `view` → `viewmodel` → `model`. Un paquete vacío indica trabajo pendiente; su existencia no significa que el flujo esté implementado.

```text
app/src/main/java/com/Jair/practica/
├── view/
│   ├── login/        Selección de rol y LoginActivity: ejemplo MVVM
│   ├── hallazgo/     Registro de objetos encontrados
│   ├── custodia/     Inventario y protocolo de entrega
│   ├── perdida/      Reportes de pérdida y búsqueda
│   ├── devolucion/   Historial, verificación y acta de entrega
│   └── common/       Navegación visual compartida
├── viewmodel/
│   ├── login/        LoginViewModel y estado observable
│   ├── hallazgo/
│   ├── custodia/
│   ├── perdida/
│   └── devolucion/
└── model/
    ├── entity/       Tipos y entidades del dominio
    ├── repository/   Contratos y fuentes de datos
    ├── remote/       Futuro cliente HTTP y DTO de la API
    └── local/        Futura persistencia local, si se requiere
```

Los XML de `app/src/main/res/layout` son parte de **View**, aunque Android los guarda en `res/layout` y no dentro del paquete Kotlin. `Activity`/`Fragment` muestran estado, recogen entradas y ejecutan acciones propias de Android, como abrir otra pantalla o mostrar un `Toast`. No validan permisos ni deciden si una devolución se autoriza.

Cada **ViewModel** recibe acciones de la vista, consulta repositorios y publica un estado observable. No debe tener una referencia a `Activity`, vistas XML ni `Context` de la pantalla.

**Model** contiene entidades y repositorios. `DemoAuthRepository` conserva las credenciales existentes solo para que el prototipo siga funcionando; no es autenticación segura ni comunica con un servidor. La API REST, las reglas de custodia, la base de datos, el almacenamiento de archivos y los avisos son trabajo posterior. El backend debe vivir como proyecto o módulo independiente del cliente Android.

El contrato de autenticación del ejemplo es síncrono porque consulta datos locales inmediatos. Al conectar la API, convertir esa operación a una llamada suspendida y ejecutarla desde el `ViewModel` con corrutinas; ninguna petición de red debe bloquear el hilo de la interfaz.

## Ejemplo para copiar: inicio de sesión

1. `view/login/LoginActivity.kt` obtiene usuario, contraseña y rol con ViewBinding, llama a `LoginViewModel.signIn` y observa `uiState`. Mostrar la contraseña, navegar y mostrar el error son acciones de interfaz.
2. `viewmodel/login/LoginViewModel.kt` decide el resultado de la acción usando `AuthRepository` y publica `Idle`, `InvalidCredentials` o `Authenticated` mediante LiveData.
3. `model/entity/LoginRole.kt` define los roles. `model/repository/AuthRepository.kt` define el contrato; `DemoAuthRepository.kt` lo implementa de forma local y temporal. Una implementación remota podrá sustituirla cuando exista la API.

Para migrar otra pantalla, crear su `ViewModel` y sus entidades/contratos necesarios, mover allí el estado y las decisiones de la pantalla, y dejar en View solamente los eventos visuales y APIs de Android. Verificar sus imports, navegación y `AndroidManifest.xml` después de cada migración.

## Estado de las demás pantallas

Todas las pantallas están agrupadas bajo `view` con nombres por función. Los archivos XML de `res/layout` también tienen nombres funcionales, y los recursos de entrega usan el prefijo `entrega_`. El paquete raíz y el `applicationId` se conservan para no cambiar la identidad instalada de la app.

| Flujo | Pantallas actuales |
| --- | --- |
| Acceso | `SeleccionRolActivity` → `LoginActivity` |
| Hallazgos | `RegistroHallazgoActivity` |
| Custodia | `InventarioActivity`, `ProtocoloEntregaActivity` |
| Devoluciones | `HistorialEntregasActivity`, `VerificacionEntregaActivity`, `ActaEntregaActivity` |
| Pérdidas | Paquete reservado; todavía no existe una pantalla funcional |

Salvo el login, las clases `Controller` siguen siendo **código previo a MVVM**: están en View porque manipulan elementos visuales, pero algunas aún contienen estado y decisiones que deben pasar gradualmente a `ViewModel`. Renombrar y mover archivos no significa que esas pantallas ya implementen MVVM.

## Tipos de tareas en los flujos

Al programar los procesos del PDF, distinguir estas responsabilidades:

| Tipo | Ejemplo | Responsable |
| --- | --- | --- |
| Humana | Recibir y entregar físicamente un objeto | Persona; la View registra su acción |
| Interfaz | Mostrar campos, seleccionar una opción, abrir cámara | View |
| Estado de pantalla | Preparar filtros, validar entradas y publicar carga/error/resultado | ViewModel |
| Datos del cliente | Consultar o enviar registros mediante un contrato | Model/Repository |
| Regla institucional y persistencia | Autorizar entrega, reservarla una sola vez, registrar custodia y generar acta | Servidor; pendiente de implementar |

Una pantalla o un `Toast` no prueban que la tarea del servidor se haya ejecutado. Marcar cada paso como real o demostrativo hasta conectar la API.
