package com.Jair.practica.view.custodia

import android.content.Intent
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.Jair.practica.R
import com.Jair.practica.model.entity.LoginRole
import com.Jair.practica.model.entity.EstadoObjeto
import com.Jair.practica.model.local.LocalDataStore
import com.Jair.practica.model.local.LocalObjetoEncontradoRepository
import com.Jair.practica.model.local.AlmacenLocal
import com.Jair.practica.view.devolucion.HistorialEntregasActivity
import com.Jair.practica.view.devolucion.VerificacionEntregaActivity
import com.Jair.practica.view.devolucion.VerificarReclamacionActivity
import com.Jair.practica.view.common.PendientesSaeActivity
import com.Jair.practica.model.remote.SincronizacionRemota
import com.Jair.practica.view.common.AppUi
import android.widget.LinearLayout
import android.view.View

class InventarioController(private val activity: AppCompatActivity) {

    fun iniciar() {
        val contenido = activity.findViewById<LinearLayout>(R.id.llContenido)
        contenido.addView(AppUi.primary(activity, "Consultar y actualizar inventario completo").apply {
            setOnClickListener { activity.startActivity(Intent(activity, GestionInventarioActivity::class.java)) }
        }, 3, LinearLayout.LayoutParams(-1, -2).apply {
            topMargin = AppUi.dp(activity, 12)
        })
        SincronizacionRemota.refrescarObjetos { cargarResumen() }
        cargarResumen()
    }

    private fun cargarResumen() {
        // El inventario se alimenta del mismo repositorio local usado por el
        // registrador. Así, un hallazgo creado en el otro rol aparece aquí
        // después de confirmar su recepción, en lugar de quedar como dato de
        // demostración aislado.
        val objetos = LocalObjetoEncontradoRepository().list()
        val ultimoId = AlmacenLocal.instancia.ultimoObjetoRegistradoId
        val inventario = objetos.count { it.estado == EstadoObjeto.BAJO_CUSTODIA }
        val reclamados = objetos.count { it.estado == EstadoObjeto.ENTREGADO }
        val vencen = objetos.count { it.estado == EstadoObjeto.PENDIENTE_RECEPCION || it.estado == EstadoObjeto.HALLAZGO_REPORTADO }

        activity.findViewById<TextView>(R.id.tvInventarioValor).text = inventario.toString()
        activity.findViewById<TextView>(R.id.tvReclamadosValor).text = reclamados.toString()
        activity.findViewById<TextView>(R.id.tvVencenValor).text = vencen.toString()

        val reciente = ultimoId?.let { id -> objetos.firstOrNull { it.id == id } }
            ?: objetos.lastOrNull()
        reciente?.let { objeto ->
            activity.findViewById<TextView>(R.id.tvZonaSubtitulo).text =
                "Último hallazgo sincronizado · ${objeto.descripcion} · Estado: ${estadoVisible(objeto.estado)}"
        }

        // Las tarjetas se construyen con los objetos reales del repositorio
        // local, que a su vez se actualiza desde Supabase. Así, la acción
        // Entregar siempre trabaja sobre el objeto que el administrador ve.
        val objetosVisibles = objetos
            .filter { it.estado != EstadoObjeto.ENTREGADO && it.estado != EstadoObjeto.ANULADO }
            .sortedByDescending { it.fechaHallazgo.orEmpty() }
            .take(3)
        val tarjetas = listOf(
            Triple(R.id.cardObjeto1, R.id.tvTituloObjeto1, R.id.tvFechaObjeto1) to R.id.tvUbicacionObjeto1,
            Triple(R.id.cardObjeto2, R.id.tvTituloObjeto2, R.id.tvFechaObjeto2) to R.id.tvUbicacionObjeto2,
            Triple(R.id.cardObjeto3, R.id.tvTituloObjeto3, R.id.tvFechaObjeto3) to R.id.tvUbicacionObjeto3
        )
        tarjetas.forEachIndexed { index, tarjeta ->
            val objeto = objetosVisibles.getOrNull(index)
            val card = activity.findViewById<View>(tarjeta.first.first)
            if (objeto == null) {
                card.visibility = View.GONE
                return@forEachIndexed
            }
            card.visibility = View.VISIBLE
            activity.findViewById<TextView>(tarjeta.first.second).text = objeto.descripcion
            activity.findViewById<TextView>(tarjeta.first.third).text = objeto.fechaHallazgo ?: "Registro reciente"
            activity.findViewById<TextView>(tarjeta.second).text =
                "Estado: ${estadoVisible(objeto.estado)} · ${objeto.lugarHallazgo ?: objeto.ubicacionId ?: "Ubicación pendiente"}"
        }

        val botones = listOf(
            R.id.btnVerFicha1, R.id.btnVerFicha2, R.id.btnVerFicha3
        )
        botones.forEachIndexed { index, id ->
            activity.findViewById<Button>(id).setOnClickListener {
                objetosVisibles.getOrNull(index)?.let { objeto ->
                    activity.startActivity(Intent(activity, ProtocoloEntregaActivity::class.java).apply {
                        putExtra("objetoId", objeto.id)
                    })
                }
            }
        }

        activity.findViewById<android.view.View>(R.id.navCustodia).setOnClickListener {
            activity.startActivity(
                Intent(activity, HistorialEntregasActivity::class.java).apply {
                    putExtra("usuarioId", LocalDataStore.USUARIO_ADMINISTRADOR_ID)
                    putExtra("rol", LoginRole.ADMINISTRADOR_SAE.code)
                }
            )
        }

        listOf(R.id.btnEntregar1, R.id.btnEntregar2, R.id.btnEntregar3).forEachIndexed { index, id ->
            activity.findViewById<Button>(id).setOnClickListener {
                objetosVisibles.getOrNull(index)?.let { objeto ->
                    activity.startActivity(Intent(activity, VerificacionEntregaActivity::class.java).apply {
                        putExtra("objetoId", objeto.id)
                    })
                }
            }
        }
        activity.findViewById<android.view.View>(R.id.navReportes).setOnClickListener {
            activity.startActivity(
                Intent(activity, HistorialEntregasActivity::class.java).apply {
                    putExtra("usuarioId", LocalDataStore.USUARIO_ADMINISTRADOR_ID)
                    putExtra("rol", LoginRole.ADMINISTRADOR_SAE.code)
                }
            )
        }

        activity.findViewById<Button>(R.id.btnVerReclamaciones).setOnClickListener {
            activity.startActivity(
                Intent(activity, PendientesSaeActivity::class.java).apply {
                    putExtra("usuarioId", LocalDataStore.USUARIO_ADMINISTRADOR_ID)
                    putExtra("rol", LoginRole.ADMINISTRADOR_SAE.code)
                    putExtra(PendientesSaeActivity.EXTRA_TIPO, PendientesSaeActivity.TIPO_RECLAMACIONES)
                }
            )
        }

        resaltarNavItem(R.id.navObjetos)
    }

