package uz.miyov.pet

import android.app.*
import android.content.*
import android.graphics.*
import android.media.MediaPlayer
import android.os.*
import android.view.*
import kotlin.math.*
import kotlin.random.Random

class PetService : Service() {
 private lateinit var wm: WindowManager
 private lateinit var cat: CatView
 private lateinit var params: WindowManager.LayoutParams
 private val handler = Handler(Looper.getMainLooper())
 private var tick = 0
 private var direction = 1
 private var sound: MediaPlayer? = null
 private val animator = object : Runnable {
  override fun run() {
   if (!::cat.isInitialized) return
   tick++
   if (tick % 180 == 0) direction = if (Random.nextBoolean()) 1 else -1
   if (!cat.dragging && tick % 3 == 0 && cat.mode == "yurish") {
    val limit = resources.displayMetrics.widthPixels / 2 - 110
    params.x = (params.x + direction * 4).coerceIn(-limit, limit)
    try { wm.updateViewLayout(cat, params) } catch (_: Exception) {}
   }
   cat.advance(tick)
   handler.postDelayed(this, 90)
  }
 }
 override fun onCreate() {
  super.onCreate()
  val nm = getSystemService(NOTIFICATION_SERVICE) as NotificationManager
  nm.createNotificationChannel(NotificationChannel("miyov", "Miyov", NotificationManager.IMPORTANCE_LOW))
  startForeground(7, Notification.Builder(this, "miyov").setSmallIcon(android.R.drawable.ic_menu_view).setContentTitle("Miyov").setContentText("Miyov ishlayapti").build())
  wm = getSystemService(WINDOW_SERVICE) as WindowManager
  params = WindowManager.LayoutParams(300, 260, WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY, WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE or WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS, PixelFormat.TRANSLUCENT).apply {
   gravity = Gravity.TOP or Gravity.CENTER_HORIZONTAL
   y = 280
  }
  cat = CatView()
  try { wm.addView(cat, params); handler.post(animator) } catch (_: Exception) { stopSelf() }
 }
 override fun onStartCommand(intent: Intent?, flags: Int, startId: Int) = START_STICKY
 override fun onBind(intent: Intent?): IBinder? = null
 override fun onDestroy() {
  handler.removeCallbacks(animator)
  sound?.release()
  if (::cat.isInitialized) try { wm.removeView(cat) } catch (_: Exception) {}
  super.onDestroy()
 }
 inner class CatView : View(this@PetService) {
  var dragging = false
  var mode = "yurish"
  private var modeUntil = 0L
  private var taps = 0
  private var lastTap = 0L
  private var startX = 0f
  private var startY = 0f
  private var lastX = 0f
  private var lastY = 0f
  private var downAt = 0L
  private var frame = 0
  private val paint = Paint(Paint.ANTI_ALIAS_FLAG or Paint.FILTER_BITMAP_FLAG)
  private val images = mutableMapOf<String, List<Bitmap>>()
  init {
   for (name in listOf("yurish", "yugurish", "sakrash", "uxlash", "erkalash", "jahl", "qiziqish")) {
    val frames = mutableListOf<Bitmap>()
    for (i in 1..8) try {
     assets.open("miyov/${name}_%02d.png".format(i)).use { stream ->
      BitmapFactory.decodeStream(stream)?.let { frames.add(it) }
     }
    } catch (_: Exception) {}
    if (frames.isNotEmpty()) images[name] = frames
   }
  }
  fun advance(t: Int) {
   if (System.currentTimeMillis() > modeUntil) {
    mode = when {
     t % 420 in 330..410 -> "uxlash"
     t % 280 in 245..270 -> "qiziqish"
     else -> "yurish"
    }
   }
   frame++
   invalidate()
  }
  override fun onDraw(canvas: Canvas) {
   super.onDraw(canvas)
   val frames = images[mode] ?: images["yurish"] ?: return
   val bitmap = frames[(frame / 2) % frames.size]
   val scale = min(width.toFloat() / bitmap.width, height.toFloat() / bitmap.height) * 0.93f
   val w = bitmap.width * scale
   val h = bitmap.height * scale
   canvas.drawBitmap(bitmap, null, RectF((width-w)/2, (height-h)/2, (width+w)/2, (height+h)/2), paint)
  }
  override fun onTouchEvent(e: MotionEvent): Boolean {
   when (e.actionMasked) {
    MotionEvent.ACTION_DOWN -> {
     startX = e.rawX; startY = e.rawY; lastX = e.rawX; lastY = e.rawY
     downAt = System.currentTimeMillis(); dragging = false; return true
    }
    MotionEvent.ACTION_MOVE -> {
     if (hypot(e.rawX-startX, e.rawY-startY) > 12f) dragging = true
     if (dragging) {
      params.x += (e.rawX-lastX).toInt(); params.y += (e.rawY-lastY).toInt()
      try { wm.updateViewLayout(this, params) } catch (_: Exception) {}
     }
     lastX = e.rawX; lastY = e.rawY; return true
    }
    MotionEvent.ACTION_UP -> {
     val now = System.currentTimeMillis()
     if (!dragging) {
      if (now-downAt > 850) stopSelf()
      else {
       taps = if (now-lastTap < 650) taps+1 else 1; lastTap = now
       mode = when { taps >= 4 -> "jahl"; taps == 3 -> "sakrash"; taps == 2 -> "erkalash"; else -> "qiziqish" }
       modeUntil = now + 1800
       if (taps >= 4) taps = 0
       try { sound?.release(); sound = MediaPlayer.create(this@PetService, R.raw.meow); sound?.start() } catch (_: Exception) {}
      }
     }
     dragging = false; invalidate(); return true
    }
   }
   return true
  }
 }
}