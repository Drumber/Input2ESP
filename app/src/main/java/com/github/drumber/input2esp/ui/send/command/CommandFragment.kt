package com.github.drumber.input2esp.ui.send.command

import android.app.Dialog
import android.content.Context
import android.content.DialogInterface
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.InputMethodManager
import android.widget.ArrayAdapter
import android.widget.AutoCompleteTextView
import android.widget.Filterable
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
                    if(Preferences.placeholderCloseSearch) {
                        clearPlaceholderSearch()
                    }
                    return@setOnChildClickListener true
                }
                false
            }
        }

        // placeholder search
        binding.searchButton.setOnClickListener {
            viewModel.searchText = if(viewModel.searchText == null) {
                binding.appbarLayout.setExpanded(false) // collapse app bar
                ""
            } else {
                clearPlaceholderSearch()
                null
            }
            updatePlaceholderSearch()
        }
        // filter on text input
        binding.placeholderSearchEditText.addTextChangedListener { editable ->
            placeholderAdapter.filter.filter(editable.toString())
        }

        updatePlaceholderSearch()
    }

    private fun updatePlaceholderSearch() {
        // switch between visible text field or visible placeholder label
        if(viewModel.searchText == null) {
            (binding.placeholdersExpandableListView.adapter as Filterable).filter.filter(null)
            binding.apply {
                placeholderTextView.visibility = View.VISIBLE
                placeholderSearchEditText.visibility = View.GONE
                searchButton.setImageResource(R.drawable.ic_search)
            }
        } else {
            binding.apply {
                placeholderTextView.visibility = View.GONE
                placeholderSearchEditText.visibility = View.VISIBLE
                placeholderSearchEditText.requestFocus()
                searchButton.setImageResource(R.drawable.ic_close)
            }
            // show keyboard
            val imm = requireContext().getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
            imm.showSoftInput(binding.placeholderSearchEditText, InputMethodManager.SHOW_IMPLICIT)
        }
    }

    private fun clearPlaceholderSearch() {
        // reset filter and text field
        (binding.placeholdersExpandableListView.adapter as Filterable).filter.filter(null)
        binding.placeholderSearchEditText.text.clear()
        // hide keyboard
        val imm = requireContext().getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        view?.let { imm.hideSoftInputFromWindow(it.rootView.windowToken, 0) }
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