package com.Jair.practica.model.local

import com.Jair.practica.model.entity.EstadoEntrega
import com.Jair.practica.model.entity.EstadoObjeto
import com.Jair.practica.model.entity.EstadoReclamacion
import com.Jair.practica.model.entity.EstadoReportePerdida
import com.Jair.practica.model.entity.Entrega
import com.Jair.practica.model.entity.Evidencia
import com.Jair.practica.model.entity.EventoAuditoria
import com.Jair.practica.model.entity.LoginRole
import com.Jair.practica.model.entity.ObjetoEncontrado
import com.Jair.practica.model.entity.PersonaAtendida
import com.Jair.practica.model.entity.Reclamacion
import com.Jair.practica.model.entity.Usuario
import com.Jair.practica.model.entity.UbicacionAlmacenamiento
import com.Jair.practica.model.entity.TipoUbicacion
import com.Jair.practica.model.entity.ReportePerdida
import com.Jair.practica.PersistenciaLocal

class LocalDataStore {
    var ultimoObjetoPendienteId: String? = null
    var ultimoObjetoRegistradoId: String? = null
    var ultimaReclamacionPendienteId: String? = null
    val objetos = mutableListOf<ObjetoEncontrado>()
    val reclamaciones = mutableListOf<Reclamacion>()
    val evidencias = mutableListOf<Evidencia>()
    val personas = mutableListOf<PersonaAtendida>()
    val eventosAuditoria = mutableListOf<EventoAuditoria>()
    val entregas = mutableListOf<Entrega>()
    val usuarios = mutableListOf<Usuario>()
    val ubicaciones = mutableListOf<UbicacionAlmacenamiento>()
    val reportesPerdida = mutableListOf<ReportePerdida>()
    val operacionesAplicadas = mutableSetOf<String>()
    val operacionesEntregas = mutableSetOf<String>()

    init {
        cargarSemilla()
        PersistenciaLocal.cargarObjetos().forEach { persistido ->
            objetos.removeAll { it.id == persistido.id }
            objetos.add(persistido)
        }
        PersistenciaLocal.cargarReportes().forEach { persistido ->
            reportesPerdida.removeAll { it.id == persistido.id }
            reportesPerdida.add(persistido)
        }
        PersistenciaLocal.cargarReclamaciones().forEach { persistido ->
            reclamaciones.removeAll { it.id == persistido.id }
            reclamaciones.add(persistido)
        }
        PersistenciaLocal.cargarEntregas().forEach { persistido ->
            entregas.removeAll { it.id == persistido.id }
            entregas.add(persistido)
            persistido.idOperacion?.let(operacionesEntregas::add)
        }
        PersistenciaLocal.cargarPersonas().forEach { persistida ->
            personas.removeAll { it.id == persistida.id }
            personas.add(persistida)
        }
    }

    fun guardarObjetos(objetos: List<ObjetoEncontrado>) {
        this.objetos.clear()
        this.objetos.addAll(objetos)
    }

    fun guardarReclamaciones(reclamaciones: List<Reclamacion>) {
        this.reclamaciones.clear()
        this.reclamaciones.addAll(reclamaciones)
    }

    fun guardarEvidencias(evidencias: List<Evidencia>) {
        this.evidencias.clear()
        this.evidencias.addAll(evidencias)
    }

    fun guardarPersonas(personas: List<PersonaAtendida>) {
        this.personas.clear()
        this.personas.addAll(personas)
    }

    fun guardarEventos(eventos: List<EventoAuditoria>) {
        this.eventosAuditoria.clear()
        this.eventosAuditoria.addAll(eventos)
    }

    companion object {
        const val USUARIO_ADMINISTRADOR_ID = "usr-administrador-01"
        const val USUARIO_REGISTRADOR_ID = "usr-registrador-01"
        const val RECLAMACION_DEMO_ID = "rec-2026-0001"
    }

