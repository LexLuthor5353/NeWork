package ru.netology.nework.feature.users

import android.app.AlertDialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import com.google.android.material.datepicker.MaterialDatePicker
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import ru.netology.nework.R
import ru.netology.nework.databinding.FragmentEditJobBinding
import java.text.SimpleDateFormat
import java.util.Locale
import javax.inject.Inject

@AndroidEntryPoint
class EditJobFragment : Fragment() {

    private var _binding: FragmentEditJobBinding? = null
    private val binding get() = _binding!!
    private var startDateMillis: Long? = null
    private var endDateMillis: Long? = null
    var isCreating = false
    var onJobSaved: (() -> Unit)? = null

    @Inject
    lateinit var usersRepository: UsersRepository

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentEditJobBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.editJobDatesButton.setOnClickListener {
            showDateRangePicker()
        }

        binding.editJobSaveButton.setOnClickListener {
            saveJob()
        }
    }

    private fun showDateRangePicker() {
        val picker = MaterialDatePicker.Builder.dateRangePicker()
            .setTitleText("Enter dates")
            .build()
        picker.show(parentFragmentManager, "date_picker")
        picker.addOnPositiveButtonClickListener { selection ->
            startDateMillis = selection.first
            endDateMillis = if (selection.second == -1L) {
                null
            } else {
                selection.second
            }
            updateDatesText()
        }
    }

    private fun updateDatesText() {
        val format = SimpleDateFormat("dd.MM.yyyy", Locale.getDefault())
        var text = ""
        if (startDateMillis != null) {
            text = format.format(java.util.Date(startDateMillis!!))
        }
        if (endDateMillis != null) {
            text = text + " — " + format.format(java.util.Date(endDateMillis!!))
        } else {
            if (text.isNotEmpty()) {
                text = text + " — н.в."
            }
        }
        if (text.isNotEmpty()) {
            binding.editJobDatesText.visibility = View.VISIBLE
            binding.editJobDatesText.text = text
        } else {
            binding.editJobDatesText.visibility = View.GONE
        }
    }

    private fun saveJob() {
        val company = binding.editJobCompany.text?.toString()?.trim() ?: ""
        val position = binding.editJobPosition.text?.toString()?.trim() ?: ""
        val link = binding.editJobLink.text?.toString()?.trim()

        if (company.isEmpty() || position.isEmpty()) {
            AlertDialog.Builder(requireContext())
                .setTitle("ошибка")
                .setMessage("заполните название компании и должность")
                .setPositiveButton("ок", null)
                .show()
            return
        }

        val startStr = if (startDateMillis != null) {
            java.time.Instant.ofEpochMilli(startDateMillis!!).toString()
        } else {
            java.time.Instant.now().toString()
        }

        val endStr = if (endDateMillis != null) {
            java.time.Instant.ofEpochMilli(endDateMillis!!).toString()
        } else {
            null
        }

        viewLifecycleOwner.lifecycleScope.launch {
            try {
                usersRepository.createJob(company, position, link, startStr, endStr)
                onJobSaved?.invoke()
                parentFragmentManager.popBackStack()
            } catch (exception: Exception) {
                AlertDialog.Builder(requireContext())
                    .setTitle("ошибка")
                    .setMessage("не удалось сохранить работу")
                    .setPositiveButton("ок", null)
                    .show()
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
