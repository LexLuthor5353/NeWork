package ru.netology.nework.feature.posts

import android.content.Intent
import android.content.res.ColorStateList
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import android.widget.FrameLayout
import com.yandex.mapkit.geometry.Point
import com.yandex.mapkit.map.CameraPosition
import com.yandex.mapkit.mapview.MapView
import androidx.core.content.ContextCompat
import androidx.core.view.MenuProvider
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.bumptech.glide.Glide
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.launch
import ru.netology.nework.R
import ru.netology.nework.core.model.AttachmentType
import ru.netology.nework.core.model.User
import ru.netology.nework.core.session.TokenStore
import ru.netology.nework.databinding.FragmentPostDetailsBinding
import ru.netology.nework.feature.users.HorizontalUsersAdapter
import ru.netology.nework.feature.users.LikersFragment
import ru.netology.nework.feature.users.UsersViewModel
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.Locale
import javax.inject.Inject

@AndroidEntryPoint
class PostDetailsFragment : Fragment() {

    @Inject
    lateinit var tokenStore: TokenStore

    private var _binding: FragmentPostDetailsBinding? = null
    private val binding get() = _binding!!
    private val viewModel: PostsViewModel by viewModels()
    private val usersViewModel: UsersViewModel by viewModels()

    private var mapView: MapView? = null

    private var myUserId: String? = null

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

        val postId = arguments?.getString("postId")
        val authorName = arguments?.getString("authorName") ?: "без имени"
        val authorAvatarUrl = arguments?.getString("authorAvatarUrl")
        val authorJob = arguments?.getString("authorJob")
        val content = arguments?.getString("content") ?: ""
        var currentLikes = arguments?.getLong("likes") ?: 0
        val publishedAt = arguments?.getLong("publishedAt") ?: 0
        val link = arguments?.getString("link")
        var isLiked = arguments?.getBoolean("likedByMe", false) ?: false

        val likeOwnerIds = arguments?.getStringArrayList("likeOwnerIds")?.toMutableList() ?: mutableListOf()
        val mentionedUserIds = arguments?.getStringArrayList("mentionedUserIds") ?: arrayListOf()

        lifecycleScope.launch {
            tokenStore.userIdFlow().collect { id ->
                myUserId = id
            }
        }

        requireActivity().addMenuProvider(object : MenuProvider {
            override fun onCreateMenu(menu: Menu, menuInflater: MenuInflater) {
                menuInflater.inflate(R.menu.menu_share, menu)
            }

            override fun onMenuItemSelected(menuItem: MenuItem): Boolean {
                if (menuItem.itemId == R.id.actionShare) {
                    sharePost(authorName, content, link)
                    return true
                }
                return false
            }
        }, viewLifecycleOwner, Lifecycle.State.RESUMED)

        binding.postDetailsAuthorName.text = authorName

        if (authorJob.isNullOrBlank()) {
            binding.postDetailsAuthorJob.text = "В поиске работы"
        } else {
            binding.postDetailsAuthorJob.text = authorJob
        }

        binding.postDetailsText.text = content
        binding.postDetailsLikeCount.text = currentLikes.toString()

        updateLikeIcon(isLiked)

        binding.postDetailsLikeIcon.setOnClickListener {
            postId?.let { id ->
                if (isLiked) {
                    currentLikes -= 1
                    binding.postDetailsLikeCount.text = currentLikes.toString()
                    myUserId?.let { likeOwnerIds.remove(it) }
                    viewModel.unlikePostById(id)
                } else {
                    currentLikes += 1
                    binding.postDetailsLikeCount.text = currentLikes.toString()
                    myUserId?.let { likeOwnerIds.add(it) }
                    viewModel.likePostById(id)
                }
                isLiked = !isLiked
                updateLikeIcon(isLiked)
                showLikedAvatarsFromIds(likeOwnerIds)
            }
        }

