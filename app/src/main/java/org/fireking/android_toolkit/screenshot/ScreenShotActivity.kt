package org.fireking.android_toolkit.screenshot

import android.content.Intent
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import kotlinx.android.synthetic.main.activity_screen_shot.*
import org.fireking.android_toolkit.R

class ScreenShotActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_screen_shot)


        simpleDraweeView.setImageURI("file://${intent.getStringExtra("screenshot_url")}")
    }

    override fun onNewIntent(intent: Intent?) {
        super.onNewIntent(intent)
        setIntent(intent)

        simpleDraweeView.setImageURI("file://${intent?.getStringExtra("screenshot_url}")}")
    }
}
