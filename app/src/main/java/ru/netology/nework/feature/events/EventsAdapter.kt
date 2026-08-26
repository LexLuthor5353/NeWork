package ru.netology.nework.feature.events

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import ru.netology.nework.core.model.Event
import ru.netology.nework.core.model.EventType
import ru.netology.nework.databinding.ItemEventBinding
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class EventsAdapter(
    private var events: List<Event>,
    private val onEventClick: (Event) -> Unit
) : RecyclerView.Adapter<EventsAdapter.EventHolder>() {

    private val dateFormat = SimpleDateFormat("dd.MM.yyyy HH:mm", Locale.getDefault())

    class EventHolder(val binding: ItemEventBinding) : RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): EventHolder {
        val binding = ItemEventBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return EventHolder(binding)
    }

    override fun getItemCount(): Int {
        return events.size
    }

    override fun onBindViewHolder(holder: EventHolder, position: Int) {
        val event = events[position]
        holder.binding.eventAuthorName.text = event.author?.name ?: "без имени"
        if (event.publishedAt != null) {
            holder.binding.eventPublished.text = dateFormat.format(Date(event.publishedAt))
        } else {
            holder.binding.eventPublished.text = ""
        }
        if (event.eventAt != null) {
            holder.binding.eventDate.text = dateFormat.format(Date(event.eventAt))
        } else {
            holder.binding.eventDate.text = "-"
        }
        if (event.type == EventType.ONLINE) {
            holder.binding.eventType.text = "Online"
        } else {
            holder.binding.eventType.text = "Offline"
        }
        holder.binding.eventText.text = event.content
        holder.binding.eventLikeCount.text = (event.likeOwnerIdsCount ?: 0).toString()

        if (event.link != null && event.link != "") {
            holder.binding.eventLink.visibility = android.view.View.VISIBLE
            holder.binding.eventLink.text = event.link
        } else {
            holder.binding.eventLink.visibility = android.view.View.GONE
        }

        holder.binding.eventAttachmentImage.visibility = android.view.View.GONE
        holder.binding.eventAttachmentLabel.visibility = android.view.View.GONE

        holder.binding.root.setOnClickListener {
            onEventClick(event)
        }
    }
}
