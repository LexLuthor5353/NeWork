package ru.netology.nework.feature.users

import android.app.AlertDialog
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.bumptech.glide.Glide
import com.google.android.material.tabs.TabLayout
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.launch
import ru.netology.nework.R
import ru.netology.nework.core.model.Job
import ru.netology.nework.databinding.FragmentUserProfileBinding
import ru.netology.nework.feature.posts.PostsAdapter

@AndroidEntryPoint
class UserProfileFragment : Fragment(R.layout.fragment_user_profile) {

    private var _binding: FragmentUserProfileBinding? = null
    private val binding get() = _binding!!
    private val viewModel: UsersViewModel by viewModels()

    private var isMyProfile = false
    private var currentUserId: String = ""
    private var wallAdapter: PostsAdapter? = null
    private var jobsAdapter: JobsAdapter? = null

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        _binding = FragmentUserProfileBinding.bind(view)

        val name = arguments?.getString("name") ?: "без имени"
        val login = arguments?.getString("login") ?: ""
        currentUserId = arguments?.getString("userId") ?: ""
        val avatar = arguments?.getString("avatar")
        isMyProfile = arguments?.getBoolean("isMyProfile", false) ?: false

        (requireActivity() as? androidx.appcompat.app.AppCompatActivity)?.supportActionBar?.title = "$name ($login)"

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

        jobsAdapter = JobsAdapter(
            showDelete = isMyProfile,
            onJobClick = { job ->
                if (isMyProfile) {
                    openEditJobFragment(job)
                }
            },
            onDeleteClick = { job ->
                if (isMyProfile) {
                    showDeleteJobDialog(job)
                }
            }
        )
        binding.userProfileJobsList.layoutManager = LinearLayoutManager(requireContext())
        binding.userProfileJobsList.adapter = jobsAdapter

        if (isMyProfile) {
            binding.userProfileAddJobFab.visibility = View.VISIBLE
            binding.userProfileAddJobFab.setOnClickListener {
                openEditJobFragment(null)
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

        parentFragmentManager.setFragmentResultListener("job_saved", viewLifecycleOwner) { _, _ ->
            if (currentUserId.isNotEmpty()) {
                loadUserJobs(currentUserId)
            }
        }

        if (currentUserId.isNotEmpty()) {
            loadUserWall(currentUserId)
            loadUserJobs(currentUserId)
        }

        binding.userProfileWallList.visibility = View.VISIBLE
        binding.userProfileJobsList.visibility = View.GONE
    }

    private fun loadUserWall(userId: String) {
        viewLifecycleOwner.lifecycleScope.launch {
            try {
                val wallPosts = viewModel.loadUserWall(userId)
                wallAdapter?.submitList(wallPosts)
                binding.userProfileWallList.isVisible = wallPosts.isNotEmpty()
            } catch (cancellation: CancellationException) {
                throw cancellation
            } catch (exception: Exception) {
                Toast.makeText(requireContext(), "не удалось загрузить стену", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun loadUserJobs(userId: String) {
        viewLifecycleOwner.lifecycleScope.launch {
            try {
                val jobs = if (isMyProfile) {
                    viewModel.loadMyJobs()
                } else {
                    viewModel.loadUserJobs(userId)
                }
                jobsAdapter?.submitList(jobs)
            } catch (cancellation: CancellationException) {
                throw cancellation
            } catch (exception: Exception) {
                Toast.makeText(requireContext(), "не удалось загрузить работы", Toast.LENGTH_SHORT).show()
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
        val jobId = job.id?.toLongOrNull() ?: return

        viewLifecycleOwner.lifecycleScope.launch {
            try {
                viewModel.deleteJob(jobId)
                if (currentUserId.isNotEmpty()) {
                    loadUserJobs(currentUserId)
                }
            } catch (cancellation: CancellationException) {
                throw cancellation
            } catch (exception: Exception) {
                Toast.makeText(requireContext(), "не удалось удалить работу", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun openEditJobFragment(job: Job?) {
        val fragment = EditJobFragment()
        val arguments = Bundle()
        if (job != null) {
            arguments.putString("jobId", job.id)
            arguments.putString("company", job.company)
            arguments.putString("position", job.position)
            arguments.putString("link", job.link)
            job.startAt?.let { arguments.putLong("startAt", it) }
            job.finishAt?.let { arguments.putLong("finishAt", it) }
        }
        fragment.arguments = arguments
        parentFragmentManager.beginTransaction()
            .replace(R.id.container, fragment)
            .addToBackStack("edit_job")
            .commit()
    }

    override fun onDestroyView() {
        (requireActivity() as? androidx.appcompat.app.AppCompatActivity)?.supportActionBar?.title = getString(R.string.app_name)
        super.onDestroyView()
        _binding = null
    }
}
