package com.github.ParagonVirtuoso.memorias.presentation.memory

import android.app.DatePickerDialog
import android.app.Dialog
import android.app.TimePickerDialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import com.github.ParagonVirtuoso.memorias.databinding.DialogMemorySchedulerBinding
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale
import com.google.android.material.snackbar.Snackbar

@AndroidEntryPoint
class MemorySchedulerDialog : BottomSheetDialogFragment() {

    private var _binding: DialogMemorySchedulerBinding? = null
    private val binding get() = _binding!!

    private val viewModel: MemorySchedulerViewModel by viewModels()
    private val selectedDate: Calendar = Calendar.getInstance()
    private var onScheduleCallback: ((Long) -> Unit)? = null

    private var videoId: String? = null
    private var videoTitle: String? = null
    private var videoThumbnail: String? = null

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val dialog = super.onCreateDialog(savedInstanceState)
        dialog.window?.setBackgroundDrawableResource(android.R.color.transparent)
        return dialog
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = DialogMemorySchedulerBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupDialog()
        setupClickListeners()
        updateDateTimeTexts()
        observeViewModel()
    }

    private fun setupDialog() {
        binding.apply {
            titleTextView.text = videoTitle
            
            // Configurar data inicial como amanhã no mesmo horário
            selectedDate.add(Calendar.DAY_OF_MONTH, 1)
            updateDateTimeTexts()
        }
    }

    private fun setupClickListeners() {
        binding.apply {
            datePickerButton.setOnClickListener {
                showDatePicker()
            }
            timePickerButton.setOnClickListener {
                showTimePicker()
            }
            scheduleButton.setOnClickListener {
                scheduleMemory()
            }
            cancelButton.setOnClickListener {
                dismiss()
            }
        }
    }

    private fun showDatePicker() {
        val datePickerDialog = DatePickerDialog(
            requireContext(),
            { _, year, month, dayOfMonth ->
                selectedDate.set(Calendar.YEAR, year)
                selectedDate.set(Calendar.MONTH, month)
                selectedDate.set(Calendar.DAY_OF_MONTH, dayOfMonth)
                updateDateTimeTexts()
            },
            selectedDate.get(Calendar.YEAR),
            selectedDate.get(Calendar.MONTH),
            selectedDate.get(Calendar.DAY_OF_MONTH)
        )
        datePickerDialog.show()
    }

    private fun showTimePicker() {
        val timePickerDialog = TimePickerDialog(
            requireContext(),
            { _, hourOfDay, minute ->
                selectedDate.set(Calendar.HOUR_OF_DAY, hourOfDay)
                selectedDate.set(Calendar.MINUTE, minute)
                updateDateTimeTexts()
            },
            selectedDate.get(Calendar.HOUR_OF_DAY),
            selectedDate.get(Calendar.MINUTE),
            true
        )
        timePickerDialog.show()
    }

    private fun updateDateTimeTexts() {
        val dateFormat = SimpleDateFormat("dd/MM/yyyy", Locale("pt", "BR"))
        val timeFormat = SimpleDateFormat("HH:mm", Locale("pt", "BR"))

        binding.selectedDateText.text = dateFormat.format(selectedDate.time)
        binding.selectedTimeText.text = timeFormat.format(selectedDate.time)
    }

    private fun scheduleMemory() {
        val videoId = this.videoId
        val videoTitle = this.videoTitle
        val videoThumbnail = this.videoThumbnail

        if (videoId != null && videoTitle != null && videoThumbnail != null) {
            viewModel.scheduleMemory(
                videoId = videoId,
                videoTitle = videoTitle,
                videoThumbnail = videoThumbnail,
                notificationTime = selectedDate.time
            )
        }
    }

    private fun observeViewModel() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.uiState.collect { state ->
                when (state) {
                    is MemorySchedulerState.Success -> {
                        onScheduleCallback?.invoke(selectedDate.timeInMillis)
                        viewModel.resetState()
                        dismissAllowingStateLoss()
                    }
                    is MemorySchedulerState.Error -> {
                        showError(state.message)
                        viewModel.resetState()
                    }
                }
            }
        }
    }

    private fun showError(message: String) {
        Snackbar.make(
            requireDialog().window?.decorView ?: requireView(),
            message,
            Snackbar.LENGTH_LONG
        ).show()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    companion object {
        const val TAG = "MemorySchedulerDialog"

        fun newInstance(
            videoId: String,
            videoTitle: String,
            videoThumbnail: String,
            onScheduleCallback: (Long) -> Unit
        ): MemorySchedulerDialog {
            return MemorySchedulerDialog().apply {
                this.videoId = videoId
                this.videoTitle = videoTitle
                this.videoThumbnail = videoThumbnail
                this.onScheduleCallback = onScheduleCallback
            }
        }
    }
}