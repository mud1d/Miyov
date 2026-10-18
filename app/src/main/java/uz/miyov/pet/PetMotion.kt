package uz.miyov.pet

import kotlin.math.*

/** Time-based movement: speed and gait do not depend on display refresh rate. */
class PetMotion {
    var x = 0f
    var velocity = 0f
        private set
    var direction = 1
        private set
    var distance = 0f
        private set

    fun step(seconds: Float, targetSpeed: Float, maxX: Float, held: Boolean) {
        val dt = seconds.coerceIn(0f, 0.05f)
        if (held) { velocity = 0f; return }
        val target = direction * targetSpeed
        // Integrate exponential velocity easing analytically, preserving speed across frame rates.
        val fade = exp(-dt / 0.18f)
        val travel = target * dt + (velocity - target) * 0.18f * (1f - fade)
        velocity = target + (velocity - target) * fade
        val next = x + travel
        if (maxX <= 0f) { x = 0f; velocity = 0f; return }
        if (next > maxX || next < 0f) {
            distance += abs(next.coerceIn(0f, maxX) - x)
            x = next.coerceIn(0f, maxX)
            direction = if (next > maxX) -1 else 1
            velocity = 0f
        } else {
            x = next
            distance += abs(travel)
        }
    }
}

class PetMood {
    var taps = 0
        private set
    private var lastTap = Long.MIN_VALUE / 2
    var loveUntil = 0L
        private set
    var angryUntil = 0L
        private set
    var butterflyUntil = 0L
        private set
    var loveStarted = 0L
        private set

    fun tap(now: Long): String {
        taps = if (now - lastTap <= 1000L) taps + 1 else 1
        lastTap = now
        if (now < angryUntil || taps >= 8) {
            angryUntil = now + 3500L
            loveUntil = 0L
            butterflyUntil = 0L
            return "jahl"
        }
        if (taps >= 2) {
            if (now >= loveUntil) loveStarted = now
            loveUntil = now + 2600L
            if (taps == 3) butterflyUntil = now + 2600L
            return "erkalash"
        }
        return "qiziqish"
    }
}
