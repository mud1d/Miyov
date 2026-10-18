package uz.miyov.pet

import android.app.*
import android.content.*
import android.graphics.*
import android.os.*
import android.provider.Settings
import android.util.Log
import android.view.*
import android.widget.Toast
import kotlin.math.*

class PetService : Service() {
    private lateinit var wm: WindowManager
    private lateinit var cat: CatView
    private lateinit var params: WindowManager.LayoutParams
    private lateinit var sound: PetSound
    private val handler = Handler(Looper.getMainLooper())
    private var attached = false
    private var tick = 0
    private var direction = 1
    private val dp get() = resources.displayMetrics.density

    private val animator = object : Runnable {
        override fun run() {
            if (!attached) return
            tick++
            cat.advance(tick)
            if (!cat.touchHeld && cat.mode in listOf("yurish", "yugurish")) {
                val speed = if (cat.mode == "yugurish") 2.6f else 1.3f
                val step = max(1, (speed * dp).roundToInt())
                val maxX = max(0, resources.displayMetrics.widthPixels - params.width)
                val next = params.x + direction * step
                if (next <= 0 || next >= maxX) direction *= -1
                params.x = next.coerceIn(0, maxX)
                updatePosition()
            }
            handler.postDelayed(this, 100L)
        }
    }

    override fun onCreate() {
        super.onCreate()
        val nm = getSystemService(NOTIFICATION_SERVICE) as NotificationManager
        nm.createNotificationChannel(NotificationChannel("miyov", "Miyov", NotificationManager.IMPORTANCE_LOW))
        val open = PendingIntent.getActivity(this, 0, Intent(this, MainActivity::class.java), PendingIntent.FLAG_IMMUTABLE)
        startForeground(7, Notification.Builder(this, "miyov")
            .setSmallIcon(android.R.drawable.ic_menu_view)
            .setContentTitle("Miyov")
            .setContentText("Mushukchani bosib ovozini eshiting")
            .setContentIntent(open).build())
        if (!Settings.canDrawOverlays(this)) {
            stopSelf()
            return
        }
        sound = PetSound(this)
        wm = getSystemService(WINDOW_SERVICE) as WindowManager
        params = WindowManager.LayoutParams(
            (88 * dp).roundToInt(), (80 * dp).roundToInt(),
            WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY,
            WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE,
            PixelFormat.TRANSLUCENT
        ).apply {
            gravity = Gravity.TOP or Gravity.LEFT
            x = (16 * dp).roundToInt()
            y = (resources.displayMetrics.heightPixels * 0.6f).roundToInt()
        }
        cat = CatView()
        try {
            wm.addView(cat, params)
            attached = true
            updatePosition()
            handler.post(animator)
        } catch (error: Exception) {
            Log.e("Miyov", "Cannot show overlay", error)
            Toast.makeText(this, "Mushukchani chiqarib bo‘lmadi. Ruxsatni tekshiring.", Toast.LENGTH_LONG).show()
            stopSelf()
        }
    }

    private fun updatePosition() {
        params.x = params.x.coerceIn(0, max(0, resources.displayMetrics.widthPixels - params.width))
        params.y = params.y.coerceIn(0, max(0, resources.displayMetrics.heightPixels - params.height - (48 * dp).toInt()))
        if (attached) wm.updateViewLayout(cat, params)
    }

