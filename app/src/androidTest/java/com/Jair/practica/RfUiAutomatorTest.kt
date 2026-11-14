package com.Jair.practica

import android.content.Intent
import android.os.SystemClock
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import androidx.test.uiautomator.By
import androidx.test.uiautomator.UiDevice
import androidx.test.uiautomator.UiObject2
import androidx.test.uiautomator.Until
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import java.io.File

/**
 * Recorrido automatizado de los RF-01 al RF-12.
 * Ejecutar con: ./gradlew :app:connectedDebugAndroidTest
 * Las capturas quedan en Android/data/com.Jair.practica/files/rf-evidence/.
 */
@RunWith(AndroidJUnit4::class)
class RfUiAutomatorTest {
    private val instrumentation = InstrumentationRegistry.getInstrumentation()
    private val context = instrumentation.targetContext
    private val device = UiDevice.getInstance(instrumentation)
    private val pkg = context.packageName
    private val timeout = 8_000L

    @Before
    fun prepararDispositivo() {
        device.pressHome()
    }

    @Test
    fun recorrerRf01AlRf12() {
        // RF-01: inicio de sesión del Personal registrador.
        abrirAplicacion()
        login(personal = true)
        assertNotNull(texto("Panel registrador"))
        evidencia("RF-01-personal-panel")

        // RF-02: documentar hallazgo.
        clickTexto("Iniciar registro")
        assertNotNull(texto("Registrar Objeto Encontrado"))
        setText("etDescripcionObjeto", "Laptop negra Lenovo con stickers azules")
        // El botón es un MaterialCardView; se localiza por su texto accesible.
        repeat(2) { device.swipe(540, 1800, 540, 520, 20) }
        clickTextoContains("REGISTRAR Y ENVIAR")
        evidencia("RF-02-documentar-hallazgo")

        // RF-03: el administrador confirma recepción y custodia.
        abrirAplicacion()
        login(personal = false)
        clickTexto("Confirmar custodia")
        assertNotNull(texto("Confirmar recepción"))
        assertNotNull(textoContains("Laptop negra Lenovo"))
        clickRes("btnConfirmarRecepcion")
        evidencia("RF-03-confirmar-recepcion")

        // RF-04: consultar inventario.
        device.pressBack()
        clickTexto("Abrir inventario")
        assertNotNull(texto("Zona de Objetos Perdidos"))
        // Evidencia de sincronización entre roles: el objeto creado por el
        // registrador debe aparecer en el inventario del Administrador SAE.
        assertNotNull(textoContains("Laptop negra Lenovo"))
        evidencia("RF-04-inventario")

        // RF-05 y RF-06: registrar pérdida y revisar coincidencias.
        abrirAplicacion()
        login(personal = true)
        clickTexto("Crear reporte")
        setText("etPerdidaDescripcion", "Laptop negra Lenovo con stickers azules")
        setText("etPerdidaLugar", "Torre A, piso 6, aula A0601")
        setText("etPerdidaContacto", "jair@utp.edu.pe")
        clickRes("btnGuardarPerdida")
        assertNotNull(texto("Posibles coincidencias"))
        evidencia("RF-05-06-perdida-coincidencias")

        // RF-07: verificar reclamación.
        abrirAplicacion()
        login(personal = false)
        clickTexto("Revisar reclamación")
        assertNotNull(texto("Verificar reclamación"))
        // Las preguntas y la decisión están debajo del primer bloque de comparación.
        repeat(2) { device.swipe(540, 1800, 540, 520, 20) }
        setText("etNuevaPregunta", "¿Qué accesorio tenía el objeto?")
        clickRes("btnAgregarPregunta")
        findRes("etRespuestaItem")?.setText("Tenía una funda negra y su cargador original.")
        device.swipe(540, 1800, 540, 520, 20)
        clickRes("rbDecisionAprobar")
        setText("etMotivo", "La descripción coincide con la evidencia registrada por el SAE.")
        waitEnabled("btnRegistrarDecision").click()
        evidencia("RF-07-verificar-reclamacion")

        // RF-08: confirmar devolución; se valida la pantalla y sus controles.
        assertNotNull(texto("Confirmar devolución"))
        // La pantalla es un formulario largo: desplazar el contenedor antes de
        // buscar los campos inferiores evita falsos negativos por visibilidad.
        desplazarHasta("btnConfirmarDevolucion")
        assertNotNull(findRes("etDni"))
        assertNotNull(findRes("etCorreo"))
        assertNotNull(findRes("btnConfirmarDevolucion"))
        setText("etDni", "72875982")
        setText("etCorreo", "jair@utp.edu.pe")
        val firma = findRes("viewFirma")
        assertNotNull(firma)
        firma?.visibleBounds?.let { bounds ->
            device.swipe(bounds.left + 24, bounds.centerY(), bounds.right - 24, bounds.centerY(), 220)
        }
        waitEnabled("btnConfirmarDevolucion").click()
        esperar(By.res(pkg, "rvHistorialEntregas"), "historial después de confirmar la devolución")
        evidencia("RF-08-confirmar-devolucion")

        // RF-09 y RF-10: historial y acta de entrega.
        abrirAplicacion()
        login(personal = false)
        clickTexto("Abrir historial")
        assertNotNull(findRes("rvHistorialEntregas"))
        evidencia("RF-09-historial-entregas")
        findByTextContains("Ver acta")?.click()
        assertNotNull(texto("ACTA OFICIAL DE DEVOLUCIÓN"))
        evidencia("RF-10-acta-entrega")

        // RF-11: administrar accesos.
        abrirAplicacion()
        login(personal = false)
        clickTexto("Gestionar accesos")
        assertNotNull(texto("Administrar accesos"))
        clickTexto("Deshabilitar acceso")
        assertNotNull(textoContains("Última acción: Deshabilitar acceso"))
        evidencia("RF-11-administrar-accesos")

        // RF-12: seguimiento y avisos.
        abrirAplicacion()
        login(personal = false)
        // El panel SAE contiene varias tarjetas; el acceso de seguimiento
        // puede quedar debajo del primer viewport.
        repeat(4) { device.swipe(540, 1800, 540, 520, 20) }
        clickTexto("Ver actividad")
        assertNotNull(texto("Seguimiento y avisos"))
        assertNotNull(texto("Filtrar actividad"))
        assertNotNull(textoContains("Laptop negra Lenovo"))
        desplazarHastaTexto("Actualizar estados")
        clickTexto("Actualizar estados")
        evidencia("RF-12-seguimiento-avisos")
        assertTrue("El recorrido RF-01 a RF-12 terminó correctamente", true)
    }

