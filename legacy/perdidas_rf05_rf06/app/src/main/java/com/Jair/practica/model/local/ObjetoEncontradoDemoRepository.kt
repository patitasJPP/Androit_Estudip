package com.Jair.practica.model.local

import com.Jair.practica.model.entity.EstadoObjeto
import com.Jair.practica.model.entity.ObjetoEncontrado
import com.Jair.practica.model.repository.ObjetoEncontradoRepository

class ObjetoEncontradoDemoRepository(
    private val objetos: MutableList<ObjetoEncontrado> = semilla()
) : ObjetoEncontradoRepository {

    override fun findById(id: String): ObjetoEncontrado? = objetos.firstOrNull { it.id == id }

    override fun findByCodigo(codigo: String): ObjetoEncontrado? =
        objetos.firstOrNull { it.codigoCustodia.equals(codigo, ignoreCase = true) }

    override fun list(): List<ObjetoEncontrado> = objetos.toList()

    override fun save(objeto: ObjetoEncontrado): ObjetoEncontrado {
        val index = objetos.indexOfFirst { it.id == objeto.id }
        if (index >= 0) objetos[index] = objeto else objetos.add(objeto)
        return objeto
    }

    override fun update(objeto: ObjetoEncontrado): ObjetoEncontrado {
        val index = objetos.indexOfFirst { it.id == objeto.id }
        if (index >= 0) objetos[index] = objeto
        return objeto
    }

    companion object {
        val instancia = ObjetoEncontradoDemoRepository()

        private const val REGISTRADOR = "usr-registrador-01"

        fun semilla(): MutableList<ObjetoEncontrado> = mutableListOf(
            ObjetoEncontrado(
                id = "obj-2026-0001", codigoCustodia = "UTP-2026-0142", categoria = "Laptop",
                descripcion = "Laptop negra con stickers de la UTP y funda de tela.",
                caracteristicasReservadas = "Funda gris, cargador original, contraseña de encendido de 4 dígitos.",
                lugarHallazgo = "Pabellón B - Aula 302", fechaHallazgo = "2026-10-22",
                registradoPor = REGISTRADOR, estado = EstadoObjeto.BAJO_CUSTODIA, ubicacionId = "ubi-001",
                color = "Negro", marca = "Lenovo", modelo = "ThinkPad E14",
                pabellon = "B", piso = "3", aula = "302"
            ),
            // ENTREGADO: nunca debe sugerirse (sirve para probar la regla de estado).
            ObjetoEncontrado(
                id = "obj-2026-0002", codigoCustodia = "UTP-2026-0098", categoria = "Celular",
                descripcion = "Celular negro con funda roja.",
                caracteristicasReservadas = "IMEI y fondo de pantalla personalizado.",
                lugarHallazgo = "Pabellón A - Cafetería", fechaHallazgo = "2026-10-25",
                registradoPor = REGISTRADOR, estado = EstadoObjeto.ENTREGADO, ubicacionId = "ubi-002",
                color = "Negro", marca = "Samsung", modelo = "Galaxy A54",
                pabellon = "A", piso = "1", aula = "Cafetería"
            ),
            ObjetoEncontrado(
                id = "obj-2026-0003", codigoCustodia = "UTP-2026-0155", categoria = "Mochila",
                descripcion = "Mochila azul con llavero de la UTP.",
                caracteristicasReservadas = "Contiene un cuaderno con nombre escrito en la primera hoja.",
                lugarHallazgo = "Pabellón C - Aula 204", fechaHallazgo = "2026-10-27",
                registradoPor = REGISTRADOR, estado = EstadoObjeto.BAJO_CUSTODIA, ubicacionId = "ubi-001",
                color = "Azul", marca = "Totto",
                pabellon = "C", piso = "2", aula = "204"
            ),
            ObjetoEncontrado(
                id = "obj-2026-0004", codigoCustodia = "UTP-2026-0160", categoria = "Laptop",
                descripcion = "Laptop gris con calcomanía de un gato.",
                caracteristicasReservadas = "Rayón en la tapa y número de serie en la base.",
                lugarHallazgo = "Pabellón A - Aula 501", fechaHallazgo = "2026-10-28",
                registradoPor = REGISTRADOR, estado = EstadoObjeto.BAJO_CUSTODIA, ubicacionId = "ubi-002",
                color = "Gris", marca = "HP", modelo = "Pavilion 15",
                pabellon = "A", piso = "5", aula = "501"
            ),
            // HALLAZGO_REPORTADO: aún sin confirmar por el SAE, no está disponible ni tiene código.
            ObjetoEncontrado(
                id = "obj-2026-0005", codigoCustodia = "", categoria = "Audífonos",
                descripcion = "Audífonos inalámbricos negros.",
                lugarHallazgo = "Pabellón D - Biblioteca", fechaHallazgo = "2026-10-30",
                registradoPor = REGISTRADOR, estado = EstadoObjeto.HALLAZGO_REPORTADO,
                color = "Negro", marca = "Sony", modelo = "WH-1000XM4",
                pabellon = "D", piso = "1", aula = "Biblioteca"
            ),
            ObjetoEncontrado(
                id = "obj-2026-0006", codigoCustodia = "UTP-2026-0172", categoria = "Documentos",
                descripcion = "Carné universitario UTP.",
                caracteristicasReservadas = "Nombre y código del titular impresos en el carné.",
                lugarHallazgo = "Pabellón B - Pasillo", fechaHallazgo = "2026-10-29",
                registradoPor = REGISTRADOR, estado = EstadoObjeto.BAJO_CUSTODIA, ubicacionId = "ubi-001",
                color = "Blanco", pabellon = "B", piso = "1", aula = "Pasillo"
            ),
            ObjetoEncontrado(
                id = "obj-2026-0007", codigoCustodia = "UTP-2026-0180", categoria = "Celular",
                descripcion = "Celular blanco con funda transparente.",
                caracteristicasReservadas = "Fondo de pantalla con foto familiar.",
                lugarHallazgo = "Pabellón B - Aula 305", fechaHallazgo = "2026-10-30",
                registradoPor = REGISTRADOR, estado = EstadoObjeto.BAJO_CUSTODIA, ubicacionId = "ubi-002",
                color = "Blanco", marca = "Apple", modelo = "iPhone 13",
                pabellon = "B", piso = "3", aula = "305"
            )
        )
    }
}
