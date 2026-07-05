package com.example.myapplicationq

import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Typeface
import android.text.StaticLayout
import android.text.TextPaint
import android.content.Context
import android.text.SpannableStringBuilder
import android.text.Spanned
import android.text.style.ForegroundColorSpan
import android.text.style.StyleSpan
import com.example.myapplicationq.SettingsRepository
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking

/**
 * Utility object that supplies time‑based text (quotes / motivasi).
 * Shared by both the UI (MainActivity) and the WallpaperWorker.
 */
object TextProvider {
    // Repository instance set via init
    private lateinit var settingsRepo: SettingsRepository

    /**
     * Initialize the provider with application context. Must be called before any other methods.
     */
    fun init(context: Context) {
        if (!::settingsRepo.isInitialized) {
            settingsRepo = SettingsRepository.getInstance(context.applicationContext)
        }
    }

    // ---- DATA: quotes ---------------------------------------------------------
    val DEFAULT_MORNING = arrayOf(
        "✅ Have you made your bed? Starting the day with a neat space boosts your mood.",
        "🚿 A morning shower with fresh water stimulates blood flow and increases concentration.",
        "🥣 A nutritious breakfast (protein + complex carbs) provides stable energy until midday.",
        "📚 Read one page of a motivational book or a short article about your daily goal.",
        "🧘 5 minutes of meditation or deep breathing strengthens mental clarity.",
        "🏃‍♂️ Light movement: stretch or walk for 5‑10 minutes to activate muscles and brain.",
        "🗒️ Write down today's top 3 priorities in a notebook—writing on paper increases commitment.",
        "🔍 Check your to-do list: mark what was done yesterday, reorganize what wasn't.",
        "💧 Drink a glass of water first—brain hydration improves focus."
    )

    val DEFAULT_NIGHT = arrayOf(
        "🌙 Sleeping 6 hours or less = up to 40% reduction in memory and concentration.",
        "🧠 Staying up late increases stress hormones (cortisol), disrupting next day's mood.",
        "⚡ Physical energy drops drastically; reflexes slow down, accident risk increases.",
        "📉 Productivity decreases; decisions become impulsive and work quality drops.",
        "🕰️ Irregular sleep schedules mess up circadian rhythms, making it hard to wake up early.",
        "💤 Poor sleep quality triggers dull skin, puffy eyes, and long-term health issues.",
        "🚫 Concentration drops, making it easy to make mistakes in code or documents.",
        "📈 Risk of metabolic disorders increases; weight can rise due to disrupted leptin hormone.",
        "🔄 Repeating late-night patterns can trigger psychological issues like anxiety and depression."
    )

    val DEFAULT_DEFAULT = arrayOf(
        "Hard work during the day = brilliant results in the afternoon.",
        "Focus on one task, finish it, then move on. Productivity is 'step-by-step'.",
        "Don't let fatigue block your goals. 'Take a breath', continue!",
        "Every minute you spend learning increases your value.",
        "If you want something you've never had, do something you've 'never' done.",
        "Work 'quality' is 'more important' than quantity. Make every line of code count.",
        "A smile at your desk is contagious, 'boost' their energy.",
        "Don't delay—start now, finish half, the rest is 'victory'.",
        "Productivity doesn't mean working constantly, but working 'smart'."
    )

    // -------------------------------------------------------------------------
    // Default time boundaries (used when no user preferences are set)
    const val DEFAULT_MORNING_START = 3
    const val DEFAULT_MORNING_END = 8
    const val DEFAULT_NIGHT_START = 20

    /**
     * Returns a random quote/aktivitas yang sesuai dengan jam `hour`.
     */

    fun getCurrentTimeName(hour: Int): String {
        val morningStart = runBlocking { settingsRepo.getMorningStart().first() } ?: 3
        val morningEnd = runBlocking { settingsRepo.getMorningEnd().first() } ?: 8
        val nightStart = runBlocking { settingsRepo.getNightStart().first() } ?: 20

        // Helper to check if hour is between start and end, even if it crosses midnight
        fun isHourInWindow(h: Int, start: Int, end: Int): Boolean {
            return if (start <= end) {
                h in start..end
            } else {
                // Handles wrap around (e.g., 22:00 to 06:00)
                h >= start || h <= end
            }
        }

        return when {
            // 1. Check Morning First
            isHourInWindow(hour, morningStart, morningEnd) -> "MORNING"

            // 2. Check Night (From nightStart until the morning begins)
            isHourInWindow(hour, nightStart, if (morningStart == 0) 23 else morningStart - 1) -> "NIGHT"

            // 3. Everything else is Daytime
            else -> "DEFAULT"
        }
    }
    fun getTimeBasedText(hour: Int): String {
        // Fetch configurable boundaries, falling back to defaults if not set
        val time = getCurrentTimeName(hour)

        val quotes = when (// Night period: from nightStart to end of day, and early hours until morningStart
            time) {
            "NIGHT" -> getQuoteList(QuoteType.NIGHT)
            // Morning period
            "MORNING" -> getQuoteList(QuoteType.MORNING)
            // Default period (daytime)
            else -> getQuoteList(QuoteType.DEFAULT)
        }
        return quotes.random()
    }

