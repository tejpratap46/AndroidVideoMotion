package com.tejpratapsingh.motionstore.extensions

import android.content.Context
import com.tejpratapsingh.motionlib.core.extensions.md5
import com.tejpratapsingh.motionstore.tables.MotionProject
import java.io.File

const val PROJECTS_DIR = "projects"

/**
 * Creates and returns a directory for a specific [com.tejpratapsingh.motionstore.tables.MotionProject] within the internal app storage.
 *
 * This function ensures that the base projects directory exists and then creates a
 * subdirectory named after the unique ID of the provided [motionProject].
 *
 * @param motionProject The project for which the folder should be created.
 * @return A [File] object representing the project-specific directory.
 */
fun Context.createProjectFile(motionProject: MotionProject): File {
    val projectsDirectory: File = applicationContext.filesDir.resolve(PROJECTS_DIR)
    if (!projectsDirectory.exists()) {
        projectsDirectory.mkdirs()
    }
    val fileDirectory: File = projectsDirectory.resolve(motionProject.id)
    if (!fileDirectory.exists()) {
        fileDirectory.mkdirs()
    }

    return File(
        fileDirectory, // Use cacheDir for temp files that can be cleared
        "video_out.mp4",
    )
}
