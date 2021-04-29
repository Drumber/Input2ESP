package com.github.drumber.input2esp.ui.main

import android.os.Bundle
import android.view.*
import android.widget.AdapterView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.view.ActionMode
import androidx.core.content.res.ResourcesCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.FragmentNavigatorExtras
import androidx.navigation.fragment.findNavController
import com.github.drumber.input2esp.R
import com.github.drumber.input2esp.backend.Constants
import com.github.drumber.input2esp.backend.data.Preferences
import com.github.drumber.input2esp.backend.utils.CommonUtils
import com.github.drumber.input2esp.databinding.MainFragmentBinding
import com.github.drumber.input2esp.ui.components.DeviceListAdapter
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.transition.Hold
import com.google.android.material.transition.MaterialFadeThrough

class MainFragment : Fragment() {

    private lateinit var viewModel: MainViewModel

    private var _binding: MainFragmentBinding? = null
    private val binding get() = _binding!!

    private var devicesActionMode: ActionMode? = null

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View {
        _binding = MainFragmentBinding.inflate(inflater, container, false)

        exitTransition = MaterialFadeThrough()
        setHasOptionsMenu(true)

        return binding.root
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        viewModel = ViewModelProvider(this).get(MainViewModel::class.java)

        // create ArrayAdapter for the devices ListView
        val deviceListAdapter = DeviceListAdapter(requireContext(), R.layout.item_manage_device, mutableListOf())
        binding.devicesListView.adapter = deviceListAdapter
        // add device setting clicked listener
        deviceListAdapter.setDeviceSettingClickedListener { onEditDeviceAction(it) }
        // add ListView long clicked listener
        binding.devicesListView.setOnItemLongClickListener { adapterView, view, i, id ->
            devicesActionMode?.finish() // close existing action mode
            devicesActionMode = (activity as AppCompatActivity).startSupportActionMode(deviceListActionModeCallback.apply { actionItemIndex = i })
            true
        }

        // observe device list for updating the ArrayAdapter
        viewModel.getDeviceList().observe(viewLifecycleOwner, {
            // hide 'empty device list' message
            if(it.isNotEmpty()) binding.emptyDeviceListLayout.visibility = View.GONE
            else binding.emptyDeviceListLayout.visibility = View.VISIBLE
            // update ArrayAdapter with new data
            deviceListAdapter.clear()
            deviceListAdapter.addAll(it)
            deviceListAdapter.notifyDataSetChanged()
        })

        binding.devicesListView.onItemClickListener = AdapterView.OnItemClickListener { adapterView, view, i, l ->
            val deviceId = deviceListAdapter.getItem(i)?.id
            if(deviceId != null) {
                onClickDeviceAction(deviceId, view)
            } else {
                Toast.makeText(requireContext(), R.string.error_device_not_found, Toast.LENGTH_SHORT).show()
            }
        }

        binding.mainFab.setOnClickListener { onAddDeviceAction() }

        // observe discovering state
        viewModel.isDiscovering().observe(viewLifecycleOwner, {
            binding.progressIndicator.visibility = if(it) View.VISIBLE else View.GONE
        })

        // swipe to refresh listener
        binding.deviceListRefreshLayout.setOnRefreshListener {
            viewModel.discoverDevices()
            binding.deviceListRefreshLayout.isRefreshing = false
        }
    }

    override fun onStart() {
        super.onStart()
        if(Preferences.autoDiscovery) {
            viewModel.discoverDevices()
        }
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.menu_main_fragment, menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when(item.itemId) {
            R.id.menu_settings -> navigateToSettings()
            R.id.menu_clear_list -> onClearDeviceListAction()
            R.id.menu_add_device -> onAddDeviceAction()
            R.id.menu_discover_devices -> viewModel.discoverDevices()
        }
        return super.onOptionsItemSelected(item)
    }

    private fun navigateToSettings() {
        val action = MainFragmentDirections.actionMainFragmentToSettingsFragment()
        findNavController().navigate(action)
    }

    private fun onClickDeviceAction(deviceId: Int, viewItem: View) {
        //binding.mainFab.visibility = View.GONE

        val transitionName = "device_item_transition"
        viewItem.transitionName = transitionName
        val extras = FragmentNavigatorExtras(viewItem to transitionName)
        exitTransition = Hold().apply { duration = Constants.CONTAINER_TRANSITION_DURATION }

        val action = MainFragmentDirections.actionMainFragmentToSendFragment(deviceId)
        findNavController().navigate(action, extras)
    }

    private fun onAddDeviceAction() {
        val action = MainFragmentDirections.actionMainFragmentToESPDeviceSetup()
        findNavController().navigate(action)
    }

    private fun onEditDeviceAction(deviceId: Int) {
        val action = MainFragmentDirections.actionMainFragmentToESPDeviceSetup(deviceId.toString())
        findNavController().navigate(action)
    }

    private fun onClearDeviceListAction() {
        // show confirmation dialog
        MaterialAlertDialogBuilder(requireContext())
                .setTitle(getString(R.string.dialog_title_clear_device_list))
                .setMessage(getString(R.string.dialog_message_clear_device_list))
                .setNegativeButton(getString(R.string.action_cancel)) { dialog, which ->
                    dialog.cancel()
                }
                .setPositiveButton(getString(R.string.action_clear_list)) { dialog, which ->
                    viewModel.clearDeviceList()
                }
                .show()
    }

    private val deviceListActionModeCallback = object: ActionMode.Callback {
        var actionItemIndex: Int? = null

        override fun onCreateActionMode(mode: ActionMode?, menu: Menu?): Boolean {
            val deviceName = actionItemIndex?.let { (binding.devicesListView.adapter as DeviceListAdapter).getItem(it)?.name }
            mode?.menuInflater?.apply {
                inflate(R.menu.menu_device_list_action, menu)
            }
            mode?.title = deviceName?:""
            binding.devicesListView.selector = ResourcesCompat.getDrawable(resources, R.color.highlight_transparent, requireContext().theme)
            return true
        }

        override fun onPrepareActionMode(mode: ActionMode?, menu: Menu?): Boolean = false

        override fun onActionItemClicked(mode: ActionMode?, item: MenuItem?): Boolean {
            // get device ID from ListView adapter
            val deviceId = actionItemIndex?.let { (binding.devicesListView.adapter as DeviceListAdapter).getItem(it)?.id }

            when(item?.itemId) {
                R.id.menu_remove_device -> deviceId?.let { viewModel.deleteDevice(it) }
                R.id.menu_edit_device -> deviceId?.let { onEditDeviceAction(it) }
                else -> return false
            }
            mode?.finish()
            return true
        }

        override fun onDestroyActionMode(mode: ActionMode?) {
            devicesActionMode = null
            actionItemIndex = null
            val selectorResId = CommonUtils.getThemeResourceId(requireContext(), android.R.attr.selectableItemBackground)
            binding.devicesListView.selector = ResourcesCompat.getDrawable(resources, selectorResId, requireContext().theme)
            binding.devicesListView.invalidate()
        }

    }

    override fun onStop() {
        // disable action mode if active
        devicesActionMode?.finish()
        super.onStop()
    }

    override fun onDestroy() {
        super.onDestroy()
        _binding = null
    }

}