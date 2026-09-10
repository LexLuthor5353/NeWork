package ru.netology.nework.feature.users

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.recyclerview.widget.LinearLayoutManager
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import ru.netology.nework.R
import ru.netology.nework.databinding.FragmentUsersSelectBinding

@AndroidEntryPoint
class UsersSelectFragment : Fragment() {

    private var _binding: FragmentUsersSelectBinding? = null
    private val binding get() = _binding!!
    private val viewModel: UsersViewModel by viewModels()
    private lateinit var adapter: UsersAdapter
    var onUsersSelected: ((List<String>) -> Unit)? = null

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentUsersSelectBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        adapter = UsersAdapter(
            users = emptyList(),
            onUserClick = { user ->
            }
        )

        binding.usersSelectList.layoutManager = LinearLayoutManager(requireContext())
        binding.usersSelectList.adapter = adapter

        binding.usersSelectConfirmButton.setOnClickListener {
            val selectedIds = mutableListOf<String>()
            for (i in 0 until adapter.users.size) {
                val user = adapter.users[i]
                if (adapter.isUserSelected(i)) {
                    user.id?.let { selectedIds.add(it) }
                }
            }
            onUsersSelected?.invoke(selectedIds)
            parentFragmentManager.popBackStack()
        }

        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.users.collect { users ->
                    adapter.updateUsers(users)
                }
            }
        }

        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.loading.collect { loading ->
                    binding.usersSelectProgress.visibility = if (loading) View.VISIBLE else View.GONE
                }
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
