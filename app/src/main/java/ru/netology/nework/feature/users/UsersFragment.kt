package ru.netology.nework.feature.users

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import ru.netology.nework.R
import ru.netology.nework.core.model.User
import ru.netology.nework.databinding.FragmentUsersBinding

class UsersFragment : Fragment() {

    private var _binding: FragmentUsersBinding? = null
    private val binding get() = _binding!!

    private lateinit var adapter: UsersAdapter

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentUsersBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val users = getTestUsers()
        adapter = UsersAdapter(users) { user ->
            val fragment = UserProfileFragment()
            val arguments = Bundle()
            arguments.putString("userId", user.id)
            if (user.name != null) {
                arguments.putString("name", user.name)
            } else {
                arguments.putString("name", "без имени")
            }
            arguments.putString("login", user.login)
            fragment.arguments = arguments

            parentFragmentManager.beginTransaction()
                .replace(R.id.container, fragment)
                .addToBackStack(null)
                .commit()
        }

        binding.usersList.layoutManager = LinearLayoutManager(requireContext())
        binding.usersList.adapter = adapter
    }

    private fun getTestUsers(): List<User> {
        val users = ArrayList<User>()
        users.add(User("1", "ivan", "Иван", null))
        users.add(User("2", "anna", "Анна", null))
        users.add(User("3", "petya", "Петя", null))
        users.add(User("4", "olga", "Ольга", null))
        return users
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
