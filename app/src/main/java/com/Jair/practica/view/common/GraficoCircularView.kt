package com.Jair.practica.view.common

import android.content.Context
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.RectF
import android.view.View
import kotlin.math.min

class GraficoCircularView(context: Context) : View(context) {
    data class Segmento(val etiqueta: String, val valor: Int, val color: Int)
    var segmentos: List<Segmento> = emptyList()
        set(value) { field = value; invalidate() }

    private val paint = Paint(Paint.ANTI_ALIAS_FLAG)

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        if (segmentos.isEmpty()) return
        val total = segmentos.sumOf { it.valor }.coerceAtLeast(1)
        val size = min(width * .54f, height * .48f)
        val left = (width - size) / 2f
        val top = 12f
        val rect = RectF(left, top, left + size, top + size)
        var inicio = -90f
        segmentos.forEach { segmento ->
            val barrido = 360f * segmento.valor / total
            paint.color = segmento.color
            canvas.drawArc(rect, inicio, barrido, true, paint)
            inicio += barrido
        }
        paint.color = 0xFFF7F8FC.toInt()
        canvas.drawCircle(rect.centerX(), rect.centerY(), size * .27f, paint)
        paint.color = AppUi.INK
        paint.textSize = 30f
        paint.textAlign = Paint.Align.CENTER
        canvas.drawText(total.toString(), rect.centerX(), rect.centerY() + 10f, paint)
        paint.textSize = 11f
        canvas.drawText("objetos", rect.centerX(), rect.centerY() + 28f, paint)

        paint.textAlign = Paint.Align.LEFT
        paint.textSize = 12f
        var x = width * .14f
        var y = top + size + 34f
        segmentos.forEachIndexed { index, segmento ->
            if (index == 2) { x = width * .56f; y = top + size + 34f }
            paint.color = segmento.color
            canvas.drawCircle(x, y - 4f, 6f, paint)
            paint.color = AppUi.INK
            canvas.drawText("${segmento.etiqueta}: ${segmento.valor}", x + 14f, y, paint)
            y += 30f
        }
    }
}
