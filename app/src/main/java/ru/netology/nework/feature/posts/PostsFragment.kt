package ru.netology.nework.feature.posts

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

    private var myToken: String? = null

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        _binding = FragmentPostsBinding.bind(view)

        adapter = PostsAdapter(
            onPostClick = { post ->
                openPostDetails(post)
            },
            onShareClick = { post ->
                sharePost(post)
            },
            onMenuClick = { post ->
                showPostMenu(post)
            },
            onDeleteClick = { post ->
                deletePost(post)
            },
            onLikeClick = { post ->
                viewModel.likePost(post)
            }
        )

        binding.postsList.layoutManager = LinearLayoutManager(requireContext())
        binding.postsList.adapter = adapter

        binding.postsAddButton.setOnClickListener {
            if (myToken.isNullOrBlank()) {
                showAuthDialog()
            } else {
                (requireActivity() as MainActivity).openEditPost()
            }
        }

        binding.postsRefresh.setColorSchemeResources(R.color.purple_500)
        binding.postsRefresh.setOnRefreshListener {
            viewModel.loadPosts()
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
                viewModel.posts.collect { state ->
                    when (state) {
                        is UiState.Loading -> {
                            binding.postsProgress.isVisible = adapter.itemCount == 0
                            binding.postsRefresh.isRefreshing = adapter.itemCount > 0
                        }
                        is UiState.Success -> {
                            adapter.submitList(state.data)
                            binding.postsList.isVisible = state.data.isNotEmpty()
                            binding.postsProgress.isVisible = false
                            binding.postsRefresh.isRefreshing = false
                        }
                        is UiState.Error -> {
                            binding.postsError.visibility = View.VISIBLE
                            binding.postsError.text = state.message
                        }
                    }
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

    private fun showAuthDialog() {
        AlertDialog.Builder(requireContext())
            .setTitle("Нужно войти")
            .setMessage("чтобы создать пост, войдите или зарегистрируйтесь")
            .setPositiveButton("Войти") { _, _ ->
                (requireActivity() as MainActivity).openLogin()
            }
            .setNegativeButton("Регистрация") { _, _ ->
                (requireActivity() as MainActivity).openRegister()
            }
            .setNeutralButton("Отмена", null)
            .show()
    }

    private fun openPostDetails(post: Post) {
        val fragment = PostDetailsFragment()
        val arguments = Bundle()
        arguments.putString("postId", post.id)
        arguments.putString("authorName", post.authorName)
        arguments.putString("authorAvatarUrl", post.authorAvatarUrl)
        arguments.putString("authorJob", post.authorJob)
        arguments.putString("content", post.content)
        arguments.putLong("likes", post.likeOwnerIdsCount ?: 0)
        arguments.putLong("publishedAt", post.publishedAt ?: 0)
        arguments.putBoolean("likedByMe", post.likedByMe == true)
        arguments.putString("link", post.link)
        if (post.likeOwnerIds.isNotEmpty()) {
            arguments.putStringArrayList("likeOwnerIds", ArrayList(post.likeOwnerIds))
        }
        if (post.mentionedUserIds != null && post.mentionedUserIds.isNotEmpty()) {
            arguments.putStringArrayList("mentionedUserIds", ArrayList(post.mentionedUserIds))
        }
        if (post.attachment != null) {
            arguments.putString("attachmentType", post.attachment?.type?.name)
            arguments.putString("attachmentUrl", post.attachment?.url)
        }
        arguments.putString("authorJob", post.authorJob)
        if (post.mentionedUserIds != null && post.mentionedUserIds.isNotEmpty()) {
            arguments.putStringArrayList("mentionedUserIds", ArrayList(post.mentionedUserIds))
        }

        post.coords?.let { coords ->
            arguments.putDouble("lat", coords.lat)
            arguments.putDouble("lng", coords.lng)
        }
        fragment.arguments = arguments

        parentFragmentManager.beginTransaction()
            .replace(R.id.container, fragment)
            .addToBackStack(null)
            .commit()
    }

    private fun showPostMenu(post: Post) {
        val menuItems = arrayOf("Удалить")
        AlertDialog.Builder(requireContext())
            .setTitle("действия с постом")
            .setItems(menuItems) { _, _ ->
                deletePost(post)
            }
            .show()
    }
    // я не нашел эндпоинта доя редактирования, хотя по ТЗ функционал должен быть

    private fun deletePost(post: Post) {
        AlertDialog.Builder(requireContext())
            .setTitle("удалить пост")
            .setMessage("удалить этот пост?")
            .setPositiveButton("удалить") { _, _ ->
                viewModel.deletePost(post)
            }
            .setNegativeButton("отмена", null)
            .show()
    }

    private fun sharePost(post: Post) {
        val author = post.authorName ?: ""
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

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
//без бутылки не разобраться уже
