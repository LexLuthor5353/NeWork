package ru.netology.nework.core.navigation

sealed interface Route {
    data object Posts : Route
    data object Events : Route
    data object Users : Route
    data class UserProfile(val userId: String) : Route
    data class PostDetails(val postId: String) : Route
    data class EventDetails(val eventId: String) : Route
    data object Login : Route
    data object Register : Route
    data object MyProfile : Route
}

