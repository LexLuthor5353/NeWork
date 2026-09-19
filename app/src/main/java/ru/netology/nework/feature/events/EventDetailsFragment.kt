package ru.netology.nework.feature.events

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import android.widget.Toast
import androidx.core.view.MenuProvider
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.yandex.mapkit.geometry.Point
import com.yandex.mapkit.map.CameraPosition
import com.yandex.mapkit.mapview.MapView
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.launch
import ru.netology.nework.R
import ru.netology.nework.core.model.User
import ru.netology.nework.databinding.FragmentEventDetailsBinding
import ru.netology.nework.feature.users.HorizontalUsersAdapter
import ru.netology.nework.feature.users.UsersViewModel
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.Locale

@AndroidEntryPoint
class EventDetailsFragment : Fragment() {

    private var _binding: FragmentEventDetailsBinding? = null
    private val binding get() = _binding!!
    private val usersViewModel: UsersViewModel by viewModels()

    private var mapView: MapView? = null

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentEventDetailsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val author = arguments?.getString("authorName") ?: "без имени"
        val content = arguments?.getString("content") ?: ""
        val likes = arguments?.getLong("likes") ?: 0
        val publishedAt = arguments?.getLong("publishedAt") ?: 0
        val eventAt = arguments?.getLong("eventAt") ?: 0
        val type = arguments?.getString("type") ?: "Online"
        val link = arguments?.getString("link")
        val job = arguments?.getString("authorJob") ?: "В поиске работы"
        val speakerIds = arguments?.getStringArrayList("speakerIds")
        val participantIds = arguments?.getStringArrayList("participantIds")

        requireActivity().addMenuProvider(object : MenuProvider {
            override fun onCreateMenu(menu: Menu, menuInflater: MenuInflater) {
                menuInflater.inflate(R.menu.menu_share, menu)
            }

            override fun onMenuItemSelected(menuItem: MenuItem): Boolean {
                if (menuItem.itemId == R.id.actionShare) {
                    shareEvent(author, content, link)
                    return true
                }
                return false
            }
        }, viewLifecycleOwner, Lifecycle.State.RESUMED)

        binding.eventDetailsAuthorName.text = author
        binding.eventDetailsText.text = content
        binding.eventDetailsLikeCount.text = likes.toString()
        binding.eventDetailsType.text = type
        binding.eventDetailsJob.text = job

        val formatter = DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm", Locale.getDefault())

        if (publishedAt > 0) {
            binding.eventDetailsPublished.text = formatter.format(
                Instant.ofEpochMilli(publishedAt).atZone(ZoneId.systemDefault()).toLocalDateTime()
            )
        } else {
            binding.eventDetailsPublished.text = ""
        }

        if (eventAt > 0) {
            binding.eventDetailsDate.text = formatter.format(
                Instant.ofEpochMilli(eventAt).atZone(ZoneId.systemDefault()).toLocalDateTime()
            )
        } else {
            binding.eventDetailsDate.text = ""
        }

        if (link.isNullOrBlank()) {
            binding.eventDetailsLink.visibility = View.GONE
        } else {
            binding.eventDetailsLink.visibility = View.VISIBLE
            binding.eventDetailsLink.text = link
            binding.eventDetailsLink.setOnClickListener {
                try {
                    val intent = Intent(Intent.ACTION_VIEW, android.net.Uri.parse(link))
                    startActivity(intent)
                } catch (exception: Exception) {
                    Toast.makeText(
                        requireContext(),
                        "не удалось открыть ссылку",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }
        }

        binding.eventDetailsAttachmentImage.visibility = View.GONE
        binding.eventDetailsAttachmentLabel.visibility = View.GONE

        if (participantIds != null && participantIds.isNotEmpty()) {
            binding.eventDetailsParticipantsTitle.visibility = View.VISIBLE
            binding.eventDetailsParticipantsList.visibility = View.VISIBLE
            loadUsers(participantIds, true)
        } else {
            binding.eventDetailsParticipantsTitle.visibility = View.GONE
            binding.eventDetailsParticipantsList.visibility = View.GONE
        }

        if (speakerIds != null && speakerIds.isNotEmpty()) {
            binding.eventDetailsSpeakersTitle.visibility = View.VISIBLE
            binding.eventDetailsSpeakersList.visibility = View.VISIBLE
            loadUsers(speakerIds, false)
        } else {
            binding.eventDetailsSpeakersTitle.visibility = View.GONE
            binding.eventDetailsSpeakersList.visibility = View.GONE
        }

        val lat = arguments?.getDouble("lat")
        val lng = arguments?.getDouble("lng")
        if (lat != null && lng != null && lat != 0.0 && lng != 0.0) {
            binding.eventDetailsMapTitle.visibility = View.VISIBLE
            binding.eventDetailsMapContainer.visibility = View.VISIBLE
            setupMap(lat, lng)
        } else {
            binding.eventDetailsMapTitle.visibility = View.GONE
            binding.eventDetailsMapContainer.visibility = View.GONE
        }
    }

    private fun setupMap(lat: Double, lng: Double) {
        if (mapView == null) {
            mapView = MapView(requireContext())
            val params = FrameLayout.LayoutParams(
                FrameLayout.LayoutParams.MATCH_PARENT,
                FrameLayout.LayoutParams.MATCH_PARENT
            )
            binding.eventDetailsMapContainer.addView(mapView, params)
        }

        val point = Point(lat, lng)
        val camera = CameraPosition(point, 15.0f, 0.0f, 0.0f)
        mapView?.map?.move(camera)
        mapView?.map?.mapObjects?.clear()
        mapView?.map?.mapObjects?.addPlacemark(point)

        binding.eventDetailsMapStub.visibility = View.GONE
    }

    override fun onStart() {
        super.onStart()
        mapView?.onStart()
    }

    override fun onStop() {
        super.onStop()
        mapView?.onStop()
    }

    private fun loadUsers(ids: List<String>, isParticipants: Boolean) {
        viewLifecycleOwner.lifecycleScope.launch {
            val users = mutableListOf<User>()
            for (id in ids) {
                try {
                    users.add(usersViewModel.loadUser(id))
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
            val adapter = HorizontalUsersAdapter(users)
            if (isParticipants) {
                binding.eventDetailsParticipantsList.layoutManager = LinearLayoutManager(
                    requireContext(),
                    LinearLayoutManager.HORIZONTAL,
                    false
                )
                binding.eventDetailsParticipantsList.adapter = adapter
            } else {
                binding.eventDetailsSpeakersList.layoutManager = LinearLayoutManager(
                    requireContext(),
                    LinearLayoutManager.HORIZONTAL,
                    false
                )
                binding.eventDetailsSpeakersList.adapter = adapter
            }
        }
    }

    private fun shareEvent(author: String, content: String, link: String?) {
        val text = buildString {
            if (author.isNotEmpty()) {
                append(author)
                append("\n")
            }
            append(content)
            if (!link.isNullOrBlank()) {
                append("\n")
                append(link)
            }
        }
        val intent = Intent(Intent.ACTION_SEND)
        intent.type = "text/plain"
        intent.putExtra(Intent.EXTRA_TEXT, text)
        startActivity(Intent.createChooser(intent, getString(R.string.btn_share)))
    }

    override fun onDestroyView() {
        val current = mapView
        if (current != null) {
            binding.eventDetailsMapContainer.removeView(current)
        }
        mapView = null
        _binding = null
        super.onDestroyView()
    }
}
