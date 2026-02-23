package com.tejpratapsingh.motion.metadataextractor.presentation

import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.inputmethod.EditorInfo
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.isVisible
import androidx.lifecycle.lifecycleScope
import com.tejpratapsingh.motion.metadataextractor.data.MetaDataResult
import com.tejpratapsingh.motion.metadataextractor.data.SocialMeta
import com.tejpratapsingh.motion.metadataextractor.databinding.ActivityShareReceiverBinding
import kotlinx.coroutines.launch

class ShareReceiverActivity : AppCompatActivity() {
    companion object {
        private const val TAG = "ShareReceiverActivity"

        const val EXTRA_METADATA = "extra_metadata"
        const val ACTIVITY_INTENT_ACTION = "com.tejpratapsingh.motion.metadataextractor.action.OPEN"

        fun readMetadataFromIntent(intent: Intent): SocialMeta? =
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                intent.getParcelableExtra(EXTRA_METADATA, SocialMeta::class.java)
            } else {
                intent.getParcelableExtra(EXTRA_METADATA) as SocialMeta?
            }
    }

    private lateinit var binding: ActivityShareReceiverBinding
    private val metadataViewModel by viewModels<MetaDataViewModel>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        binding = ActivityShareReceiverBinding.inflate(layoutInflater)
        setContentView(binding.root)
        ViewCompat.setOnApplyWindowInsetsListener(binding.main) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        binding.loading.isVisible = true
        findSharedUrl()
        observerMetaData()
    }

    private fun findSharedUrl() {
        when (intent?.action) {
            Intent.ACTION_SEND -> {
                when (intent.type) {
                    "text/plain" -> {
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

    private fun handleSharedText(intent: Intent) {
        val sharedText = intent.getStringExtra(Intent.EXTRA_TEXT)
        Log.d(TAG, "Received text: $sharedText")
        val links = extractLinks(sharedText ?: "")

        links.firstOrNull()?.let { sharedLink ->
            Log.d(TAG, "Received link: $sharedLink")
            metadataViewModel.getMetaData(sharedLink)
        } ?: run {
            Log.w(TAG, "No links found in shared text")
            Toast.makeText(this, "No link found in shared text", Toast.LENGTH_SHORT).show()
            finish()
        }
    }

    private fun extractLinks(text: String): List<String> {
        // A robust regex for URLs that includes http, https, or ftp protocols
        val urlRegex = Regex("""\b(?:https?|ftp)://\S+\b""")

        // Find all matches and map them to their string values
        val matches = urlRegex.findAll(text).map { it.value }.toList()
        return matches
    }

    private fun observerMetaData() {
        metadataViewModel.metadata.observe(this) { result ->
            Log.d("ShareReceiver", "Received result: $result")
            binding.loading.isVisible = false
            when (result) {
                is MetaDataResult.Success -> {
                    Log.d("ShareReceiver", "Received metadata: ${result.metaData}")
                    result.metaData.image?.also {
                        loadImage(it)
                    }

                    binding.tvTitle.setText(result.metaData.title ?: "No Title Found")

                    binding.btnNext.isEnabled = true
                    val onDone = {
                        startActivity(
                            Intent(ACTIVITY_INTENT_ACTION).apply {
                                putExtra(
                                    EXTRA_METADATA,
                                    result.metaData
                                        .copy(title = binding.tvTitle.text.toString()),
                                )
                            },
                        )
                        finish()
                    }

                    binding.btnNext.setOnClickListener { onDone() }

                    binding.tvTitle.setOnEditorActionListener { _, actionId, _ ->
                        if (actionId == EditorInfo.IME_ACTION_NEXT || actionId == EditorInfo.IME_ACTION_DONE) {
                            onDone()
                            true
                        } else {
                            false
                        }
                    }
                }

                is MetaDataResult.Error -> {
                    Log.e(TAG, "Received error", result.error)
                    Toast
                        .makeText(this, "Failed to fetch metadata", Toast.LENGTH_SHORT)
                        .show()
                }
            }
        }
    }

    private fun loadImage(url: String) {
        lifecycleScope.launch {
            val image = metadataViewModel.downloadImage(url)
            binding.ivImage.setImageBitmap(image)
        }
    }
}
