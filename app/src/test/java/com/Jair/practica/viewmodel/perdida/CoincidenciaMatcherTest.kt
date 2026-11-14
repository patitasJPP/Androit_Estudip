package com.Jair.practica.viewmodel.perdida

import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class CoincidenciaMatcherTest {
    @Test fun reconoceAulaConCeroOpcional() {
        val lugar = "CAMPUS CENTRAL - UTP, Torre A, Piso 6, Salón - A0601"
        assertTrue(CoincidenciaMatcher.coincideLugar("A601", lugar))
        assertTrue(CoincidenciaMatcher.coincideLugar("A0601", lugar))
        assertTrue(CoincidenciaMatcher.coincideLugar("Piso 6", lugar))
    }

    @Test fun descartaOtroSalon() {
        assertFalse(CoincidenciaMatcher.coincideLugar("A0602", "Torre A, Piso 6, Salón - A0601"))
    }
}
