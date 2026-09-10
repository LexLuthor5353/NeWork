package ru.netology.nework.feature.users

import android.app.AlertDialog
import android.os.Bundle
import android.view.View
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.recyclerview.widget.LinearLayoutManager
import com.bumptech.glide.Glide
import com.google.android.material.tabs.TabLayout
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import ru.netology.nework.R
import ru.netology.nework.core.model.Job
import ru.netology.nework.databinding.FragmentUserProfileBinding
import ru.netology.nework.feature.posts.PostsAdapter
import javax.inject.Inject

@AndroidEntryPoint
class UserProfileFragment : Fragment(R.layout.fragment_user_profile) {

    private var _binding: FragmentUserProfileBinding? = null
    private val binding get() = _binding!!

    @Inject
    lateinit var usersRepository: UsersRepository

    private var isMyProfile = false
    private var myUserId: String? = null
    private var wallAdapter: PostsAdapter? = null
    private var jobsAdapter: JobsAdapter? = null

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        _binding = FragmentUserProfileBinding.bind(view)

        val name = arguments?.getString("name") ?: "без имени"
        val login = arguments?.getString("login") ?: ""
        val userId = arguments?.getString("userId") ?: ""
        val avatar = arguments?.getString("avatar")
        isMyProfile = arguments?.getBoolean("isMyProfile", false) ?: false
        myUserId = if (isMyProfile) userId else null

        binding.userProfileName.text = name
        binding.userProfileLogin.text = login

        if (avatar.isNullOrBlank()) {
            binding.userProfileAvatar.setImageResource(R.drawable.bg_avatar)
        } else {
            Glide.with(this)
                .load(avatar)
                .placeholder(R.drawable.bg_avatar)
                .into(binding.userProfileAvatar)
        }

        wallAdapter = PostsAdapter(
            onPostClick = {},
            onShareClick = {}
        )
        binding.userProfileWallList.layoutManager = LinearLayoutManager(requireContext())
        binding.userProfileWallList.adapter = wallAdapter

        jobsAdapter = JobsAdapter { job ->
            showDeleteJobDialog(job)
        }
        binding.userProfileJobsList.layoutManager = LinearLayoutManager(requireContext())
        binding.userProfileJobsList.adapter = jobsAdapter

        if (isMyProfile) {
            binding.userProfileAddJobFab.visibility = View.VISIBLE
            binding.userProfileAddJobFab.setOnClickListener {
                openEditJobFragment()
            }
        } else {
            binding.userProfileAddJobFab.visibility = View.GONE
        }

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
            loadUserWall(userId)
            loadUserJobs(userId)
        }

        binding.userProfileWallList.visibility = View.VISIBLE
        binding.userProfileJobsList.visibility = View.GONE
    }

    private fun loadUserWall(userId: String) {
        viewLifecycleOwner.lifecycleScope.launch {
            try {
                val wallPosts = usersRepository.loadUserWall(userId)
                wallAdapter?.submitList(wallPosts)
                binding.userProfileWallList.isVisible = wallPosts.isNotEmpty()
            } catch (exception: Exception) {
            }
        }
    }

    private fun loadUserJobs(userId: String) {
        viewLifecycleOwner.lifecycleScope.launch {
            try {
                val jobs = if (isMyProfile) {
                    usersRepository.loadMyJobs()
                } else {
                    usersRepository.loadUserJobs(userId)
                }
                jobsAdapter?.submitList(jobs)
            } catch (exception: Exception) {
            }
        }
    }

    private fun showDeleteJobDialog(job: Job) {
        AlertDialog.Builder(requireContext())
            .setTitle("удалить работу")
            .setMessage("удалить ${job.company}?")
            .setPositiveButton("удалить") { _, _ ->
                deleteJob(job)
            }
            .setNegativeButton("отмена", null)
            .show()
    }

    private fun deleteJob(job: Job) {
        job.id?.toLongOrNull()?.let { jobId ->
            viewLifecycleOwner.lifecycleScope.launch {
                try {
                    usersRepository.deleteJob(jobId)
                    val currentJobs = jobsAdapter?.currentList?.toMutableList() ?: mutableListOf()
                    currentJobs.removeAll { it.id == job.id }
                    jobsAdapter?.submitList(currentJobs)
                } catch (exception: Exception) {
                }
            }
        }
    }

    private fun openEditJobFragment() {
        val fragment = EditJobFragment()
        fragment.isCreating = true
        fragment.onJobSaved = {
            if (myUserId != null) {
                loadUserJobs(myUserId!!)
            }
        }
        parentFragmentManager.beginTransaction()
            .replace(R.id.container, fragment)
            .addToBackStack("edit_job")
            .commit()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
