package com.tejpratapsingh.lyricsmaker.presentation.compose.navigation

sealed class Screen(
    val route: String,
) {
    object Projects : Screen("projects")

    object Search : Screen("search")

    object Lyrics : Screen("lyrics")

    object TemplateSelector : Screen("template_selector/{projectId}") {
        fun createRoute(projectId: String) = "template_selector/$projectId"
    }

    object ProjectDetails : Screen("project_details/{projectId}") {
        fun createRoute(projectId: String) = "project_details/$projectId"
    }

    object VideoEditor : Screen("video_editor/{projectId}") {
        fun createRoute(projectId: String) = "video_editor/$projectId"
    }

    object AssetDownload : Screen("asset_download/{projectId}") {
        fun createRoute(projectId: String) = "asset_download/$projectId"
    }

    object Settings : Screen("settings")

    object ManageCache : Screen("manage_cache")
}
