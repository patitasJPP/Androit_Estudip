# Estructura del cliente Android

## Qué es MVVM y qué tipo de patrón es

**MVVM** significa **Model–View–ViewModel**. En este proyecto se utiliza como **patrón arquitectónico de la capa de presentación**: define responsabilidades y el flujo de información entre la pantalla, su estado y los datos. No es un patrón de diseño limitado a una clase o a un objeto, ni reemplaza la arquitectura cliente-servidor descrita en el PDF. La aplicación Android es el cliente; el servidor y la base de datos serán componentes independientes cuando se implementen.

La separación visible en el código es `view` → `viewmodel` → `model`. Un paquete vacío indica trabajo pendiente; su existencia no significa que el flujo esté implementado.

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

## Qué hace cada parte

| Parte | Responsabilidad | En este proyecto |
| --- | --- | --- |
| **View** | Presenta la interfaz, recibe toques y texto del usuario, observa el estado publicado por el ViewModel y realiza acciones propias de Android, como navegar o mostrar un `Toast`. | XML en `app/src/main/res/layout` y `Activity` en `view/`. |
| **ViewModel** | Recibe las acciones de la View, prepara el estado que debe mostrarse y solicita datos mediante repositorios. Mantiene el estado de la pantalla durante cambios de configuración. | `viewmodel/login/LoginViewModel.kt` publica `uiState` con `LiveData`. |
| **Model** | Representa los datos y ofrece las operaciones para obtenerlos o guardarlos. Los repositorios aíslan su origen, que puede ser local o, más adelante, una API. | `model/entity/LoginRole.kt`, `model/repository/AuthRepository.kt` y `DemoAuthRepository.kt`. |

Los XML pertenecen conceptualmente a **View**, aunque Android exige guardarlos en `res/layout`. El ViewModel no guarda referencias a `Activity`, vistas XML ni `Context` de la pantalla. El Model tampoco conoce la View. Las reglas institucionales, como autorizar una devolución, deberán validarse en el servidor cuando exista; cambiar un botón en la app no registra una entrega real.

## Cómo se conectan

```text
Usuario toca un botón o escribe
        ↓
View (Activity + XML) llama a una función del ViewModel
        ↓
ViewModel consulta un contrato del Model (Repository)
        ↓
Model devuelve datos o un resultado al ViewModel
        ↓
ViewModel publica el nuevo estado observable (LiveData)
        ↓
View observa ese estado y actualiza la pantalla o navega
```

La comunicación es por funciones y estado observable: la View no consulta directamente la fuente de datos, y el Model no modifica controles visuales. En el login, `LoginActivity` llama a `signIn`, `LoginViewModel` utiliza `AuthRepository` y publica `Authenticated` o `InvalidCredentials`; la `Activity` observa ese resultado y decide qué mostrar o qué pantalla abrir.

`DemoAuthRepository` conserva las credenciales existentes solo para que el prototipo siga funcionando; no es autenticación segura ni comunica con un servidor. La API REST, las reglas de custodia, la base de datos, el almacenamiento de archivos y los avisos son trabajo posterior. El backend debe vivir como proyecto o módulo independiente del cliente Android.

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
