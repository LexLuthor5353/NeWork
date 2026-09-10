package ru.netology.nework.feature.posts

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import com.bumptech.glide.Glide
import ru.netology.nework.R
import ru.netology.nework.core.model.AttachmentType
import ru.netology.nework.core.model.Post
import ru.netology.nework.databinding.ItemPostBinding

class PostsAdapter(
    private val onPostClick: (Post) -> Unit,
    private val onShareClick: (Post) -> Unit = {},
    private val onMenuClick: (Post) -> Unit = {},
    private val onDeleteClick: (Post) -> Unit = {},
    private val onLikeClick: (Post) -> Unit = {}
) : ListAdapter<Post, PostsAdapter.PostHolder>(PostDiffCallback()) {

    class PostHolder(val binding: ItemPostBinding) : androidx.recyclerview.widget.RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PostHolder {
        val binding = ItemPostBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return PostHolder(binding)
    }

    override fun onBindViewHolder(holder: PostHolder, position: Int) {
        val post = getItem(position)
        bindViewHolder(holder, post)
    }

    private fun bindViewHolder(holder: PostHolder, post: Post) {
        holder.binding.postAuthorName.text = post.authorName ?: "без имени"
        holder.binding.postPublished.text = post.publishedFormatted
        holder.binding.postText.text = post.content
        holder.binding.postLikeButton.text = (post.likeOwnerIdsCount ?: 0).toString()
        holder.binding.postLikeButton.isChecked = post.likedByMe == true

        if (post.authorAvatarUrl.isNullOrBlank()) {
            holder.binding.postAvatar.setImageResource(R.drawable.bg_avatar)
        } else {
            Glide.with(holder.binding.postAvatar.context)
                .load(post.authorAvatarUrl)
                .placeholder(R.drawable.bg_avatar)
                .circleCrop()
                .into(holder.binding.postAvatar)
        }

        if (post.link.isNullOrBlank()) {
            holder.binding.postLink.visibility = android.view.View.GONE
        } else {
            holder.binding.postLink.visibility = android.view.View.VISIBLE
            holder.binding.postLink.text = post.link
        }

        val attachment = post.attachment
        when {
            attachment?.type == AttachmentType.IMAGE && attachment.url.isNullOrBlank() -> {
                holder.binding.postAttachmentBlock.visibility = android.view.View.GONE
                holder.binding.postPlayIcon.visibility = android.view.View.GONE
                holder.binding.postAttachmentLabel.visibility = android.view.View.GONE
            }
            attachment?.type == AttachmentType.IMAGE -> {
                holder.binding.postAttachmentBlock.visibility = android.view.View.VISIBLE
                holder.binding.postAttachmentLabel.visibility = android.view.View.GONE
                holder.binding.postPlayIcon.visibility = android.view.View.GONE
                Glide.with(holder.binding.postAttachmentImage.context)
                    .load(attachment.url)
                    .into(holder.binding.postAttachmentImage)
            }
            attachment?.type == AttachmentType.VIDEO && attachment.url.isNullOrBlank() -> {
                holder.binding.postAttachmentBlock.visibility = android.view.View.GONE
                holder.binding.postPlayIcon.visibility = android.view.View.GONE
                holder.binding.postAttachmentLabel.visibility = android.view.View.GONE
            }
            attachment?.type == AttachmentType.VIDEO -> {
                holder.binding.postAttachmentBlock.visibility = android.view.View.VISIBLE
                holder.binding.postAttachmentLabel.visibility = android.view.View.GONE
                holder.binding.postPlayIcon.visibility = android.view.View.VISIBLE
                Glide.with(holder.binding.postAttachmentImage.context)
                    .load(attachment.url)
                    .into(holder.binding.postAttachmentImage)
            }
            attachment != null -> {
                holder.binding.postAttachmentBlock.visibility = android.view.View.GONE
                holder.binding.postPlayIcon.visibility = android.view.View.GONE
                holder.binding.postAttachmentLabel.visibility = android.view.View.VISIBLE
                holder.binding.postAttachmentLabel.text = attachment.type.name
            }
            else -> {
                holder.binding.postAttachmentBlock.visibility = android.view.View.GONE
                holder.binding.postPlayIcon.visibility = android.view.View.GONE
                holder.binding.postAttachmentLabel.visibility = android.view.View.GONE
            }
        }

        holder.binding.postShareButton.setOnClickListener { onShareClick(post) }
        holder.binding.postLikeButton.setOnClickListener { onLikeClick(post) }
        holder.binding.postMenuButton.setOnClickListener { onMenuClick(post) }
        holder.binding.root.setOnClickListener { onPostClick(post) }
    }

    private class PostDiffCallback : DiffUtil.ItemCallback<Post>() {
        override fun areItemsTheSame(oldItem: Post, newItem: Post): Boolean {
            return oldItem.id == newItem.id
        }

        override fun areContentsTheSame(oldItem: Post, newItem: Post): Boolean {
            return oldItem == newItem
        }
    }
}