    override fun onConfigurationChanged(newConfig: android.content.res.Configuration) {
        super.onConfigurationChanged(newConfig)
        if (attached) updatePosition()
    }
    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int) = START_NOT_STICKY
    override fun onBind(intent: Intent?): IBinder? = null
    override fun onDestroy() {
        handler.removeCallbacksAndMessages(null)
        if (::sound.isInitialized) sound.release()
        if (attached) {
            wm.removeView(cat)
            attached = false
        }
        super.onDestroy()
    }

    inner class CatView : View(this@PetService) {
        var touchHeld = false
        private var dragging = false
        var mode = "yurish"
        private var modeUntil = 0L
        private var frame = 0
        private var taps = 0
        private var lastTap = 0L
        private var startX = 0f
        private var startY = 0f
        private var originX = 0
        private var originY = 0
        private var longPressed = false
        private val slop = ViewConfiguration.get(this@PetService).scaledTouchSlop
        private val closePet = Runnable {
            if (touchHeld && !dragging) {
                longPressed = true
                stopSelf()
            }
        }
        private val paint = Paint(Paint.ANTI_ALIAS_FLAG or Paint.FILTER_BITMAP_FLAG)
        private val images = mutableMapOf<String, List<Bitmap>>()

        init {
            contentDescription = "Miyov mushukcha"
            for (name in listOf("yurish", "yugurish", "sakrash", "uxlash", "erkalash", "jahl", "qiziqish")) {
                images[name] = (1..8).map { i ->
                    assets.open("miyov/${name}_%02d.png".format(java.util.Locale.US, i)).use {
                        requireNotNull(BitmapFactory.decodeStream(it))
                    }
                }
            }
        }

        fun advance(t: Int) {
            if (touchHeld) return
            if (SystemClock.uptimeMillis() > modeUntil) {
                val next = when {
                    t % 420 in 330..410 -> "uxlash"
                    t % 420 in 90..120 -> "yugurish"
                    t % 280 in 245..270 -> "qiziqish"
                    else -> "yurish"
                }
                if (next != mode) frame = 0
                mode = next
            }
            frame++
            invalidate()
        }

        override fun onDraw(canvas: Canvas) {
            super.onDraw(canvas)
            val frames = images[mode] ?: return
            val index = when (mode) {
                "sakrash" -> frame.coerceAtMost(frames.lastIndex)
                "uxlash" -> (frame / 3) % frames.size
                else -> frame % frames.size
            }
            val bitmap = frames[index]
            // Fixed scale and common baseline keep different frames from wobbling in size.
            val scale = min(width / 256f, height / 256f)
            val w = bitmap.width * scale
            val h = bitmap.height * scale
            val save = canvas.save()
            if (direction < 0) canvas.scale(-1f, 1f, width / 2f, height / 2f)
            canvas.drawBitmap(bitmap, null, RectF((width-w)/2f, height-h, (width+w)/2f, height.toFloat()), paint)
            canvas.restoreToCount(save)
        }

        override fun performClick(): Boolean {
            super.performClick()
            val now = SystemClock.uptimeMillis()
            taps = if (now - lastTap < 650) taps + 1 else 1
            lastTap = now
            mode = when {
                taps >= 4 -> "jahl"
                taps == 3 -> "sakrash"
                taps == 2 -> "erkalash"
                else -> "qiziqish"
            }
            if (taps >= 4) taps = 0
            modeUntil = now + 1800
            frame = 0
            sound.play()
            invalidate()
            return true
        }

        override fun onTouchEvent(e: MotionEvent): Boolean {
            when (e.actionMasked) {
                MotionEvent.ACTION_DOWN -> {
                    touchHeld = true
                    dragging = false
                    longPressed = false
                    startX = e.rawX; startY = e.rawY
                    originX = params.x; originY = params.y
                    handler.postDelayed(closePet, 850L)
                }
                MotionEvent.ACTION_MOVE -> {
                    if (hypot(e.rawX-startX, e.rawY-startY) > slop) {
                        dragging = true
                        handler.removeCallbacks(closePet)
                    }
                    if (dragging) {
                        params.x = originX + (e.rawX-startX).roundToInt()
                        params.y = originY + (e.rawY-startY).roundToInt()
                        updatePosition()
                    }
                }
                MotionEvent.ACTION_UP -> {
                    handler.removeCallbacks(closePet)
                    touchHeld = false
                    if (!dragging && !longPressed) performClick()
                    dragging = false
                }
                MotionEvent.ACTION_CANCEL -> {
                    handler.removeCallbacks(closePet)
                    touchHeld = false
                    dragging = false
                }
            }
            return true
        }
    }
}
