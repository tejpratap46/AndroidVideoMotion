package com.tejpratapsingh.motion.assetstore.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.tejpratapsingh.motion.assetstore.model.AssetFileType
import com.tejpratapsingh.motion.assetstore.model.AssetItem
import com.tejpratapsingh.motion.assetstore.repository.AssetStoreRepository
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch

@OptIn(FlowPreview::class)
class AssetPickerViewModel(
    private val repository: AssetStoreRepository,
) : ViewModel() {
    private val _fileType = MutableStateFlow(AssetFileType.IMAGE)
    val fileType: StateFlow<AssetFileType> = _fileType.asStateFlow()

    private val _selectedTab = MutableStateFlow(1) // Default to Online / Pexels
    val selectedTab: StateFlow<Int> = _selectedTab.asStateFlow()

    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    private val _imagesState = MutableStateFlow<AssetPickerUiState<AssetItem.Image>>(AssetPickerUiState.Loading)
    val imagesState: StateFlow<AssetPickerUiState<AssetItem.Image>> = _imagesState.asStateFlow()

    private val _videosState = MutableStateFlow<AssetPickerUiState<AssetItem.Video>>(AssetPickerUiState.Loading)
    val videosState: StateFlow<AssetPickerUiState<AssetItem.Video>> = _videosState.asStateFlow()

    private val _downloadingProgress = MutableStateFlow<Map<String, Int>>(emptyMap())
    val downloadingProgress: StateFlow<Map<String, Int>> = _downloadingProgress.asStateFlow()

    init {
        // Automatically perform search when query changes with debounce
        _searchQuery
            .debounce(500)
            .distinctUntilChanged()
            .onEach {
                refreshSearch()
            }.launchIn(viewModelScope)
    }

    fun initForFileType(type: AssetFileType) {
        if (_fileType.value != type) {
            _fileType.value = type
            refreshSearch()
        } else if (_imagesState.value is AssetPickerUiState.Loading && type == AssetFileType.IMAGE) {
            refreshSearch()
        } else if (_videosState.value is AssetPickerUiState.Loading && type == AssetFileType.VIDEO) {
            refreshSearch()
        }
    }

    fun setTab(index: Int) {
        _selectedTab.value = index
    }

    fun setFileType(type: AssetFileType) {
        _fileType.value = type
        refreshSearch()
    }

    fun onQueryChanged(query: String) {
        _searchQuery.value = query
    }

    fun refreshSearch() {
        val query = _searchQuery.value
        val currentType = _fileType.value

        viewModelScope.launch {
            if (currentType == AssetFileType.IMAGE) {
                _imagesState.value = AssetPickerUiState.Loading
                repository
                    .searchImages(query = query, page = 1)
                    .onSuccess { res ->
                        _imagesState.value =
                            AssetPickerUiState.Success(
                                items = res.items,
                                page = res.page,
                                totalResults = res.totalResults,
                                hasNextPage = res.hasNextPage,
                            )
                    }.onFailure { err ->
                        _imagesState.value = AssetPickerUiState.Error(err.localizedMessage ?: "Search failed")
                    }
            } else {
                _videosState.value = AssetPickerUiState.Loading
                repository
                    .searchVideos(query = query, page = 1)
                    .onSuccess { res ->
                        _videosState.value =
                            AssetPickerUiState.Success(
                                items = res.items,
                                page = res.page,
                                totalResults = res.totalResults,
                                hasNextPage = res.hasNextPage,
                            )
                    }.onFailure { err ->
                        _videosState.value = AssetPickerUiState.Error(err.localizedMessage ?: "Search failed")
                    }
            }
        }
    }

    fun loadNextPage() {
        val query = _searchQuery.value
        val currentType = _fileType.value

        viewModelScope.launch {
            if (currentType == AssetFileType.IMAGE) {
                val current = _imagesState.value as? AssetPickerUiState.Success<AssetItem.Image> ?: return@launch
                if (!current.hasNextPage || current.isLoadingMore) return@launch

                _imagesState.value = current.copy(isLoadingMore = true)
                val nextPage = current.page + 1

                repository
                    .searchImages(query = query, page = nextPage)
                    .onSuccess { res ->
                        _imagesState.value =
                            AssetPickerUiState.Success(
                                items = current.items + res.items,
                                page = res.page,
                                totalResults = res.totalResults,
                                hasNextPage = res.hasNextPage,
                                isLoadingMore = false,
                            )
                    }.onFailure {
                        _imagesState.value = current.copy(isLoadingMore = false)
                    }
            } else {
                val current = _videosState.value as? AssetPickerUiState.Success<AssetItem.Video> ?: return@launch
                if (!current.hasNextPage || current.isLoadingMore) return@launch

                _videosState.value = current.copy(isLoadingMore = true)
                val nextPage = current.page + 1

                repository
                    .searchVideos(query = query, page = nextPage)
                    .onSuccess { res ->
                        _videosState.value =
                            AssetPickerUiState.Success(
                                items = current.items + res.items,
                                page = res.page,
                                totalResults = res.totalResults,
                                hasNextPage = res.hasNextPage,
                                isLoadingMore = false,
                            )
                    }.onFailure {
                        _videosState.value = current.copy(isLoadingMore = false)
                    }
            }
        }
    }

    fun downloadAndSelectAsset(
        item: AssetItem,
        onSelected: (String) -> Unit,
    ) {
        val cached = repository.getCachedUri(item.downloadUrl)
        if (cached != null) {
            onSelected(item.downloadUrl)
            return
        }

        viewModelScope.launch {
            repository
                .downloadAndCacheAsset(item.downloadUrl)
                .collect { progress ->
                    val map = _downloadingProgress.value.toMutableMap()
                    map[item.id] = progress
                    _downloadingProgress.value = map

                    if (progress == 100) {
                        onSelected(item.downloadUrl)
                    }
                }
        }
    }
}
