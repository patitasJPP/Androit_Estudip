package com.Jair.practica.model.local

import com.Jair.practica.model.entity.ObjetoEncontrado
import com.Jair.practica.model.repository.ObjetoEncontradoRepository

class LocalObjetoEncontradoRepository(
    private val store: LocalDataStore = AlmacenLocal.instancia
) : ObjetoEncontradoRepository {

    override fun findById(id: String): ObjetoEncontrado? =
        store.objetos.firstOrNull { it.id == id }

    override fun findByCodigo(codigo: String): ObjetoEncontrado? =
        store.objetos.firstOrNull { it.codigoCustodia.equals(codigo, ignoreCase = true) }

    override fun list(): List<ObjetoEncontrado> = store.objetos.toList()

    override fun save(objeto: ObjetoEncontrado): ObjetoEncontrado {
        val index = store.objetos.indexOfFirst { it.id == objeto.id }
        if (index >= 0) {
            store.objetos[index] = objeto
        } else {
            store.objetos.add(objeto)
        }
        return objeto
    }

    override fun update(objeto: ObjetoEncontrado): ObjetoEncontrado {
        val index = store.objetos.indexOfFirst { it.id == objeto.id }
        if (index >= 0) {
            store.objetos[index] = objeto
        }
        return objeto
    }
}
