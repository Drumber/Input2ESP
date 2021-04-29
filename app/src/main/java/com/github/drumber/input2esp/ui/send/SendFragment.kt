package com.github.drumber.input2esp.ui.send

import android.content.res.ColorStateList
import android.graphics.Color
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.widget.ImageViewCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.navArgs
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.github.drumber.input2esp.MainActivity
import com.github.drumber.input2esp.R
import com.github.drumber.input2esp.backend.Constants
import com.github.drumber.input2esp.backend.models.ESPDeviceModel
import com.github.drumber.input2esp.backend.models.Payload
import com.github.drumber.input2esp.backend.network.NetworkClientState
import com.github.drumber.input2esp.backend.utils.CommonUtils
import com.github.drumber.input2esp.backend.utils.DeviceUtils
import com.github.drumber.input2esp.backend.utils.event.EventObserver
import com.github.drumber.input2esp.databinding.SendFragmentBinding
import com.github.drumber.input2esp.ui.components.ExtendedFloatingActionButtonScrollListener
import com.github.drumber.input2esp.ui.components.touchhelper.SimpleItemTouchHelperCallback
import com.github.drumber.input2esp.ui.send.command.CommandFragment
import com.google.android.material.snackbar.Snackbar
import com.google.android.material.transition.MaterialContainerTransform

class SendFragment : Fragment(), OnStartDragListener {

    private lateinit var viewModel: SendViewModel
    private val args: SendFragmentArgs by navArgs()

    private var _binding: SendFragmentBinding? = null
    private val binding get() = _binding!!

    private lateinit var itemTouchHelper: ItemTouchHelper

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        sharedElementEnterTransition = MaterialContainerTransform().apply {
            //drawingViewId = R.id.fragment
            scrimColor = Color.TRANSPARENT
            duration = Constants.CONTAINER_TRANSITION_DURATION
            setAllContainerColors(CommonUtils.getThemeValue(requireContext(), R.attr.colorSurface))
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = SendFragmentBinding.inflate(inflater, container, false)

        animateExtendedFAB()

        return binding.root
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        viewModel = ViewModelProvider(this).get(SendViewModel::class.java)

        viewModel.setDeviceModel(args.deviceId)

        // initialize components
        binding.apply {
            deviceNameTextView.text = viewModel.deviceModel?.name?.let {if(it.isNotBlank()) it else getString(R.string.error_unnamed_device) }
            deviceDescriptionTextView.text = viewModel.deviceModel?.let {
                "${it.getTypeString()} ${DeviceUtils.getDescriptionForModel(it)}"
            }

            // hide connect button if mode is not TCP
            viewModel.deviceModel?.let {
                if(it is ESPDeviceModel && it.provider != ESPDeviceModel.Provider.TCP) {
                    binding.deviceConnectButton.visibility = View.GONE
                }
            }

            addCommandButton.setOnClickListener { viewModel.addPayloadCommand() }
        }

        // initialize adapter for recycler view
        val payloadAdapter = PayloadAdapter(viewModel.getPayloadList().value!!, this, { payload ->
            payload?.let { onEditPayloadItem(it) }
        })
        // initialize touch handlers
        val touchCallback = SimpleItemTouchHelperCallback(payloadAdapter)
        itemTouchHelper = ItemTouchHelper(touchCallback)
        itemTouchHelper.attachToRecyclerView(binding.payloadCommandRecyclerView)

        binding.payloadCommandRecyclerView.apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = payloadAdapter
        }

        viewModel.getPayloadList().observe(viewLifecycleOwner, {
            payloadAdapter.notifyDataSetChanged()
        })

        // observe live client state
        viewModel.deviceModel?.getConnectionState()?.observe(viewLifecycleOwner, {
            binding.deviceStateTextView.text = it.getString()
            ImageViewCompat.setImageTintList(binding.deviceStateImageView, ColorStateList.valueOf(DeviceUtils.getColorForClientState(requireContext(), it)))

            updateConnectButton(it)
            if(it == NetworkClientState.CONNECTION_LOST) {
                showSnackbar(getString(R.string.error_network_connection_lost))
            }
        })

        binding.deviceConnectButton.setOnClickListener { viewModel.onConnectButtonClicked() }
        binding.extendedFab.setOnClickListener {
            viewModel.onPayloadSend()
        }
        viewModel.isSending().observe(viewLifecycleOwner, {
            binding.extendedFab.isEnabled = !it
            if(it) {
                Toast.makeText(requireContext(), R.string.message_sending_data, Toast.LENGTH_SHORT).show()
            }
        })

        // observe live error message
        viewModel.getLiveErrorMessage().observe(viewLifecycleOwner, EventObserver {
            showSnackbar(it)
        })

        binding.bodyNestedScrollView.setOnScrollChangeListener(ExtendedFloatingActionButtonScrollListener(binding.extendedFab, 100))

        (requireActivity() as MainActivity).viewModel.getCredentialsData().observe(viewLifecycleOwner, {
            viewModel.onCredentialsAvailable(it)
        })
    }

    override fun onStartDrag(viewHolder: RecyclerView.ViewHolder) {
        itemTouchHelper.startDrag(viewHolder)
    }

    private fun onEditPayloadItem(payload: Payload) {
        val commandFragment = CommandFragment.createInstance(payload, viewModel.placeholderManager, viewModel)
        val transaction = parentFragmentManager.beginTransaction()
        transaction.add(0, commandFragment)
                .addToBackStack(null)
                .commit()
    }

    private fun updateConnectButton(state: NetworkClientState) {
        binding.deviceConnectButton.apply {
            when(state) {
                NetworkClientState.CONNECTED,
                    NetworkClientState.MESSAGE_RECEIVED -> {
                    text = getString(R.string.action_disconnect)
                    isEnabled = true
                    }
                NetworkClientState.CONNECTING -> {
                    text = getString(R.string.state_connecting)
                    isEnabled = false
                }
                else -> {
                    text = getString(R.string.action_connect)
                    isEnabled = true
                }
            }
        }
    }

    private fun showSnackbar(message: String,
                             listener: View.OnClickListener = View.OnClickListener { /* do nothing by default */ }) {
        Snackbar.make(binding.root, message, Snackbar.LENGTH_INDEFINITE)
                .setAction(R.string.action_dismiss, listener)
                .show()
    }

    private fun animateExtendedFAB() {
        binding.extendedFab.apply {
            shrink()
            Handler(Looper.getMainLooper()).postDelayed({
                extend()
            }, 500)
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        _binding = null
    }

}