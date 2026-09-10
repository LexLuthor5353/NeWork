package ru.netology.nework.feature.users

import android.os.Bundle
import android.view.View
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.recyclerview.widget.LinearLayoutManager
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import ru.netology.nework.R
import ru.netology.nework.databinding.FragmentUsersBinding

@AndroidEntryPoint
class UsersFragment : Fragment(R.layout.fragment_users) {

    private var _binding: FragmentUsersBinding? = null
    private val binding get() = _binding!!
    private val viewModel: UsersViewModel by viewModels()
    private lateinit var adapter: UsersAdapter

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        _binding = FragmentUsersBinding.bind(view)

        adapter = UsersAdapter(
            users = emptyList(),
            onUserClick = { user ->
                val fragment = UserProfileFragment()
                val arguments = Bundle()
                arguments.putString("userId", user.id)
                if (user.name != null) {
                    arguments.putString("name", user.name)
                } else {
                    arguments.putString("name", "без имени")
                }
                arguments.putString("login", user.login)
                arguments.putString("avatar", user.avatarUrl)
                fragment.arguments = arguments

                parentFragmentManager.beginTransaction()
                    .replace(R.id.container, fragment)
                    .addToBackStack(null)
                    .commit()
            }
        )

        binding.usersList.layoutManager = LinearLayoutManager(requireContext())
        binding.usersList.adapter = adapter

        binding.usersRefresh.setColorSchemeResources(R.color.purple_500)
        binding.usersRefresh.setOnRefreshListener {
            viewModel.loadUsers()
        }

        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.users.collect { users ->
                    adapter.updateUsers(users)
                    binding.usersList.isVisible = users.isNotEmpty()
                }
            }
        }

        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.loading.collect { loading ->
                    binding.usersProgress.isVisible = loading && adapter.itemCount == 0
                    binding.usersRefresh.isRefreshing = loading && adapter.itemCount > 0
                }
            }
        }

        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.errorMessage.collect { message ->
                    if (message != null) {
                        binding.usersError.visibility = View.VISIBLE
                        binding.usersError.text = message
                    } else {
                        binding.usersError.visibility = View.GONE
                    }
                }
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
