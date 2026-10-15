package uz.miyov.pet

import android.app.Service
import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Intent
import android.graphics.*
import android.media.AudioManager
import android.media.ToneGenerator
import android.os.Handler
import android.os.IBinder
import android.os.Looper
import android.view.*
import kotlin.math.*
import kotlin.random.Random

class PetService: Service() {
 private lateinit var wm: WindowManager
 private lateinit var cat: CatView
 private lateinit var params: WindowManager.LayoutParams
 private val handler = Handler(Looper.getMainLooper())
 private var tone: ToneGenerator? = null
 private var ticks=0
 private var dir=1
 private val mover=object:Runnable { override fun run() {
  if (::cat.isInitialized) {
   ticks++
   if (ticks%40==0) dir=if(Random.nextBoolean()) 1 else -1
   if (ticks%5==0 && !cat.dragging) {
    val screen=resources.displayMetrics.widthPixels
    params.x=(params.x+dir*6).coerceIn(-screen/2+80,screen/2-80)
    try { wm.updateViewLayout(cat,params) } catch (_:Exception) {}
   }
   cat.sleeping = ticks%180 in 145..178
   cat.invalidate()
   handler.postDelayed(this,100)
  }
 } }
 override fun onCreate() { super.onCreate()
  val manager = getSystemService(NOTIFICATION_SERVICE) as NotificationManager
  manager.createNotificationChannel(NotificationChannel("miyov", "Miyov ishlamoqda", NotificationManager.IMPORTANCE_LOW))
  val notification = Notification.Builder(this, "miyov")
    .setContentTitle("Miyov 🐱").setContentText("Mushuk ekranda yurmoqda")
    .setSmallIcon(android.R.drawable.ic_menu_view).build()
  startForeground(7, notification)
  tone=ToneGenerator(AudioManager.STREAM_MUSIC,75)
  wm=getSystemService(WINDOW_SERVICE) as WindowManager
  params=WindowManager.LayoutParams(180,180,WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY,
   WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE or WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS,
   PixelFormat.TRANSLUCENT).apply { gravity=Gravity.TOP or Gravity.CENTER_HORIZONTAL; y=240 }
  cat=CatView()
  try { wm.addView(cat,params); handler.post(mover) } catch (_:Exception) { stopSelf() }
 }
 override fun onStartCommand(intent:Intent?,flags:Int,startId:Int)=START_STICKY
 override fun onDestroy() { tone?.release(); tone=null; handler.removeCallbacks(mover); if(::cat.isInitialized) try { wm.removeView(cat) } catch (_:Exception) {}; super.onDestroy() }
 override fun onBind(intent:Intent?):IBinder?=null
 inner class CatView:View(this@PetService) {
  var dragging=false; var sleeping=false
  private val p=Paint(3)
  private var downX=0f; private var downY=0f; private var lastX=0f; private var lastY=0f; private var downAt=0L
  override fun onDraw(c:Canvas) { super.onDraw(c)
   c.save(); c.scale(width/180f,height/180f)
   p.color=Color.rgb(243,164,91)
   c.drawOval(26f,74f,155f,158f,p)
   val ears=Path().apply { moveTo(38f,75f); lineTo(45f,18f); lineTo(85f,65f); lineTo(122f,18f); lineTo(143f,78f); close() }
   c.drawPath(ears,p)
   p.color=Color.rgb(255,195,201)
   c.drawOval(46f,35f,62f,66f,p); c.drawOval(116f,35f,133f,66f,p)
   p.color=Color.rgb(255,210,154); c.drawOval(31f,56f,151f,150f,p)
   p.color=Color.rgb(42,37,38)
   if(sleeping) { p.strokeWidth=4f; c.drawLine(57f,104f,76f,104f,p); c.drawLine(108f,104f,127f,104f,p) }
   else { c.drawOval(62f,92f,73f,109f,p); c.drawOval(113f,92f,124f,109f,p) }
   p.color=Color.rgb(232,115,128); val nose=Path().apply { moveTo(87f,115f); lineTo(101f,115f); lineTo(94f,123f); close() }; c.drawPath(nose,p)
   p.color=Color.DKGRAY; p.strokeWidth=2f
   for (side in listOf(-1,1)) { val start=if(side<0) 84f else 104f; c.drawLine(start,124f,start+side*40,118f,p); c.drawLine(start,129f,start+side*39,136f,p) }
   c.restore()
  }
  override fun onTouchEvent(e:MotionEvent):Boolean { when(e.actionMasked) {
   MotionEvent.ACTION_DOWN -> { downX=e.rawX;downY=e.rawY;lastX=e.rawX;lastY=e.rawY;downAt=System.currentTimeMillis();dragging=false;return true }
   MotionEvent.ACTION_MOVE -> { val dx=e.rawX-lastX;val dy=e.rawY-lastY
    if(hypot(e.rawX-downX,e.rawY-downY)>12) dragging=true
    if(dragging) {params.x+=dx.toInt();params.y+=dy.toInt();wm.updateViewLayout(this,params)}
    lastX=e.rawX;lastY=e.rawY;return true }
   MotionEvent.ACTION_UP -> { if(!dragging) { if(System.currentTimeMillis()-downAt>650) {
      stopSelf()
     } else { try { tone?.startTone(ToneGenerator.TONE_PROP_BEEP2,250) } catch (_:Exception){} } }
    dragging=false;return true }
  }; return true }
 }
}
