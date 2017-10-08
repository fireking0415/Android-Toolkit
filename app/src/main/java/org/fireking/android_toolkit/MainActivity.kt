package org.fireking.android_toolkit

import android.content.Intent
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.widget.Toast
import org.fireking.android_toolkit.screenshot.ScreenShotActivity
import org.fireking.screenshot.RxScreenshotDetector

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)


        RxScreenshotDetector(this).start()
                .subscribe({
                    val intent = Intent(this, ScreenShotActivity::class.java)
                    intent.putExtra("screenshot_url", it)
                    startActivity(intent)
                }, {
                    Toast.makeText(this, "截取屏幕失败", Toast.LENGTH_SHORT).show()
                })
    }
}
