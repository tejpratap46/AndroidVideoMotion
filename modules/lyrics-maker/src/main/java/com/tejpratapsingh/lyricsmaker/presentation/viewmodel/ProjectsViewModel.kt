package com.tejpratapsingh.lyricsmaker.presentation.viewmodel

import timber.log.Timber
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.tejpratapsingh.motionstore.dao.MotionProjectDao
import com.tejpratapsingh.motionstore.infra.PreferenceManager
import com.tejpratapsingh.motionstore.tables.MotionProject
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class ProjectsViewModel(
    private val motionProject: MotionProjectDao,
    private val preferenceManager: PreferenceManager,
) : ViewModel() {
    private val _projects = MutableStateFlow<List<MotionProject>>(emptyList())
    val projects: StateFlow<List<MotionProject>> = _projects.asStateFlow()

    private val _sortOrder = MutableStateFlow(preferenceManager.projectSortOrder)
    val sortOrder: StateFlow<String> = _sortOrder.asStateFlow()

    private val _isRefreshing = MutableStateFlow(false)
    val isRefreshing: StateFlow<Boolean> = _isRefreshing.asStateFlow()

    private val _shareEvent = MutableSharedFlow<MotionProject>()
    val shareEvent: SharedFlow<MotionProject> = _shareEvent.asSharedFlow()

    private val _syncEvent = MutableSharedFlow<MotionProject>()
    val syncEvent: SharedFlow<MotionProject> = _syncEvent.asSharedFlow()

    init {
        loadProjects()
    }

    fun loadProjects() {
        viewModelScope.launch(Dispatchers.IO) {
            _projects.value = motionProject.findAll("${sortOrder.value} DESC")
        }
    }

    fun updateSortOrder(newSortOrder: String) {
        preferenceManager.projectSortOrder = newSortOrder
        _sortOrder.value = newSortOrder
        loadProjects()
    }

    fun refresh() {
        viewModelScope.launch {
            _isRefreshing.value = true
            // re-fetch your projects here
            loadProjects()
            _isRefreshing.value = false
        }
    }

    fun deleteProject(project: MotionProject) {
        viewModelScope.launch {
            motionProject.deleteById(project.id)
            loadProjects()
        }
    }

    fun shareProject(project: MotionProject) {
        viewModelScope.launch {
            _shareEvent.emit(project)
        }
    }

    fun syncProject(project: MotionProject) {
        viewModelScope.launch {
            Timber.d("syncProject: Called")
            _syncEvent.emit(project)
        }
    }

}

class ProjectsViewModelFactory(
    private val motionProject: MotionProjectDao,
    private val preferenceManager: PreferenceManager,
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(ProjectsViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return ProjectsViewModel(motionProject, preferenceManager) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
