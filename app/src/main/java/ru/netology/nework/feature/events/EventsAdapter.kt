package ru.netology.nework.feature.events

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import android.content.Intent
import android.net.Uri
import android.widget.Toast
import com.bumptech.glide.Glide
import ru.netology.nework.R
import ru.netology.nework.core.model.AttachmentType
import ru.netology.nework.core.model.Event
import ru.netology.nework.databinding.ItemEventBinding

class EventsAdapter(
    private val onEventClick: (Event) -> Unit,
    private val onShareClick: (Event) -> Unit = {},
    private val onMenuClick: (Event) -> Unit = {},
    private val onDeleteClick: (Event) -> Unit = {},
    private val onLikeClick: (Event) -> Unit = {}
) : ListAdapter<Event, EventsAdapter.EventHolder>(EventDiffCallback()) {

    var myId: String? = null

    class EventHolder(val binding: ItemEventBinding) : androidx.recyclerview.widget.RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): EventHolder {
        val binding = ItemEventBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return EventHolder(binding)
    }

    override fun onBindViewHolder(holder: EventHolder, position: Int) {
        val event = getItem(position)
        bindViewHolder(holder, event)
    }

    private fun bindViewHolder(holder: EventHolder, event: Event) {
        holder.binding.eventAuthorName.text = event.authorName ?: "без имени"
        holder.binding.eventPublished.text = event.publishedFormatted
        holder.binding.eventDate.text = event.eventAtFormatted
        holder.binding.eventType.text = event.typeFormatted
        holder.binding.eventText.text = event.content
        holder.binding.eventLikeButton.text = (event.likeOwnerIdsCount ?: 0).toString()
        holder.binding.eventLikeButton.isChecked = event.likedByMe == true
        holder.binding.eventShareButton.text = ""
        holder.binding.eventViewsButton.visibility = View.GONE

        if (event.authorId != null && myId != null && event.authorId == myId) {
            holder.binding.eventMenuButton.visibility = View.VISIBLE
        } else {
            holder.binding.eventMenuButton.visibility = View.GONE
        }

        if (event.authorAvatarUrl.isNullOrBlank()) {
            holder.binding.eventAvatar.setImageResource(R.drawable.bg_avatar)
        } else {
            Glide.with(holder.binding.eventAvatar.context)
                .load(event.authorAvatarUrl)
                .placeholder(R.drawable.bg_avatar)
                .circleCrop()
                .into(holder.binding.eventAvatar)
        }

        if (event.link.isNullOrBlank()) {
            holder.binding.eventLink.visibility = View.GONE
        } else {
            holder.binding.eventLink.visibility = View.VISIBLE
            holder.binding.eventLink.text = event.link
            holder.binding.eventLink.setOnClickListener {
                try {
                    val intent = Intent(Intent.ACTION_VIEW, Uri.parse(event.link))
                    holder.binding.root.context.startActivity(intent)
                } catch (exception: Exception) {
                    Toast.makeText(
                        holder.binding.root.context,
                        "не удалось открыть ссылку",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }
        }

        val attachment = event.attachment
        when {
            attachment?.type == AttachmentType.IMAGE && attachment.url.isNullOrBlank() -> {
                holder.binding.eventAttachmentImage.visibility = View.GONE
                holder.binding.eventAttachmentLabel.visibility = View.GONE
            }
            attachment?.type == AttachmentType.IMAGE -> {
                holder.binding.eventAttachmentImage.visibility = View.VISIBLE
                holder.binding.eventAttachmentLabel.visibility = View.GONE
                Glide.with(holder.binding.eventAttachmentImage.context)
                    .load(attachment.url)
                    .into(holder.binding.eventAttachmentImage)
            }
            attachment != null -> {
                holder.binding.eventAttachmentImage.visibility = View.GONE
                holder.binding.eventAttachmentLabel.visibility = View.VISIBLE
                holder.binding.eventAttachmentLabel.text = attachment.type.name
            }
            else -> {
                holder.binding.eventAttachmentImage.visibility = View.GONE
                holder.binding.eventAttachmentLabel.visibility = View.GONE
            }
        }

        holder.binding.eventShareButton.setOnClickListener { onShareClick(event) }
        holder.binding.eventLikeButton.setOnClickListener { onLikeClick(event) }
        holder.binding.eventMenuButton.setOnClickListener { onMenuClick(event) }
        holder.binding.root.setOnClickListener { onEventClick(event) }
    }

    private class EventDiffCallback : DiffUtil.ItemCallback<Event>() {
        override fun areItemsTheSame(oldItem: Event, newItem: Event): Boolean {
            return oldItem.id == newItem.id
        }

        override fun areContentsTheSame(oldItem: Event, newItem: Event): Boolean {
            return oldItem == newItem
        }
    }
}
