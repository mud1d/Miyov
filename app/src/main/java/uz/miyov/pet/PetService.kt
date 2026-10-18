package uz.miyov.pet

import android.app.*
import android.content.*
import android.graphics.*
import android.media.*
import android.os.*
import android.view.*
import kotlin.math.*
import kotlin.random.Random

class PetService : Service() {
 private lateinit var wm: WindowManager
 private lateinit var pet: PetView
 private lateinit var lp: WindowManager.LayoutParams
 private val h=Handler(Looper.getMainLooper())
 private var frame=0
 private var direction=1
 private var movementTicks=0
 private fun meow() {
  Thread {
   try {
    val rate = 22050
    val duration = 0.55
    val samples = ShortArray((rate * duration).toInt())
    var phase = 0.0
    for (i in samples.indices) {
     val progress = i.toDouble() / samples.size
     val freq = if (progress < 0.4) 520.0 + 500.0 * progress / 0.4 else 1020.0 - 620.0 * (progress - 0.4) / 0.6
     phase += 2.0 * Math.PI * freq / rate
     val envelope = sin(Math.PI * progress).coerceAtLeast(0.0)
     val voice = sin(phase) + 0.25 * sin(phase * 2.0)
     samples[i] = (voice * envelope * 10000).toInt().coerceIn(-32768, 32767).toShort()
    }
    val audio = AudioTrack.Builder()
     .setAudioAttributes(AudioAttributes.Builder().setUsage(AudioAttributes.USAGE_ASSISTANCE_SONIFICATION).setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION).build())
     .setAudioFormat(AudioFormat.Builder().setEncoding(AudioFormat.ENCODING_PCM_16BIT).setSampleRate(rate).setChannelMask(AudioFormat.CHANNEL_OUT_MONO).build())
     .setBufferSizeInBytes(samples.size * 2).setTransferMode(AudioTrack.MODE_STATIC).build()
    audio.write(samples, 0, samples.size)
    audio.play()
    Thread.sleep(650)
    audio.stop()
    audio.release()
   } catch (_: Exception) {}
  }.start()
 }

 private val ticker=object:Runnable { override fun run() {
  if (::pet.isInitialized) {
   frame++
   if(frame%110==0) direction=if(Random.nextBoolean()) 1 else -1
   pet.sleeping=frame%480 in 355..465
   if(frame%4==0 && !pet.dragging && !pet.sleeping) {
    val half=resources.displayMetrics.widthPixels/2-105
    lp.x=(lp.x+direction*3).coerceIn(-half,half)
    try { wm.updateViewLayout(pet,lp) } catch (_:Exception) {}
   }
   pet.invalidate()
   h.postDelayed(this,45)
  }
 } }
 override fun onCreate() { super.onCreate()
  val nm=getSystemService(NOTIFICATION_SERVICE) as NotificationManager
  nm.createNotificationChannel(NotificationChannel("miyov","Miyov",NotificationManager.IMPORTANCE_LOW))
  startForeground(7,Notification.Builder(this,"miyov").setSmallIcon(android.R.drawable.ic_menu_view).setContentTitle("Miyov 🐱").setContentText("Miyov ekranda").build())
  wm=getSystemService(WINDOW_SERVICE) as WindowManager
  lp=WindowManager.LayoutParams(260,280,WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY,
   WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE or WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS,
   PixelFormat.TRANSLUCENT).apply {gravity=Gravity.TOP or Gravity.CENTER_HORIZONTAL;y=260}
  pet=PetView()
  try {wm.addView(pet,lp);h.post(ticker)} catch (_:Exception){stopSelf()}
 }
 override fun onStartCommand(intent:Intent?,flags:Int,startId:Int)=START_STICKY
 override fun onBind(intent:Intent?):IBinder?=null
 override fun onDestroy(){h.removeCallbacks(ticker);if(::pet.isInitialized)try{wm.removeView(pet)}catch(_:Exception){};super.onDestroy()}

 inner class PetView:View(this@PetService) {
  var dragging=false;var sleeping=false
  private val paint=Paint(3)
  private var downX=0f;private var downY=0f;private var lastX=0f;private var lastY=0f;private var downTime=0L
  private var loveUntil=0L;private var angryUntil=0L;private var butterflyUntil=0L
  private var taps=0;private var lastTap=0L
  private val orange=Color.rgb(245,168,88)
  private val light=Color.rgb(255,221,177)
  private val white=Color.rgb(255,248,236)
  private val brown=Color.rgb(78,48,40)
  private fun oval(c:Canvas,color:Int,l:Float,t:Float,r:Float,b:Float){paint.color=color;paint.style=Paint.Style.FILL;c.drawOval(l,t,r,b,paint)}
  private fun circle(c:Canvas,color:Int,x:Float,y:Float,r:Float){paint.color=color;paint.style=Paint.Style.FILL;c.drawCircle(x,y,r,paint)}
  private fun line(c:Canvas,color:Int,x:Float,y:Float,x2:Float,y2:Float,w:Float=2f){paint.color=color;paint.style=Paint.Style.STROKE;paint.strokeWidth=w;paint.strokeCap=Paint.Cap.ROUND;c.drawLine(x,y,x2,y2,paint);paint.style=Paint.Style.FILL}
  private fun path(c:Canvas,color:Int,vararg pts:Float){val p=Path();p.moveTo(pts[0],pts[1]);var i=2;while(i<pts.size){p.lineTo(pts[i],pts[i+1]);i+=2};p.close();paint.color=color;c.drawPath(p,paint)}
  private fun text(c:Canvas,s:String,x:Float,y:Float,size:Float,color:Int){paint.color=color;paint.textSize=size;paint.typeface=Typeface.create("sans-serif",Typeface.BOLD);paint.textAlign=Paint.Align.CENTER;c.drawText(s,x,y,paint)}
  override fun onDraw(canvas:Canvas){super.onDraw(canvas)
   canvas.save();canvas.scale(width/260f,height/280f)
   val t=frame*.16f
   val hop=if(sleeping || dragging) 0f else abs(sin(t.toDouble())).toFloat()*3f
   canvas.translate(0f,-hop)
   if(sleeping) { canvas.translate(0f,13f); canvas.scale(1.04f,0.91f,130f,185f) }
   // Curved fluffy tail behind body
   paint.color=orange;paint.style=Paint.Style.STROKE;paint.strokeWidth=24f;paint.strokeCap=Paint.Cap.ROUND
   val tail=Path().apply {moveTo(177f,178f);cubicTo(238f,189f,244f,114f,215f+sin(t.toDouble()).toFloat()*9f,111f)}
   canvas.drawPath(tail,paint);paint.style=Paint.Style.FILL
   circle(canvas,light,215f+sin(t.toDouble()).toFloat()*9f,111f,14f)
   // Animated paws
   val step=if(sleeping || dragging)0f else sin(t.toDouble()).toFloat()*6f
   oval(canvas,orange,69f,185f+step,104f,230f+step)
   oval(canvas,orange,153f,185f-step,188f,230f-step)
   oval(canvas,white,68f,215f+step,108f,239f+step)
   oval(canvas,white,151f,215f-step,191f,239f-step)
   // Body, chest, belly
   oval(canvas,orange,67f,115f,195f,219f)
   oval(canvas,light,82f,135f,184f,218f)
   oval(canvas,white,112f,142f,165f,217f)
   // ears, head
   path(canvas,orange,63f,85f,67f,28f,111f,64f)
   path(canvas,orange,150f,65f,194f,28f,199f,87f)
   path(canvas,Color.rgb(255,168,172),75f,71f,76f,47f,99f,70f)
   path(canvas,Color.rgb(255,168,172),164f,69f,187f,47f,188f,73f)
   oval(canvas,orange,54f,57f,207f,171f)
   oval(canvas,light,62f,77f,199f,172f)
   oval(canvas,white,73f,122f,137f,172f)
   oval(canvas,white,127f,122f,192f,172f)
   // stripes
   line(canvas,Color.rgb(220,127,64),109f,68f,114f,85f,5f)
   line(canvas,Color.rgb(220,127,64),132f,66f,132f,84f,5f)
   line(canvas,Color.rgb(220,127,64),153f,68f,148f,85f,5f)
   if(sleeping || frame%150 in 145..149){
    line(canvas,brown,85f,116f,113f,116f,4f);line(canvas,brown,150f,116f,178f,116f,4f)
   }else{
    oval(canvas,brown,86f,101f,113f,132f);oval(canvas,brown,150f,101f,177f,132f)
    circle(canvas,Color.WHITE,95f,108f,7f);circle(canvas,Color.WHITE,159f,108f,7f)
    circle(canvas,Color.WHITE,107f,122f,3f);circle(canvas,Color.WHITE,171f,122f,3f)
   }
   path(canvas,Color.rgb(234,121,137),124f,136f,140f,136f,132f,144f)
   line(canvas,brown,132f,145f,132f,151f,2f)
   line(canvas,brown,132f,151f,123f,155f,2f);line(canvas,brown,132f,151f,141f,155f,2f)
   for (i in 0..1){val dy=i*8f;line(canvas,brown,112f,145f+dy,65f,140f+dy,1.5f);line(canvas,brown,152f,145f+dy,199f,140f+dy,1.5f)}
   val now=System.currentTimeMillis()
   if(sleeping) {text(canvas,"Zzz",203f,48f,23f,Color.rgb(149,170,238))}
   if(now<loveUntil){
    val rise=((loveUntil-now)/20).toFloat()
    text(canvas,"♥",52f,58f-rise%32,30f,Color.rgb(255,92,140))
    text(canvas,"♥",209f,92f-(rise*1.5f)%42,22f,Color.rgb(255,133,167))
   }
   if(now<angryUntil){
    circle(canvas,Color.rgb(255,115,115),79f,141f,11f)
    circle(canvas,Color.rgb(255,115,115),181f,141f,11f)
    line(canvas,brown,94f,96f,113f,103f,4f)
    line(canvas,brown,168f,96f,150f,103f,4f)
   }
   if(now<butterflyUntil){
    val bx=190f+sin(t.toDouble()).toFloat()*20f
    val by=52f+cos(t.toDouble()).toFloat()*14f
    val flap=abs(sin(t.toDouble()*3)).toFloat()
    oval(canvas,Color.rgb(173,125,243),bx-15f-flap*7f,by-11f,bx-1f,by+9f)
    oval(canvas,Color.rgb(251,156,218),bx+1f,by-11f,bx+15f+flap*7f,by+9f)
    line(canvas,brown,bx,by-8f,bx,by+9f,2f)
   }
   canvas.restore()
  }
  override fun onTouchEvent(e:MotionEvent):Boolean {when(e.actionMasked){
   MotionEvent.ACTION_DOWN->{downX=e.rawX;downY=e.rawY;lastX=e.rawX;lastY=e.rawY;downTime=System.currentTimeMillis();dragging=false;return true}
   MotionEvent.ACTION_MOVE->{if(hypot(e.rawX-downX,e.rawY-downY)>12f)dragging=true
    if(dragging){lp.x+=(e.rawX-lastX).toInt();lp.y+=(e.rawY-lastY).toInt();try{wm.updateViewLayout(this,lp)}catch(_:Exception){}}
    lastX=e.rawX;lastY=e.rawY;return true}
   MotionEvent.ACTION_UP->{val now=System.currentTimeMillis()
    if(!dragging){
     if(now-downTime>850){stopSelf()}
     else{
      if(now-lastTap<650)taps++ else taps=1
      lastTap=now
      if(taps>=4){angryUntil=now+1700;taps=0}
      else if(taps==2){loveUntil=now+2000}
      else if(taps==3){butterflyUntil=now+2500}
      meow()
     }
    }
    dragging=false;invalidate();return true}
  };return true}
 }
}