    fun actualizarDesdeSupabase() {
        SincronizacionRemota.refrescarObjetos { cargarResumen() }
    }

    private fun resaltarNavItem(itemActivoId: Int) {
        val items = listOf(
            R.id.navObjetos to Pair(R.id.imgNavObjetos, R.id.tvNavObjetos),
            R.id.navCustodia to Pair(R.id.imgNavCustodia, R.id.tvNavCustodia),
            R.id.navReportes to Pair(R.id.imgNavReportes, R.id.tvNavReportes),
            R.id.navPerfil to Pair(R.id.imgNavPerfil, R.id.tvNavPerfil)
        )
        items.forEach { (itemId, ids) ->
            val color = if (itemId == itemActivoId) "#930029" else "#1B1B25"
            activity.findViewById<ImageView>(ids.first).setColorFilter(
                android.graphics.Color.parseColor(color)
            )
            activity.findViewById<TextView>(ids.second).setTextColor(
                android.graphics.Color.parseColor(color)
            )
        }
    }

    private fun estadoVisible(estado: EstadoObjeto): String = when (estado) {
        EstadoObjeto.BAJO_CUSTODIA -> "Bajo custodia"
        EstadoObjeto.PENDIENTE_RECEPCION -> "Pendiente de recepción"
        EstadoObjeto.HALLAZGO_REPORTADO -> "Hallazgo reportado"
        EstadoObjeto.ENTREGADO -> "Entregado"
        EstadoObjeto.ANULADO -> "Anulado"
    }
}
