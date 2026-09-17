package ru.netology.nework.feature.users

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.google.android.material.datepicker.MaterialDatePicker
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import ru.netology.nework.databinding.FragmentEditJobBinding
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@AndroidEntryPoint
class EditJobFragment : Fragment() {

    private var _binding: FragmentEditJobBinding? = null
    private val binding get() = _binding!!
    private val viewModel: EditJobViewModel by viewModels()

    private var jobId: String? = null
    private var startDateMillis: Long? = null
    private var endDateMillis: Long? = null

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

        jobId = arguments?.getString("jobId")
        val initialCompany = arguments?.getString("company").orEmpty()
        val initialPosition = arguments?.getString("position").orEmpty()
        val initialLink = arguments?.getString("link")
        val initialStart = arguments?.getLong("startAt", 0L) ?: 0L
        val initialFinish = arguments?.getLong("finishAt", 0L) ?: 0L

        binding.editJobCompany.setText(initialCompany)
        binding.editJobPosition.setText(initialPosition)
        binding.editJobLink.setText(initialLink.orEmpty())

        if (initialStart > 0) {
            startDateMillis = initialStart
        }
        if (initialFinish > 0) {
            endDateMillis = initialFinish
            binding.editJobStillWorking.isChecked = false
        } else if (initialStart > 0) {
            binding.editJobStillWorking.isChecked = true
        }
        updateDatesText()

        binding.editJobDatesButton.setOnClickListener {
            showStartDatePicker()
        }

        binding.editJobEndDateButton.setOnClickListener {
            showEndDatePicker()
        }

        binding.editJobStillWorking.setOnCheckedChangeListener { _, checked ->
            if (checked) {
                endDateMillis = null
                binding.editJobEndDateButton.isEnabled = false
            } else {
                binding.editJobEndDateButton.isEnabled = true
            }
            updateDatesText()
        }

        binding.editJobSaveButton.setOnClickListener {
            saveJob()
        }

        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.loading.collect { loading ->
                    binding.editJobProgress.isVisible = loading
                    binding.editJobSaveButton.isEnabled = !loading
                }
            }
        }

        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.errorMessage.collect { message ->
                    if (message != null) {
                        binding.editJobError.visibility = View.VISIBLE
                        binding.editJobError.text = message
                    } else {
                        binding.editJobError.visibility = View.GONE
                    }
                }
            }
        }

        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.saved.collect { saved ->
                    if (saved) {
                        Toast.makeText(requireContext(), "Работа сохранена", Toast.LENGTH_SHORT).show()
                        parentFragmentManager.setFragmentResult("job_saved", Bundle())
                        parentFragmentManager.popBackStack()
                    }
                }
            }
        }
    }

    private fun showStartDatePicker() {
        val picker = MaterialDatePicker.Builder.datePicker()
            .setTitleText("Начало работы")
            .build()
        picker.addOnPositiveButtonClickListener { selection ->
            startDateMillis = selection
            updateDatesText()
        }
        picker.show(parentFragmentManager, "start_picker")
    }

    private fun showEndDatePicker() {
        val picker = MaterialDatePicker.Builder.datePicker()
            .setTitleText("Окончание работы")
            .build()
        picker.addOnPositiveButtonClickListener { selection ->
            endDateMillis = selection
            updateDatesText()
        }
        picker.show(parentFragmentManager, "end_picker")
    }

    private fun updateDatesText() {
        val format = SimpleDateFormat("dd.MM.yyyy", Locale.getDefault())
        var text = ""
        if (startDateMillis != null) {
            text = format.format(Date(startDateMillis!!))
        }
        if (endDateMillis != null) {
            text = text + " — " + format.format(Date(endDateMillis!!))
        } else if (text.isNotEmpty()) {
            text = text + " — н.в."
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

        binding.editJobCompanyLayout.error = null
        binding.editJobPositionLayout.error = null

        var hasError = false
        if (company.isEmpty()) {
            binding.editJobCompanyLayout.error = "введите название компании"
            hasError = true
        }
        if (position.isEmpty()) {
            binding.editJobPositionLayout.error = "введите должность"
            hasError = true
        }
        if (hasError) {
            return
        }

        viewModel.saveJob(
            jobId = jobId,
            company = company,
            position = position,
            link = if (link.isNullOrEmpty()) null else link,
            startAt = startDateMillis,
            finishAt = endDateMillis
        )
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
