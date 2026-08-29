package ru.netology.nework.feature.users

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import ru.netology.nework.core.model.User
import ru.netology.nework.databinding.ItemUserBinding

class UsersAdapter(
    private var users: List<User>,
    private val onUserClick: (User) -> Unit
) : RecyclerView.Adapter<UsersAdapter.UserHolder>() {

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
        if (user.name != null) {
            holder.binding.userName.text = user.name
        } else {
            holder.binding.userName.text = "без имени"
        }
        holder.binding.userLogin.text = user.login

        holder.binding.root.setOnClickListener {
            onUserClick(user)
        }
    }
}
