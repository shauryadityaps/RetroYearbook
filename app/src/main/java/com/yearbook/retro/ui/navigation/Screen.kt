package com.yearbook.retro.ui.navigation

sealed class Screen(val route: String) {
    object Login : Screen("login")
    object Dashboard : Screen("dashboard")
    object Library : Screen("library")
    object AddJoin : Screen("add_join")

    object InsideYearbook : Screen("inside_yearbook/{yearbookId}") {
        fun createRoute(yearbookId: String) = "inside_yearbook/$yearbookId"
    }

    object Slideshow : Screen("slideshow/{yearbookId}") {
        fun createRoute(yearbookId: String) = "slideshow/$yearbookId"
    }

    object VideoExport : Screen("video_export/{yearbookId}") {
        fun createRoute(yearbookId: String) = "video_export/$yearbookId"
    }
}
