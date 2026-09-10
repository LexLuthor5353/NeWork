package ru.netology.nework.feature.posts

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
import ru.netology.nework.databinding.FragmentPostDetailsBinding
import ru.netology.nework.feature.users.UsersAdapter
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.Locale

@AndroidEntryPoint
class PostDetailsFragment : Fragment() {

    private var _binding: FragmentPostDetailsBinding? = null
    private val binding get() = _binding!!
    private val viewModel: PostsViewModel by viewModels()

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

        val authorName = arguments?.getString("authorName") ?: "без имени"
        val content = arguments?.getString("content") ?: ""
        val likes = arguments?.getLong("likes") ?: 0
        val publishedAt = arguments?.getLong("publishedAt") ?: 0
        val link = arguments?.getString("link")
        val job = arguments?.getString("job") ?: "В поиске работы"

        binding.postDetailsAuthorName.text = authorName
        binding.postDetailsText.text = content
        binding.postDetailsLikeCount.text = likes.toString()
        binding.postDetailsJob.text = job

        if (publishedAt > 0) {
            val formatter = DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm", Locale.getDefault())
            binding.postDetailsPublished.text = formatter.format(
                Instant.ofEpochMilli(publishedAt).atZone(ZoneId.systemDefault()).toLocalDateTime()
            )
        } else {
            binding.postDetailsPublished.text = ""
        }

        if (link.isNullOrBlank()) {
            binding.postDetailsLink.visibility = View.GONE
        } else {
            binding.postDetailsLink.visibility = View.VISIBLE
            binding.postDetailsLink.text = link
        }

        binding.postDetailsAttachmentImage.visibility = View.GONE
        binding.postDetailsAttachmentLabel.visibility = View.GONE

        val likeOwnerIds = arguments?.getStringArrayList("likeOwnerIds")
        if (likeOwnerIds != null && likeOwnerIds.isNotEmpty()) {
            binding.postDetailsLikedByTitle.visibility = View.VISIBLE
            binding.postDetailsLikedByList.visibility = View.VISIBLE
            loadLikedByUsers(likeOwnerIds)
        } else {
            binding.postDetailsLikedByTitle.visibility = View.GONE
            binding.postDetailsLikedByList.visibility = View.GONE
        }
    }

    private fun loadLikedByUsers(likeOwnerIds: List<String>) {
        viewLifecycleOwner.lifecycleScope.launch {
            val likedByUsers = mutableListOf<User>()
            for (userId in likeOwnerIds) {
                try {
                    val user = viewModel.loadUser(userId)
                    likedByUsers.add(user)
                } catch (e: Exception) {
                }
            }
            val adapter = UsersAdapter(
                users = likedByUsers,
                onUserClick = {}
            )
            binding.postDetailsLikedByList.layoutManager = LinearLayoutManager(requireContext())
            binding.postDetailsLikedByList.adapter = adapter
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
