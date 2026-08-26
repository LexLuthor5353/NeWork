package ru.netology.nework.feature.events

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import ru.netology.nework.databinding.FragmentEventDetailsBinding
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class EventDetailsFragment : Fragment() {

    private var _binding: FragmentEventDetailsBinding? = null
    private val binding get() = _binding!!

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

        val author = arguments?.getString("author")
        if (author == null) {
            binding.eventDetailsAuthorName.text = "без имени"
        } else {
            binding.eventDetailsAuthorName.text = author
        }
        binding.eventDetailsText.text = arguments?.getString("content") ?: ""
        binding.eventDetailsLikeCount.text = (arguments?.getLong("likes") ?: 0).toString()
        binding.eventDetailsType.text = arguments?.getString("type") ?: "Online"
        binding.eventDetailsJob.text = arguments?.getString("job") ?: "В поиске работы"

        val published = arguments?.getString("published")
        if (published != null && published.length > 0) {
            try {
                val dateFormat = SimpleDateFormat("dd.MM.yyyy HH:mm", Locale.getDefault())
                binding.eventDetailsPublished.text = dateFormat.format(Date(published.toLong()))
            } catch (exception: Exception) {
                binding.eventDetailsPublished.text = published
            }
        } else {
            binding.eventDetailsPublished.text = ""
        }

        val eventAt = arguments?.getString("eventAt")
        if (eventAt != null && eventAt != "") {
            try {
                val dateFormat = SimpleDateFormat("dd.MM.yyyy HH:mm", Locale.getDefault())
                binding.eventDetailsDate.text = dateFormat.format(Date(eventAt.toLong()))
            } catch (exception: Exception) {
                binding.eventDetailsDate.text = eventAt
            }
        } else {
            binding.eventDetailsDate.text = ""
        }

        val link = arguments?.getString("link")
        if (link != null && link.isNotEmpty()) {
            binding.eventDetailsLink.visibility = View.VISIBLE
            binding.eventDetailsLink.text = link
        } else {
            binding.eventDetailsLink.visibility = View.GONE
        }

        binding.eventDetailsAttachmentImage.visibility = View.GONE
        binding.eventDetailsAttachmentLabel.visibility = View.GONE
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
