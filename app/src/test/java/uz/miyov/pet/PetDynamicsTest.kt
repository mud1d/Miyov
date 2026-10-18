package uz.miyov.pet

import org.junit.Assert.*
import org.junit.Test

class PetDynamicsTest {
    @Test fun distanceIsIndependentOfRefreshRate() {
        fun simulate(hz: Int): PetMotion {
            val m=PetMotion(); m.x=10f
            repeat(hz*10) { m.step(1f/hz,20f,10000f,false) }
            return m
        }
        val slow=simulate(30);val normal=simulate(60);val fast=simulate(120)
        assertEquals(slow.x,normal.x,0.02f)
        assertEquals(normal.x,fast.x,0.02f)
        assertEquals(normal.distance,fast.distance,0.02f)
    }
    @Test fun gentleTapsShowLoveAndEightRapidTapsShowAnger() {
        val m=PetMood()
        assertEquals("qiziqish",m.tap(100L))
        assertEquals("erkalash",m.tap(300L))
        assertTrue(m.loveUntil>300L)
        m.tap(500L);assertTrue(m.butterflyUntil>500L)
        repeat(4) { m.tap(700L+it*200L) }
        assertEquals(7,m.taps)
        assertEquals(0L,m.angryUntil)
        assertEquals("jahl",m.tap(1600L))
        assertTrue(m.angryUntil>1600L)
        assertEquals(0L,m.loveUntil)
        assertEquals(0L,m.butterflyUntil)
        assertEquals("qiziqish",m.tap(7000L))
    }
    @Test fun holdingStopsBothMovementAndGait() {
        val m=PetMotion();m.x=100f
        repeat(60) { m.step(1f/60,20f,500f,false) }
        val pos=m.x;val phase=m.distance
        repeat(120) { m.step(1f/60,20f,500f,true) }
        assertEquals(pos,m.x,0f);assertEquals(phase,m.distance,0f)
    }
    @Test fun edgeTurnsAroundAndNeverLeavesScreen() {
        val m=PetMotion();m.x=99f
        repeat(600) { m.step(1f/60,20f,100f,false); assertTrue(m.x>=0f && m.x<=100f) }
        assertTrue(m.distance>100f)
    }
}
