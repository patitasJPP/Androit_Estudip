package com.Jair.practica.model.repository

import com.Jair.practica.model.entity.ObjetoEncontrado

interface ObjetoEncontradoRepository {
    fun findById(id: String): ObjetoEncontrado?
    fun findByCodigo(codigo: String): ObjetoEncontrado?
    fun list(): List<ObjetoEncontrado>
    fun save(objeto: ObjetoEncontrado): ObjetoEncontrado
    fun update(objeto: ObjetoEncontrado): ObjetoEncontrado
}
