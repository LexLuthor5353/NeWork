package ru.netology.nework.feature.events

import android.app.AlertDialog
import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.recyclerview.widget.LinearLayoutManager
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import ru.netology.nework.MainActivity
import ru.netology.nework.R
import ru.netology.nework.core.common.UiState
import ru.netology.nework.core.model.Event
import ru.netology.nework.core.session.TokenStore
import ru.netology.nework.databinding.FragmentEventsBinding
import javax.inject.Inject

@AndroidEntryPoint
class EventsFragment : Fragment(R.layout.fragment_events) {

    @Inject
    lateinit var tokenStore: TokenStore

    private var _binding: FragmentEventsBinding? = null
    private val binding get() = _binding!!
    private val viewModel: EventsViewModel by viewModels()
    private lateinit var adapter: EventsAdapter

    private var myToken: String? = null

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        _binding = FragmentEventsBinding.bind(view)

        adapter = EventsAdapter(
            onEventClick = { event ->
                openEventDetails(event)
            },
            onShareClick = { event ->
                shareEvent(event)
            },
            onMenuClick = { event ->
                showEventMenu(event)
            },
            onDeleteClick = { event ->
                deleteEvent(event)
            },
            onLikeClick = { event ->
                viewModel.likeEvent(event)
            }
        )

        binding.eventsList.layoutManager = LinearLayoutManager(requireContext())
        binding.eventsList.adapter = adapter

        binding.eventsAddButton.setOnClickListener {
            if (myToken.isNullOrBlank()) {
                showAuthDialog()
            } else {
                parentFragmentManager.beginTransaction()
                    .replace(R.id.container, EditEventFragment())
                    .addToBackStack("edit")
                    .commit()
            }
        }

        binding.eventsRefresh.setColorSchemeResources(R.color.purple_500)
        binding.eventsRefresh.setOnRefreshListener {
            viewModel.loadEvents()
        }

        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                tokenStore.tokenFlow().collect { token ->
                    myToken = token
                }
            }
        }

        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                tokenStore.userIdFlow().collect { userId ->
                    adapter.myId = userId
                    adapter.notifyDataSetChanged()
                }
            }
        }

        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.events.collect { state ->
                    when (state) {
                        is UiState.Loading -> {
                            binding.eventsProgress.isVisible = adapter.itemCount == 0
                            binding.eventsRefresh.isRefreshing = adapter.itemCount > 0
                        }
                        is UiState.Success -> {
                            adapter.submitList(state.data)
                            binding.eventsList.isVisible = state.data.isNotEmpty()
                            binding.eventsProgress.isVisible = false
                            binding.eventsRefresh.isRefreshing = false
                        }
                        is UiState.Error -> {
                            binding.eventsError.visibility = View.VISIBLE
                            binding.eventsError.text = state.message
                        }
                    }
                }
            }
        }

        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.errorMessage.collect { message ->
                    if (message != null) {
                        binding.eventsError.visibility = View.VISIBLE
                        binding.eventsError.text = message
                    } else {
                        binding.eventsError.visibility = View.GONE
                    }
                }
            }
        }
    }

    private fun showAuthDialog() {
        AlertDialog.Builder(requireContext())
            .setTitle("Нужно войти")
            .setMessage("чтобы создать событие, войдите или зарегистрируйтесь")
            .setPositiveButton("Войти") { _, _ ->
                (requireActivity() as MainActivity).openLogin()
            }
            .setNegativeButton("Регистрация") { _, _ ->
                (requireActivity() as MainActivity).openRegister()
            }
            .setNeutralButton("Отмена", null)
            .show()
    }

    private fun openEventDetails(event: Event) {
        val fragment = EventDetailsFragment()
        val arguments = Bundle()
        arguments.putString("eventId", event.id)
        arguments.putString("authorName", event.authorName)
        arguments.putString("authorAvatarUrl", event.authorAvatarUrl)
        arguments.putString("authorJob", event.authorJob)
        arguments.putString("content", event.content)
        arguments.putLong("likes", event.likeOwnerIdsCount ?: 0)
        arguments.putLong("publishedAt", event.publishedAt ?: 0)
        arguments.putLong("eventAt", event.eventAt ?: 0)
        arguments.putString("type", event.typeFormatted)
        arguments.putString("link", event.link)
        arguments.putBoolean("likedByMe", event.likedByMe == true)
        if (event.speakerIds != null && event.speakerIds.isNotEmpty()) {
            arguments.putStringArrayList("speakerIds", ArrayList(event.speakerIds))
        }
        if (event.participantIds != null && event.participantIds.isNotEmpty()) {
            arguments.putStringArrayList("participantIds", ArrayList(event.participantIds))
        }
        if (event.attachment != null) {
            arguments.putString("attachmentType", event.attachment?.type?.name)
            arguments.putString("attachmentUrl", event.attachment?.url)
        }
        event.coords?.let { coords ->
            arguments.putDouble("lat", coords.lat)
            arguments.putDouble("lng", coords.lng)
        }
        fragment.arguments = arguments

        parentFragmentManager.beginTransaction()
            .replace(R.id.container, fragment)
            .addToBackStack(null)
            .commit()
    }

    private fun showEventMenu(event: Event) {
        val menuItems = arrayOf("Удалить")
        AlertDialog.Builder(requireContext())
            .setTitle("действия с событием")
            .setItems(menuItems) { _, _ ->
                deleteEvent(event)
            }
            .show()
    }

    //Не нашел энпоинт редактирования как жить дальше

    private fun deleteEvent(event: Event) {
        AlertDialog.Builder(requireContext())
            .setTitle("удалить событие")
            .setMessage("удалить это событие?")
            .setPositiveButton("удалить") { _, _ ->
                viewModel.deleteEvent(event)
            }
            .setNegativeButton("отмена", null)
            .show()
    }

    private fun shareEvent(event: Event) {
        val author = event.authorName ?: ""
        val text = buildString {
            if (author.isNotEmpty()) {
                append(author)
                append("\n")
            }
            append(event.content)
            if (!event.link.isNullOrBlank()) {
                append("\n")
                append(event.link)
            }
        }
        val intent = Intent(Intent.ACTION_SEND)
        intent.type = "text/plain"
        intent.putExtra(Intent.EXTRA_TEXT, text)
        startActivity(Intent.createChooser(intent, getString(R.string.btn_share)))
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}

