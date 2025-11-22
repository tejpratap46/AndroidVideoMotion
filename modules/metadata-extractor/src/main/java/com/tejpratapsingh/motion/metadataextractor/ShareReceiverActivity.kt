package com.tejpratapsingh.motion.metadataextractor

import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.util.Log
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.AppCompatImageView
import androidx.appcompat.widget.AppCompatTextView
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.lifecycleScope
import com.google.android.material.button.MaterialButton
import com.tejpratapsingh.motionlib.core.extensions.fetchBitmap
import io.ktor.client.HttpClient
import io.ktor.client.engine.cio.CIO
import kotlinx.coroutines.launch

class ShareReceiverActivity : AppCompatActivity() {

    companion object {
        private const val TAG = "ShareReceiverActivity"

        const val EXTRA_METADATA = "extra_metadata"
        const val ACTIVITY_INTENT_ACTION = "com.tejpratapsingh.motion.metadataextractor.action.OPEN"

        fun readMetadataFromIntent(intent: Intent): SocialMeta? {
            return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                intent.getParcelableExtra(EXTRA_METADATA, SocialMeta::class.java)
            } else {
                intent.getParcelableExtra(EXTRA_METADATA) as SocialMeta?
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_share_receiver)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        when (intent?.action) {
            Intent.ACTION_SEND -> {
                when (intent.type) {
                    "text/plain" -> lifecycleScope.launch {
                        handleSharedText(intent)
                    }

                    else -> {
                        // Handle other types if needed
                        finish() // Close the activity if the type is unsupported
                    }
                }
            }
        }
    }

    private suspend fun handleSharedText(intent: Intent) {
        val sharedText = intent.getStringExtra(Intent.EXTRA_TEXT)
        val client = HttpClient(CIO)

        sharedText?.let {
            Log.d("ShareReceiver", "Received text: $it")
            client.extractSocialMetadata(it)?.also { metaData ->
                findViewById<AppCompatImageView>(R.id.iv_image).also { iv ->
                    client.fetchBitmap(metaData.image!!)?.let { bitmap ->
                        iv.setImageBitmap(bitmap)
                    }
                }

                findViewById<AppCompatTextView>(R.id.tv_title).also { tv ->
                    tv.text = metaData.title ?: "No Title Found"
                }

                findViewById<MaterialButton>(R.id.btn_next).also { btn ->
                    btn.setOnClickListener {
                        startActivity(Intent(ACTIVITY_INTENT_ACTION).apply {
                            putExtra(EXTRA_METADATA, metaData)
                        })
                        finish()
                    }
                }
            }
        }
    }
}