package ru.netology.nework.feature.users

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import ru.netology.nework.R
import ru.netology.nework.core.model.User
import ru.netology.nework.databinding.ItemLikerBinding

class LikerAdapter(
    private var users: List<User>
) : RecyclerView.Adapter<LikerAdapter.Holder>() {

    class Holder(val binding: ItemLikerBinding) : RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): Holder {
        val binding = ItemLikerBinding.inflate(
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
        holder.binding.likerName.text = user.name ?: "без имени"
        holder.binding.likerLogin.text = user.login

        if (user.avatarUrl.isNullOrBlank()) {
            holder.binding.likerAvatar.setImageResource(R.drawable.bg_avatar)
        } else {
            Glide.with(holder.binding.likerAvatar.context)
                .load(user.avatarUrl)
                .placeholder(R.drawable.bg_avatar)
                .circleCrop()
                .into(holder.binding.likerAvatar)
        }
    }

    fun updateUsers(newUsers: List<User>) {
        users = newUsers
        notifyDataSetChanged()
    }
}
