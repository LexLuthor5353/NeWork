package ru.netology.nework.feature.events

import android.Manifest
import android.app.DatePickerDialog
import android.app.TimePickerDialog
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.view.View
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import androidx.core.view.MenuProvider
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.bumptech.glide.Glide
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import ru.netology.nework.R
import ru.netology.nework.core.util.FilePartUtils
import ru.netology.nework.databinding.FragmentEditEventBinding
import ru.netology.nework.feature.common.MapFragment
import ru.netology.nework.feature.users.UsersSelectFragment
import java.io.File
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

@AndroidEntryPoint
class EditEventFragment : Fragment(R.layout.fragment_edit_event) {

    private var _binding: FragmentEditEventBinding? = null
    private val binding get() = _binding!!
    private val viewModel: EditEventViewModel by viewModels()

    private var eventId: String? = null
    private var attachmentUri: Uri? = null
    private var cameraPhotoUri: Uri? = null
    private var pendingStorageAction: (() -> Unit)? = null
    private var selectedSpeakerUserIds: MutableList<String> = mutableListOf()
    private var eventDateMillis: Long? = null
    private var currentLat: Double? = null
    private var currentLng: Double? = null
    private var existingAttachmentUrl: String? = null
    private var existingAttachmentType: String? = null

    private val pickImageLauncher = registerForActivityResult(
        ActivityResultContracts.GetContent()
    ) { uri ->
        if (uri != null) {
            showAttachment(uri)
        }
    }

    private val pickVideoLauncher = registerForActivityResult(
        ActivityResultContracts.GetContent()
    ) { uri ->
        if (uri != null) {
            showAttachment(uri)
        }
    }

    private val pickAudioLauncher = registerForActivityResult(
        ActivityResultContracts.GetContent()
    ) { uri ->
        if (uri != null) {
            showAttachment(uri)
        }
    }

    private val takePhotoLauncher = registerForActivityResult(
        ActivityResultContracts.TakePicture()
    ) { success ->
        if (success && cameraPhotoUri != null) {
            showAttachment(cameraPhotoUri!!)
        }
    }

    private val cameraPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { granted ->
        if (granted) {
            openCamera()
        } else {
            Toast.makeText(requireContext(), R.string.permission_camera_denied, Toast.LENGTH_SHORT).show()
        }
    }

