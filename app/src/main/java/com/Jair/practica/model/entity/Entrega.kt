package com.Jair.practica.model.entity

import android.graphics.Bitmap

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
    val firmaImagen: Bitmap? = null
)
