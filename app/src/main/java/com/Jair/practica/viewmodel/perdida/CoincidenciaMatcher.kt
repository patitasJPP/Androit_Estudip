package com.Jair.practica.viewmodel.perdida

import java.util.Locale

/** Compara un lugar aproximado con la ubicación registrada del hallazgo. */
object CoincidenciaMatcher {
    private val codigoAula = Regex("\\b([A-Z]{1,4})-?0*(\\d{2,5})\\b")

    fun coincideLugar(lugarReportado: String?, lugarHallazgo: String?): Boolean {
        if (lugarReportado.isNullOrBlank()) return true
        val reportado = lugarReportado.trim().uppercase(Locale.ROOT)
        val hallazgo = lugarHallazgo.orEmpty().uppercase(Locale.ROOT)
        if (hallazgo.contains(reportado)) return true

        val codigoReportado = codigoAula.find(reportado) ?: return false
        val codigoHallazgo = codigoAula.findAll(hallazgo).firstOrNull {
            it.groupValues[1] == codigoReportado.groupValues[1] &&
                it.groupValues[2].toIntOrNull() == codigoReportado.groupValues[2].toIntOrNull()
        }
        return codigoHallazgo != null
    }
}
