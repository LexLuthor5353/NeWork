package ru.netology.nework.feature.users

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import ru.netology.nework.R
import ru.netology.nework.core.model.User
import ru.netology.nework.databinding.ItemUserBinding
import java.util.HashSet

class UsersAdapter(
    var users: List<User>,
    private val onUserClick: (User) -> Unit,
    private val selectedPositions: Set<Int> = HashSet()
) : RecyclerView.Adapter<UsersAdapter.UserHolder>() {

    private val selectedSet = HashSet(selectedPositions)

    class UserHolder(val binding: ItemUserBinding) : RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): UserHolder {
        val binding = ItemUserBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return UserHolder(binding)
    }

    override fun getItemCount(): Int {
        return users.size
    }

    override fun onBindViewHolder(holder: UserHolder, position: Int) {
        val user = users[position]
        val isSelected = selectedSet.contains(position)

        if (user.name != null && user.name!!.isNotEmpty()) {
            holder.binding.userName.text = user.name!!
        } else {
            holder.binding.userName.text = "без имени"
        }
        holder.binding.userLogin.text = user.login

        val avatarUrl = user.avatarUrl
        if (avatarUrl != null && avatarUrl.isNotEmpty()) {
            Glide.with(holder.binding.userAvatar.context)
                .load(avatarUrl)
                .placeholder(R.drawable.bg_avatar)
                .into(holder.binding.userAvatar)
        } else {
            holder.binding.userAvatar.setImageResource(R.drawable.bg_avatar)
        }

        if (isSelected) {
            holder.binding.root.setBackgroundColor(
                ContextCompat.getColor(holder.binding.root.context, R.color.purple_500)
            )
            holder.binding.userName.setTextColor(ContextCompat.getColor(holder.binding.root.context, R.color.white))
            holder.binding.userLogin.setTextColor(ContextCompat.getColor(holder.binding.root.context, R.color.white))
        } else {
            holder.binding.root.setBackgroundColor(
                ContextCompat.getColor(holder.binding.root.context, R.color.white)
            )
            holder.binding.userName.setTextColor(ContextCompat.getColor(holder.binding.root.context, R.color.black))
            holder.binding.userLogin.setTextColor(ContextCompat.getColor(holder.binding.root.context, R.color.gray))
        }

        holder.binding.root.setOnClickListener {
            if (selectedSet.contains(position)) {
                selectedSet.remove(position)
            } else {
                selectedSet.add(position)
            }
            notifyItemChanged(position)
            onUserClick(user)
        }
    }

    fun updateUsers(newUsers: List<User>) {
        users = newUsers
        notifyDataSetChanged()
    }

    fun isUserSelected(position: Int): Boolean {
        return selectedSet.contains(position)
    }
}
