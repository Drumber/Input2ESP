package com.github.drumber.input2esp.ui.setups

import android.os.Bundle
import android.view.*
import android.widget.ArrayAdapter
import android.widget.AutoCompleteTextView
import android.widget.Toast
import androidx.activity.OnBackPressedCallback
import androidx.core.widget.addTextChangedListener
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import com.github.drumber.input2esp.R
import com.github.drumber.input2esp.backend.models.ESPDeviceModel
import com.github.drumber.input2esp.databinding.EspDeviceSetupFragmentBinding
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.transition.MaterialFadeThrough

class ESPDeviceSetupFragment : Fragment() {

    private lateinit var viewModel: ESPDeviceSetupViewModel
    private val args: ESPDeviceSetupFragmentArgs by navArgs()

    private var _binding: EspDeviceSetupFragmentBinding? = null
    private val binding get() = _binding!!

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enterTransition = MaterialFadeThrough()
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        _binding = EspDeviceSetupFragmentBinding.inflate(inflater, container, false)

        setHasOptionsMenu(true)
        return binding.root
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        viewModel = ViewModelProvider(this).get(ESPDeviceSetupViewModel::class.java)

        // set device from args (can be null)
        val existingDeviceId: Int? = args.existingDevice?.toIntOrNull()
        viewModel.setDeviceModel(existingDeviceId)

        // initialize components
        binding.deviceNameTextField.editText?.apply {
            setText(viewModel.name)
            addTextChangedListener { viewModel.name = text.toString() }
        }
        binding.deviceHostnameTextField.editText?.apply {
            setText(viewModel.hostname)
            addTextChangedListener { viewModel.hostname = text.toString() }
        }
        binding.devicePortTextField.editText?.apply {
            setText((viewModel.port ?: "").toString())
            addTextChangedListener { viewModel.port = text.toString().toIntOrNull() }
        }

        // protocol mode dropdown
        val protocolList = ESPDeviceModel.Provider.values().map { it.name }
        val protocolAdapter = ArrayAdapter(requireContext(), R.layout.dropdown_list_item, protocolList)
        (binding.protocolModeTextField.editText as? AutoCompleteTextView)?.apply {
            setAdapter(protocolAdapter)
            setText((viewModel.protocol ?: ESPDeviceModel.Provider.TCP).name, false)
            setOnItemClickListener { adapterView, view, i, l ->
                viewModel.protocol = protocolAdapter.getItem(i)?.let { ESPDeviceModel.Provider.valueOf(it) }
            }
        }

        // add on back pressed callback
        activity?.onBackPressedDispatcher?.addCallback(viewLifecycleOwner, object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                if(viewModel.isDeviceEdited()) {
                    showSaveConfirmationDialog()
                } else {
                    isEnabled = false
                    navigateUp()
                }
            }
        })
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.menu_esp_device_setup, menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when(item.itemId) {
            R.id.menu_save_device -> onSaveDeviceAction()
            R.id.menu_delete_device -> onDeleteDeviceAction()
        }
        return super.onOptionsItemSelected(item)
    }

    private fun onSaveDeviceAction() {
        // TODO: check if fields are not empty
        viewModel.saveDeviceModel()
        Toast.makeText(requireContext(), getString(R.string.message_device_saved), Toast.LENGTH_SHORT).show()
        navigateUp()
    }

    private fun onDeleteDeviceAction() {
        MaterialAlertDialogBuilder(requireContext())
            .setTitle(getString(R.string.dialog_title_delete_device))
            .setMessage(getString(R.string.dialog_message_delete_device))
            .setNegativeButton(getString(R.string.action_cancel)) { dialog, which ->
                dialog.cancel()
            }
            .setPositiveButton(getString(R.string.action_delete)) { dialog, which ->
                onDeleteDeviceAccepted()
            }
            .show()
    }

    private fun onDeleteDeviceAccepted() {
        viewModel.deleteDeviceModel()
        navigateUp()
    }

    private fun showSaveConfirmationDialog() {
        MaterialAlertDialogBuilder(requireContext())
                .setTitle(getString(R.string.dialog_title_save_changes))
                .setMessage(getString(R.string.dialog_message_save_changes))
                .setNegativeButton(getString(R.string.action_discard)) { dialog, which ->
                    // go back without saving changes
                    findNavController().navigateUp()
                }
                .setPositiveButton(getString(R.string.action_save)) { dialog, which ->
                    onSaveDeviceAction()
                }
                .show()
    }

    private fun navigateUp() = findNavController().navigateUp()

    override fun onDestroy() {
        super.onDestroy()
        _binding = null
    }

}