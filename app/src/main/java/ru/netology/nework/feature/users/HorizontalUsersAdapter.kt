package ru.netology.nework.feature.users

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import ru.netology.nework.R
import ru.netology.nework.core.model.User
import ru.netology.nework.databinding.ItemAvatarHorizontalBinding

class HorizontalUsersAdapter(
    private var users: List<User>
) : RecyclerView.Adapter<HorizontalUsersAdapter.Holder>() {

    class Holder(val binding: ItemAvatarHorizontalBinding) :
        RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): Holder {
        val binding = ItemAvatarHorizontalBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return Holder(binding)
    }

    override fun getItemCount(): Int {
        return users.size
    }

    override fun onBindViewHolder(holder: Holder, position: Int) {
        val user = users[position]
        if (user.avatarUrl.isNullOrBlank()) {
            holder.binding.avatarImage.setImageResource(R.drawable.bg_avatar)
        } else {
            Glide.with(holder.binding.avatarImage.context)
                .load(user.avatarUrl)
                .placeholder(R.drawable.bg_avatar)
                .circleCrop()
                .into(holder.binding.avatarImage)
        }
    }

    fun submit(newUsers: List<User>) {
        users = newUsers
        notifyDataSetChanged()
    }
}
