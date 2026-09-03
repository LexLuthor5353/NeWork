package ru.netology.nework.feature.posts

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import ru.netology.nework.R
import ru.netology.nework.core.model.AttachmentType
import ru.netology.nework.core.model.Post
import ru.netology.nework.databinding.ItemPostBinding
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class PostsAdapter(
    private var posts: List<Post>,
    private val onPostClick: (Post) -> Unit,
    private val onShareClick: (Post) -> Unit = {}
) : RecyclerView.Adapter<PostsAdapter.PostHolder>() {

    private val dateFormat = SimpleDateFormat("dd.MM.yyyy HH:mm", Locale.getDefault())

    class PostHolder(val binding: ItemPostBinding) : RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PostHolder {
        val binding = ItemPostBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return PostHolder(binding)
    }

    override fun getItemCount(): Int {
        return posts.size
    }

    override fun onBindViewHolder(holder: PostHolder, position: Int) {
        val post = posts[position]
        holder.binding.postAuthorName.text = post.author?.name ?: "без имени"
        if (post.publishedAt != null) {
            holder.binding.postPublished.text = dateFormat.format(Date(post.publishedAt))
        } else {
            holder.binding.postPublished.text = ""
        }
        holder.binding.postText.text = post.content
        holder.binding.postLikeButton.text = (post.likeOwnerIdsCount ?: 0).toString()
        holder.binding.postLikeButton.isChecked = post.likedByMe == true

        val avatarUrl = post.author?.avatarUrl
        if (avatarUrl != null && avatarUrl.isNotEmpty()) {
            Glide.with(holder.binding.postAvatar.context)
                .load(avatarUrl)
                .placeholder(R.drawable.bg_avatar)
                .circleCrop()
                .into(holder.binding.postAvatar)
        } else {
            holder.binding.postAvatar.setImageResource(R.drawable.bg_avatar)
        }

        if (post.link != null && post.link.isNotEmpty()) {
            holder.binding.postLink.visibility = View.VISIBLE
            holder.binding.postLink.text = post.link
        } else {
            holder.binding.postLink.visibility = View.GONE
        }

        val attachment = post.attachment
        if (attachment != null && attachment.type == AttachmentType.IMAGE && attachment.url != null) {
            holder.binding.postAttachmentBlock.visibility = View.VISIBLE
            holder.binding.postAttachmentLabel.visibility = View.GONE
            holder.binding.postPlayIcon.visibility = View.GONE
            Glide.with(holder.binding.postAttachmentImage.context)
                .load(attachment.url)
                .into(holder.binding.postAttachmentImage)
        } else if (attachment != null && attachment.type == AttachmentType.VIDEO && attachment.url != null) {
            holder.binding.postAttachmentBlock.visibility = View.VISIBLE
            holder.binding.postAttachmentLabel.visibility = View.GONE
            holder.binding.postPlayIcon.visibility = View.VISIBLE
            Glide.with(holder.binding.postAttachmentImage.context)
                .load(attachment.url)
                .into(holder.binding.postAttachmentImage)
        } else if (attachment != null) {
            holder.binding.postAttachmentBlock.visibility = View.GONE
            holder.binding.postPlayIcon.visibility = View.GONE
            holder.binding.postAttachmentLabel.visibility = View.VISIBLE
            holder.binding.postAttachmentLabel.text = attachment.type.name
        } else {
            holder.binding.postAttachmentBlock.visibility = View.GONE
            holder.binding.postPlayIcon.visibility = View.GONE
            holder.binding.postAttachmentLabel.visibility = View.GONE
        }

        holder.binding.postShareButton.setOnClickListener {
            onShareClick(post)
        }

        holder.binding.root.setOnClickListener {
            onPostClick(post)
        }
    }

    fun updatePosts(newPosts: List<Post>) {
        posts = newPosts
        notifyDataSetChanged()
    }
}
