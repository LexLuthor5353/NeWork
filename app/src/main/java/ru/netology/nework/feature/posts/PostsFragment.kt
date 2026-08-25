package ru.netology.nework.feature.posts

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import ru.netology.nework.R
import ru.netology.nework.core.model.Post
import ru.netology.nework.core.model.User
import ru.netology.nework.databinding.FragmentPostsBinding

class PostsFragment : Fragment() {

    private var _binding: FragmentPostsBinding? = null
    private val binding get() = _binding!!

    private lateinit var adapter: PostsAdapter

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentPostsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        adapter = PostsAdapter(getTestPosts()) { post ->
            val fragment = PostDetailsFragment()
            val args = Bundle()
            args.putString("author", post.author?.name ?: "без имени")
            args.putString("content", post.content)
            args.putLong("likes", post.likeOwnerIdsCount ?: 0)
            args.putString("published", post.publishedAt?.toString() ?: "")
            args.putString("link", post.link)
            args.putString("job", "В поиске работы")
            fragment.arguments = args

            parentFragmentManager.beginTransaction()
                .replace(R.id.container, fragment)
                .addToBackStack(null)
                .commit()
        }

        binding.postsList.layoutManager = LinearLayoutManager(requireContext())
        binding.postsList.adapter = adapter

        binding.postsAddButton.setOnClickListener {
            parentFragmentManager.beginTransaction()
                .replace(R.id.container, EditPostFragment())
                .addToBackStack(null)
                .commit()
        }
    }

    private fun getTestPosts(): List<Post> {
        val now = System.currentTimeMillis()
        val list = ArrayList<Post>()
        list.add(
            Post(
                id = "1",
                author = User("1", "ivan", "Иван", null),
                publishedAt = now,
                content = "Привет это мой первый пост",
                link = null,
                attachment = null,
                coords = null,
                mentionedUserIds = emptyList(),
                likeOwnerIdsCount = 2,
                likedByMe = false
            )
        )
        list.add(
            Post(
                id = "2",
                author = User("2", "anna", "Анна", null),
                publishedAt = now - 1000 * 60 * 60,
                content = "Сегодня хороший день для учебы",
                link = "https://netology.ru",
                attachment = null,
                coords = null,
                mentionedUserIds = emptyList(),
                likeOwnerIdsCount = 5,
                likedByMe = true
            )
        )
        list.add(
            Post(
                id = "3",
                author = User("3", "petya", "Петя", null),
                publishedAt = now - 1000 * 60 * 60 * 24,
                content = "Делаю дипломный проект NeWork",
                link = null,
                attachment = null,
                coords = null,
                mentionedUserIds = emptyList(),
                likeOwnerIdsCount = 0,
                likedByMe = false
            )
        )
        return list
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
