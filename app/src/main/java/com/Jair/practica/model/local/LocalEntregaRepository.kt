package com.Jair.practica.model.local

import com.Jair.practica.model.entity.EstadoEntrega
import com.Jair.practica.model.entity.EstadoObjeto
import com.Jair.practica.model.entity.EstadoReclamacion
import com.Jair.practica.model.entity.Entrega
import com.Jair.practica.model.entity.EventoAuditoria
import com.Jair.practica.model.entity.LoginRole
import com.Jair.practica.model.repository.EntregaRepository
import com.Jair.practica.model.repository.MotivoRechazoEntrega
import com.Jair.practica.model.repository.ResultadoEntrega

class LocalEntregaRepository(
    private val store: LocalDataStore = AlmacenLocal.instancia,
    private val actorRol: LoginRole = LoginRole.ADMINISTRADOR_SAE
) : EntregaRepository {

    override fun findById(id: String): Entrega? =
        store.entregas.firstOrNull { it.id == id }

    override fun save(entrega: Entrega): Entrega {
        val index = store.entregas.indexOfFirst { it.id == entrega.id }
        if (index >= 0) {
            store.entregas[index] = entrega
        } else {
            store.entregas.add(entrega)
        }
        return entrega
    }

    override fun update(entrega: Entrega): Entrega {
        val index = store.entregas.indexOfFirst { it.id == entrega.id }
        if (index >= 0) {
            store.entregas[index] = entrega
        }
        return entrega
    }

    override fun list(): List<Entrega> = store.entregas.toList()

    override fun confirmarEntrega(
        reclamacionId: String,
        receptorId: String,
        administradorId: String,
        firmaCapturada: Boolean,
        fecha: String,
        idOperacion: String
    ): ResultadoEntrega {
        if (actorRol != LoginRole.ADMINISTRADOR_SAE) {
            return ResultadoEntrega.Denegada(MotivoRechazoEntrega.ROL_NO_AUTORIZADO)
        }

        if (idOperacion.isNotBlank() && store.operacionesEntregas.contains(idOperacion)) {
            val entregaRegistrada = store.entregas.firstOrNull { it.idOperacion == idOperacion }
            return ResultadoEntrega.Registrada(
                entrega = entregaRegistrada ?: Entrega(
                    id = "ent-0000",
                    objetoEncontradoId = "",
                    reclamacionId = reclamacionId,
                    receptorId = receptorId,
                    administradorId = administradorId,
                    estado = EstadoEntrega.CONFIRMADA
                ),
                estadoAnterior = EstadoEntrega.RESERVADA
            )
        }

        val reclamacion = store.reclamaciones.firstOrNull { it.id == reclamacionId }
            ?: return ResultadoEntrega.Denegada(MotivoRechazoEntrega.ENTREGA_NO_ENCONTRADA)
        if (reclamacion.estado != EstadoReclamacion.APROBADA) {
            return ResultadoEntrega.Denegada(MotivoRechazoEntrega.RECLAMACION_NO_APROBADA)
        }

        val objeto = store.objetos.firstOrNull { it.id == reclamacion.objetoEncontradoId }
        if (objeto?.estado == EstadoObjeto.ENTREGADO) {
            return ResultadoEntrega.Denegada(MotivoRechazoEntrega.OBJETO_ENTREGADO)
        }

        if (!firmaCapturada) {
            return ResultadoEntrega.Denegada(MotivoRechazoEntrega.FALTA_FIRMA)
        }

        val persona = store.personas.firstOrNull { it.id == receptorId }
        val dnicliente = persona?.identificador ?: ""
        val correoValido = Regex("^[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$")
            .matches(persona?.correo ?: "")
        val dniValido = dnicliente.length == 8 && dnicliente.all { it.isDigit() }
        if (persona == null || !dniValido || !correoValido) {
            return ResultadoEntrega.Denegada(MotivoRechazoEntrega.DATOS_INVALIDOS)
        }

        val entrega = Entrega(
            id = "ent-${store.entregas.size + 1}",
            objetoEncontradoId = reclamacion.objetoEncontradoId,
            reclamacionId = reclamacionId,
            receptorId = receptorId,
            administradorId = administradorId,
            fechaConfirmacion = fecha,
            firmaCapturada = true,
            estado = EstadoEntrega.CONFIRMADA,
            idOperacion = idOperacion
        )

        store.entregas.add(entrega)
        store.operacionesEntregas.add(idOperacion)

        val indiceObjeto = store.objetos.indexOfFirst { it.id == objeto?.id }
        if (indiceObjeto >= 0) {
            store.objetos[indiceObjeto] = store.objetos[indiceObjeto].copy(estado = EstadoObjeto.ENTREGADO)
        }

        val indiceReclamacion = store.reclamaciones.indexOfFirst { it.id == reclamacionId }
        if (indiceReclamacion >= 0) {
            store.reclamaciones[indiceReclamacion] = reclamacion.copy(estado = EstadoReclamacion.ATENDIDA)
        }

        store.eventosAuditoria.add(
            EventoAuditoria(
                id = "aud-${store.eventosAuditoria.size + 1}",
                entidadTipo = "Entrega",
                entidadId = entrega.id,
                accion = "ENTREGA_CONFIRMADA",
                usuarioId = administradorId,
                fecha = fecha,
                detalle = "Entrega física confirmada para reclamación ${reclamacionId}",
                estadoAnterior = EstadoEntrega.RESERVADA.name,
                estadoNuevo = EstadoEntrega.CONFIRMADA.name
            )
        )

        return ResultadoEntrega.Registrada(entrega, EstadoEntrega.RESERVADA)
    }
}
