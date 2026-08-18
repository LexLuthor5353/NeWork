package ru.netology.nework.core.common

sealed interface AppError {
    data object Unauthorized : AppError
    data object Forbidden : AppError
    data object BadRequest : AppError
    data object NotFound : AppError
    data class Server(val message: String?) : AppError
    data object Network : AppError
    data object Unknown : AppError
}

