package com.Jair.practica.view.devolucion

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.Jair.practica.R
import com.Jair.practica.model.entity.Entrega
import com.Jair.practica.viewmodel.devolucion.EntregaHistorial
import com.Jair.practica.viewmodel.devolucion.HistorialEntregasViewModel

class HistorialEntregasAdapter(
    private val onVerActa: (Entrega) -> Unit
) : RecyclerView.Adapter<HistorialEntregasAdapter.HistorialEntregasViewHolder>() {

    private val items = mutableListOf<EntregaHistorial>()

    fun submitList(nuevosItems: List<EntregaHistorial>) {
        items.clear()
        items.addAll(nuevosItems)
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): HistorialEntregasViewHolder =
        HistorialEntregasViewHolder(
            LayoutInflater.from(parent.context).inflate(R.layout.item_acta_entrega, parent, false)
        )

    override fun onBindViewHolder(holder: HistorialEntregasViewHolder, position: Int) {
        holder.bind(items[position], onVerActa)
    }

    override fun getItemCount(): Int = items.size

    inner class HistorialEntregasViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {

        fun bind(item: EntregaHistorial, onVerActa: (Entrega) -> Unit) {
            itemView.findViewById<TextView>(R.id.tvCodigoActa).text = item.entrega.id
            itemView.findViewById<TextView>(R.id.tvEstadoEntrega).text =
                HistorialEntregasViewModel.etiquetaEstadoEntrega(item.entrega.estado)

            val textoObjeto = listOfNotNull(
                item.objeto?.categoria,
                item.objeto?.descripcion
            ).filter { !it.isNullOrBlank() }.joinToString(" · ").ifEmpty { item.entrega.objetoEncontradoId }
            itemView.findViewById<TextView>(R.id.tvNombreObjeto).text = textoObjeto

            itemView.findViewById<TextView>(R.id.tvFechaEntrega).text = item.entrega.fechaConfirmacion ?: ""
            itemView.findViewById<TextView>(R.id.tvSedeEntrega).text = item.objeto?.lugarHallazgo ?: ""

            itemView.findViewById<TextView>(R.id.tvNombrePersona).text = item.persona?.nombre ?: ""
            itemView.findViewById<TextView>(R.id.tvCodigoPersona).text = item.persona?.identificador ?: ""
            itemView.findViewById<TextView>(R.id.tvDniPersona).text =
                HistorialEntregasViewModel.enmascararDni(item.persona?.identificador)
            itemView.findViewById<TextView>(R.id.tvVerificacionPersona).text =
                if (item.persona?.identificador.isNullOrBlank()) "" else "Cotejación de identidad: OK"

            itemView.findViewById<View>(R.id.btnVerActa).setOnClickListener { onVerActa(item.entrega) }
        }
    }
}
