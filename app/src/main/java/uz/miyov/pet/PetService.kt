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
    private val motion = PetMotion()
    private val mood = PetMood()
    private val clock = Choreographer.getInstance()
    private var attached = false
    private var lastFrame = 0L
    private var started = 0L
    private val dp get() = resources.displayMetrics.density

    private val ticker = object : Choreographer.FrameCallback {
        override fun doFrame(frameTimeNanos: Long) {
            if (!attached) return
            val now = SystemClock.uptimeMillis()
            val dt = if (lastFrame == 0L) 0f else ((frameTimeNanos-lastFrame)/1_000_000_000.0).toFloat()
            lastFrame = frameTimeNanos
            cat.advance(now)
            val speed = when (cat.mode) {
                "yurish" -> 30f * dp
                "yugurish" -> 48f * dp
                else -> 0f
            }
            val oldX = params.x
            motion.step(dt, speed, max(0, resources.displayMetrics.widthPixels-params.width).toFloat(), cat.touchHeld)
            params.x = motion.x.roundToInt()
            if (oldX != params.x) updatePosition()
            cat.invalidate()
            clock.postFrameCallback(this)
        }
    }

    override fun onCreate() {
        super.onCreate()
        val nm = getSystemService(NOTIFICATION_SERVICE) as NotificationManager
        nm.createNotificationChannel(NotificationChannel("miyov", "Miyov", NotificationManager.IMPORTANCE_LOW))
        val open = PendingIntent.getActivity(this, 0, Intent(this, MainActivity::class.java), PendingIntent.FLAG_IMMUTABLE)
        startForeground(7, Notification.Builder(this, "miyov")
            .setSmallIcon(android.R.drawable.ic_menu_view).setContentTitle("Miyov")
            .setContentText("2 marta bos: yurakchalar · ko‘p bos: jahli chiqadi")
            .setContentIntent(open).build())
        if (!Settings.canDrawOverlays(this)) { stopSelf(); return }
        sound = PetSound(this)
        wm = getSystemService(WINDOW_SERVICE) as WindowManager
        params = WindowManager.LayoutParams(
            (88*dp).roundToInt(), (104*dp).roundToInt(),
            WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY,
            WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE, PixelFormat.TRANSLUCENT
        ).apply {
            gravity = Gravity.TOP or Gravity.LEFT
            x = (16*dp).roundToInt()
            y = (resources.displayMetrics.heightPixels*0.6f).roundToInt()
        }
        motion.x = params.x.toFloat()
        started = SystemClock.uptimeMillis()
        cat = CatView()
        try {
            wm.addView(cat, params)
            attached = true
            updatePosition()
            clock.postFrameCallback(ticker)
        } catch (error: Exception) {
            Log.e("Miyov", "Cannot show overlay", error)
            Toast.makeText(this,"Mushukchani chiqarib bo‘lmadi. Ruxsatni tekshiring.",Toast.LENGTH_LONG).show()
            stopSelf()
        }
    }

    private fun updatePosition() {
        params.x = params.x.coerceIn(0,max(0,resources.displayMetrics.widthPixels-params.width))
        params.y = params.y.coerceIn(0,max(0,resources.displayMetrics.heightPixels-params.height-(48*dp).toInt()))
        if (attached) wm.updateViewLayout(cat, params)
    }
    override fun onConfigurationChanged(config: android.content.res.Configuration) {
        super.onConfigurationChanged(config)
        if (attached) { updatePosition(); motion.x = params.x.toFloat() }
    }
    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int) = START_NOT_STICKY
    override fun onBind(intent: Intent?): IBinder? = null
    override fun onDestroy() {
        clock.removeFrameCallback(ticker)
        handler.removeCallbacksAndMessages(null)
        if (::sound.isInitialized) sound.release()
        if (attached) { wm.removeView(cat); attached = false }
        super.onDestroy()
    }

    inner class CatView : View(this@PetService) {
        var touchHeld = false
        private var dragging = false
        var mode = "yurish"
            private set
        private var modeStarted = 0L
        private var reactionUntil = 0L
        private var reaction = "qiziqish"
        private var tapsStartX = 0f
        private var tapsStartY = 0f
        private var originX = 0
        private var originY = 0
        private var longPressed = false
        private var heldSince = 0L
        private val slop = ViewConfiguration.get(this@PetService).scaledTouchSlop
        private val closePet = Runnable {
            if (touchHeld && !dragging) { longPressed = true; stopSelf() }
        }
        private val bitmapPaint = Paint(Paint.ANTI_ALIAS_FLAG or Paint.FILTER_BITMAP_FLAG)
        private val fx = Paint(Paint.ANTI_ALIAS_FLAG)
        private val heart = Path().apply {
            moveTo(0f,5f)
            cubicTo(-14f,-4f,-10f,-15f,0f,-7f)
            cubicTo(10f,-15f,14f,-4f,0f,5f)
            close()
        }
        private val images = mutableMapOf<String,List<Bitmap>>()
        init {
            contentDescription = "Miyov mushukcha"
            val atlas = assets.open("natural/walk32_atlas.png").use {
                requireNotNull(BitmapFactory.decodeStream(it))
            }
            require(atlas.width == 2048 && atlas.height == 1024)
            val walkFrames = (0 until 32).map { i ->
                Bitmap.createBitmap(atlas, (i % 8) * 256, (i / 8) * 256, 256, 256)
            }
            atlas.recycle()
            images["yurish"] = walkFrames
            images["yugurish"] = walkFrames
            for (name in listOf("sakrash","uxlash","erkalash","jahl","qiziqish")) {
                val count = 8
                images[name] = (1..count).map { i ->
                    assets.open("miyov/${name}_%02d.png".format(java.util.Locale.US,i)).use {
                        requireNotNull(BitmapFactory.decodeStream(it))
                    }
                }
            }
        }
        private fun setMode(next: String, now: Long) {
            if (next != mode) { mode = next; modeStarted = now }
        }
        fun advance(now: Long) {
            if (touchHeld) return
            val elapsed = (now-started)%120000L
            val next = when {
                now < mood.angryUntil -> "jahl"
                now < mood.loveUntil -> "erkalash"
                now < reactionUntil -> reaction
                elapsed in 114000L..119999L -> "uxlash"
                elapsed in 50000L..52999L -> "yugurish"
                else -> "yurish"
            }
            setMode(next,now)
        }
        override fun onDraw(canvas: Canvas) {
            super.onDraw(canvas)
            val now = SystemClock.uptimeMillis()
            val frames = images[mode] ?: return
            val age = (if (touchHeld) heldSince else now)-modeStarted
            // Gait follows actual distance, so feet cannot keep stepping while the pet stops.
            val frame = when (mode) {
                "yurish" -> ((motion.distance/(28f*dp))*frames.size).toInt()%frames.size
                "yugurish" -> ((motion.distance/(28f*dp))*frames.size).toInt()%frames.size
                "uxlash" -> ((age.coerceAtLeast(0)/350L)%frames.size).toInt()
                else -> ((age.coerceAtLeast(0)/135L)%frames.size).toInt()
            }
            val scale = min(width/256f,(height-24*dp)/256f)
            val save = canvas.save()
            canvas.translate((width-256*scale)/2f,height-256*scale)
            canvas.scale(scale,scale)
            val mirror = canvas.save()
            if (motion.direction < 0) canvas.scale(-1f,1f,128f,128f)
            bitmapPaint.alpha = 255
            canvas.drawBitmap(frames[frame],0f,0f,bitmapPaint)
            // Four red bent strokes form the anime forehead anger symbol, never a font glyph.
            if (now < mood.angryUntil) {
                val pulse = 1f+0.06f*sin((now%650L)/650.0*2*PI).toFloat()
                val a = canvas.save()
                canvas.translate(211f,128f);canvas.scale(pulse,pulse)
                fx.color = Color.rgb(237,56,67);fx.style = Paint.Style.STROKE
                fx.strokeWidth = 5f;fx.strokeCap = Paint.Cap.ROUND;fx.strokeJoin = Paint.Join.ROUND
                val mark = Path().apply {
                    moveTo(-14f,-5f);lineTo(-6f,-5f);lineTo(-6f,-14f)
                    moveTo(6f,-14f);lineTo(6f,-5f);lineTo(14f,-5f)
                    moveTo(14f,6f);lineTo(6f,6f);lineTo(6f,14f)
                    moveTo(-6f,14f);lineTo(-6f,6f);lineTo(-14f,6f)
                }
                canvas.drawPath(mark,fx);fx.style = Paint.Style.FILL
                canvas.restoreToCount(a)
            }
            canvas.restoreToCount(mirror)
            if (now < mood.loveUntil) drawHearts(canvas,now)
            if (now < mood.butterflyUntil) drawButterfly(canvas,now)
            if (mode == "uxlash") {
                fx.color = Color.rgb(149,170,238);fx.textSize = 20f
                canvas.drawText("Zzz",178f,81f,fx)
            }
            canvas.restoreToCount(save)
        }
        private fun drawHearts(c: Canvas, now: Long) {
            for (i in 0..4) {
                val t = (((now-mood.loveStarted)+i*310L)%1700L)/1700f
                val a = c.save()
                val x = 51f+i*35f+sin(t*PI*2+i).toFloat()*9f
                c.translate(x,105f-t*105f)
                val size = 0.65f+i%3*0.18f
                c.scale(size,size)
                fx.style = Paint.Style.FILL
                fx.color = if (i%2==0) Color.rgb(255,91,141) else Color.rgb(255,143,178)
                fx.alpha = (255f*sin(PI*t).toFloat()).roundToInt().coerceIn(0,255)
                c.drawPath(heart,fx);fx.alpha=255;c.restoreToCount(a)
            }
        }
        private fun drawButterfly(c: Canvas, now: Long) {
            val t = now/1000.0
            val x = 184f+sin(t*2).toFloat()*17f
            val y = 66f+cos(t*2).toFloat()*11f
            val wing = 4f+abs(sin(t*15)).toFloat()*11f
            fx.style=Paint.Style.FILL;fx.color=Color.rgb(172,119,237)
            c.drawOval(x-wing,y-10f,x-1f,y+9f,fx)
            fx.color=Color.rgb(255,146,200);c.drawOval(x+1f,y-10f,x+wing,y+9f,fx)
            fx.color=Color.rgb(90,53,88);fx.strokeWidth=2f;c.drawLine(x,y-7f,x,y+9f,fx)
        }
        override fun performClick(): Boolean {
            super.performClick()
            val now=SystemClock.uptimeMillis()
            reaction=mood.tap(now)
            reactionUntil=now+1100L
            setMode(reaction,now)
            sound.play()
            invalidate()
            return true
        }
        override fun onTouchEvent(e: MotionEvent): Boolean {
            when(e.actionMasked) {
                MotionEvent.ACTION_DOWN -> {
                    touchHeld=true;dragging=false;longPressed=false;heldSince=SystemClock.uptimeMillis()
                    tapsStartX=e.rawX;tapsStartY=e.rawY;originX=params.x;originY=params.y
                    handler.postDelayed(closePet,850L)
                }
                MotionEvent.ACTION_MOVE -> {
                    if (hypot(e.rawX-tapsStartX,e.rawY-tapsStartY)>slop) {
                        dragging=true;handler.removeCallbacks(closePet)
                    }
                    if (dragging) {
                        params.x=originX+(e.rawX-tapsStartX).roundToInt()
                        params.y=originY+(e.rawY-tapsStartY).roundToInt()
                        updatePosition()
                        motion.x=params.x.toFloat()
                    }
                }
                MotionEvent.ACTION_UP, MotionEvent.ACTION_CANCEL -> {
                    handler.removeCallbacks(closePet)
                    val now=SystemClock.uptimeMillis()
                    modeStarted+=now-heldSince
                    touchHeld=false
                    if(e.actionMasked==MotionEvent.ACTION_UP && !dragging && !longPressed)performClick()
                    dragging=false
                }
            }
            return true
        }
    }
}
