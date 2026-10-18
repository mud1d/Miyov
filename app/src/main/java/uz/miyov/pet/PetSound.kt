package uz.miyov.pet

import android.content.Context
import android.media.AudioAttributes
import android.media.AudioManager
import android.media.MediaPlayer
import android.util.Log
import android.widget.Toast

/** One reusable player; both the overlay and the test button use media volume. */
class PetSound(private val context: Context) {
    private var player: MediaPlayer? = null

    fun play() {
        val audio = context.getSystemService(Context.AUDIO_SERVICE) as AudioManager
        if (audio.getStreamVolume(AudioManager.STREAM_MUSIC) == 0) {
            Toast.makeText(context, "Media ovozi o‘chiq. Ovoz tugmasi bilan ko‘taring.", Toast.LENGTH_LONG).show()
        }
        try {
            val current = player
            if (current != null) {
                // Let a real meow finish: rapid taps must not chop it into a buzzing sound.
                if (!current.isPlaying) current.start()
                return
            }
            val next = MediaPlayer()
            player = next
            next.setAudioAttributes(AudioAttributes.Builder()
                .setUsage(AudioAttributes.USAGE_MEDIA)
                .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION).build())
            context.resources.openRawResourceFd(R.raw.meow).use { fd ->
                next.setDataSource(fd.fileDescriptor, fd.startOffset, fd.length)
            }
            next.setOnErrorListener { _, what, extra ->
                Log.e("MiyovSound", "Playback error: $what/$extra")
                release()
                Toast.makeText(context, "Ovozni ijro etib bo‘lmadi. Qayta bosing.", Toast.LENGTH_SHORT).show()
                true
            }
            next.prepare()
            next.setVolume(0.85f, 0.85f)
            next.start()
        } catch (error: Exception) {
            Log.e("MiyovSound", "Cannot play bundled sound", error)
            release()
            Toast.makeText(context, "Ovoz ochilmadi. Qayta urinib ko‘ring.", Toast.LENGTH_SHORT).show()
        }
    }

    fun release() {
        player?.release()
        player = null
    }
}
