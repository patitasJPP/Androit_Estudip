package com.Jair.practica.model.local

import com.Jair.practica.model.entity.EstadoObjeto
import com.Jair.practica.model.entity.EstadoReportePerdida
import com.Jair.practica.model.entity.LoginRole
import com.Jair.practica.model.perdida.DatosReportePerdida
import com.Jair.practica.model.perdida.FiltrosCoincidencia
import com.Jair.practica.model.repository.MotivoRechazoPerdida
import com.Jair.practica.model.repository.ResultadoBusqueda
import com.Jair.practica.model.repository.ResultadoReporte
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test

class LocalReportePerdidaRepositoryTest {

    private val registrador = AlmacenPerdidas.USUARIO_REGISTRADOR_ID
    private val rol = LoginRole.PERSONAL_REGISTRADOR
    private val fecha = "2026-10-31 10:00"

    private val store = AlmacenPerdidas()
    private val objetos = ObjetoEncontradoDemoRepository()
    private val repo = LocalReportePerdidaRepository(store, objetos)

    private val datosValidos = DatosReportePerdida(
        descripcion = "Laptop negra con stickers de la UTP",
        categoria = "Laptop", color = "Negro",
        contactoCorreo = "alumno@utp.edu.pe"
    )

    private fun registrarNuevo(op: String = "op-nuevo"): ResultadoReporte.Registrada =
        repo.registrar(datosValidos, null, registrador, rol, fecha, op) as ResultadoReporte.Registrada

    //RF-05

    @Test
    fun registrarCreaReporteSinCoincidenciaYAudita() {
        val r = registrarNuevo()
        assertEquals("rep-2026-0003", r.reporte.id)
        assertEquals(EstadoReportePerdida.SIN_COINCIDENCIA, r.reporte.estado)
        assertEquals(registrador, r.reporte.creadoPor)
        val eventos = store.eventosAuditoria.filter { it.entidadId == r.reporte.id }
        assertEquals(1, eventos.size)
        assertTrue(eventos[0].detalle!!.contains("SIN_COINCIDENCIA"))
    }

    @Test
    fun elAdministradorNoPuedeRegistrarPerdidas() {
        val r = repo.registrar(datosValidos, null, "usr-administrador-01", LoginRole.ADMINISTRADOR_SAE, fecha, "op-1")
        assertEquals(ResultadoReporte.Denegada(MotivoRechazoPerdida.ROL_NO_AUTORIZADO), r)
        assertEquals(2, store.reportes.size)
    }

    @Test
    fun registrarSinContactoDevuelveErrorPorCampoYNoGuarda() {
        val r = repo.registrar(datosValidos.copy(contactoCorreo = ""), null, registrador, rol, fecha, "op-1")
        assertTrue(r is ResultadoReporte.Invalida)
        assertNotNull((r as ResultadoReporte.Invalida).errores.medioContacto)
        assertEquals(2, store.reportes.size)
    }

    @Test
    fun repetirLaMismaOperacionNoCreaDuplicado() {
        val primera = registrarNuevo("op-doble-toque")
        val segunda = registrarNuevo("op-doble-toque")
        assertEquals(primera.reporte.id, segunda.reporte.id)
        assertEquals(3, store.reportes.size)
        assertEquals(1, store.eventosAuditoria.count { it.entidadId == primera.reporte.id })
    }

    @Test
    fun borradorVacioSeRechaza() {
        val r = repo.guardarBorrador(DatosReportePerdida(), null, registrador, rol, fecha, "op-1")
        assertEquals(ResultadoReporte.Denegada(MotivoRechazoPerdida.BORRADOR_VACIO), r)
    }

    @Test
    fun borradorSeGuardaIncompletoYSePuedeReanudarYRegistrar() {
        val borrador = repo.guardarBorrador(
            DatosReportePerdida(categoria = "Llaves"), null, registrador, rol, fecha, "op-1"
        ) as ResultadoReporte.Registrada
        assertEquals(EstadoReportePerdida.BORRADOR, borrador.reporte.estado)

        val registrado = repo.registrar(
            datosValidos.copy(categoria = "Llaves"), borrador.reporte.id, registrador, rol, fecha, "op-2"
        ) as ResultadoReporte.Registrada
        // Reanudar: es el MISMO reporte, no uno nuevo.
        assertEquals(borrador.reporte.id, registrado.reporte.id)
        assertEquals(EstadoReportePerdida.SIN_COINCIDENCIA, registrado.reporte.estado)
        assertEquals(3, store.reportes.size)
    }

