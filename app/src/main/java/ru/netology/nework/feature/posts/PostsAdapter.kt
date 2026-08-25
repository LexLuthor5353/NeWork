package ru.netology.nework.feature.posts

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import ru.netology.nework.core.model.Post
import ru.netology.nework.databinding.ItemPostBinding
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class PostsAdapter(
    private var posts: List<Post>,
    private val onPostClick: (Post) -> Unit
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
        holder.binding.postLikeCount.text = (post.likeOwnerIdsCount ?: 0).toString()

        if (post.link != null && post.link.isNotEmpty()) {
            holder.binding.postLink.visibility = android.view.View.VISIBLE
            holder.binding.postLink.text = post.link
        } else {
            holder.binding.postLink.visibility = android.view.View.GONE
        }

        holder.binding.postAttachmentImage.visibility = android.view.View.GONE
        holder.binding.postAttachmentLabel.visibility = android.view.View.GONE

        holder.binding.root.setOnClickListener {
            onPostClick(post)
        }
    }
}
