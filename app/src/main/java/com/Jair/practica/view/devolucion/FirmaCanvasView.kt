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
    private var dibujando = false
    var cambioEstadoFirma: ((tiene: Boolean) -> Unit)? = null

    private fun notificarFirma() {
        cambioEstadoFirma?.invoke(tieneFirma())
    }

    override fun onTouchEvent(event: MotionEvent): Boolean {
        when (event.actionMasked) {
            MotionEvent.ACTION_DOWN -> {
                parent?.requestDisallowInterceptTouchEvent(true)
                currentStroke = Path().apply { moveTo(event.x, event.y) }
                dibujando = true
                invalidate()
                return true
            }
            MotionEvent.ACTION_MOVE -> {
                if (dibujando) currentStroke.lineTo(event.x, event.y)
                invalidate()
                return true
            }
            MotionEvent.ACTION_UP -> {
                if (dibujando) {
                    currentStroke.lineTo(event.x, event.y)
                    strokes.add(currentStroke)
                }
                currentStroke = Path()
                dibujando = false
                parent?.requestDisallowInterceptTouchEvent(false)
                invalidate()
                notificarFirma()
                performClick()
                return true
            }
            MotionEvent.ACTION_CANCEL -> {
                currentStroke = Path()
                dibujando = false
                parent?.requestDisallowInterceptTouchEvent(false)
                invalidate()
                return true
            }
        }
        return super.onTouchEvent(event)
    }

    override fun performClick(): Boolean {
        super.performClick()
        return true
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        strokes.forEach { canvas.drawPath(it, paint) }
        if (dibujando) canvas.drawPath(currentStroke, paint)
    }

    fun limpiar() {
        strokes.clear()
        currentStroke = Path()
        dibujando = false
        invalidate()
        notificarFirma()
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
