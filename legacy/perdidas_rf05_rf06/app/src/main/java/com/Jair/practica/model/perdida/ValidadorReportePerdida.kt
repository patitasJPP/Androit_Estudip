package com.Jair.practica.model.perdida

/** Por qué no se puede ejecutar una búsqueda con los filtros dados. */
enum class ErrorFiltros { SIN_FILTROS, FUERA_DE_CATALOGO }

object ValidadorReportePerdida {
    const val DESCRIPCION_MIN = 10
    const val DESCRIPCION_MAX = 240
    const val LUGAR_MAX = 120
    const val TEXTO_CORTO_MAX = 40
    const val NOMBRE_MAX = 80

    private val CORREO = Regex("^[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$")
    private val TELEFONO_MOVIL = Regex("^9\\d{8}$")

    /** Deja solo los 9 dígitos del teléfono: quita espacios, guiones y el prefijo +51 si viene. */
    fun limpiarTelefono(texto: String): String {
        val digitos = texto.filter { it.isDigit() }
        return if (digitos.length == 11 && digitos.startsWith("51")) digitos.drop(2) else digitos
    }

    /** Validación para REGISTRAR: exige descripción y medio de contacto, además de los formatos. */
    fun validarRegistro(datos: DatosReportePerdida): ErroresReporte {
        val base = validarFormatos(datos)
        val descripcion = datos.descripcion.trim()
        val errorDescripcion = when {
            descripcion.isEmpty() -> "Describe el objeto perdido."
            descripcion.length < DESCRIPCION_MIN -> "Escribe al menos $DESCRIPCION_MIN caracteres."
            else -> base.descripcion
        }
        val sinContacto = datos.contactoCorreo.isBlank() && datos.contactoTelefono.isBlank()
        return base.copy(
            descripcion = errorDescripcion,
            medioContacto = if (sinContacto) "Indica un correo o un teléfono de contacto." else null
        )
    }

    /** Validación para guardar un BORRADOR: solo formatos y longitudes de lo que ya se escribió. */
    fun validarBorrador(datos: DatosReportePerdida): ErroresReporte = validarFormatos(datos)

    /** Validación de los filtros de RF-06: al menos uno, y valores de catálogo válidos. */
    fun validarFiltros(filtros: FiltrosCoincidencia): ErrorFiltros? {
        if (!filtros.hayAlguno()) return ErrorFiltros.SIN_FILTROS
        val fuera = !CatalogoPerdidas.estaEn(filtros.pabellon, CatalogoPerdidas.PABELLONES) ||
            !CatalogoPerdidas.estaEn(filtros.piso, CatalogoPerdidas.PISOS) ||
            !CatalogoPerdidas.estaEn(filtros.categoria, CatalogoPerdidas.CATEGORIAS) ||
            !CatalogoPerdidas.estaEn(filtros.color, CatalogoPerdidas.COLORES)
        return if (fuera) ErrorFiltros.FUERA_DE_CATALOGO else null
    }

    private fun validarFormatos(d: DatosReportePerdida): ErroresReporte {
        val correo = d.contactoCorreo.trim()
        val telefono = d.contactoTelefono.trim()
        return ErroresReporte(
            descripcion = if (d.descripcion.trim().length > DESCRIPCION_MAX) {
                "Máximo $DESCRIPCION_MAX caracteres."
            } else null,
            lugarAproximado = largoMaximo(d.lugarAproximado, LUGAR_MAX),
            categoria = catalogo(d.categoria, CatalogoPerdidas.CATEGORIAS, "Categoría"),
            color = catalogo(d.color, CatalogoPerdidas.COLORES, "Color"),
            marca = largoMaximo(d.marca, TEXTO_CORTO_MAX),
            modelo = largoMaximo(d.modelo, TEXTO_CORTO_MAX),
            pabellon = catalogo(d.pabellon, CatalogoPerdidas.PABELLONES, "Pabellón"),
            piso = catalogo(d.piso, CatalogoPerdidas.PISOS, "Piso"),
            aula = largoMaximo(d.aula, TEXTO_CORTO_MAX),
            contactoNombre = largoMaximo(d.contactoNombre, NOMBRE_MAX),
            contactoCorreo = if (correo.isNotEmpty() && !CORREO.matches(correo)) {
                "Correo no válido."
            } else null,
            contactoTelefono = if (telefono.isNotEmpty() &&
                !TELEFONO_MOVIL.matches(limpiarTelefono(telefono))
            ) "Teléfono no válido: 9 dígitos que empiecen con 9." else null
        )
    }

    private fun largoMaximo(valor: String, maximo: Int): String? =
        if (valor.trim().length > maximo) "Máximo $maximo caracteres." else null

    private fun catalogo(valor: String, catalogo: List<String>, nombre: String): String? =
        if (CatalogoPerdidas.estaEn(valor, catalogo)) null else "$nombre no válido."
}
