package com.Jair.practica.model.entity

data class Entrega(
    val id: String,
    val objetoEncontradoId: String,
    val reclamacionId: String,
    val receptorId: String,
    val administradorId: String,
    val fechaConfirmacion: String? = null,
    val firmaCapturada: Boolean = false,
    val estado: EstadoEntrega = EstadoEntrega.RESERVADA,
    val idOperacion: String? = null,
    /** Referencia persistible de la firma capturada; la imagen no pertenece al dominio. */
    val firmaReferencia: String? = null,
    val receptorDni: String? = null,
    val receptorCorreo: String? = null
)
