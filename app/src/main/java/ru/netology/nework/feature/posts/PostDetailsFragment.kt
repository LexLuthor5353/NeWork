package ru.netology.nework.feature.posts

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import ru.netology.nework.databinding.FragmentPostDetailsBinding

class PostDetailsFragment : Fragment() {

    private var _binding: FragmentPostDetailsBinding? = null
    private val binding get() = requireNotNull(_binding)

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
        binding.author.text = arguments?.getString("author") ?: "Без имени"
        binding.content.text = arguments?.getString("content") ?: ""
        val likes = arguments?.getLong("likes") ?: 0
        binding.likes.text = "Лайков: $likes"
    }

    override fun onDestroyView() {
        _binding = null
        super.onDestroyView()
    }

    companion object {
        fun newInstance(author: String, content: String, likes: Long): PostDetailsFragment {
            val fragment = PostDetailsFragment()
            fragment.arguments = Bundle().apply {
                putString("author", author)
                putString("content", content)
                putLong("likes", likes)
            }
            return fragment
        }
    }
}
