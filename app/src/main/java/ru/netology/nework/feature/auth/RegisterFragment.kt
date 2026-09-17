package ru.netology.nework.feature.auth

import android.Manifest
import android.content.pm.PackageManager
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.View
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.bumptech.glide.Glide
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import ru.netology.nework.MainActivity
import ru.netology.nework.R
import ru.netology.nework.databinding.FragmentRegisterBinding
import java.io.File

@AndroidEntryPoint
class RegisterFragment : Fragment(R.layout.fragment_register) {

    private var _binding: FragmentRegisterBinding? = null
    private val binding get() = _binding!!
    private val viewModel: AuthViewModel by viewModels()

    private var avatarUri: Uri? = null
    private var cameraPhotoUri: Uri? = null

    private val formWatcher = object : TextWatcher {
        override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}

        override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}

        override fun afterTextChanged(s: Editable?) {
            updateRegisterButton()
        }
    }

    private val pickAvatarLauncher = registerForActivityResult(
        ActivityResultContracts.GetContent()
    ) { uri ->
        if (uri != null) {
            checkAndShowAvatar(uri)
        }
    }

    private val takePhotoLauncher = registerForActivityResult(
        ActivityResultContracts.TakePicture()
    ) { success ->
        if (success && cameraPhotoUri != null) {
            checkAndShowAvatar(cameraPhotoUri!!)
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

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        _binding = FragmentRegisterBinding.bind(view)

        binding.registerLoginEdit.addTextChangedListener(formWatcher)
        binding.registerNameEdit.addTextChangedListener(formWatcher)
        binding.registerPasswordEdit.addTextChangedListener(formWatcher)
        binding.registerPasswordRepeatEdit.addTextChangedListener(formWatcher)
        updateRegisterButton()

        binding.registerAvatarBlock.setOnClickListener {
            showAvatarPickerDialog()
        }

        binding.registerGoLogin.setOnClickListener {
            val activity = requireActivity()
            if (activity is MainActivity) {
                activity.openLogin(replaceOnly = true)
            }
        }

        binding.registerButton.setOnClickListener {
            val login = binding.registerLoginEdit.text?.toString()?.trim() ?: ""
            val name = binding.registerNameEdit.text?.toString()?.trim() ?: ""
            val password = binding.registerPasswordEdit.text?.toString()?.trim() ?: ""
            val passwordRepeat = binding.registerPasswordRepeatEdit.text?.toString()?.trim() ?: ""

            binding.registerLoginLayout.error = null
            binding.registerNameLayout.error = null
            binding.registerPasswordLayout.error = null
            binding.registerPasswordRepeatLayout.error = null

            var hasError = false
            if (login.isEmpty()) {
                binding.registerLoginLayout.error = getString(R.string.login_empty_error)
                hasError = true
            }
            if (name.isEmpty()) {
                binding.registerNameLayout.error = getString(R.string.name_empty_error)
                hasError = true
            }
            if (password.isEmpty()) {
                binding.registerPasswordLayout.error = getString(R.string.password_empty_error)
                hasError = true
            }
            if (passwordRepeat != password) {
                binding.registerPasswordRepeatLayout.error = getString(R.string.passwords_not_match)
                hasError = true
            }
            if (hasError) {
                return@setOnClickListener
            }

            viewModel.register(login, password, name, avatarUri)
        }

        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.loading.collect { loading ->
                    binding.registerButton.isEnabled = !loading && isFormValid()
                    binding.registerProgress.isVisible = loading
                }
            }
        }

        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.errorMessage.collect { message ->
                    if (message != null) {
                        Toast.makeText(requireContext(), message, Toast.LENGTH_SHORT).show()
                        viewModel.clearError()
                    }
                }
            }
        }

        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.success.collect { success ->
                    if (success) {
                        viewModel.resetState()
                        parentFragmentManager.popBackStack()
                    }
                }
            }
        }
    }

    private fun showAvatarPickerDialog() {
        AlertDialog.Builder(requireContext())
            .setTitle(R.string.pick_avatar_title)
            .setItems(
                arrayOf(
                    getString(R.string.pick_avatar_camera),
                    getString(R.string.pick_avatar_gallery)
                )
            ) { _, which ->
                if (which == 0) {
                    requestCameraAndOpen()
                } else {
                    pickAvatarLauncher.launch("image/*")
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

    private fun openCamera() {
        val photosDir = File(requireContext().cacheDir, "photos")
        if (!photosDir.exists()) {
            photosDir.mkdirs()
        }
        val photoFile = File(photosDir, "avatar_${System.currentTimeMillis()}.jpg")
        cameraPhotoUri = FileProvider.getUriForFile(
            requireContext(),
            requireContext().packageName + ".fileprovider",
            photoFile
        )
        takePhotoLauncher.launch(cameraPhotoUri)
    }

    private fun checkAndShowAvatar(uri: Uri) {
        val mimeType = requireContext().contentResolver.getType(uri)
        if (mimeType != "image/jpeg" && mimeType != "image/png") {
            Toast.makeText(requireContext(), "нужен формат jpeg или png", Toast.LENGTH_SHORT).show()
            return
        }

        val options = BitmapFactory.Options()
        options.inJustDecodeBounds = true
        try {
            requireContext().contentResolver.openInputStream(uri)?.use { stream ->
                BitmapFactory.decodeStream(stream, null, options)
            }
        } catch (exception: Exception) {
            Toast.makeText(requireContext(), "не удалось прочитать изображение", Toast.LENGTH_SHORT).show()
            return
        }

        if (options.outWidth > 2048 || options.outHeight > 2048) {
            Toast.makeText(requireContext(), "картинка больше 2048x2048", Toast.LENGTH_SHORT).show()
            return
        }

        showAvatar(uri)
    }

    private fun showAvatar(uri: Uri) {
        avatarUri = uri
        binding.registerAvatarIcon.isVisible = false
        Glide.with(this)
            .load(uri)
            .centerCrop()
            .into(binding.registerAvatarPreview)
    }

    private fun isFormValid(): Boolean {
        val login = binding.registerLoginEdit.text?.toString()?.trim() ?: ""
        val name = binding.registerNameEdit.text?.toString()?.trim() ?: ""
        val password = binding.registerPasswordEdit.text?.toString()?.trim() ?: ""
        val passwordRepeat = binding.registerPasswordRepeatEdit.text?.toString()?.trim() ?: ""
        return login.isNotEmpty() &&
                name.isNotEmpty() &&
                password.isNotEmpty() &&
                passwordRepeat.isNotEmpty() &&
                password == passwordRepeat
    }

    private fun updateRegisterButton() {
        binding.registerButton.isEnabled = isFormValid()
    }

    override fun onDestroyView() {
        binding.registerLoginEdit.removeTextChangedListener(formWatcher)
        binding.registerNameEdit.removeTextChangedListener(formWatcher)
        binding.registerPasswordEdit.removeTextChangedListener(formWatcher)
        binding.registerPasswordRepeatEdit.removeTextChangedListener(formWatcher)
        super.onDestroyView()
        _binding = null
    }
}
