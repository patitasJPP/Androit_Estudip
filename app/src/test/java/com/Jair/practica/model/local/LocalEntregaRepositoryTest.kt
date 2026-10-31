package com.Jair.practica.model.local

import com.Jair.practica.model.entity.Entrega
import com.Jair.practica.model.entity.EstadoEntrega
import com.Jair.practica.model.entity.EstadoObjeto
import com.Jair.practica.model.entity.EstadoReclamacion
import com.Jair.practica.model.entity.LoginRole
import com.Jair.practica.model.entity.PersonaAtendida
import com.Jair.practica.model.entity.Reclamacion
import com.Jair.practica.model.repository.MotivoRechazoEntrega
import com.Jair.practica.model.repository.EntregaRepository
import com.Jair.practica.model.repository.ResultadoEntrega
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class LocalEntregaRepositoryTest {

    private val store = LocalDataStore()
    private val repository: EntregaRepository = LocalEntregaRepository(store, LoginRole.ADMINISTRADOR_SAE)

    private fun prepararContexto() {
        store.entregas.clear()
        store.personas.add(
            PersonaAtendida(
                id = "per-rf08-0001",
                nombre = "Cliente Receptor",
                vinculoUniversidad = "Estudiante",
                identificador = "12345678",
                correo = "receptor@utp.edu.pe",
                telefono = "947000000"
            )
        )
        store.reclamaciones.add(
            Reclamacion(
                id = "rec-rf08-0001",
                objetoEncontradoId = "obj-rf08-0001",
                personaAtendidaId = "per-2026-0001",
                estado = EstadoReclamacion.APROBADA,
                motivoDecision = "Evidencia verificada",
                revisadoPor = LocalDataStore.USUARIO_ADMINISTRADOR_ID,
                fechaRegistro = "2026-10-27",
                descripcionReclamante = "Laptop negra con stickers de la UTP y funda de tela.",
                caracteristicasDeclaradas = "Tiene funda gris y el cargador original.",
                fechaDecision = "2026-10-30 09:30",
                estadoAnterior = EstadoReclamacion.PENDIENTE_VERIFICACION,
                idOperacion = null
            )
        )
        store.objetos.add(
            com.Jair.practica.model.entity.ObjetoEncontrado(
                id = "obj-rf08-0001",
                codigoCustodia = "UTP-2026-0777",
                categoria = "Laptop",
                descripcion = "Laptop entregada para devolución.",
                caracteristicasReservadas = "Funda gris, cargador original.",
                lugarHallazgo = "Pabellón B - Aula 302",
                fechaHallazgo = "2026-10-22",
                registradoPor = LocalDataStore.USUARIO_REGISTRADOR_ID,
                estado = EstadoObjeto.BAJO_CUSTODIA,
                ubicacionId = "ubi-001"
            )
        )
    }

    @Test
    fun confirmaEntregaConFirmaYActualizaEstados() {
        prepararContexto()
        val resultado = repository.confirmarEntrega(
            reclamacionId = "rec-rf08-0001",
            receptorId = "per-rf08-0001",
            administradorId = LocalDataStore.USUARIO_ADMINISTRADOR_ID,
            firmaCapturada = true,
            fecha = "2026-10-31 11:00",
            idOperacion = "op-ent-001"
        )

        assertTrue(resultado is ResultadoEntrega.Registrada)
        val entrega = (resultado as ResultadoEntrega.Registrada).entrega
        assertEquals(EstadoEntrega.CONFIRMADA, entrega.estado)
        assertTrue(entrega.firmaCapturada)

        assertEquals(EstadoObjeto.ENTREGADO, store.objetos.first { it.id == "obj-rf08-0001" }.estado)
        assertEquals(EstadoReclamacion.ATENDIDA, store.reclamaciones.first { it.id == "rec-rf08-0001" }.estado)
        assertTrue(store.eventosAuditoria.any { evento ->
            evento.entidadId == entrega.id && evento.accion == "ENTREGA_CONFIRMADA"
        })
    }

    @Test
    fun rechazaConfirmacionDelPersonalRegistrador() {
        prepararContexto()
        val resultado = LocalEntregaRepository(store, LoginRole.PERSONAL_REGISTRADOR).confirmarEntrega(
            reclamacionId = "rec-rf08-0001",
            receptorId = "per-rf08-0001",
            administradorId = LocalDataStore.USUARIO_REGISTRADOR_ID,
            firmaCapturada = true,
            fecha = "2026-10-31 11:05",
            idOperacion = "op-ent-002"
        )

        assertTrue(resultado is ResultadoEntrega.Denegada)
        assertEquals(MotivoRechazoEntrega.ROL_NO_AUTORIZADO, (resultado as ResultadoEntrega.Denegada).motivo)
    }

    @Test
    fun rechazaConfirmacionSinAprobacionDeReclamacion() {
        prepararContexto()
        val resultado = repository.confirmarEntrega(
            reclamacionId = "rec-rf08-0001",
            receptorId = "per-rf08-0001",
            administradorId = LocalDataStore.USUARIO_ADMINISTRADOR_ID,
            firmaCapturada = true,
            fecha = "2026-10-31 11:06",
            idOperacion = "op-ent-003"
        )
        // Reutilizamos la misma operación para forzar el caso de estado previo no aprobado.
        store.reclamaciones[0] = store.reclamaciones[0].copy(estado = EstadoReclamacion.PENDIENTE_VERIFICACION)
        val rechazo = repository.confirmarEntrega(
            reclamacionId = "rec-rf08-0001",
            receptorId = "per-rf08-0001",
            administradorId = LocalDataStore.USUARIO_ADMINISTRADOR_ID,
            firmaCapturada = true,
            fecha = "2026-10-31 11:07",
            idOperacion = "op-ent-004"
        )

        assertTrue(resultado is ResultadoEntrega.Registrada)
        assertTrue(rechazo is ResultadoEntrega.Denegada)
        assertEquals(MotivoRechazoEntrega.RECLAMACION_NO_APROBADA, (rechazo as ResultadoEntrega.Denegada).motivo)
    }

    @Test
    fun rechazaFirmaVacia() {
        prepararContexto()
        val resultado = repository.confirmarEntrega(
            reclamacionId = "rec-rf08-0001",
            receptorId = "per-rf08-0001",
            administradorId = LocalDataStore.USUARIO_ADMINISTRADOR_ID,
            firmaCapturada = false,
            fecha = "2026-10-31 11:08",
            idOperacion = "op-ent-005"
        )

        assertTrue(resultado is ResultadoEntrega.Denegada)
        assertEquals(MotivoRechazoEntrega.FALTA_FIRMA, (resultado as ResultadoEntrega.Denegada).motivo)
    }

    @Test
    fun rechazaDatosInvalidosDelReceptor() {
        prepararContexto()
        val indicePersona = store.personas.indexOfFirst { it.id == "per-rf08-0001" }
        store.personas[indicePersona] = store.personas[indicePersona].copy(
            identificador = "123",
            correo = "correo-invalido"
        )
        val resultado = repository.confirmarEntrega(
            reclamacionId = "rec-rf08-0001",
            receptorId = "per-rf08-0001",
            administradorId = LocalDataStore.USUARIO_ADMINISTRADOR_ID,
            firmaCapturada = true,
            fecha = "2026-10-31 11:10",
            idOperacion = "op-ent-007"
        )

        assertTrue(resultado is ResultadoEntrega.Denegada)
        assertEquals(MotivoRechazoEntrega.DATOS_INVALIDOS, (resultado as ResultadoEntrega.Denegada).motivo)
    }

    @Test
    fun reanudaLaMismaOperacionDeEntregaSinDuplicado() {
        prepararContexto()
        val primera = repository.confirmarEntrega(
            reclamacionId = "rec-rf08-0001",
            receptorId = "per-rf08-0001",
            administradorId = LocalDataStore.USUARIO_ADMINISTRADOR_ID,
            firmaCapturada = true,
            fecha = "2026-10-31 11:11",
            idOperacion = "op-ent-008"
        )
        val segunda = repository.confirmarEntrega(
            reclamacionId = "rec-rf08-0001",
            receptorId = "per-rf08-0001",
            administradorId = LocalDataStore.USUARIO_ADMINISTRADOR_ID,
            firmaCapturada = true,
            fecha = "2026-10-31 11:12",
            idOperacion = "op-ent-008"
        )

        assertTrue(primera is ResultadoEntrega.Registrada)
        assertTrue(segunda is ResultadoEntrega.Registrada)
        assertEquals(1, store.entregas.size)
    }

    @Test
    fun listaSoloEntregasConfirmadasParaAdministrador() {
        prepararContexto()
        store.entregas.add(
            Entrega(
                id = "ent-rf09-0001",
                objetoEncontradoId = "obj-rf08-0001",
                reclamacionId = "rec-rf08-0001",
                receptorId = "per-rf08-0001",
                administradorId = LocalDataStore.USUARIO_ADMINISTRADOR_ID,
                fechaConfirmacion = "2026-10-31 09:00",
                firmaCapturada = true,
                estado = EstadoEntrega.CONFIRMADA,
                idOperacion = "op-rf09-001"
            )
        )

        val entregasAdmin = repository.listByAdministrador(LoginRole.ADMINISTRADOR_SAE)
        assertEquals(1, entregasAdmin.size)
        assertEquals(EstadoEntrega.CONFIRMADA, entregasAdmin.first().estado)

        val entregasPersonal = LocalEntregaRepository(
            store,
            LoginRole.PERSONAL_REGISTRADOR
        ).listByAdministrador(LoginRole.PERSONAL_REGISTRADOR)

        assertTrue(entregasPersonal.isEmpty())
    }
}