    @Test
    fun validarReportesPerfilYSeguimientoPersonal() {
        abrirAplicacion()
        login(personal = false)
        desplazarHastaTexto("Abrir reportes")
        clickTexto("Abrir reportes")
        assertNotNull(texto("Reportes operativos"))
        assertNotNull(texto("Datos actualizados desde el repositorio local compartido"))
        desplazarHasta("btnExportarCsv")
        assertNotNull(findRes("btnExportarCsv"))

        abrirAplicacion()
        login(personal = false)
        desplazarHastaTexto("Abrir perfil")
        clickTexto("Abrir perfil")
        assertNotNull(texto("Mi perfil"))
        assertNotNull(textoContains("Administrador SAE"))

        abrirAplicacion()
        login(personal = true)
        desplazarHastaTexto("Ver actividad")
        clickTexto("Ver actividad")
        assertNotNull(texto("Seguimiento y avisos"))
    }

    private fun abrirAplicacion() {
        val intent = context.packageManager.getLaunchIntentForPackage(pkg)!!.apply {
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK)
        }
        context.startActivity(intent)
        esperar(By.res(pkg, "rootLogin"), "pantalla de login")
    }

    private fun login(personal: Boolean) {
        if (personal) clickRes("rbPersonalRegistrador") else clickRes("rbAdministradorSae")
        setText("etUsuario", "jair")
        setText("etContrasena", if (personal) "jair" else "jair123")
        clickRes("btnLogin")
        esperar(By.textContains(if (personal) "Panel registrador" else "Panel SAE"), "panel por rol")
    }

    private fun setText(id: String, value: String) {
        val field = findRes(id) ?: error("No se encontró el campo $id")
        field.setText(value)
    }

    private fun clickRes(id: String) {
        val view = findRes(id) ?: error("No se encontró el control $id")
        view.click()
    }

    private fun clickTexto(value: String) {
        val view = texto(value)
        view.click()
    }

    private fun clickTextoContains(value: String) {
        val view = findByTextContains(value) ?: error("No se encontró el texto '$value'")
        view.click()
    }

    private fun findRes(id: String): UiObject2? = device.wait(Until.findObject(By.res(pkg, id)), timeout)

    private fun desplazarHasta(id: String) {
        repeat(8) {
            if (device.findObject(By.res(pkg, id)) != null) return
            device.swipe(540, 1800, 540, 520, 20)
        }
    }

    private fun desplazarHastaTexto(valor: String) {
        repeat(8) {
            if (device.findObject(By.textContains(valor)) != null) return
            device.swipe(540, 1800, 540, 520, 20)
        }
    }

    private fun waitEnabled(id: String): UiObject2 =
        run {
            val deadline = SystemClock.uptimeMillis() + timeout
            while (SystemClock.uptimeMillis() < deadline) {
                val current = device.findObject(By.res(pkg, id))
                if (current?.isEnabled == true) return@run current
                SystemClock.sleep(120)
            }
            error("El control $id no se habilitó")
        }

    private fun texto(value: String): UiObject2 = esperar(By.text(value), "texto '$value'")

    private fun textoContains(value: String): UiObject2 = esperar(By.textContains(value), "texto '$value'")

    private fun findByTextContains(value: String): UiObject2? = device.wait(Until.findObject(By.textContains(value)), timeout)

    private fun esperar(selector: androidx.test.uiautomator.BySelector, descripcion: String): UiObject2 =
        device.wait(Until.findObject(selector), timeout)
            ?: error("Tiempo agotado esperando $descripcion")

    private fun evidencia(nombre: String) {
        val dir = context.getExternalFilesDir("rf-evidence") ?: return
        dir.mkdirs()
        val file = File(dir, "$nombre.png")
        assertTrue("No se pudo capturar $nombre", device.takeScreenshot(file))
    }
}
