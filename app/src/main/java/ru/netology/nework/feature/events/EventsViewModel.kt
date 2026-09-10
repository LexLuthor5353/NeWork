package ru.netology.nework.feature.events

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import ru.netology.nework.core.common.UiState
import ru.netology.nework.core.model.Event
import ru.netology.nework.core.model.User
import ru.netology.nework.feature.users.UsersRepository
import javax.inject.Inject

@HiltViewModel
class EventsViewModel @Inject constructor(
    private val eventsRepository: EventsRepository,
    private val usersRepository: UsersRepository
) : ViewModel() {

    private val _events = MutableStateFlow<UiState<List<Event>>>(UiState.Loading)
    val events: StateFlow<UiState<List<Event>>> = _events.asStateFlow()

    private val _loading = MutableStateFlow(false)
    val loading = _loading.asStateFlow()

    private val _errorMessage = MutableStateFlow<String?>(null)
    val errorMessage = _errorMessage.asStateFlow()

    init {
        loadEvents()
    }

    fun loadEvents() {
        viewModelScope.launch {
            _loading.value = true
            _errorMessage.value = null
            try {
                val loadedEvents = eventsRepository.loadLatestEvents()
                _events.value = UiState.Success(loadedEvents)
            } catch (exception: Exception) {
                _errorMessage.value = exception.message ?: "не удалось загрузить события"
            }
            _loading.value = false
        }
    }

    fun likeEvent(event: Event) {
        viewModelScope.launch {
            event.id?.toLongOrNull()?.let { eventId ->
                try {
                    val currentLiked = event.likedByMe ?: false
                    if (currentLiked) {
                        eventsRepository.unlikeEvent(eventId)
                    } else {
                        eventsRepository.likeEvent(eventId)
                    }
                    val currentEvents = (_events.value as? UiState.Success)?.data?.toMutableList() ?: mutableListOf()
                    val index = currentEvents.indexOfFirst { it.id == event.id }
                    if (index >= 0) {
                        currentEvents[index] = event.copy(
                            likedByMe = !currentLiked,
                            likeOwnerIdsCount = (event.likeOwnerIdsCount ?: 0) + if (currentLiked) -1 else 1
                        )
                        _events.value = UiState.Success(currentEvents)
                    }
                    _events.value = UiState.Success(currentEvents)
                } catch (e: Exception) {
                    _errorMessage.value = "не удалось обновить лайк"
                }
            }
        }
    }

    fun deleteEvent(event: Event) {
        viewModelScope.launch {
            event.id?.toLongOrNull()?.let { eventId ->
                try {
                    eventsRepository.deleteEvent(eventId)
                    val currentEvents = (_events.value as? UiState.Success)?.data?.toMutableList() ?: mutableListOf()
                    currentEvents.removeAll { it.id == event.id }
                    _events.value = UiState.Success(currentEvents)
                } catch (e: Exception) {
                    _errorMessage.value = "не удалось удалить событие"
                }
            }
        }
    }

    suspend fun loadUser(userId: String): User {
        return usersRepository.loadUser(userId)
    }
}
