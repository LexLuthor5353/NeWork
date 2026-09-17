package ru.netology.nework.feature.users

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import ru.netology.nework.core.model.Job
import ru.netology.nework.databinding.ItemJobBinding

class JobsAdapter(
    private val showDelete: Boolean = false,
    private val onJobClick: (Job) -> Unit = {},
    private val onDeleteClick: (Job) -> Unit = {}
) : ListAdapter<Job, JobsAdapter.JobHolder>(JobDiffCallback()) {

    class JobHolder(val binding: ItemJobBinding) : androidx.recyclerview.widget.RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): JobHolder {
        val binding = ItemJobBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return JobHolder(binding)
    }

    override fun onBindViewHolder(holder: JobHolder, position: Int) {
        val job = getItem(position)
        holder.binding.jobCompany.text = job.company
        holder.binding.jobPosition.text = job.position
        holder.binding.jobPeriod.text = job.periodFormatted

        if (showDelete) {
            holder.binding.jobDeleteButton.visibility = View.VISIBLE
        } else {
            holder.binding.jobDeleteButton.visibility = View.GONE
        }

        holder.binding.jobDeleteButton.setOnClickListener { onDeleteClick(job) }
        holder.binding.root.setOnClickListener { onJobClick(job) }
    }

    private class JobDiffCallback : DiffUtil.ItemCallback<Job>() {
        override fun areItemsTheSame(oldItem: Job, newItem: Job): Boolean {
            return oldItem.id == newItem.id
        }

        override fun areContentsTheSame(oldItem: Job, newItem: Job): Boolean {
            return oldItem == newItem
        }
    }
}