    private fun cargarSemilla() {
        ubicaciones.add(UbicacionAlmacenamiento("ubi-001", TipoUbicacion.CAJA, "CAJA-A01", "Caja principal de objetos recibidos"))
        ubicaciones.add(UbicacionAlmacenamiento("ubi-002", TipoUbicacion.ANAQUEL, "ANAQUEL-B02", "Anaquel de custodia"))
        personas.add(
            PersonaAtendida(
                id = "per-2026-0001",
                nombre = "María Fernanda Quispe Rojas",
                vinculoUniversidad = "Estudiante",
                identificador = "U202114587",
                dni = "72875982",
                correo = "mquisep@edu.utp.edu.pe",
                telefono = "947123456"
            )
        )

        objetos.add(
            ObjetoEncontrado(
                id = "obj-2026-0001",
                codigoCustodia = "UTP-2026-0142",
                categoria = "Laptop",
                descripcion = "Laptop negra con stickers de la UTP y funda de tela.",
                caracteristicasReservadas = "Funda gris, cargador original, marca Lenovo, contraseña de encendido de 4 dígitos.",
                lugarHallazgo = "Pabellón B - Aula 302",
                fechaHallazgo = "2026-10-22",
                registradoPor = LocalDataStore.USUARIO_REGISTRADOR_ID,
                estado = EstadoObjeto.BAJO_CUSTODIA,
                ubicacionId = "ubi-001"
            )
        )

        // Datos semilla adicionales para que el inventario y los reportes
        // representen una operación real desde el primer arranque.
        listOf(
            ObjetoEncontrado("obj-2026-0003", "UTP-2026-0150", "Billetera", "Billetera marrón con tarjetas", "Iniciales J.P. en el interior", "Torre C - Pasillo central", "2026-10-26", USUARIO_REGISTRADOR_ID, EstadoObjeto.BAJO_CUSTODIA, "ubi-001"),
            ObjetoEncontrado("obj-2026-0004", "UTP-2026-0151", "Mochila", "Mochila azul con cuaderno de cálculo", "Llavero metálico en el cierre", "Pabellón A - Aula 204", "2026-10-27", USUARIO_REGISTRADOR_ID, EstadoObjeto.BAJO_CUSTODIA, "ubi-002"),
            ObjetoEncontrado("obj-2026-0005", "UTP-2026-0152", "Carné", "Carné universitario UTP", "Código visible en el reverso", "Biblioteca - Mesa 12", "2026-10-28", USUARIO_REGISTRADOR_ID, EstadoObjeto.PENDIENTE_RECEPCION, null),
            ObjetoEncontrado("obj-2026-0006", "UTP-2026-0153", "Audífonos", "Audífonos inalámbricos negros", "Estuche con una marca blanca", "Torre B - Laboratorio 3", "2026-10-28", USUARIO_REGISTRADOR_ID, EstadoObjeto.BAJO_CUSTODIA, "ubi-002"),
            ObjetoEncontrado("obj-2026-0007", "UTP-2026-0101", "Calculadora", "Calculadora científica gris", "Etiqueta con nombre del propietario", "Pabellón C - Aula 101", "2026-10-18", USUARIO_REGISTRADOR_ID, EstadoObjeto.ENTREGADO, "ubi-001"),
            ObjetoEncontrado("obj-2026-0008", "UTP-2026-0102", "USB", "Memoria USB roja de 32 GB", "Sticker de una estrella", "Sala de cómputo 2", "2026-10-19", USUARIO_REGISTRADOR_ID, EstadoObjeto.ENTREGADO, "ubi-001")
        ).forEach(objetos::add)

        reportesPerdida.addAll(
            listOf(
                ReportePerdida("rep-2026-0001", "per-2026-0001", "Laptop negra con stickers de la UTP", "Pabellón B - Aula 302", "2026-10-27 09:10", EstadoReportePerdida.COINCIDENCIA_SUGERIDA, USUARIO_REGISTRADOR_ID, "mquisep@edu.utp.edu.pe"),
                ReportePerdida("rep-2026-0002", "per-2026-0001", "Carné universitario azul", "Biblioteca", "2026-10-28 14:20", EstadoReportePerdida.BUSCANDO, USUARIO_REGISTRADOR_ID, "947123456"),
                ReportePerdida("rep-2026-0003", "per-2026-0001", "Audífonos inalámbricos negros", "Torre B", "2026-10-29 11:05", EstadoReportePerdida.EN_SEGUIMIENTO, USUARIO_REGISTRADOR_ID, "mquisep@edu.utp.edu.pe")
            )
        )

        objetos.add(
            ObjetoEncontrado(
                id = "obj-2026-0002",
                codigoCustodia = "UTP-2026-0098",
                categoria = "Celular",
                descripcion = "Celular negro con funda roja.",
                caracteristicasReservadas = "IMEI 8712345678901, marca Samsung, color negro.",
                lugarHallazgo = "Pabellón A - Cafetería",
                fechaHallazgo = "2026-10-25",
                registradoPor = LocalDataStore.USUARIO_REGISTRADOR_ID,
                estado = EstadoObjeto.ENTREGADO,
                ubicacionId = "ubi-002"
            )
        )

        reclamaciones.add(
            Reclamacion(
                id = RECLAMACION_DEMO_ID,
                objetoEncontradoId = "obj-2026-0001",
                personaAtendidaId = "per-2026-0001",
                reportePerdidaId = "rep-2026-0001",
                evidenciaIds = listOf("evi-2026-0001", "evi-2026-0002"),
                estado = EstadoReclamacion.PENDIENTE_VERIFICACION,
                motivoDecision = null,
                revisadoPor = null,
                fechaRegistro = "2026-10-27",
                descripcionReclamante = "Laptop negra con stickers de la UTP y funda de tela.",
                caracteristicasDeclaradas = "Tiene funda gris y el cargador original; olvidé la contraseña de encendido.",
                preguntas = emptyList(),
                fechaDecision = null,
                estadoAnterior = null,
                idOperacion = null
            )
        )

        evidencias.add(
            Evidencia(
                id = "evi-2026-0001",
                objetoEncontradoId = "obj-2026-0001",
                reclamacionId = RECLAMACION_DEMO_ID,
                tipo = "Fotografía",
                descripcion = "Fotografía de la contraseña de encendido y características registradas.",
                referenciaArchivo = "evidencias/evi-2026-0001.jpg",
                reservada = true,
                registradaPor = LocalDataStore.USUARIO_REGISTRADOR_ID
            )
        )

        evidencias.add(
            Evidencia(
                id = "evi-2026-0002",
                objetoEncontradoId = "obj-2026-0001",
                reclamacionId = RECLAMACION_DEMO_ID,
                tipo = "Fotografía",
                descripcion = "Fotografía general del estado del equipo.",
                referenciaArchivo = "evidencias/evi-2026-0002.jpg",
                reservada = false,
                registradaPor = LocalDataStore.USUARIO_REGISTRADOR_ID
            )
        )

        entregas.add(
            Entrega(
                id = "ent-2026-0001",
                objetoEncontradoId = "obj-2026-0001",
                reclamacionId = RECLAMACION_DEMO_ID,
                receptorId = "per-2026-0001",
                administradorId = LocalDataStore.USUARIO_ADMINISTRADOR_ID,
                fechaConfirmacion = "2026-10-30 10:24",
                firmaCapturada = true,
                estado = EstadoEntrega.CONFIRMADA,
                idOperacion = "op-demo-entrega-0001",
                firmaReferencia = "firma-demo-ent-2026-0001",
                receptorDni = "72123456",
                receptorCorreo = "mquisep@edu.utp.edu.pe"
            )
        )

        usuarios.add(
            Usuario(
                id = LocalDataStore.USUARIO_ADMINISTRADOR_ID,
                username = "administrador_sae",
                nombre = "Administrador SAE",
                role = LoginRole.ADMINISTRADOR_SAE
            )
        )

        eventosAuditoria.add(
            EventoAuditoria(
                id = "aud-2026-0001",
                entidadTipo = "Reclamacion",
                entidadId = RECLAMACION_DEMO_ID,
                accion = "RECLAMACION_REGISTRADA",
                usuarioId = LocalDataStore.USUARIO_REGISTRADOR_ID,
                fecha = "2026-10-27 09:14",
                detalle = "Reclamación registrada por el personal registrador."
            )
        )
    }
}

object AlmacenLocal {
    val instancia = LocalDataStore()
}
