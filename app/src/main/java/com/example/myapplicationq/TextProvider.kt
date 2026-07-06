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
import androidx.core.graphics.createBitmap
import androidx.core.graphics.toColorInt
import android.util.TypedValue
import android.os.Build

/**
 * Utility object that supplies time‑based text (quotes / motivasi).
 * Shared by both the UI (MainActivity) and the WallpaperWorker.
 */
object TextProvider {
    // Repository instance set via init
    private lateinit var settingsRepo: SettingsRepository
    private lateinit var appContext: Context

    /**
     * Initialize the provider with application context. Must be called before any other methods.
     */
    fun init(context: Context) {
        if (!::appContext.isInitialized) {
            appContext = context.applicationContext
        }
        if (!::settingsRepo.isInitialized) {
            settingsRepo = SettingsRepository.getInstance(context.applicationContext)
        }
    }

    // ---- DATA: quotes ---------------------------------------------------------
val DEFAULT_MORNING = arrayOf(
    "[Make your bed] first. It is the first task of the day completed.",
    "Go drink a [glass of water]. Your brain needs hydration before stimulation.",
    "Step outside for [five minutes]. Sunlight is better for you than blue light.",
    "Stop scrolling and go take a [cold shower]. Wake your body up properly.",
    "The morning [sets the tone] for the day. Do not start it by looking at a screen.",
    "A quick [workout] now will give you energy for the rest of the day.",
    "Eat a [proper breakfast]. Information consumption is not a substitute for food.",
    "[Stretch] your back and neck. Sitting all day takes a toll on your posture.",
    "\"Either you [run the day], or the day runs you.\" - Jim Rohn",
    "Get up and open the window. [Fresh air] clears a foggy mind.",
    "Prepare your workspace before you start working. A [clean desk] helps you focus.",
    "Read a page of a [physical book] before opening your apps.",
    "Write down [three things] you want to achieve today on a piece of paper.",
    "\"[Action] is the foundational key to all success.\" - Pablo Picasso",
    "Do the [hardest task first] while your energy is at its peak.",
    "Looking at your phone right after waking up spikes your [anxiety]. Put it down.",
    "Your eyes need to adjust to [natural light] first.",
    "Spend [five minutes in silence] before letting the noise of the world in.",
    "\"The secret of getting ahead is [getting started].\" - Mark Twain",
    "[Plan your day] instead of letting notifications plan it for you.",
    "Walk around the house for a bit. Get your [blood flowing].",
    "[Delay gratification]. Earn your screen time.",
    "Make coffee or tea [mindfully]. Enjoy the process before the rush.",
    "\"Morning is an important time of day, because how you spend your morning can often tell you [what kind of day] you are going to have.\" - Lemony Snicket",
    "Take care of your [physical needs] before your digital habits.",
    "Set an [intention] for the day right now.",
    "Avoid the [news] first thing in the morning. Protect your peace.",
    "Look at the [horizon]. Your eyes are meant to look at distant objects, not just close screens.",
    "\"First say to yourself [what you would be]; and then do what you have to do.\" - Epictetus",
    "The [real world] is happening right now outside this device.",
    "Start with [movement], not consumption."
)
val DEFAULT_DEFAULT = arrayOf(
    "Focus on [one thing] at a time. Multitasking is a myth that destroys productivity.",
    "\"Amateurs sit and wait for inspiration, the rest of us just [get up and go to work].\" - Stephen King",
    "Take a [20-second break] to look at something 20 feet away. Give your eyes a rest.",
    "\"[Focus] is a matter of deciding what things you're not going to do.\" - John Carmack",
    "[Small progress] is still progress. Keep going.",
    "[Done] is better than perfect. Finish the draft first.",
    "\"You don't have to be great to [start], but you have to start to be great.\" - Zig Ziglar",
    "If a task takes less than [two minutes], do it right now.",
    "Remove [distractions]. Put the phone away if you need to do deep work.",
    "\"Success is the sum of [small efforts], repeated day in and day out.\" - Robert Collier",
    "Stop waiting for the [perfect moment]. The time is now.",
    "[Action] creates motivation, not the other way around.",
    "\"The only way to do [great work] is to love what you do.\" - Steve Jobs",
    "Are you being [productive] or just busy? Evaluate your current task.",
    "Break large tasks into [smaller, manageable pieces].",
    "\"It's not always that we need to do more but rather that we need to [focus on less].\" - Nathan W. Morris",
    "Remember [why you started] in the first place.",
    "[Discipline] is choosing between what you want now and what you want most.",
    "\"You will never find time for anything. If you want time you must [make it].\" - Charles Buxton",
    "Stop [overthinking] and just execute the plan.",
    "[Consistency] beats intensity over the long term.",
    "\"Don't watch the clock; do what it does. [Keep going].\" - Sam Levenson",
    "[Prioritize] your tasks. Not everything is equally important.",
    "Step away from the desk for [five minutes] to clear your head.",
    "\"[Productivity] is being able to do things that you were never able to do before.\" - Franz Kafka",
    "Protect your [time]. It is your most valuable asset.",
    "Do not let [minor setbacks] ruin your entire day.",
    "\"Efficiency is doing things right; [effectiveness] is doing the right things.\" - Peter Drucker",
    "Focus on [output], not just hours worked.",
    "[Eliminate] the non-essential.",
    "Stay [hydrated]. A tired brain cannot focus.",
    "\"The way to get started is to [quit talking] and begin doing.\" - Walt Disney"
)

val DEFAULT_NIGHT = arrayOf(
    "[Go to sleep]. Everything will still be here tomorrow.",
    "[Sleep deprivation] destroys your memory consolidation. Rest is essential for learning.",
    "Staring at this screen is actively tricking your brain into thinking it is [daytime].",
    "\"Sleep is the [best meditation].\" - Dalai Lama",
    "Staying up late now means you are [stealing energy] from tomorrow.",
    "Your body [heals and repairs] itself only when you sleep. Do not interrupt the process.",
    "Continuing to work now will only result in [mistakes] you have to fix tomorrow.",
    "\"There is a time for many words, and there is also a [time for sleep].\" - Homer",
    "Chronic lack of sleep compromises your [immune system]. Go rest.",
    "Put the phone [across the room] and close your eyes.",
    "A good tomorrow starts with a [good night's sleep] tonight.",
    "\"A ruffled mind makes a [restless pillow].\" - Charlotte Brontë",
    "[Blue light] suppresses melatonin production. You are making it harder to fall asleep.",
    "Late-night scrolling is [empty stimulation]. It offers no real value.",
    "If you cannot solve the problem now, [sleep on it]. Your brain will work on it in the background.",
    "\"[Fatigue] makes cowards of us all.\" - Vince Lombardi",
    "[Disconnect] to reconnect with yourself tomorrow.",
    "Lack of sleep increases [stress hormones]. Protect your mental health and go to bed.",
    "You have done [enough] for today. Let it go.",
    "\"Man should forget his [anger] before he lies down to sleep.\" - Mahatma Gandhi",
    "It is time to [power down]. The day is over.",
    "Sleep is not a luxury; it is a [biological necessity].",
    "Stop [negotiating] with your sleep schedule. Go to bed.",
    "\"Think in the morning. Act in the noon. Eat in the evening. [Sleep in the night].\" - William Blake",
    "Continuing to stay awake will ruin your [mood and focus] for the entire next day.",
    "Give your [eyes] a rest. They have been working hard all day.",
    "There is nothing on this device that is more important than your [health].",
    "\"Each night, when I go to sleep, I die. And the next morning, when I wake up, I am [reborn].\" - Mahatma Gandhi",
    "Read a [physical book] if you need to wind down, not a screen.",
    "Your [future self] will thank you for going to sleep right now.",
    "[Turn it off]."
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
        val bitmap = createBitmap(screenWidth, screenHeight)
        val canvas = Canvas(bitmap)
        canvas.drawColor(Color.BLACK)               // Latar belakang hitam

        val paint = TextPaint().apply {
            isAntiAlias = true
            color = Color.WHITE
            textSize = (screenHeight * 0.02f)       // Ukuran relatif
            typeface = Typeface.create("serif", Typeface.NORMAL)
        }

        // Resolve dynamic accent color. Since wallpaper is black, we want the system dark primary color.
        val primaryColorVal = if (::appContext.isInitialized) {
            getSystemDarkPrimaryColor(appContext)
        } else {
            Color.parseColor("#D0BCFF")
        }

        // Apply highlights for words inside square brackets [word] and remove brackets
        val spannable = SpannableStringBuilder()
        val regex = Regex("\\[(.*?)\\]")
        var lastIndex = 0
        val matches = regex.findAll(text)
        for (match in matches) {
            val start = match.range.first
            val end = match.range.last + 1
            
            // Append normal preceding text
            spannable.append(text.substring(lastIndex, start))
            
            // Append and style the text inside the brackets
            val innerText = text.substring(start + 1, end - 1)
            val spanStart = spannable.length
            spannable.append(innerText)
            val spanEnd = spannable.length
            
            if (spanStart < spanEnd) {
                // Highlight with Material Theme Dark Primary color resolved dynamically
                spannable.setSpan(
                    ForegroundColorSpan(primaryColorVal),
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

    private fun getSystemDarkPrimaryColor(context: Context): Int {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE) {
            context.getColor(android.R.color.system_primary_dark)
        } else {
            val typedValue = TypedValue()
            if (context.theme.resolveAttribute(android.R.attr.colorPrimary, typedValue, true)) {
                typedValue.data
            } else {
                "#FFD700".toColorInt()
            }
        }
    }
}

