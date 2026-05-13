package com.tejpratapsingh.motion.metadataextractor.presentation

import android.content.Intent
import android.os.Bundle
import android.util.Patterns
import android.view.inputmethod.EditorInfo
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.IntentCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.isVisible
import androidx.lifecycle.lifecycleScope
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.tejpratapsingh.motion.metadataextractor.R
import com.tejpratapsingh.motion.metadataextractor.data.MetaDataResult
import com.tejpratapsingh.motion.metadataextractor.data.SocialMeta
import com.tejpratapsingh.motion.metadataextractor.databinding.ActivityShareReceiverBinding
import kotlinx.coroutines.launch
import timber.log.Timber

class ShareReceiverActivity : AppCompatActivity() {
    companion object {
        const val EXTRA_METADATA = "extra_metadata"
        const val ACTIVITY_INTENT_ACTION = "com.tejpratapsingh.motion.metadataextractor.action.OPEN"

        fun readMetadataFromIntent(intent: Intent): SocialMeta? =
            IntentCompat.getParcelableExtra(intent, EXTRA_METADATA, SocialMeta::class.java)
    }

    private lateinit var binding: ActivityShareReceiverBinding
    private val metadataViewModel by viewModels<MetaDataViewModel>()
    private var sharedLink: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        binding = ActivityShareReceiverBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupWindowInsets()
        handleIntent(intent)
        observeMetaData()
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        handleIntent(intent)
    }

    private fun setupWindowInsets() {
        ViewCompat.setOnApplyWindowInsetsListener(binding.main) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
    }

    private fun handleIntent(intent: Intent?) {
        if (intent?.action == Intent.ACTION_SEND && intent.type == "text/plain") {
            handleSharedText(intent)
        } else if (intent != null) {
            // Unsupported action or type
            finish()
        }
    }

    private fun handleSharedText(intent: Intent) {
        val sharedText = intent.getStringExtra(Intent.EXTRA_TEXT).orEmpty()
        Timber.d("Received text: $sharedText")

        // Suggestion: Move extractLinks to MetaDataViewModel
        val links = extractLinks(sharedText)

        sharedLink = links.firstOrNull()
        sharedLink?.let { link ->
            Timber.d("Received link: $link")
            binding.loading.isVisible = true
            metadataViewModel.getMetaData(link)
        } ?: run {
            Timber.w("No links found in shared text")
            Toast.makeText(this, R.string.no_link_found, Toast.LENGTH_SHORT).show()
            finish()
        }
    }

    private fun extractLinks(text: String): List<String> {
        // Use Android's built-in Patterns for robust URL matching
        val matcher = Patterns.WEB_URL.matcher(text)
        val links = mutableListOf<String>()
        while (matcher.find()) {
            links.add(matcher.group())
        }
        return links
    }

    private fun observeMetaData() {
        metadataViewModel.metadata.observe(this) { result ->
            Timber.d("Received result: $result")
            binding.loading.isVisible = false
            when (result) {
                is MetaDataResult.Success -> {
                    val meta = result.metaData
                    binding.tvTitle.setText(meta.title ?: getString(R.string.no_title_found))
                    meta.image?.let { loadImage(it) }

                    binding.btnNext.isEnabled = true
                    binding.btnNext.setOnClickListener { navigateToNext(meta) }

                    binding.tvTitle.setOnEditorActionListener { _, actionId, _ ->
                        if (actionId == EditorInfo.IME_ACTION_NEXT || actionId == EditorInfo.IME_ACTION_DONE) {
                            navigateToNext(meta)
                            true
                        } else {
                            false
                        }
                    }
                }

                is MetaDataResult.Error -> {
                    handleFetchError(result.error)
                }
            }
        }
    }

    private fun navigateToNext(metaData: SocialMeta) {
        val updatedMeta = metaData.copy(title = binding.tvTitle.text.toString())
        val intent =
            Intent(ACTIVITY_INTENT_ACTION).apply {
                putExtra(EXTRA_METADATA, updatedMeta)
            }
        startActivity(intent)
        finish()
    }

    private fun handleFetchError(error: Exception) {
        Timber.e(error, "Received error")
        binding.btnNext.isEnabled = false
        MaterialAlertDialogBuilder(this)
            .setTitle(R.string.error_fetching_metadata)
            .setMessage(R.string.failed_to_fetch_metadata_message)
            .setPositiveButton(R.string.retry) { dialog, _ ->
                dialog.dismiss()
                sharedLink?.let {
                    binding.loading.isVisible = true
                    metadataViewModel.getMetaData(it)
                }
            }.setNegativeButton(R.string.close) { dialog, _ ->
                dialog.dismiss()
                finish()
            }.setCancelable(false)
            .show()
    }

    private fun loadImage(url: String) {
        lifecycleScope.launch {
            val image = metadataViewModel.downloadImage(url)
            binding.ivImage.setImageBitmap(image)
        }
    }
}
