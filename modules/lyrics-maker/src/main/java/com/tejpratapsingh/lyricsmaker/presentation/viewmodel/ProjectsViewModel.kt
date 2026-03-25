package com.tejpratapsingh.lyricsmaker.presentation.viewmodel

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.tejpratapsingh.motionstore.dao.MotionProjectDao
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
) : ViewModel() {
    private val _projects = MutableStateFlow<List<MotionProject>>(emptyList())
    val projects: StateFlow<List<MotionProject>> = _projects.asStateFlow()

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
            _projects.value = motionProject.findAll()
        }
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
            Log.d(TAG, "syncProject: Called")
            _syncEvent.emit(project)
        }
    }

    companion object {
        private const val TAG = "ProjectsViewModel"
    }
}

class ProjectsViewModelFactory(
    private val motionProject: MotionProjectDao,
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(ProjectsViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return ProjectsViewModel(motionProject) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
