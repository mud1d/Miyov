package uz.miyov.pet

import android.content.res.AssetManager
import android.graphics.*
import kotlin.math.*

/** One stable body and four articulated legs, rather than independently changing whole-body frames. */
class WalkingRig(assets: AssetManager) {
    private fun load(a: AssetManager,n: String): Bitmap = a.open("rig/$n.png").use { requireNotNull(BitmapFactory.decodeStream(it)) }
    private val body=load(assets,"body")
    private val tail=load(assets,"tail")
    private val fore=load(assets,"fore_upper")
    private val forePaw=load(assets,"fore_paw")
    private val hind=load(assets,"hind_upper")
    private val hindPaw=load(assets,"hind_paw")
    private val paint=Paint(Paint.ANTI_ALIAS_FLAG or Paint.FILTER_BITMAP_FLAG)

    fun draw(c: Canvas,phase: Float,running: Boolean) {
        // Far legs are slightly darker and are occluded by the stable torso.
        paint.colorFilter=LightingColorFilter(0xffc9c9c9.toInt(),0)
        leg(c,84f,174f,if(running)0.5f else 0.5f,phase,false,running)
        leg(c,180f,174f,if(running)0f else 0.25f,phase,true,running)
        paint.colorFilter=null
        val save=c.save()
        c.rotate(sin(phase*2*PI).toFloat()*5f,77f,170f)
        c.drawBitmap(tail,null,RectF(17f,111f,88f,181f),paint)
        c.restoreToCount(save)
        leg(c,91f,175f,0f,phase,false,running)
        leg(c,186f,175f,if(running)0.5f else 0.75f,phase,true,running)
        c.drawBitmap(body,null,RectF(59f,86f,226f,195f),paint)
    }
    private fun leg(c: Canvas,hx: Float,hy: Float,offset: Float,phase: Float,front: Boolean,run: Boolean) {
        val foot=PetGait.foot(phase,offset,run)
        val fx=hx+foot.x;val fy=foot.y
        val ax=fx;val ay=fy-8f
        val dx=ax-hx;val dy=ay-hy
        val d=hypot(dx,dy).coerceIn(1f,55.9f)
        val upper=27f;val lower=29f
        val bend=acos(((upper*upper+d*d-lower*lower)/(2f*upper*d)).coerceIn(-1f,1f))
        val angle=atan2(dy,dx)+(if(front)1f else -1f)*bend
        val kx=hx+upper*cos(angle);val ky=hy+upper*sin(angle)
        val part=if(front)fore else hind
        segment(c,part,hx,hy,kx,ky,if(front)17f else 22f)
        segment(c,part,kx,ky,ax,ay,if(front)12f else 14f)
        val paw=if(front)forePaw else hindPaw
        c.drawBitmap(paw,Rect(0,(paw.height*0.52f).toInt(),paw.width,paw.height),RectF(fx-10f,fy-20f,fx+15f,fy),paint)
    }
    private fun segment(c: Canvas,b: Bitmap,x1: Float,y1: Float,x2: Float,y2: Float,width: Float) {
        val s=c.save()
        c.translate(x1,y1)
        c.rotate((atan2(y2-y1,x2-x1)*180f/PI-90f).toFloat())
        c.drawBitmap(b,Rect(0,0,b.width,(b.height*0.72f).toInt()),RectF(-width/2,-5f,width/2,hypot(x2-x1,y2-y1)+5f),paint)
        c.restoreToCount(s)
    }
}
