package ru.netology.nework.feature.users

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.material.tabs.TabLayout
import ru.netology.nework.core.model.Job
import ru.netology.nework.core.model.Post
import ru.netology.nework.core.model.User
import ru.netology.nework.databinding.FragmentUserProfileBinding
import ru.netology.nework.feature.posts.PostsAdapter

class UserProfileFragment : Fragment() {

    private var _binding: FragmentUserProfileBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentUserProfileBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val name = arguments?.getString("name") ?: "без имени"
        val login = arguments?.getString("login") ?: ""
        val userId = arguments?.getString("userId") ?: ""

        binding.userProfileName.text = name
        binding.userProfileLogin.text = login

        val wallPosts = getWallPosts(userId, name, login)
        val wallAdapter = PostsAdapter(wallPosts) { }
        binding.userProfileWallList.layoutManager = LinearLayoutManager(requireContext())
        binding.userProfileWallList.adapter = wallAdapter

        val jobs = getTestJobs(userId)
        val jobsAdapter = JobsAdapter(jobs)
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
    }

    private fun getWallPosts(userId: String, name: String, login: String): List<Post> {
        val currentTime = System.currentTimeMillis()
        val posts = ArrayList<Post>()
        val author = User(userId, login, name, null)
        posts.add(
            Post(
                userId,
                author,
                currentTime,
                "Пост на стене пользователя " + name,
                null,
                null,
                null,
                emptyList(),
                1,
                false
            )
        )
        return posts
    }

    private fun getTestJobs(userId: String): List<Job> {
        val jobs = ArrayList<Job>()
        if (userId == "1") {
            jobs.add(
                Job(
                    "1",
                    "Яндекс",
                    "Android разработчик",
                    "https://yandex.ru",
                    System.currentTimeMillis() - 1000L * 60 * 60 * 24 * 365,
                    null
                )
            )
        } else if (userId == "2") {
            jobs.add(
                Job(
                    "2",
                    "Сбер",
                    "Тестировщик",
                    null,
                    System.currentTimeMillis() - 1000L * 60 * 60 * 24 * 500,
                    System.currentTimeMillis() - 1000L * 60 * 60 * 24 * 100
                )
            )
        }
        return jobs
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
