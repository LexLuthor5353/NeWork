package ru.netology.nework.feature.events

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import ru.netology.nework.R
import ru.netology.nework.core.model.Event
import ru.netology.nework.core.model.EventType
import ru.netology.nework.core.model.User
import ru.netology.nework.databinding.FragmentEventsBinding

class EventsFragment : Fragment() {

    private var _binding: FragmentEventsBinding? = null
    private val binding get() = _binding!!

    lateinit var adapter: EventsAdapter

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentEventsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val data = getTestEvents()
        adapter = EventsAdapter(data) { event ->
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
        }

        binding.eventsList.layoutManager = LinearLayoutManager(requireContext())
        binding.eventsList.adapter = adapter

        binding.eventsAddButton.setOnClickListener {
            parentFragmentManager.beginTransaction()
                .replace(R.id.container, EditEventFragment())
                .addToBackStack("edit")
                .commit()
        }
    }

    fun getTestEvents(): List<Event> {
        val currentTime = System.currentTimeMillis()
        val events = ArrayList<Event>()
        events.add(
            Event(
                "1",
                User("1", "ivan", "Иван", null),
                currentTime,
                currentTime + 1000 * 60 * 60 * 24,
                EventType.ONLINE,
                "Митап по android",
                null,
                null,
                null,
                emptyList(),
                emptyList(),
                4,
                false
            )
        )
        events.add(
            Event(
                "2",
                User("2", "anna", "Анна", null),
                currentTime - 50000,
                currentTime + 1000 * 60 * 60 * 48,
                EventType.OFFLINE,
                "Встреча в офисе",
                "https://netology.ru",
                null,
                null,
                emptyList(),
                emptyList(),
                10,
                true
            )
        )
        return events
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
