package com.Jair.practica.view.devolucion

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Path
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.View

/** RF-08: captura de firma dibujada por el usuario en pantalla (sin servicios externos). */
class FirmaCanvasView(
    context: Context,
    attrs: AttributeSet? = null
) : View(context, attrs) {

    private val paint = Paint().apply {
        color = Color.BLACK
        strokeWidth = 8f
        isAntiAlias = true
        style = Paint.Style.STROKE
    }

    private val strokes = mutableListOf<Path>()
    private var currentStroke = Path()
    var cambioEstadoFirma: ((tiene: Boolean) -> Unit)? = null

    private fun notificarFirma() {
        cambioEstadoFirma?.invoke(tieneFirma())
    }

    override fun onTouchEvent(event: MotionEvent): Boolean {
        when (event.actionMasked) {
            MotionEvent.ACTION_DOWN -> {
                currentStroke = Path().apply { moveTo(event.x, event.y) }
                return true
            }
            MotionEvent.ACTION_MOVE -> {
                currentStroke.lineTo(event.x, event.y)
                invalidate()
                return true
            }
            MotionEvent.ACTION_UP -> {
                strokes.add(currentStroke)
                currentStroke = Path()
                invalidate()
                return true
            }
        }
        return super.onTouchEvent(event)
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        strokes.forEach { canvas.drawPath(it, paint) }
        notificarFirma()
    }

    fun limpiar() {
        strokes.clear()
        invalidate()
    }

    fun tieneFirma(): Boolean = strokes.isNotEmpty()

    /** RF-10: renderiza los trazos capturados en pantalla a un Bitmap (sin servicios externos). */
    fun renderFirma(): Bitmap? {
        if (strokes.isEmpty()) return null
        val bitmap = Bitmap.createBitmap(width.coerceAtLeast(1), height.coerceAtLeast(1), Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bitmap)
        val paint = Paint().apply {
            color = Color.BLACK
            strokeWidth = 8f
            isAntiAlias = true
            style = Paint.Style.STROKE
        }
        strokes.forEach { canvas.drawPath(it, paint) }
        return bitmap
    }
}
