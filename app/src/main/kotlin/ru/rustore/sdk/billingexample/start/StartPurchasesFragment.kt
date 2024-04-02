package ru.rustore.sdk.billingexample.start

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.flowWithLifecycle
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import kotlinx.coroutines.launch
import ru.rustore.sdk.billingclient.utils.resolveForBilling
import ru.rustore.sdk.billingexample.R
import ru.rustore.sdk.billingexample.databinding.FragmentStartPurchasesBinding
import ru.rustore.sdk.billingexample.start.model.StartPurchasesEvent
import ru.rustore.sdk.billingexample.start.model.StartPurchasesState
import ru.rustore.sdk.billingexample.util.showToast
import ru.rustore.sdk.core.feature.model.FeatureAvailabilityResult

class StartPurchasesFragment : Fragment() {

    private val viewModel: StartPurchasesViewModel by viewModels()

    private var binding: FragmentStartPurchasesBinding? = null

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        binding = FragmentStartPurchasesBinding.inflate(inflater, container, false)
        return binding?.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding?.initView()

        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.state
                .flowWithLifecycle(viewLifecycleOwner.lifecycle)
                .collect { state ->
                    binding?.updateState(state)
                }
        }
        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.event
                .flowWithLifecycle(viewLifecycleOwner.lifecycle)
                .collect { event ->
                    handleEvent(event)
                }
        }
    }

    private fun FragmentStartPurchasesBinding.initView() {
        startPurchasesButton.setOnClickListener {
            viewModel.checkPurchasesAvailability()
        }
    }

    private fun FragmentStartPurchasesBinding.updateState(state: StartPurchasesState) {
        progressBar.isVisible = state.isLoading
        startPurchasesButton.isEnabled = !state.isLoading
    }

    private fun handleEvent(event: StartPurchasesEvent) {
        when (event) {
            is StartPurchasesEvent.PurchasesAvailability -> {
                when (event.availability) {
                    is FeatureAvailabilityResult.Available -> {
                        this@StartPurchasesFragment.findNavController().navigate(
                            R.id.action_mainFragment_to_billingExampleFragment
                        )
                    }

                    is FeatureAvailabilityResult.Unavailable -> {
                        event.availability.cause.resolveForBilling(requireContext())
                        event.availability.cause.message?.let(::showToast)
                    }

                    else -> {}
                }
            }

            is StartPurchasesEvent.Error -> {
                showToast(event.throwable.message ?: getString(R.string.billing_common_error))
            }
        }
    }

    override fun onDestroyView() {
        binding = null
        super.onDestroyView()
    }
}
