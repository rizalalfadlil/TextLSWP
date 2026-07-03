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
        "✅ Sudah merapikan tempat tidur? Memulai hari dengan ruang rapi meningkatkan mood.",
        "🚿 Mandi pagi dengan air segar menstimulasi aliran darah dan meningkatkan konsentrasi.",
        "🥣 Sarapan bergizi (protein + karbohidrat kompleks) memberi energi stabil sampai siang.",
        "📚 Bacalah satu halaman buku motivasi atau artikel singkat tentang goal harian Anda.",
        "🧘 5 menit meditasi atau pernapasan dalam memperkuat kejernihan pikiran.",
        "🏃‍♂️ Gerakan ringan: stretching atau jalan kaki 5‑10 menit mengaktifkan otot dan otak.",
        "🗒️ Tulis 3 prioritas utama hari ini di notebook—tulis di atas kertas meningkatkan komitmen.",
        "🔍 Periksa to‑do list: tandai apa yang sudah selesai kemarin, atur ulang yang belum.",
        "💧 Minum segelas air putih dulu—hidrasi otak meningkatkan fokus."
    )

    val DEFAULT_NIGHT = arrayOf(
        "🌙 Kurang tidur 6 jam atau kurang = penurunan memori dan konsentrasi hingga 40 %.",
        "🧠 Begadang meningkatkan hormon stres (cortisol) yang mengganggu mood keesokan harinya.",
        "⚡ Energi fisik menurun drastis; refleks melambat, risiko kecelakaan meningkat.",
        "📉 Produktivasi menurun; keputusan menjadi impulsif dan kualitas kerja menurun.",
        "🕰️ Waktu tidur yang tidak teratur mengacaukan jam sirkadian, susah bangun pagi.",
        "💤 Kualitas tidur buruk memicu kulit kusam, mata bengkak, dan gangguan kesehatan jangka panjang.",
        "🚫 Konsentrasi menurun, sehingga mudah membuat kesalahan dalam kode atau dokumen.",
        "📈 Risiko gangguan metabolisme meningkat; berat badan dapat naik karena hormon leptin terganggu.",
        "🔄 Pola begadang berulang dapat memicu gangguan psikologis seperti kecemasan dan depresi."
    )

    val DEFAULT_DEFAULT = arrayOf(
        "Kerja keras di siang hari = hasil gemilang di sore hari.",
        "Fokus pada satu tugas, selesaikan, lalu beralih. Produktif itu 'bertahap'.",
        "Jangan biarkan rasa lelah menghalangi tujuan. 'Tarik napas', lanjutkan!",
        "Setiap menit yang Anda gunakan untuk belajar, menambah nilai diri.",
        "Jika Anda ingin sesuatu yang belum pernah Anda miliki, lakukan sesuatu yang 'belum pernah' Anda lakukan.",
        "'Kualitas' kerja 'lebih penting' daripada kuantitas. Buat setiap baris kode berarti.",
        "Senyum di meja kerja menular pada tim, 'tingkatkan energi' mereka.",
        "Jangan menunda—mulai sekarang, selesaikan setengahnya, sisanya jadi 'kemenangan'.",
        "Produktivitas bukan berarti bekerja terus, melainkan bekerja 'pintar'."
    )

    // -------------------------------------------------------------------------
    // Default time boundaries (used when no user preferences are set)
    private const val DEFAULT_MORNING_START = 6
    private const val DEFAULT_MORNING_END = 9
    private const val DEFAULT_NIGHT_START = 21

    /**
     * Returns a random quote/aktivitas yang sesuai dengan jam `hour`.
     */
    fun getTimeBasedText(hour: Int): String {
        // Fetch configurable boundaries, falling back to defaults if not set
        val morningStart = runBlocking { settingsRepo.getMorningStart().first() } ?: 6
        val morningEnd = runBlocking { settingsRepo.getMorningEnd().first() } ?: 9
        val nightStart = runBlocking { settingsRepo.getNightStart().first() } ?: 21

        val quotes = when {
            // Night period: from nightStart to end of day, and early hours until morningStart
            hour >= nightStart || hour < morningStart -> getQuoteList(QuoteType.NIGHT)
            // Morning period
            hour in morningStart..morningEnd -> getQuoteList(QuoteType.MORNING)
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

