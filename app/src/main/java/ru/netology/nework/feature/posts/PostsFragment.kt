package ru.netology.nework.feature.posts

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
import ru.netology.nework.core.model.Post
import ru.netology.nework.core.session.TokenStore
import ru.netology.nework.databinding.FragmentPostsBinding
import javax.inject.Inject

@AndroidEntryPoint
class PostsFragment : Fragment(R.layout.fragment_posts) {

    @Inject
    lateinit var tokenStore: TokenStore

    private var _binding: FragmentPostsBinding? = null
    private val binding get() = _binding!!
    private val viewModel: PostsViewModel by viewModels()
    private lateinit var adapter: PostsAdapter

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        _binding = FragmentPostsBinding.bind(view)

        adapter = PostsAdapter(
            posts = emptyList(),
            onPostClick = { post ->
                val fragment = PostDetailsFragment()
                val arguments = Bundle()
                arguments.putString("author", post.author?.name ?: "без имени")
                arguments.putString("content", post.content)
                arguments.putLong("likes", post.likeOwnerIdsCount ?: 0)
                arguments.putString("published", post.publishedAt?.toString() ?: "")
                arguments.putString("link", post.link)
                arguments.putString("job", "В поиске работы")
                fragment.arguments = arguments

                parentFragmentManager.beginTransaction()
                    .replace(R.id.container, fragment)
                    .addToBackStack(null)
                    .commit()
            },
            onShareClick = { post ->
                sharePost(post)
            }
        )

        binding.postsList.layoutManager = LinearLayoutManager(requireContext())
        binding.postsList.adapter = adapter

        binding.postsLoginButton.setOnClickListener {
            val activity = requireActivity()
            if (activity is MainActivity) {
                activity.openLogin()
            }
        }

        binding.postsRegisterButton.setOnClickListener {
            val activity = requireActivity()
            if (activity is MainActivity) {
                activity.openRegister()
            }
        }

        binding.postsAddButton.setOnClickListener {
            val activity = requireActivity()
            if (activity is MainActivity) {
                activity.openEditPost()
            }
        }

        binding.postsRefresh.setColorSchemeResources(R.color.purple_500)
        binding.postsRefresh.setOnRefreshListener {
            viewModel.loadPosts()
        }

        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                tokenStore.tokenFlow().collect { token ->
                    binding.postsAuthBar.isVisible = token.isNullOrBlank()
                }
            }
        }

        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.posts.collect { posts ->
                    adapter.updatePosts(posts)
                    binding.postsList.isVisible = posts.isNotEmpty()
                }
            }
        }

        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.loading.collect { loading ->
                    binding.postsProgress.isVisible = loading && adapter.itemCount == 0
                    binding.postsRefresh.isRefreshing = loading && adapter.itemCount > 0
                }
            }
        }

        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.errorMessage.collect { message ->
                    if (message != null) {
                        binding.postsError.visibility = View.VISIBLE
                        binding.postsError.text = message
                    } else {
                        binding.postsError.visibility = View.GONE
                    }
                }
            }
        }
    }

    private fun sharePost(post: Post) {
        val author = post.author?.name ?: ""
        val text = buildString {
            if (author.isNotEmpty()) {
                append(author)
                append("\n")
            }
            append(post.content)
            if (!post.link.isNullOrBlank()) {
                append("\n")
                append(post.link)
            }
        }
        val intent = Intent(Intent.ACTION_SEND)
        intent.type = "text/plain"
        intent.putExtra(Intent.EXTRA_TEXT, text)
        startActivity(Intent.createChooser(intent, getString(R.string.btn_share)))
    }

    override fun onResume() {
        super.onResume()
        viewModel.loadPosts()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
