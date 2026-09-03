package ru.netology.nework.feature.users

import android.os.Bundle
import android.view.View
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.bumptech.glide.Glide
import com.google.android.material.tabs.TabLayout
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import ru.netology.nework.R
import ru.netology.nework.databinding.FragmentUserProfileBinding
import ru.netology.nework.feature.posts.PostsAdapter
import javax.inject.Inject

@AndroidEntryPoint
class UserProfileFragment : Fragment(R.layout.fragment_user_profile) {

    private var _binding: FragmentUserProfileBinding? = null
    private val binding get() = _binding!!

    @Inject
    lateinit var usersRepository: UsersRepository

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        _binding = FragmentUserProfileBinding.bind(view)

        val name = arguments?.getString("name") ?: "без имени"
        val login = arguments?.getString("login") ?: ""
        val userId = arguments?.getString("userId") ?: ""
        val avatar = arguments?.getString("avatar")

        binding.userProfileName.text = name
        binding.userProfileLogin.text = login

        if (avatar != null && avatar.isNotEmpty()) {
            Glide.with(this)
                .load(avatar)
                .placeholder(R.drawable.bg_avatar)
                .into(binding.userProfileAvatar)
        }

        val wallAdapter = PostsAdapter(
            posts = emptyList(),
            onPostClick = {},
            onShareClick = {}
        )
        binding.userProfileWallList.layoutManager = LinearLayoutManager(requireContext())
        binding.userProfileWallList.adapter = wallAdapter

        val jobsAdapter = JobsAdapter(emptyList())
        binding.userProfileJobsList.layoutManager = LinearLayoutManager(requireContext())
        binding.userProfileJobsList.adapter = jobsAdapter

        binding.userProfileTabs.addOnTabSelectedListener(object : TabLayout.OnTabSelectedListener {
            override fun onTabSelected(tab: TabLayout.Tab) {
                if (tab.position == 0) {
                    binding.userProfileWallList.visibility = View.VISIBLE
                    binding.userProfileJobsList.visibility = View.GONE
                } else {
                    binding.userProfileWallList.visibility = View.GONE
                    binding.userProfileJobsList.visibility = View.VISIBLE
                }
            }

            override fun onTabUnselected(tab: TabLayout.Tab) {
            }

            override fun onTabReselected(tab: TabLayout.Tab) {
            }
        })

        if (userId.isNotEmpty()) {
            viewLifecycleOwner.lifecycleScope.launch {
                try {
                    val wallPosts = usersRepository.loadUserWall(userId)
                    wallAdapter.updatePosts(wallPosts)
                    binding.userProfileWallList.isVisible = wallPosts.isNotEmpty()
                } catch (exception: Exception) {
                }
            }
            viewLifecycleOwner.lifecycleScope.launch {
                try {
                    val jobs = usersRepository.loadUserJobs(userId)
                    jobsAdapter.updateJobs(jobs)
                } catch (exception: Exception) {
                }
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
