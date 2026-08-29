package ru.netology.nework.feature.users

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import ru.netology.nework.core.model.Job
import ru.netology.nework.databinding.ItemJobBinding
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class JobsAdapter(
    private var jobs: List<Job>
) : RecyclerView.Adapter<JobsAdapter.JobHolder>() {

    private val dateFormat = SimpleDateFormat("dd MMM yyyy", Locale.getDefault())

    class JobHolder(val binding: ItemJobBinding) : RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): JobHolder {
        val binding = ItemJobBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return JobHolder(binding)
    }

    override fun getItemCount(): Int {
        return jobs.size
    }

    override fun onBindViewHolder(holder: JobHolder, position: Int) {
        val job = jobs[position]
        holder.binding.jobCompany.text = job.company
        holder.binding.jobPosition.text = job.position

        var periodText = ""
        if (job.startAt != null) {
            periodText = dateFormat.format(Date(job.startAt))
        }
        if (job.finishAt != null) {
            periodText = periodText + " — " + dateFormat.format(Date(job.finishAt))
        } else {
            if (periodText.length > 0) {
                periodText = periodText + " — сейчас"
            }
        }
        holder.binding.jobPeriod.text = periodText

        holder.binding.jobActions.visibility = android.view.View.GONE
    }
}
