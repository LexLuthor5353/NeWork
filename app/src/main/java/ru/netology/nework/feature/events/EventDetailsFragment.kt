package ru.netology.nework.feature.events

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import ru.netology.nework.R
import ru.netology.nework.core.model.User
import ru.netology.nework.databinding.FragmentEventDetailsBinding
import ru.netology.nework.feature.users.UsersAdapter
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.Locale

@AndroidEntryPoint
class EventDetailsFragment : Fragment() {

    private var _binding: FragmentEventDetailsBinding? = null
    private val binding get() = _binding!!
    private val viewModel: EventsViewModel by viewModels()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentEventDetailsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val author = arguments?.getString("author") ?: "без имени"
        val content = arguments?.getString("content") ?: ""
        val likes = arguments?.getLong("likes") ?: 0
        val publishedAt = arguments?.getLong("publishedAt") ?: 0
        val eventAt = arguments?.getLong("eventAt") ?: 0
        val type = arguments?.getString("type") ?: "Online"
        val link = arguments?.getString("link")
        val job = arguments?.getString("job") ?: "В поиске работы"
        val speakerIds = arguments?.getStringArrayList("speakerIds")

        binding.eventDetailsAuthorName.text = author
        binding.eventDetailsText.text = content
        binding.eventDetailsLikeCount.text = likes.toString()
        binding.eventDetailsType.text = type
        binding.eventDetailsJob.text = job

        val formatter = DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm", Locale.getDefault())

        if (publishedAt > 0) {
            binding.eventDetailsPublished.text = formatter.format(
                Instant.ofEpochMilli(publishedAt).atZone(ZoneId.systemDefault()).toLocalDateTime()
            )
        } else {
            binding.eventDetailsPublished.text = ""
        }

        if (eventAt > 0) {
            binding.eventDetailsDate.text = formatter.format(
                Instant.ofEpochMilli(eventAt).atZone(ZoneId.systemDefault()).toLocalDateTime()
            )
        } else {
            binding.eventDetailsDate.text = ""
        }

        if (link.isNullOrBlank()) {
            binding.eventDetailsLink.visibility = View.GONE
        } else {
            binding.eventDetailsLink.visibility = View.VISIBLE
            binding.eventDetailsLink.text = link
        }

        binding.eventDetailsAttachmentImage.visibility = View.GONE
        binding.eventDetailsAttachmentLabel.visibility = View.GONE

        if (speakerIds != null && speakerIds.isNotEmpty()) {
            binding.eventDetailsSpeakersTitle.visibility = View.VISIBLE
            binding.eventDetailsSpeakersList.visibility = View.VISIBLE
            loadSpeakers(speakerIds)
        } else {
            binding.eventDetailsSpeakersTitle.visibility = View.GONE
            binding.eventDetailsSpeakersList.visibility = View.GONE
        }
    }

    private fun loadSpeakers(speakerIds: List<String>) {
        viewLifecycleOwner.lifecycleScope.launch {
            val speakers = mutableListOf<User>()
            for (speakerId in speakerIds) {
                try {
                    val user = viewModel.loadUser(speakerId)
                    speakers.add(user)
                } catch (e: Exception) {
                }
            }
            val adapter = UsersAdapter(
                users = speakers,
                onUserClick = {}
            )
            binding.eventDetailsSpeakersList.layoutManager = LinearLayoutManager(requireContext())
            binding.eventDetailsSpeakersList.adapter = adapter
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
