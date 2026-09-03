package ru.netology.nework.feature.posts

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
import ru.netology.nework.databinding.FragmentEditPostBinding
import ru.netology.nework.feature.common.MapFragment
import java.io.File

@AndroidEntryPoint
class EditPostFragment : Fragment(R.layout.fragment_edit_post) {

    private var _binding: FragmentEditPostBinding? = null
    private val binding get() = _binding!!
    private val viewModel: EditPostViewModel by viewModels()
    private var attachmentUri: Uri? = null
    private var cameraPhotoUri: Uri? = null
    private var pendingStorageAction: (() -> Unit)? = null

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
        _binding = FragmentEditPostBinding.bind(view)

        requireActivity().addMenuProvider(object : MenuProvider {
            override fun onCreateMenu(menu: Menu, menuInflater: MenuInflater) {
                menuInflater.inflate(R.menu.menu_save, menu)
            }

            override fun onMenuItemSelected(menuItem: MenuItem): Boolean {
                if (menuItem.itemId == R.id.actionSave) {
                    savePost()
                    return true
                }
                return false
            }
        }, viewLifecycleOwner, Lifecycle.State.RESUMED)

        binding.editPostPhotoButton.setOnClickListener {
            requestCameraAndOpen()
        }

        binding.editPostAttachButton.setOnClickListener {
            showAttachDialog()
        }

        binding.editPostMentionButton.setOnClickListener {
            parentFragmentManager.beginTransaction()
                .replace(R.id.container, ru.netology.nework.feature.users.UsersSelectFragment())
                .addToBackStack(null)
                .commit()
        }

        binding.editPostLocationButton.setOnClickListener {
            parentFragmentManager.beginTransaction()
                .replace(R.id.container, MapFragment.newInstance(pickLocation = true))
                .addToBackStack(null)
                .commit()
        }

        binding.editPostRemoveAttachment.setOnClickListener {
            clearAttachment()
        }

        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.loading.collect { loading ->
                    binding.editPostProgress.isVisible = loading
                }
            }
        }

        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.errorMessage.collect { message ->
                    if (message != null) {
                        binding.editPostError.visibility = View.VISIBLE
                        binding.editPostError.text = message
                    } else {
                        binding.editPostError.visibility = View.GONE
                    }
                }
            }
        }

        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.saved.collect { saved ->
                    if (saved) {
                        Toast.makeText(requireContext(), "Пост сохранён", Toast.LENGTH_SHORT).show()
                        parentFragmentManager.popBackStack()
                    }
                }
            }
        }
    }

    private fun showAttachDialog() {
        AlertDialog.Builder(requireContext())
            .setTitle(R.string.attach_title)
            .setItems(
                arrayOf(
                    getString(R.string.attach_gallery),
                    getString(R.string.attach_video),
                    getString(R.string.attach_audio)
                )
            ) { _, which ->
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
        val photoFile = File(photosDir, "post_${System.currentTimeMillis()}.jpg")
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
        binding.editPostAttachmentBlock.visibility = View.VISIBLE

        if (mimeType.startsWith("image/")) {
            binding.editPostAttachmentPreview.visibility = View.VISIBLE
            binding.editPostAttachmentName.visibility = View.GONE
            Glide.with(this)
                .load(uri)
                .centerCrop()
                .into(binding.editPostAttachmentPreview)
        } else {
            binding.editPostAttachmentPreview.setImageDrawable(null)
            binding.editPostAttachmentPreview.visibility = View.VISIBLE
            binding.editPostAttachmentName.visibility = View.VISIBLE
            binding.editPostAttachmentName.text = uri.lastPathSegment ?: getString(R.string.attachment_placeholder)
        }
    }

    private fun clearAttachment() {
        attachmentUri = null
        binding.editPostAttachmentBlock.visibility = View.GONE
        binding.editPostAttachmentPreview.setImageDrawable(null)
        binding.editPostAttachmentName.visibility = View.GONE
    }

    private fun savePost() {
        val content = binding.editPostText.text?.toString()?.trim() ?: ""
        if (content.isEmpty()) {
            Toast.makeText(requireContext(), R.string.post_text_hint, Toast.LENGTH_SHORT).show()
            return
        }
        viewModel.savePost(content, attachmentUri)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
