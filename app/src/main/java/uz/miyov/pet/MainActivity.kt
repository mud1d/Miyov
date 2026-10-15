package uz.miyov.pet

import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.provider.Settings
import android.widget.Button
import android.widget.LinearLayout
import android.widget.TextView

class MainActivity : Activity() {
 override fun onCreate(savedInstanceState: Bundle?) { super.onCreate(savedInstanceState)
  val layout = LinearLayout(this).apply { orientation = LinearLayout.VERTICAL; setPadding(36,64,36,36) }
  layout.addView(TextView(this).apply { text="🐱 Miyov — ekrandagi mushuk\n\nMushukni bos: ovoz chiqaradi.\nUshlab sudra: joyini o‘zgartir.\nUzoq bos: mushukni yop.\n\nOvoz bilan chaqirish keyingi versiyada."; textSize=20f })
  layout.addView(Button(this).apply { text="Mushukni chiqar"; setOnClickListener {
   if (!Settings.canDrawOverlays(this@MainActivity)) startActivity(Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION, Uri.parse("package:$packageName")))
   else startService(Intent(this@MainActivity, PetService::class.java))
  } })
  layout.addView(Button(this).apply { text="Mushukni yop"; setOnClickListener { stopService(Intent(this@MainActivity,PetService::class.java)) } })
  setContentView(layout)
 }
}
