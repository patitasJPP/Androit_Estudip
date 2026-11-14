package com.Jair.practica.view.common

import android.content.Context
import android.graphics.Color
import android.graphics.Typeface
import android.graphics.drawable.GradientDrawable
import android.view.Gravity
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.ImageView
import android.widget.ScrollView
import android.widget.TextView

/** Componentes visuales compartidos para mantener una experiencia consistente. */
object AppUi {
    const val RED = 0xFFB5122B.toInt()
    const val INK = 0xFF1F2937.toInt()
    const val MUTED = 0xFF64748B.toInt()
    const val BG = 0xFFF7F8FC.toInt()
    const val LINE = 0xFFE2E8F0.toInt()
    const val GREEN = 0xFF18794E.toInt()
    const val AMBER = 0xFF9A6700.toInt()

    fun dp(context: Context, value: Int): Int = (value * context.resources.displayMetrics.density).toInt()

    fun rounded(color: Int, radius: Int = 14, stroke: Int? = null): GradientDrawable =
        GradientDrawable().apply {
            setColor(color); cornerRadius = radius.toFloat(); stroke?.let { setStroke(1, it) }
        }

    fun screen(context: Context): Pair<ScrollView, LinearLayout> {
        val scroll = ScrollView(context).apply { setBackgroundColor(BG); isFillViewport = true }
        val content = LinearLayout(context).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(dp(context, 16), dp(context, 14), dp(context, 16), dp(context, 28))
        }
        scroll.addView(content, ViewGroup.LayoutParams(-1, -2)); return scroll to content
    }

    fun header(context: Context, title: String, module: String, onBack: (() -> Unit)? = null): View {
        val row = LinearLayout(context).apply { gravity = Gravity.CENTER_VERTICAL; setPadding(0, 0, 0, dp(context, 12)) }
        val back = TextView(context).apply {
            text = "‹"; textSize = 30f; setTextColor(INK); gravity = Gravity.CENTER
            visibility = if (onBack == null) View.GONE else View.VISIBLE; setOnClickListener { onBack?.invoke() }
        }
        row.addView(back, LinearLayout.LayoutParams(dp(context, 36), dp(context, 36)))
        val labels = LinearLayout(context).apply { orientation = LinearLayout.VERTICAL }
        labels.addView(TextView(context).apply { text = title; textSize = 22f; typeface = Typeface.DEFAULT_BOLD; setTextColor(INK) })
        labels.addView(TextView(context).apply { text = module.uppercase(); textSize = 10f; letterSpacing = .12f; setTextColor(MUTED) })
        row.addView(labels, LinearLayout.LayoutParams(0, -2, 1f))
        row.addView(ImageView(context).apply { setImageResource(com.Jair.practica.R.drawable.ic_objetos_perdidos); setColorFilter(RED); scaleType = ImageView.ScaleType.CENTER_INSIDE }, LinearLayout.LayoutParams(dp(context, 30), dp(context, 30)))
        return row
    }

    fun sectionTitle(context: Context, title: String, required: Boolean = false): TextView = TextView(context).apply {
        text = if (required) "$title *" else title; textSize = 12f; typeface = Typeface.DEFAULT_BOLD; setTextColor(INK)
        setPadding(0, dp(context, 2), 0, dp(context, 7))
    }

    fun card(context: Context, top: Int = 12): LinearLayout = LinearLayout(context).apply {
        orientation = LinearLayout.VERTICAL; setPadding(dp(context, 15), dp(context, 14), dp(context, 15), dp(context, 14))
        background = rounded(Color.WHITE, 16, LINE); elevation = dp(context, 1).toFloat()
        layoutParams = LinearLayout.LayoutParams(-1, -2).apply { topMargin = dp(context, top) }
    }

    fun field(context: Context, hint: String, value: String = ""): EditText = EditText(context).apply {
        this.hint = hint; setText(value); textSize = 14f; setTextColor(INK); setHintTextColor(0xFF94A3B8.toInt())
        setPadding(dp(context, 12), 0, dp(context, 12), 0); minHeight = dp(context, 48); background = rounded(0xFFF8FAFC.toInt(), 10, LINE)
    }

    fun primary(context: Context, text: String): Button = Button(context).apply {
        this.text = text; textSize = 12f; typeface = Typeface.DEFAULT_BOLD; setTextColor(Color.WHITE)
        background = rounded(RED, 12); minHeight = dp(context, 48); isAllCaps = false
    }

    fun secondary(context: Context, text: String): Button = Button(context).apply {
        this.text = text; textSize = 12f; typeface = Typeface.DEFAULT_BOLD; setTextColor(INK)
        background = rounded(0xFFE8ECF3.toInt(), 12); minHeight = dp(context, 46); isAllCaps = false
    }

    fun status(context: Context, text: String, color: Int): TextView = TextView(context).apply {
        this.text = text; textSize = 11f; typeface = Typeface.DEFAULT_BOLD; setTextColor(color)
        val tint = when (color) { GREEN -> 0xFFE8F6EE.toInt(); AMBER -> 0xFFFFF3D6.toInt(); RED -> 0xFFFFE8ED.toInt(); else -> 0xFFE8ECF3.toInt() }
        setPadding(dp(context, 10), dp(context, 6), dp(context, 10), dp(context, 6)); background = rounded(tint, 30)
    }

    fun bottomNav(context: Context, onObjects: () -> Unit, onResolved: () -> Unit, onReports: () -> Unit, onProfile: () -> Unit): LinearLayout =
        LinearLayout(context).apply {
            orientation = LinearLayout.HORIZONTAL; gravity = Gravity.CENTER; setPadding(0, dp(context, 8), 0, dp(context, 6))
            setBackgroundColor(Color.WHITE)
            listOf(Triple(com.Jair.practica.R.drawable.ic_nav_objetos, "Objetos", onObjects), Triple(com.Jair.practica.R.drawable.ic_check_circle, "Resuelto", onResolved), Triple(com.Jair.practica.R.drawable.ic_nav_reportes, "Reportes", onReports), Triple(com.Jair.practica.R.drawable.ic_nav_perfil, "Mi Perfil", onProfile)).forEach { (icon, label, action) ->
                addView(LinearLayout(context).apply {
                    orientation = LinearLayout.VERTICAL; gravity = Gravity.CENTER
                    addView(ImageView(context).apply { setImageResource(icon); setColorFilter(INK); scaleType = ImageView.ScaleType.CENTER_INSIDE }, LinearLayout.LayoutParams(dp(context, 25), dp(context, 25)))
                    addView(TextView(context).apply { text = label; textSize = 11f; typeface = Typeface.DEFAULT_BOLD; gravity = Gravity.CENTER; setTextColor(INK); setPadding(0, dp(context, 2), 0, 0) })
                    setOnClickListener { action() }
                }, LinearLayout.LayoutParams(0, dp(context, 58), 1f))
            }
        }
}
