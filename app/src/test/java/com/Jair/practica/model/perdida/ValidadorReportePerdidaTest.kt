package com.Jair.practica.model.perdida

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class ValidadorReportePerdidaTest {

    private val completo = DatosReportePerdida(
        descripcion = "Laptop negra con stickers de la UTP",
        contactoCorreo = "alumno@utp.edu.pe"
    )

    @Test
    fun registroValidoConSoloCorreoNoTieneErrores() {
        assertFalse(ValidadorReportePerdida.validarRegistro(completo).hayErrores)
    }

    @Test
    fun registroValidoConSoloTelefonoNoTieneErrores() {
        val datos = completo.copy(contactoCorreo = "", contactoTelefono = "987 654 321")
        assertFalse(ValidadorReportePerdida.validarRegistro(datos).hayErrores)
    }

    @Test
    fun registroSinDescripcionSeRechaza() {
        val errores = ValidadorReportePerdida.validarRegistro(completo.copy(descripcion = "   "))
        assertNotNull(errores.descripcion)
    }

    @Test
    fun descripcionMuyCortaSeRechaza() {
        val errores = ValidadorReportePerdida.validarRegistro(completo.copy(descripcion = "laptop"))
        assertNotNull(errores.descripcion)
    }

    @Test
    fun descripcionMuyLargaSeRechaza() {
        val errores = ValidadorReportePerdida.validarRegistro(completo.copy(descripcion = "x".repeat(241)))
        assertNotNull(errores.descripcion)
    }

    @Test
    fun registroSinMedioDeContactoSeRechaza() {
        val errores = ValidadorReportePerdida.validarRegistro(completo.copy(contactoCorreo = ""))
        assertNotNull(errores.medioContacto)
        assertTrue(errores.hayErrores)
    }

    @Test
    fun correoInvalidoSeReportaAunqueElTelefonoSeaValido() {
        val datos = completo.copy(contactoCorreo = "no-es-correo", contactoTelefono = "987654321")
        val errores = ValidadorReportePerdida.validarRegistro(datos)
        assertNotNull(errores.contactoCorreo)
        assertNull(errores.contactoTelefono)
    }

    @Test
    fun telefonoConPrefijoPeruSeAcepta() {
        val datos = completo.copy(contactoCorreo = "", contactoTelefono = "+51 987-654-321")
        assertFalse(ValidadorReportePerdida.validarRegistro(datos).hayErrores)
        assertEquals("987654321", ValidadorReportePerdida.limpiarTelefono("+51 987-654-321"))
    }

    @Test
    fun telefonoConOchoDigitosSeRechaza() {
        val datos = completo.copy(contactoCorreo = "", contactoTelefono = "98765432")
        assertNotNull(ValidadorReportePerdida.validarRegistro(datos).contactoTelefono)
    }

    @Test
    fun valoresFueraDeCatalogoSeRechazan() {
        val errores = ValidadorReportePerdida.validarRegistro(
            completo.copy(categoria = "Dinosaurio", pabellon = "Z", piso = "99", color = "Fucsia")
        )
        assertNotNull(errores.categoria)
        assertNotNull(errores.pabellon)
        assertNotNull(errores.piso)
        assertNotNull(errores.color)
    }

    @Test
    fun catalogoIgnoraMayusculasYTildes() {
        val errores = ValidadorReportePerdida.validarRegistro(
            completo.copy(categoria = "audifonos", color = "MARRON")
        )
        assertNull(errores.categoria)
        assertNull(errores.color)
    }

    @Test
    fun borradorNoExigeDescripcionNiContacto() {
        val errores = ValidadorReportePerdida.validarBorrador(DatosReportePerdida(categoria = "Mochila"))
        assertFalse(errores.hayErrores)
    }

    @Test
    fun borradorSiRechazaFormatosInvalidos() {
        val errores = ValidadorReportePerdida.validarBorrador(DatosReportePerdida(contactoCorreo = "xx"))
        assertNotNull(errores.contactoCorreo)
    }

    @Test
    fun filtrosVaciosSeRechazan() {
        assertEquals(ErrorFiltros.SIN_FILTROS, ValidadorReportePerdida.validarFiltros(FiltrosCoincidencia()))
    }

    @Test
    fun filtroValidoSeAcepta() {
        assertNull(ValidadorReportePerdida.validarFiltros(FiltrosCoincidencia(color = "Negro")))
    }

    @Test
    fun filtroFueraDeCatalogoSeRechaza() {
        assertEquals(
            ErrorFiltros.FUERA_DE_CATALOGO,
            ValidadorReportePerdida.validarFiltros(FiltrosCoincidencia(piso = "99"))
        )
    }
}