    @Test
    fun elBorradorDeLaSemillaApareceEntrePendientesYSePuedeCompletar() {
        val pendientes = repo.listPendientes(registrador, rol)
        assertEquals("rep-2026-0002", pendientes.first().id)  // el más reciente
        val r = repo.registrar(
            datosValidos.copy(categoria = "Mochila", color = "Azul"), "rep-2026-0002",
            registrador, rol, fecha, "op-1"
        ) as ResultadoReporte.Registrada
        assertEquals(EstadoReportePerdida.SIN_COINCIDENCIA, r.reporte.estado)
        assertEquals(2, store.reportes.size)
    }

    @Test
    fun otroRegistradorNoPuedeModificarUnReporteAjeno() {
        val r = repo.registrar(datosValidos, "rep-2026-0002", "usr-otro", rol, fecha, "op-1")
        assertEquals(ResultadoReporte.Denegada(MotivoRechazoPerdida.NO_ES_PROPIETARIO), r)
        assertTrue(repo.listPendientes("usr-otro", rol).isEmpty())
    }

    @Test
    fun noSeEditaUnReporteQueYaTieneCoincidencias() {
        val r = repo.registrar(datosValidos, "rep-2026-0001", registrador, rol, fecha, "op-1")
        assertEquals(ResultadoReporte.Denegada(MotivoRechazoPerdida.ESTADO_INVALIDO), r)
    }

    @Test
    fun unReporteRegistradoNoVuelveABorrador() {
        val reg = registrarNuevo()
        val r = repo.guardarBorrador(
            DatosReportePerdida(descripcion = "otra cosa"), reg.reporte.id, registrador, rol, fecha, "op-2"
        )
        assertEquals(ResultadoReporte.Denegada(MotivoRechazoPerdida.ESTADO_INVALIDO), r)
    }

    // ---------- RF-06 ----------

    @Test
    fun noSeBuscaSobreUnBorrador() {
        val r = repo.buscarCoincidencias("rep-2026-0002", FiltrosCoincidencia(color = "Azul"), registrador, rol, fecha, "op-1")
        assertEquals(ResultadoBusqueda.Denegada(MotivoRechazoPerdida.ESTADO_INVALIDO), r)
    }

    @Test
    fun buscarSinFiltrosSeRechaza() {
        val reg = registrarNuevo()
        val r = repo.buscarCoincidencias(reg.reporte.id, FiltrosCoincidencia(), registrador, rol, fecha, "op-2")
        assertEquals(ResultadoBusqueda.Denegada(MotivoRechazoPerdida.SIN_FILTROS), r)
    }

    @Test
    fun buscarConFiltroFueraDeCatalogoSeRechaza() {
        val reg = registrarNuevo()
        val r = repo.buscarCoincidencias(reg.reporte.id, FiltrosCoincidencia(piso = "99"), registrador, rol, fecha, "op-2")
        assertEquals(ResultadoBusqueda.Denegada(MotivoRechazoPerdida.FILTROS_INVALIDOS), r)
    }

    @Test
    fun buscarConservaElResultadoYPasaACoincidenciaSugerida() {
        val reg = registrarNuevo()
        val r = repo.buscarCoincidencias(
            reg.reporte.id, FiltrosCoincidencia(color = "Negro"), registrador, rol, fecha, "op-2"
        ) as ResultadoBusqueda.Realizada
        assertEquals(EstadoReportePerdida.COINCIDENCIA_SUGERIDA, r.reporte.estado)
        assertEquals(listOf("obj-2026-0001"), r.coincidencias.map { it.objetoId })

        // El resultado queda guardado en el caso.
        assertEquals(listOf("obj-2026-0001"), repo.findById(reg.reporte.id)!!.coincidenciaIds)
        assertEquals(1, repo.coincidenciasGuardadas(reg.reporte.id, registrador, rol).size)
    }

    @Test
    fun buscarSinResultadosDejaElCasoSinCoincidencia() {
        val reg = registrarNuevo()
        val r = repo.buscarCoincidencias(
            reg.reporte.id, FiltrosCoincidencia(pabellon = "D", piso = "8"), registrador, rol, fecha, "op-2"
        ) as ResultadoBusqueda.Realizada
        assertEquals(EstadoReportePerdida.SIN_COINCIDENCIA, r.reporte.estado)
        assertTrue(r.coincidencias.isEmpty())
    }

