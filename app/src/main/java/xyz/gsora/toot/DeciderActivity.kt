package xyz.gsora.toot

import android.content.Intent
import android.os.Bundle
import android.support.v7.app.AppCompatActivity

class DeciderActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_decider)

        if (Toot.hasLoggedIn()) {
            startActivity(Intent(this, TimelineFragmentContainer::class.java))
        } else {
            startActivity(Intent(this, LoginActivity::class.java))
        }
    }
}
