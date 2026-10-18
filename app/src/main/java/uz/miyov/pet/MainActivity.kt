package uz.miyov.pet

import android.app.Activity
import android.content.Intent
import android.media.AudioManager
import android.net.Uri
import android.os.Bundle
import android.provider.Settings
import android.widget.*

class MainActivity : Activity() {
    private lateinit var sound: PetSound
    private var pendingShow = false
    private fun showCat() = startForegroundService(Intent(this, PetService::class.java))

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        volumeControlStream = AudioManager.STREAM_MUSIC
        sound = PetSound(this)
        val dp = resources.displayMetrics.density
        val layout = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding((24*dp).toInt(), (32*dp).toInt(), (24*dp).toInt(), (24*dp).toInt())
        }
        layout.addView(TextView(this).apply {
            text = "Miyov · PNG mushukcha\n\nBos — ovoz chiqaradi.\nSudra — joyini o‘zgartiradi.\nUzoq bos — yopiladi.\n\nKichik mushukcha: 88 × 80 dp.\nOvoz uchun media balandligini tekshir.\n"
            textSize = 18f
        })
        layout.addView(Button(this).apply {
            text = "Mushukchani chiqar"
            setOnClickListener {
                if (!Settings.canDrawOverlays(this@MainActivity)) {
                    pendingShow = true
                    startActivity(Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION, Uri.parse("package:$packageName")))
                } else showCat()
            }
        })
        layout.addView(Button(this).apply {
            text = "Ovozni tekshir"
            setOnClickListener { sound.play() }
        })
        layout.addView(Button(this).apply {
            text = "Mushukchani yop"
            setOnClickListener { stopService(Intent(this@MainActivity, PetService::class.java)) }
        })
        setContentView(ScrollView(this).apply { addView(layout) })
    }
    override fun onResume() {
        super.onResume()
        if (pendingShow && Settings.canDrawOverlays(this)) {
            pendingShow = false
            showCat()
        }
    }
    override fun onStop() {
        sound.release()
        super.onStop()
    }
}
