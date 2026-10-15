package com.tejpratapsingh.motion.assetstore.service.pexels

data class PexelsConfig(
    val apiKey: String = DEFAULT_PEXELS_API_KEY,
) {
    companion object {
        // Default public demo API key or fallback
        const val DEFAULT_PEXELS_API_KEY = "xPuMzRMDKs4vD8WFSrH2ZJVse1olzePJw9JBBceNNbqmREdjUVGzep9u"
    }
}
