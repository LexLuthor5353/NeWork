package ru.netology.nework.feature.events

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
import ru.netology.nework.R
import ru.netology.nework.core.model.Event
import ru.netology.nework.core.model.EventType
import ru.netology.nework.databinding.FragmentEventsBinding

@AndroidEntryPoint
class EventsFragment : Fragment(R.layout.fragment_events) {

    private var _binding: FragmentEventsBinding? = null
    private val binding get() = _binding!!
    private val viewModel: EventsViewModel by viewModels()
    private lateinit var adapter: EventsAdapter

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        _binding = FragmentEventsBinding.bind(view)

        adapter = EventsAdapter(
            events = emptyList(),
            onEventClick = { event ->
            val fragment = EventDetailsFragment()
            val arguments = Bundle()
            arguments.putString("author", event.author?.name ?: "без имени")
            arguments.putString("content", event.content)
            arguments.putLong("likes", event.likeOwnerIdsCount ?: 0)
            if (event.publishedAt != null) {
                arguments.putString("published", event.publishedAt.toString())
            } else {
                arguments.putString("published", "")
            }
            if (event.eventAt != null) {
                arguments.putString("eventAt", event.eventAt.toString())
            }
            if (event.type == EventType.ONLINE) {
                arguments.putString("type", "Online")
            } else {
                arguments.putString("type", "Offline")
            }
            arguments.putString("link", event.link)
            arguments.putString("job", "В поиске работы")
            fragment.arguments = arguments

            parentFragmentManager.beginTransaction()
                .replace(R.id.container, fragment)
                .addToBackStack(null)
                .commit()
            },
            onShareClick = { event ->
                shareEvent(event)
            }
        )

        binding.eventsList.layoutManager = LinearLayoutManager(requireContext())
        binding.eventsList.adapter = adapter

        binding.eventsAddButton.setOnClickListener {
            parentFragmentManager.beginTransaction()
                .replace(R.id.container, EditEventFragment())
                .addToBackStack("edit")
                .commit()
        }

        binding.eventsRefresh.setColorSchemeResources(R.color.purple_500)
        binding.eventsRefresh.setOnRefreshListener {
            viewModel.loadEvents()
        }

        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.events.collect { events ->
                    adapter.updateEvents(events)
                    binding.eventsList.isVisible = events.isNotEmpty()
                }
            }
        }

        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.loading.collect { loading ->
                    binding.eventsProgress.isVisible = loading && adapter.itemCount == 0
                    binding.eventsRefresh.isRefreshing = loading && adapter.itemCount > 0
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

    private fun shareEvent(event: Event) {
        val author = event.author?.name ?: ""
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
