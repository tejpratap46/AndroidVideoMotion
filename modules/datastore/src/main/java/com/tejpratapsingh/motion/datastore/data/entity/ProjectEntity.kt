package com.tejpratapsingh.motion.datastore.data.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "projects")
data class ProjectEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0L,
    val title: String?,
    val description: String?,
    val image: String?,
    val siteName: String?,
    val twitterCard: String?,
    val url: String?,
    val trackName: String?,
    val artistName: String?,
    val albumName: String?,
    val duration: Float?,
    val instrumental: Boolean?,
    val plainLyrics: String?,
    val syncedLyrics: String?,
    val totalLyrics: String?,
    val selectedLyrics: String?,
    val savedFilePath: String?,
)