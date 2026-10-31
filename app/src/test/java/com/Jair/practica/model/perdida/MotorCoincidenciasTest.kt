package com.Jair.practica.model.perdida

import com.Jair.practica.model.local.ObjetoEncontradoDemoRepository
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

/** Reglas de RF-06: solo bajo custodia, sin datos reservados, filtros combinados. */
class MotorCoincidenciasTest {

    private val objetos = ObjetoEncontradoDemoRepository.semilla()

    private fun ids(f: FiltrosCoincidencia) = MotorCoincidencias.buscar(objetos, f).map { it.objetoId }

    @Test
    fun soloSugiereObjetosBajoCustodia() {
        // Hay 3 objetos negros: laptop (bajo custodia), celular (ENTREGADO) y audífonos (HALLAZGO_REPORTADO).
        assertEquals(listOf("obj-2026-0001"), ids(FiltrosCoincidencia(color = "Negro")))
    }

    @Test
    fun filtraPorPabellonYPiso() {
        assertEquals(
            listOf("obj-2026-0001", "obj-2026-0007"),
            ids(FiltrosCoincidencia(pabellon = "B", piso = "3"))
        )
    }

    @Test
    fun marcaSeBuscaSinMayusculasNiTildes() {
        assertEquals(listOf("obj-2026-0001"), ids(FiltrosCoincidencia(marca = "LENOVO")))
        assertEquals(listOf("obj-2026-0001"), ids(FiltrosCoincidencia(modelo = "thinkpad")))
    }

    @Test
    fun todosLosFiltrosEscritosSeExigenALaVez() {
        assertEquals(listOf("obj-2026-0004"), ids(FiltrosCoincidencia(categoria = "Laptop", color = "Gris")))
        assertTrue(ids(FiltrosCoincidencia(categoria = "Laptop", color = "Rojo")).isEmpty())
    }

    @Test
    fun objetoSinElDatoNoCoincide() {
        // La mochila no tiene modelo registrado.
        assertTrue(ids(FiltrosCoincidencia(categoria = "Mochila", modelo = "cualquiera")).isEmpty())
    }

    @Test
    fun resultadoOrdenadoPorCodigoDeCustodia() {
        val resultado = MotorCoincidencias.buscar(objetos, FiltrosCoincidencia(categoria = "Laptop"))
        assertEquals(listOf("UTP-2026-0142", "UTP-2026-0160"), resultado.map { it.codigoCustodia })
    }

    @Test
    fun laVistaPublicaNoExponeInformacionReservada() {
        val campos = CoincidenciaSugerida::class.java.declaredFields.map { it.name.lowercase() }
        assertFalse(campos.any { it.contains("reservad") })
        assertFalse(campos.any { it.contains("registradopor") })
        assertFalse(campos.any { it.contains("ubicacion") })
    }
}
