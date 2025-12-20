package com.tejpratapsingh.motion.ongoing.domain.mapper

import com.tejpratapsingh.motion.datastore.data.entity.ProjectEntity
import com.tejpratapsingh.motion.ongoing.domain.CurrentProject

fun CurrentProject.toProjectEntity() = ProjectEntity(
    title = this.title,
    description = this.description,
    image = this.image,
    siteName = this.siteName,
    twitterCard = this.twitterCard,
    url = this.url,
    trackName = this.trackName,
    artistName = this.artistName,
    albumName = this.albumName,
    duration = this.duration,
    instrumental = this.instrumental,
    plainLyrics = this.plainLyrics,
    syncedLyrics = this.syncedLyrics,
    savedFilePath = this.savedFilePath,
    selectedLyrics = this.selectedLyrics,
    totalLyrics = this.totalLyrics
)

fun ProjectEntity.toCurrentProject() = CurrentProject(
    title = this.title,
    description = this.description,
    image = this.image,
    siteName = this.siteName,
    twitterCard = this.twitterCard,
    url = this.url,
    trackName = this.trackName,
    artistName = this.artistName,
    albumName = this.albumName,
    duration = this.duration,
    instrumental = this.instrumental,
    plainLyrics = this.plainLyrics,
    syncedLyrics = this.syncedLyrics,
    savedFilePath = this.savedFilePath,
    selectedLyrics = this.selectedLyrics,
    totalLyrics = this.totalLyrics
)