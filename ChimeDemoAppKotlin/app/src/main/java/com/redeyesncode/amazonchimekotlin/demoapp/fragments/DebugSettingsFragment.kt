package com.redeyesncode.amazonchimekotlin.demoapp.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.DialogFragment
import androidx.lifecycle.ViewModelProvider
import com.redeyesncode.amazonchimekotlin.R
import com.redeyesncode.amazonchimekotlin.databinding.FragmentDebugSettingsBinding
import com.redeyesncode.amazonchimekotlin.model.DebugSettingsViewModel

class DebugSettingsFragment : DialogFragment() {
    private lateinit var debugSettingsViewModel: DebugSettingsViewModel

    private lateinit var binding :FragmentDebugSettingsBinding

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {

        binding = FragmentDebugSettingsBinding.inflate(inflater,container,false);
        // Inflate the layout to use as embedded fragment
        return binding.root
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setStyle(STYLE_NO_TITLE, android.R.style.Theme_DeviceDefault_Light_NoActionBar)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        debugSettingsViewModel = ViewModelProvider(requireActivity()).get(DebugSettingsViewModel::class.java)
        binding.endpointUrlEditText.setText(debugSettingsViewModel.endpointUrl.value)
        binding.primaryMeetingIdEditText.setText(debugSettingsViewModel.primaryMeetingId.value)
        setupClickListeners(view)
    }


    private fun setupClickListeners(view: View) {
        binding.saveButton.setOnClickListener {
            debugSettingsViewModel.sendEndpointUrl(binding.endpointUrlEditText.text.toString())
            debugSettingsViewModel.sendPrimaryMeetingId(binding.primaryMeetingIdEditText.text.toString())
            dismiss()
        }
    }
}