    // -------------------------------------------------------------------------
    // Helper that can also be used by WallpaperWorker to create bitmap
    fun createTextBitmap(
        text: String,
        screenWidth: Int,
        screenHeight: Int
    ): Bitmap {
        val bitmap = Bitmap.createBitmap(screenWidth, screenHeight, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bitmap)
        canvas.drawColor(Color.BLACK)               // Latar belakang hitam

        // Seeded random based on the text hash code for stable visual styles per text
        val seed = text.hashCode().toLong()
        val random = java.util.Random(seed)

        val fontFamilies = arrayOf("serif", "sans-serif", "monospace")
        val randomFont = fontFamilies[random.nextInt(fontFamilies.size)]

        val isBold = random.nextBoolean()
        val isItalic = random.nextBoolean()
        val isUnderline = random.nextBoolean()

        val style = when {
            isBold && isItalic -> Typeface.BOLD_ITALIC
            isBold -> Typeface.BOLD
            isItalic -> Typeface.ITALIC
            else -> Typeface.NORMAL
        }

        // TextPaint – gunakan font random dan gaya random
        val paint = TextPaint().apply {
            isAntiAlias = true
            color = Color.WHITE
            textSize = (screenHeight * 0.02f)       // Ukuran relatif
            typeface = Typeface.create(randomFont, style)
            isUnderlineText = isUnderline
        }

        // Apply highlights for words inside single quotes 'word' and remove quotes
        val spannable = SpannableStringBuilder()
        val regex = Regex("'(.*?)'")
        var lastIndex = 0
        val matches = regex.findAll(text)
        for (match in matches) {
            val start = match.range.first
            val end = match.range.last + 1
            
            // Append normal preceding text
            spannable.append(text.substring(lastIndex, start))
            
            // Append and style the text inside the quotes
            val innerText = text.substring(start + 1, end - 1)
            val spanStart = spannable.length
            spannable.append(innerText)
            val spanEnd = spannable.length
            
            if (spanStart < spanEnd) {
                // Highlight with a premium Gold color
                spannable.setSpan(
                    ForegroundColorSpan(Color.parseColor("#FFD700")),
                    spanStart,
                    spanEnd,
                    Spanned.SPAN_EXCLUSIVE_EXCLUSIVE
                )
                // Always make the highlighted word Bold
                spannable.setSpan(
                    StyleSpan(Typeface.BOLD),
                    spanStart,
                    spanEnd,
                    Spanned.SPAN_EXCLUSIVE_EXCLUSIVE
                )
            }
            lastIndex = end
        }
        if (lastIndex < text.length) {
            spannable.append(text.substring(lastIndex))
        }

        // StaticLayout untuk men‑wrap teks secara otomatis
        val layout = StaticLayout.Builder.obtain(
            spannable, 0, spannable.length, paint, (screenWidth * 0.7).toInt()
        )
            .setAlignment(android.text.Layout.Alignment.ALIGN_CENTER)
            .setLineSpacing(1.0f, 1.2f)
            .setIncludePad(false)
            .build()

        // Posisi tengah layar
        val x = (screenWidth - layout.width) / 2f
        val y = (screenHeight - layout.height) / 2f
        canvas.save()
        canvas.translate(x, y)
        layout.draw(canvas)
        canvas.restore()

        return bitmap
    }

    // -------------------------------------------------------------------------
    // Internal helpers
    private enum class QuoteType { MORNING, NIGHT, DEFAULT }

    private fun getQuoteList(type: QuoteType): Array<String> {
        val list = when (type) {
            QuoteType.MORNING -> runBlocking { settingsRepo.getMorningQuotes().first() }
            QuoteType.NIGHT -> runBlocking { settingsRepo.getNightQuotes().first() }
            QuoteType.DEFAULT -> runBlocking { settingsRepo.getDefaultQuotes().first() }
        }
        return if (list == null || list.isEmpty()) {
            when (type) {
                QuoteType.MORNING -> DEFAULT_MORNING
                QuoteType.NIGHT -> DEFAULT_NIGHT
                QuoteType.DEFAULT -> DEFAULT_DEFAULT
            }
        } else {
            list
        }
    }
}

