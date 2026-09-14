package ru.netology.nework.feature.events

import android.Manifest
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
import java.util.Date
import java.util.Locale

@AndroidEntryPoint
class EditEventFragment : Fragment(R.layout.fragment_edit_event) {

    private var _binding: FragmentEditEventBinding? = null
    private val binding get() = _binding!!
    private val viewModel: EditEventViewModel by viewModels()
    private var attachmentUri: Uri? = null
    private var cameraPhotoUri: Uri? = null
    private var pendingStorageAction: (() -> Unit)? = null
    private var selectedSpeakerUserIds: MutableList<String> = mutableListOf()
    private var eventDateMillis: Long? = null
    private var currentLocation: String = ""

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

        parentFragmentManager.setFragmentResultListener("location_pick", viewLifecycleOwner) { requestKey, bundle ->
            val lat = bundle.getDouble("lat")
            val lng = bundle.getDouble("lng")
            currentLocation = String.format("%.4f, %.4f", lat, lng)
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

    private fun showDatePicker() {
        val calendar = java.util.Calendar.getInstance()
        if (eventDateMillis != null) {
            calendar.timeInMillis = eventDateMillis!!
        }
        val datePicker = android.app.DatePickerDialog(requireContext(),
            { _, year, month, day ->
                val cal = java.util.Calendar.getInstance()
                cal.set(year, month, day)
                val timePicker = android.app.TimePickerDialog(requireContext(),
                    { _, hour, minute ->
                        cal.set(java.util.Calendar.HOUR_OF_DAY, hour)
                        cal.set(java.util.Calendar.MINUTE, minute)
                        eventDateMillis = cal.timeInMillis
                        val format = SimpleDateFormat("dd.MM.yyyy HH:mm", Locale.getDefault())
                        binding.editEventDateText.visibility = View.VISIBLE
                        binding.editEventDateText.text = format.format(Date(eventDateMillis!!))
                    },
                    calendar.get(java.util.Calendar.HOUR_OF_DAY),
                    calendar.get(java.util.Calendar.MINUTE),
                    true)
                timePicker.show()
            },
            calendar.get(java.util.Calendar.YEAR),
            calendar.get(java.util.Calendar.MONTH),
            calendar.get(java.util.Calendar.DAY_OF_MONTH))
        datePicker.show()
    }

    private fun showAttachDialog() {
        AlertDialog.Builder(requireContext())
            .setTitle("вложение")
            .setItems(arrayOf("Галерея", "Видео", "Аудио")) { _, which ->
                when (which) {
                    0 -> requestStorage(Manifest.permission.READ_MEDIA_IMAGES) {
                        pickImageLauncher.launch("image/*")
                    }
                    1 -> requestStorage(Manifest.permission.READ_MEDIA_VIDEO) {
                        pickVideoLauncher.launch("video/*")
                    }
                    2 -> requestStorage(Manifest.permission.READ_MEDIA_AUDIO) {
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
        val actualPermission = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            permission
        } else {
            Manifest.permission.READ_EXTERNAL_STORAGE
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
        binding.editEventAttachmentBlock.visibility = View.GONE
        binding.editEventAttachmentPreview.setImageDrawable(null)
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

    private fun saveEvent() {
        val content = binding.editEventText.text?.toString()?.trim() ?: ""
        if (content.isEmpty()) {
            Toast.makeText(requireContext(), "введите текст события", Toast.LENGTH_SHORT).show()
            return
        }
        val isOnline = binding.editEventTypeOnline.isChecked
        val type = if (isOnline) "ONLINE" else "OFFLINE"
        viewModel.saveEvent(content, attachmentUri, type, eventDateMillis, selectedSpeakerUserIds)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