        if (authorAvatarUrl.isNullOrBlank()) {
            binding.postDetailsAvatar.setImageResource(R.drawable.bg_avatar)
        } else {
            Glide.with(this)
                .load(authorAvatarUrl)
                .placeholder(R.drawable.bg_avatar)
                .circleCrop()
                .into(binding.postDetailsAvatar)
        }

        if (publishedAt > 0) {
            val formatter = DateTimeFormatter.ofPattern("dd.MM.yy HH:mm", Locale.getDefault())
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
            binding.postDetailsLink.setOnClickListener {
                try {
                    val intent = Intent(Intent.ACTION_VIEW, Uri.parse(link))
                    startActivity(intent)
                } catch (exception: Exception) {
                    Toast.makeText(
                        requireContext(),
                        "не удалось открыть ссылку",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }
        }

        val attachmentType = arguments?.getString("attachmentType")
        val attachmentUrl = arguments?.getString("attachmentUrl")

        if (attachmentType != null && attachmentUrl != null && attachmentUrl.isNotEmpty()) {
            binding.postDetailsAttachmentImage.visibility = View.VISIBLE
            binding.postDetailsAttachmentLabel.visibility = View.VISIBLE

            when (AttachmentType.valueOf(attachmentType)) {
                AttachmentType.IMAGE -> {
                    Glide.with(this)
                        .load(attachmentUrl)
                        .centerCrop()
                        .into(binding.postDetailsAttachmentImage)
                    binding.postDetailsAttachmentLabel.visibility = View.GONE
                }

                AttachmentType.VIDEO -> {
                    Glide.with(this)
                        .load(attachmentUrl)
                        .centerCrop()
                        .into(binding.postDetailsAttachmentImage)
                    binding.postDetailsAttachmentLabel.text = "Видео"
                }

                AttachmentType.AUDIO -> {
                    binding.postDetailsAttachmentImage.visibility = View.GONE
                    binding.postDetailsAttachmentLabel.text = "Аудио"
                }
            }
        } else {
            binding.postDetailsAttachmentImage.visibility = View.GONE
            binding.postDetailsAttachmentLabel.visibility = View.GONE
        }

        binding.postDetailsLikesAvatars.layoutManager = LinearLayoutManager(
            requireContext(),
            LinearLayoutManager.HORIZONTAL,
            false
        )
        binding.postDetailsLikesAvatars.adapter = HorizontalUsersAdapter(emptyList())

        binding.postDetailsMentionedAvatars.layoutManager = LinearLayoutManager(
            requireContext(),
            LinearLayoutManager.HORIZONTAL,
            false
        )
        binding.postDetailsMentionedAvatars.adapter = HorizontalUsersAdapter(emptyList())

        if (likeOwnerIds.isNotEmpty()) {
            binding.postDetailsLikesTitle.visibility = View.VISIBLE
            binding.postDetailsLikesRow.visibility = View.VISIBLE
            showLikedAvatarsFromIds(likeOwnerIds)
        } else {
            binding.postDetailsLikesTitle.visibility = View.GONE
            binding.postDetailsLikesRow.visibility = View.GONE
        }

        if (mentionedUserIds.isNotEmpty()) {
            binding.postDetailsMentionedTitle.visibility = View.VISIBLE
            binding.postDetailsMentionedRow.visibility = View.VISIBLE
            loadMentionedUsers(mentionedUserIds)
        } else {
            binding.postDetailsMentionedTitle.visibility = View.GONE
            binding.postDetailsMentionedRow.visibility = View.GONE
        }

        val lat = arguments?.getDouble("lat")
        val lng = arguments?.getDouble("lng")
        if (lat != null && lng != null && lat != 0.0 && lng != 0.0) {
            binding.postDetailsMapTitle.visibility = View.VISIBLE
            binding.postDetailsMapContainer.visibility = View.VISIBLE
            setupMap(lat, lng)
        } else {
            binding.postDetailsMapTitle.visibility = View.GONE
            binding.postDetailsMapContainer.visibility = View.GONE
        }
    }

    private fun setupMap(lat: Double, lng: Double) {
        if (mapView == null) {
            mapView = MapView(requireContext())
            val params = FrameLayout.LayoutParams(
                FrameLayout.LayoutParams.MATCH_PARENT,
                FrameLayout.LayoutParams.MATCH_PARENT
            )
            binding.postDetailsMapContainer.addView(mapView, params)
        }

        val point = Point(lat, lng)
        val camera = CameraPosition(point, 15.0f, 0.0f, 0.0f)
        mapView?.map?.move(camera)
        mapView?.map?.mapObjects?.clear()
        mapView?.map?.mapObjects?.addPlacemark(point)

        binding.postDetailsMapStub.visibility = View.GONE
    }

    override fun onStart() {
        super.onStart()
        mapView?.onStart()
    }

    override fun onStop() {
        super.onStop()
        mapView?.onStop()
    }

    private fun updateLikeIcon(isLiked: Boolean) {
        if (isLiked) {
            binding.postDetailsLikeIcon.setImageResource(R.drawable.ic_like_filled_24)
            binding.postDetailsLikeIcon.imageTintList = ColorStateList.valueOf(
                ContextCompat.getColor(requireContext(), R.color.red)
            )
        } else {
            binding.postDetailsLikeIcon.setImageResource(R.drawable.ic_like_outlined_24)
            binding.postDetailsLikeIcon.imageTintList = ColorStateList.valueOf(
                ContextCompat.getColor(requireContext(), R.color.gray)
            )
        }
    }

    private fun showLikedAvatarsFromIds(ids: List<String>) {
        viewLifecycleOwner.lifecycleScope.launch {
            val users = mutableListOf<User>()
            for (id in ids) {
                try {
                    users.add(usersViewModel.loadUser(id))
                } catch (cancellation: CancellationException) {
                    throw cancellation
                } catch (exception: Exception) {
                }
            }
            val shown = users.take(5)
            val adapter = HorizontalUsersAdapter(shown)
            binding.postDetailsLikesAvatars.adapter = adapter

            if (users.size > 5) {
                binding.postDetailsLikesMore.visibility = View.VISIBLE
                binding.postDetailsLikesMore.text = "+" + (users.size - 5)
                binding.postDetailsLikesMore.setOnClickListener {
                    openLikers(ids)
                }
            } else {
                binding.postDetailsLikesMore.visibility = View.GONE
            }
        }
    }

    private fun openLikers(ids: List<String>) {
        val fragment = LikersFragment()
        val args = Bundle()
        args.putStringArrayList("userIds", ArrayList(ids))
        fragment.arguments = args
        parentFragmentManager.beginTransaction()
            .replace(R.id.container, fragment)
            .addToBackStack("likers")
            .commit()
    }

    private fun loadMentionedUsers(ids: List<String>) {
        viewLifecycleOwner.lifecycleScope.launch {
            val users = mutableListOf<User>()
            for (id in ids) {
                try {
                    users.add(usersViewModel.loadUser(id))
                } catch (cancellation: CancellationException) {
                    throw cancellation
                } catch (exception: Exception) {
                    Toast.makeText(
                        requireContext(),
                        "не удалось загрузить пользователя",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }
            binding.postDetailsMentionedCount.text = users.size.toString()
            val adapter = HorizontalUsersAdapter(users)
            binding.postDetailsMentionedAvatars.adapter = adapter
        }
    }

    private fun sharePost(author: String, content: String, link: String?) {
        val text = buildString {
            if (author.isNotEmpty()) {
                append(author)
                append("\n")
            }
            append(content)
            if (!link.isNullOrBlank()) {
                append("\n")
                append(link)
            }
        }
        val intent = Intent(Intent.ACTION_SEND)
        intent.type = "text/plain"
        intent.putExtra(Intent.EXTRA_TEXT, text)
        startActivity(Intent.createChooser(intent, getString(R.string.btn_share)))
    }

    override fun onDestroyView() {
        val current = mapView
        if (current != null) {
            binding.postDetailsMapContainer.removeView(current)
        }
        mapView = null
        _binding = null
        super.onDestroyView()
    }
}
