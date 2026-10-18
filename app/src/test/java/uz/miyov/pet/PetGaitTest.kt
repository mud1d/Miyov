package uz.miyov.pet

import org.junit.Assert.*
import org.junit.Test

class PetGaitTest {
    @Test fun stancePawIsPlantedAsBodyMovesForward() {
        for (run in listOf(false,true)) {
            val cycle=if(run)108.8f else 76.8f
            val a=PetGait.foot(0.1f,0f,run)
            val b=PetGait.foot(0.3f,0f,run)
            assertEquals(0.1f*cycle+a.x,0.3f*cycle+b.x,0.0001f)
            assertEquals(224f,a.y,0f);assertEquals(224f,b.y,0f)
        }
    }
    @Test fun swingActuallyLiftsAndReturnsThePaw() {
        val contact=PetGait.foot(0.5f,0f)
        val lift=PetGait.foot(0.825f,0f)
        assertTrue(lift.y<contact.y-15f)
        assertTrue(kotlin.math.abs(PetGait.foot(0.01f,0f).x-PetGait.foot(0.50f,0f).x)>30f)
    }
    @Test fun cycleClosesWithoutAPositionJump() {
        val a=PetGait.foot(0f,0f)
        val b=PetGait.foot(0.99999f,0f)
        assertEquals(a.x,b.x,0.01f);assertEquals(a.y,b.y,0.01f)
    }
}
