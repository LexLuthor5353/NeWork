package ru.netology.nework.feature.auth

import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.View
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import ru.netology.nework.MainActivity
import ru.netology.nework.R
import ru.netology.nework.databinding.FragmentLoginBinding

@AndroidEntryPoint
class LoginFragment : Fragment(R.layout.fragment_login) {

    private var _binding: FragmentLoginBinding? = null
    private val binding get() = _binding!!
    private val viewModel: AuthViewModel by viewModels()

    private val formWatcher = object : TextWatcher {
        override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}

        override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}

        override fun afterTextChanged(s: Editable?) {
            updateLoginButton()
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        _binding = FragmentLoginBinding.bind(view)

        binding.loginLoginEdit.addTextChangedListener(formWatcher)
        binding.loginPasswordEdit.addTextChangedListener(formWatcher)
        updateLoginButton()

        binding.loginGoRegister.setOnClickListener {
            val activity = requireActivity()
            if (activity is MainActivity) {
                activity.openRegister(replaceOnly = true)
            }
        }

        binding.loginButton.setOnClickListener {
            val login = binding.loginLoginEdit.text?.toString()?.trim() ?: ""
            val password = binding.loginPasswordEdit.text?.toString()?.trim() ?: ""

            binding.loginLoginLayout.error = null
            binding.loginPasswordLayout.error = null
            binding.loginError.visibility = View.GONE

            var hasError = false
            if (login.isEmpty()) {
                binding.loginLoginLayout.error = getString(R.string.login_empty_error)
                hasError = true
            }
            if (password.isEmpty()) {
                binding.loginPasswordLayout.error = getString(R.string.password_empty_error)
                hasError = true
            }
            if (hasError) {
                return@setOnClickListener
            }

            viewModel.login(login, password)
        }

        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.loading.collect { loading ->
                    binding.loginButton.isEnabled = !loading && isFormValid()
                    binding.loginProgress.isVisible = loading
                }
            }
        }

        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.errorMessage.collect { message ->
                    if (message != null) {
                        binding.loginError.visibility = View.VISIBLE
                        binding.loginError.text = message
                    } else {
                        binding.loginError.visibility = View.GONE
                    }
                }
            }
        }

        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.success.collect { success ->
                    if (success) {
                        parentFragmentManager.popBackStack()
                    }
                }
            }
        }
    }

    private fun isFormValid(): Boolean {
        val login = binding.loginLoginEdit.text?.toString()?.trim() ?: ""
        val password = binding.loginPasswordEdit.text?.toString()?.trim() ?: ""
        return login.isNotEmpty() && password.isNotEmpty()
    }

    private fun updateLoginButton() {
        binding.loginButton.isEnabled = isFormValid()
    }

    override fun onDestroyView() {
        binding.loginLoginEdit.removeTextChangedListener(formWatcher)
        binding.loginPasswordEdit.removeTextChangedListener(formWatcher)
        super.onDestroyView()
        _binding = null
    }
}
