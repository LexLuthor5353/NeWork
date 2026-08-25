package ru.netology.nework.feature.posts

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import ru.netology.nework.databinding.FragmentPostDetailsBinding
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class PostDetailsFragment : Fragment() {

    private var _binding: FragmentPostDetailsBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentPostDetailsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val author = arguments?.getString("author") ?: "без имени"
        val content = arguments?.getString("content") ?: ""
        val likes = arguments?.getLong("likes") ?: 0
        val published = arguments?.getString("published")
        val link = arguments?.getString("link")
        val job = arguments?.getString("job") ?: "В поиске работы"

        binding.postDetailsAuthorName.text = author
        binding.postDetailsText.text = content
        binding.postDetailsLikeCount.text = likes.toString()
        binding.postDetailsJob.text = job

        if (published != null && published.isNotEmpty()) {
            try {
                val time = published.toLong()
                val format = SimpleDateFormat("dd.MM.yyyy HH:mm", Locale.getDefault())
                binding.postDetailsPublished.text = format.format(Date(time))
            } catch (e: Exception) {
                binding.postDetailsPublished.text = published
            }
        } else {
            binding.postDetailsPublished.text = ""
        }

        if (link != null && link.isNotEmpty()) {
            binding.postDetailsLink.visibility = View.VISIBLE
            binding.postDetailsLink.text = link
        } else {
            binding.postDetailsLink.visibility = View.GONE
        }

        binding.postDetailsAttachmentImage.visibility = View.GONE
        binding.postDetailsAttachmentLabel.visibility = View.GONE
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
