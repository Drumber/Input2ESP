package com.github.drumber.input2esp.ui.send.command

import android.app.Dialog
import android.content.DialogInterface
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.AutoCompleteTextView
import androidx.core.widget.addTextChangedListener
import androidx.fragment.app.DialogFragment
import androidx.lifecycle.ViewModelProvider
import com.github.drumber.input2esp.R
import com.github.drumber.input2esp.backend.data.Preferences
import com.github.drumber.input2esp.backend.models.CommandType
import com.github.drumber.input2esp.backend.models.Payload
import com.github.drumber.input2esp.backend.placeholders.PlaceholderManager
import com.github.drumber.input2esp.backend.utils.Callback
import com.github.drumber.input2esp.databinding.FragmentCommandBinding
import com.github.drumber.input2esp.ui.send.SendViewModel

class CommandFragment : DialogFragment() {

    private lateinit var viewModel: CommandViewModel

    private var _binding: FragmentCommandBinding? = null
    private val binding get() = _binding!!

    private var viewModelCreatedCallback: Callback<CommandViewModel>? = null

    override fun onStart() {
        super.onStart()
        // make the dialog fullscreen
        dialog?.apply {
            window?.setLayout(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT
            )
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setStyle(STYLE_NORMAL, R.style.Dialog_App_FullScreenDialog)
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        _binding = FragmentCommandBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val dialog = super.onCreateDialog(savedInstanceState)
        return dialog
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        viewModel = ViewModelProvider(this).get(CommandViewModel::class.java)
        viewModelCreatedCallback?.onCallback(viewModel)

        binding.commandToolbar.apply {
            setNavigationOnClickListener { dismiss() }
            setOnMenuItemClickListener {
                when(it.itemId) {
                    R.id.menu_delete_command -> {
                        // delete current payload and dismiss dialog
                        viewModel.deletePayload()
                        dismiss()
                        true
                    }
                    else -> false
                }
            }
        }

        viewModel.getPayload().observe(viewLifecycleOwner, {
            binding.payloadTextField.editText?.setText(it.payload)
            (binding.commandTypeTextField.editText as? AutoCompleteTextView)?.setText(it.type.name, false)
            binding.commandDelayTextField.editText?.setText(it.delay.toString())
        })

        // payload field
        binding.payloadTextField.editText?.apply {
            addTextChangedListener {
                viewModel.getPayload().value?.payload = text.toString()
            }
        }

        // command delay field
        binding.commandDelayTextField.editText?.apply {
            addTextChangedListener {
                viewModel.getPayload().value?.delay = text.toString().toIntOrNull() ?: 0
            }
        }
        binding.commandDelayTextField.setEndIconOnClickListener {
            binding.commandDelayTextField.editText?.setText(Preferences.defaultCommandDelay)
        }

        // command type text field
        val typeAdapter = ArrayAdapter(requireContext(), R.layout.dropdown_list_item, CommandType.values())
        typeAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        (binding.commandTypeTextField.editText as? AutoCompleteTextView)?.apply {
            setAdapter(typeAdapter)
            setOnItemClickListener { adapterView, view, i, l ->
                viewModel.getPayload().value?.type = CommandType.values()[i]
            }
        }

        // placeholder list
        val placeholderGroups = viewModel.getPlaceholderGroups()
        val placeholderAdapter = PlaceholderListAdapter(requireContext(),placeholderGroups.keys.toList(), placeholderGroups)
        binding.placeholdersExpandableListView.apply {
            setAdapter(placeholderAdapter)
            setOnChildClickListener { expandableListView, view, groupIndex, childIndex, l ->
                val placeholder = placeholderAdapter.getChild(groupIndex, childIndex)
                if(placeholder is String) {
                    binding.payloadTextField.editText?.append(viewModel.makePlaceholder(placeholder))
                    return@setOnChildClickListener true
                }
                false
            }
        }
    }

    override fun onDismiss(dialog: DialogInterface) {
        super.onDismiss(dialog)
        viewModel.sendViewModel?.notifyPayloadChange()
    }

    override fun onDestroy() {
        super.onDestroy()
        _binding = null
    }

    companion object {
        fun createInstance(payload: Payload, placeholderManager: PlaceholderManager, sendViewModel: SendViewModel): DialogFragment {
            return CommandFragment().also {
                it.viewModelCreatedCallback = Callback { viewModel ->
                    viewModel?.placeholderManager = placeholderManager
                    viewModel?.setPayload(payload)
                    viewModel?.sendViewModel = sendViewModel
                    // set callback to null, because we want set this only once
                    it.viewModelCreatedCallback = null
                }
            }
        }
    }

}