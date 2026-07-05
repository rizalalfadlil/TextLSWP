import android.Manifest
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.text.Layout
import android.text.StaticLayout
import android.text.TextPaint
import android.app.WallpaperManager
import android.content.Context
import androidx.annotation.RequiresPermission

fun createTextBitmap(text: String, screenWidth: Int, screenHeight: Int): Bitmap {
    // 1. Buat kanvas/gambar kosong seukuran layar
    val bitmap = Bitmap.createBitmap(screenWidth, screenHeight, Bitmap.Config.ARGB_8888)
    val canvas = Canvas(bitmap)

    // 2. Warnai latar belakang (misal: warna gelap/hitam)
    canvas.drawColor(Color.parseColor("#121212"))

    // 3. Konfigurasi gaya teks (warna, ukuran, font)
    val textPaint = TextPaint().apply {
        isAntiAlias = true
        color = Color.WHITE
        textSize = 80f // Sesuaikan ukuran
        // typeface = Typeface.create(...) // Jika ingin custom font
    }

    // 4. Buat StaticLayout agar teks bisa otomatis turun baris (word-wrap)
    val textWidth = screenWidth - 100 // Beri margin kiri-kanan 50px
    val staticLayout = StaticLayout.Builder.obtain(text, 0, text.length, textPaint, textWidth)
        .setAlignment(Layout.Alignment.ALIGN_CENTER)
        .build()

    // 5. Hitung posisi tengah layar dan gambar teksnya
    canvas.save()
    val textHeight = staticLayout.height
    val x = (screenWidth - textWidth) / 2f
    val y = (screenHeight - textHeight) / 2f
    canvas.translate(x, y)

    staticLayout.draw(canvas)
    canvas.restore()

    return bitmap
}

@RequiresPermission(Manifest.permission.SET_WALLPAPER)
fun setLockScreenWallpaper(context: Context, bitmap: Bitmap) {
    val wallpaperManager = WallpaperManager.getInstance(context)
    try {
        // FLAG_LOCK memastikan gambar hanya diaplikasikan ke layar kunci
        wallpaperManager.setBitmap(bitmap, null, false, WallpaperManager.FLAG_LOCK)
    } catch (e: Exception) {
        e.printStackTrace()
    }
}
