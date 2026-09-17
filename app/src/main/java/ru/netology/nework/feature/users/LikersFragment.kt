package ru.netology.nework.feature.users

import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.launch
import ru.netology.nework.R
import ru.netology.nework.core.model.User
import ru.netology.nework.databinding.FragmentLikersBinding

@AndroidEntryPoint
class LikersFragment : Fragment(R.layout.fragment_likers) {

    private var _binding: FragmentLikersBinding? = null
    private val binding get() = _binding!!
    private val viewModel: UsersViewModel by viewModels()
    private lateinit var adapter: LikerAdapter

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        _binding = FragmentLikersBinding.bind(view)

        val ids = arguments?.getStringArrayList("userIds") ?: arrayListOf()

        adapter = LikerAdapter(emptyList())
        binding.likersList.layoutManager = LinearLayoutManager(requireContext())
        binding.likersList.adapter = adapter

        loadUsers(ids)
    }

    private fun loadUsers(ids: List<String>) {
        viewLifecycleOwner.lifecycleScope.launch {
            val users = mutableListOf<User>()
            for (id in ids) {
                try {
                    users.add(viewModel.loadUser(id))
                } catch (cancellation: CancellationException) {
                    throw cancellation
                } catch (exception: Exception) {
                    Toast.makeText(
                        requireContext(),
                        "не удалось загрузить пользователя",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }
            adapter.updateUsers(users)
            binding.likersProgress.isVisible = false
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