    private val storagePermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { granted ->
        if (granted) {
            pendingStorageAction?.invoke()
            pendingStorageAction = null
        } else {
            Toast.makeText(requireContext(), R.string.permission_storage_denied, Toast.LENGTH_SHORT).show()
            pendingStorageAction = null
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        _binding = FragmentEditEventBinding.bind(view)

        eventId = arguments?.getString("eventId")
        val initialContent = arguments?.getString("content").orEmpty()
        val initialType = arguments?.getString("type")
        val initialEventAt = arguments?.getLong("eventAt", 0L) ?: 0L
        val initialLat = arguments?.getDouble("lat")
        val initialLng = arguments?.getDouble("lng")
        existingAttachmentUrl = arguments?.getString("attachmentUrl")
        existingAttachmentType = arguments?.getString("attachmentType")
        val initialSpeakers = arguments?.getStringArrayList("speakerIds")

        if (initialSpeakers != null) {
            selectedSpeakerUserIds.addAll(initialSpeakers)
        }

        binding.editEventText.setText(initialContent)

        if (initialType == "OFFLINE") {
            binding.editEventTypeOffline.isChecked = true
        } else {
            binding.editEventTypeOnline.isChecked = true
        }

        if (initialEventAt > 0) {
            eventDateMillis = initialEventAt
            val format = SimpleDateFormat("dd.MM.yyyy HH:mm", Locale.getDefault())
            binding.editEventDateText.isVisible = true
            binding.editEventDateText.text = format.format(Date(initialEventAt))
        }

        if (initialLat != null && initialLng != null) {
            currentLat = initialLat
            currentLng = initialLng
        }
        updateLocationText()
        updateSpeakersText()

        if (existingAttachmentUrl != null) {
            binding.editEventAttachmentBlock.visibility = View.VISIBLE
            if (existingAttachmentType == "IMAGE" || existingAttachmentType == "VIDEO") {
                binding.editEventAttachmentPreview.visibility = View.VISIBLE
                Glide.with(this)
                    .load(existingAttachmentUrl)
                    .centerCrop()
                    .into(binding.editEventAttachmentPreview)
            } else {
                binding.editEventAttachmentPreview.visibility = View.VISIBLE
                binding.editEventAttachmentPreview.setImageDrawable(null)
            }
        }

        requireActivity().addMenuProvider(object : MenuProvider {
            override fun onCreateMenu(menu: Menu, menuInflater: MenuInflater) {
                menuInflater.inflate(R.menu.menu_save, menu)
            }

            override fun onMenuItemSelected(menuItem: MenuItem): Boolean {
                if (menuItem.itemId == R.id.actionSave) {
                    saveEvent()
                    return true
                }
                return false
            }
        }, viewLifecycleOwner, Lifecycle.State.RESUMED)

        binding.editEventPhotoButton.setOnClickListener {
            requestCameraAndOpen()
        }

        binding.editEventAttachButton.setOnClickListener {
            showAttachDialog()
        }

        binding.editEventDateButton.setOnClickListener {
            showDatePicker()
        }

        binding.editEventSpeakersButton.setOnClickListener {
            val fragment = UsersSelectFragment()
            fragment.onUsersSelected = { userIds ->
                selectedSpeakerUserIds.clear()
                selectedSpeakerUserIds.addAll(userIds)
                updateSpeakersText()
            }
            parentFragmentManager.beginTransaction()
                .replace(R.id.container, fragment)
                .addToBackStack(null)
                .commit()
        }

        binding.editEventLocationButton.setOnClickListener {
            parentFragmentManager.beginTransaction()
                .replace(R.id.container, MapFragment.newInstance(pickLocation = true))
                .addToBackStack(null)
                .commit()
        }

        parentFragmentManager.setFragmentResultListener("location_pick", viewLifecycleOwner) { _, bundle ->
            currentLat = bundle.getDouble("lat")
            currentLng = bundle.getDouble("lng")
            updateLocationText()
        }

        binding.editEventRemoveAttachment.setOnClickListener {
            clearAttachment()
        }

        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.loading.collect { loading ->
                    binding.editEventProgress.isVisible = loading
                }
            }
        }

        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.errorMessage.collect { message ->
                    if (message != null) {
                        binding.editEventError.visibility = View.VISIBLE
                        binding.editEventError.text = message
                    } else {
                        binding.editEventError.visibility = View.GONE
                    }
                }
            }
        }

        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.saved.collect { saved ->
                    if (saved) {
                        Toast.makeText(requireContext(), "Событие сохранено", Toast.LENGTH_SHORT).show()
                        parentFragmentManager.popBackStack()
                    }
                }
            }
        }
    }

    private fun updateLocationText() {
        val b = _binding ?: return
        if (currentLat != null && currentLng != null) {
            b.editEventLocationText.isVisible = true
            b.editEventLocationText.text = String.format(
                Locale.getDefault(),
                "Локация: %.4f, %.4f",
                currentLat,
                currentLng
            )
        } else {
            b.editEventLocationText.isVisible = false
        }
    }

    private fun updateSpeakersText() {
        val b = _binding ?: return
        if (selectedSpeakerUserIds.isNotEmpty()) {
            b.editEventSpeakersText.visibility = View.VISIBLE
            b.editEventSpeakersText.text = "спикеры: " + selectedSpeakerUserIds.size
        } else {
            b.editEventSpeakersText.visibility = View.GONE
        }
    }

    private fun showDatePicker() {
        val calendar = Calendar.getInstance()
        if (eventDateMillis != null) {
            calendar.timeInMillis = eventDateMillis!!
        }

        val dateDialog = DatePickerDialog(
            requireContext(),
            { _, year, month, day ->
                val cal = Calendar.getInstance()
                cal.set(year, month, day)

                val timeDialog = TimePickerDialog(
                    requireContext(),
                    { _, hour, minute ->
                        cal.set(Calendar.HOUR_OF_DAY, hour)
                        cal.set(Calendar.MINUTE, minute)
                        eventDateMillis = cal.timeInMillis
                        val format = SimpleDateFormat("dd.MM.yyyy HH:mm", Locale.getDefault())
                        binding.editEventDateText.visibility = View.VISIBLE
                        binding.editEventDateText.text = format.format(Date(eventDateMillis!!))
                    },
                    calendar.get(Calendar.HOUR_OF_DAY),
                    calendar.get(Calendar.MINUTE),
                    true
                )
                timeDialog.show()
            },
            calendar.get(Calendar.YEAR),
            calendar.get(Calendar.MONTH),
            calendar.get(Calendar.DAY_OF_MONTH)
        )
        dateDialog.show()
    }

    private fun showAttachDialog() {
        AlertDialog.Builder(requireContext())
            .setTitle("вложение")
            .setItems(arrayOf("Галерея", "Видео", "Аудио")) { _, which ->
                if (which == 0) {
                    requestStorage(Manifest.permission.READ_MEDIA_IMAGES) {
                        pickImageLauncher.launch("image/*")
                    }
                } else if (which == 1) {
                    requestStorage(Manifest.permission.READ_MEDIA_VIDEO) {
                        pickVideoLauncher.launch("video/*")
                    }
                } else {
                    requestStorage(Manifest.permission.READ_MEDIA_AUDIO) {
                        pickAudioLauncher.launch("audio/*")
                    }
                }
            }
            .show()
    }

    private fun requestCameraAndOpen() {
        val granted = ContextCompat.checkSelfPermission(
            requireContext(),
            Manifest.permission.CAMERA
        ) == PackageManager.PERMISSION_GRANTED

        if (granted) {
            openCamera()
        } else {
            cameraPermissionLauncher.launch(Manifest.permission.CAMERA)
        }
    }

    private fun requestStorage(permission: String, action: () -> Unit) {
        var actualPermission = permission
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU) {
            actualPermission = Manifest.permission.READ_EXTERNAL_STORAGE
        }

        val granted = ContextCompat.checkSelfPermission(
            requireContext(),
            actualPermission
        ) == PackageManager.PERMISSION_GRANTED

        if (granted) {
            action()
        } else {
            pendingStorageAction = action
            storagePermissionLauncher.launch(actualPermission)
        }
    }

    private fun openCamera() {
        val photosDir = File(requireContext().cacheDir, "photos")
        if (!photosDir.exists()) {
            photosDir.mkdirs()
        }
        val photoFile = File(photosDir, "event_${System.currentTimeMillis()}.jpg")
        cameraPhotoUri = FileProvider.getUriForFile(
            requireContext(),
            requireContext().packageName + ".fileprovider",
            photoFile
        )
        takePhotoLauncher.launch(cameraPhotoUri)
    }

    private fun showAttachment(uri: Uri) {
        attachmentUri = uri
        existingAttachmentUrl = null
        existingAttachmentType = null
        val mimeType = FilePartUtils.getMimeType(requireContext(), uri)
        binding.editEventAttachmentBlock.visibility = View.VISIBLE

        if (mimeType.startsWith("image/")) {
            binding.editEventAttachmentPreview.visibility = View.VISIBLE
            Glide.with(this)
                .load(uri)
                .centerCrop()
                .into(binding.editEventAttachmentPreview)
        } else {
            binding.editEventAttachmentPreview.setImageDrawable(null)
            binding.editEventAttachmentPreview.visibility = View.VISIBLE
        }
    }

    private fun clearAttachment() {
        attachmentUri = null
        existingAttachmentUrl = null
        existingAttachmentType = null
        binding.editEventAttachmentBlock.visibility = View.GONE
        binding.editEventAttachmentPreview.setImageDrawable(null)
    }

    private fun saveEvent() {
        val content = binding.editEventText.text?.toString()?.trim() ?: ""
        if (content.isEmpty()) {
            Toast.makeText(requireContext(), "введите текст события", Toast.LENGTH_SHORT).show()
            return
        }

        var type = "ONLINE"
        if (binding.editEventTypeOffline.isChecked) {
            type = "OFFLINE"
        }


        viewModel.saveEvent(
            eventId = eventId,
            content = content,
            type = type,
            eventDateMillis = eventDateMillis,
            lat = currentLat,
            lng = currentLng,
            attachmentUri = attachmentUri,
            speakerUserIds = selectedSpeakerUserIds,
            existingAttachmentUrl = existingAttachmentUrl,
            existingAttachmentType = existingAttachmentType
        )
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
