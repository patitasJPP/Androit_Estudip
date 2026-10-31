package com.Jair.practica.view.perdida

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.Jair.practica.R
import com.Jair.practica.databinding.ItemCoincidenciaBinding
import com.Jair.practica.model.perdida.CoincidenciaSugerida

class CoincidenciasAdapter(
    private val alSeguir: (String) -> Unit
) : RecyclerView.Adapter<CoincidenciasAdapter.Holder>() {

    private var items: List<CoincidenciaSugerida> = emptyList()
    private var objetoEnSeguimientoId: String? = null
    private var puedeSeguir: Boolean = true

    fun enviar(
        items: List<CoincidenciaSugerida>,
        objetoEnSeguimientoId: String?,
        puedeSeguir: Boolean
    ) {
        this.items = items
        this.objetoEnSeguimientoId = objetoEnSeguimientoId
        this.puedeSeguir = puedeSeguir
        notifyDataSetChanged()
    }

    class Holder(val binding: ItemCoincidenciaBinding) : RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): Holder =
        Holder(ItemCoincidenciaBinding.inflate(LayoutInflater.from(parent.context), parent, false))

    override fun getItemCount(): Int = items.size

    override fun onBindViewHolder(holder: Holder, position: Int) {
        val item = items[position]
        val b = holder.binding
        val ctx = b.root.context

        b.tvCodigoCoincidencia.text = ctx.getString(R.string.perdida_item_codigo, item.codigoCustodia)
        b.tvDescripcionCoincidencia.text = item.descripcion
        b.tvAtributosCoincidencia.text = listOfNotNull(
            item.categoria,
            item.color,
            listOfNotNull(item.marca, item.modelo).joinToString(" ").ifBlank { null }
        ).joinToString(" · ")
        b.tvUbicacionCoincidencia.text = listOfNotNull(
            item.pabellon?.let { ctx.getString(R.string.perdida_item_pabellon, it) },
            item.piso?.let { ctx.getString(R.string.perdida_item_piso, it) },
            item.aula?.let { ctx.getString(R.string.perdida_item_aula, it) },
            item.fechaHallazgo?.let { ctx.getString(R.string.perdida_item_hallado, it) }
        ).joinToString(" · ")

        val enSeguimiento = item.objetoId == objetoEnSeguimientoId
        b.tvEnSeguimiento.visibility = if (enSeguimiento) View.VISIBLE else View.GONE
        b.btnSeguimiento.visibility = if (!enSeguimiento && puedeSeguir) View.VISIBLE else View.GONE
        b.btnSeguimiento.setOnClickListener { alSeguir(item.objetoId) }
    }
}