    @Test
    fun unaNuevaBusquedaSinResultadosVuelveASinCoincidencia() {
        val reg = registrarNuevo()
        repo.buscarCoincidencias(reg.reporte.id, FiltrosCoincidencia(color = "Negro"), registrador, rol, fecha, "op-2")
        val r = repo.buscarCoincidencias(
            reg.reporte.id, FiltrosCoincidencia(pabellon = "D", piso = "8"), registrador, rol, fecha, "op-3"
        ) as ResultadoBusqueda.Realizada
        assertEquals(EstadoReportePerdida.SIN_COINCIDENCIA, r.reporte.estado)
        assertTrue(r.reporte.coincidenciaIds.isEmpty())
    }

    @Test
    fun elAdministradorNoVeCoincidenciasDelRegistrador() {
        assertTrue(repo.coincidenciasGuardadas("rep-2026-0001", registrador, LoginRole.ADMINISTRADOR_SAE).isEmpty())
        val r = repo.buscarCoincidencias(
            "rep-2026-0001", FiltrosCoincidencia(color = "Negro"), "usr-administrador-01",
            LoginRole.ADMINISTRADOR_SAE, fecha, "op-1"
        )
        assertEquals(ResultadoBusqueda.Denegada(MotivoRechazoPerdida.ROL_NO_AUTORIZADO), r)
    }


    @Test
    fun darSeguimientoMarcaElCasoYAuditaSinEntregarNada() {
        val r = repo.marcarSeguimiento("rep-2026-0001", "obj-2026-0001", registrador, rol, fecha, "op-1")
            as ResultadoReporte.Registrada
        assertEquals(EstadoReportePerdida.EN_SEGUIMIENTO, r.reporte.estado)
        assertEquals("obj-2026-0001", r.reporte.objetoSeguimientoId)
        // La coincidencia es solo una sugerencia: el objeto sigue bajo custodia.
        assertEquals(EstadoObjeto.BAJO_CUSTODIA, objetos.findById("obj-2026-0001")!!.estado)

        val evento = store.eventosAuditoria.single { it.accion == "SEGUIMIENTO_INICIADO" }
        assertTrue(evento.detalle!!.contains("COINCIDENCIA_SUGERIDA"))
        assertTrue(evento.detalle!!.contains("EN_SEGUIMIENTO"))
    }

    @Test
    fun darSeguimientoDosVecesNoDuplicaLaAuditoria() {
        repo.marcarSeguimiento("rep-2026-0001", "obj-2026-0001", registrador, rol, fecha, "op-1")
        val segunda = repo.marcarSeguimiento("rep-2026-0001", "obj-2026-0001", registrador, rol, fecha, "op-2")
        assertTrue(segunda is ResultadoReporte.Registrada)
        assertEquals(1, store.eventosAuditoria.count { it.accion == "SEGUIMIENTO_INICIADO" })
    }

    @Test
    fun noSeSiguenObjetosQueNoFueronSugeridos() {
        val r = repo.marcarSeguimiento("rep-2026-0001", "obj-2026-0004", registrador, rol, fecha, "op-1")
        assertEquals(ResultadoReporte.Denegada(MotivoRechazoPerdida.COINCIDENCIA_NO_SUGERIDA), r)
    }

    @Test
    fun noSeSigueUnObjetoQueYaFueEntregado() {
        val obj = objetos.findById("obj-2026-0001")!!
        objetos.update(obj.copy(estado = EstadoObjeto.ENTREGADO))
        val r = repo.marcarSeguimiento("rep-2026-0001", "obj-2026-0001", registrador, rol, fecha, "op-1")
        assertEquals(ResultadoReporte.Denegada(MotivoRechazoPerdida.OBJETO_NO_DISPONIBLE), r)
        // Y deja de aparecer entre las coincidencias guardadas.
        assertFalse(repo.coincidenciasGuardadas("rep-2026-0001", registrador, rol).any { it.objetoId == "obj-2026-0001" })
    }

    @Test
    fun noSePuedeDarSeguimientoSiElReporteNoTieneCoincidenciaSugerida() {
        val reg = registrarNuevo()
        val r = repo.marcarSeguimiento(reg.reporte.id, "obj-2026-0001", registrador, rol, fecha, "op-2")
        assertEquals(ResultadoReporte.Denegada(MotivoRechazoPerdida.ESTADO_INVALIDO), r)
    }

    @Test
    fun laSemillaCumpleLaReferenciaDeJair() {
        assertNotNull(repo.findById(AlmacenPerdidas.REPORTE_DEMO_ID))
    }
}
