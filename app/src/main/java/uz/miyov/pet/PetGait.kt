package uz.miyov.pet

import kotlin.math.*

/** Coordinates in the fixed 256 px character canvas. Stance feet stay planted in world space. */
object PetGait {
    data class Foot(val x: Float, val y: Float)
    fun foot(phase: Float, offset: Float, running: Boolean = false): Foot {
        val p = ((phase+offset)%1f+1f)%1f
        val duty = if (running) 0.48f else 0.65f
        val cycle = if (running) 108.8f else 76.8f
        val stride = cycle*duty
        if (p<duty) return Foot(stride/2-cycle*p,224f)
        val q=(p-duty)/(1f-duty)
        val ease=q*q*(3f-2f*q)
        return Foot(-stride/2+stride*ease,224f-(if(running)28f else 19f)*sin(PI*q).toFloat())
    }
}
