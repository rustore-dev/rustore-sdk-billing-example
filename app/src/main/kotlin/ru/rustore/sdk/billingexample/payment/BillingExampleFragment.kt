package ru.rustore.sdk.billingexample.payment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.flowWithLifecycle
import androidx.lifecycle.lifecycleScope
import com.google.android.material.snackbar.Snackbar
import kotlinx.coroutines.launch
import ru.rustore.sdk.billingclient.utils.resolveForBilling
import ru.rustore.sdk.billingexample.R
import ru.rustore.sdk.core.exception.RuStoreException
import ru.rustore.sdk.billingexample.databinding.FragmentBillingExampleBinding
import ru.rustore.sdk.billingexample.payment.adapter.ProductsAdapter
import ru.rustore.sdk.billingexample.payment.model.*
import ru.rustore.sdk.billingexample.util.showAlertDialog
import ru.rustore.sdk.billingexample.util.showToast

class BillingExampleFragment : Fragment() {

    private val viewModel: BillingExampleViewModel by viewModels()

    private val productsAdapter = ProductsAdapter {
        viewModel.onProductClick(it)
    }

    private var binding: FragmentBillingExampleBinding? = null

    private var snackbar: Snackbar? = null

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        binding = FragmentBillingExampleBinding.inflate(inflater, container, false)
        return binding?.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding?.initViews()

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

    override fun onDestroyView() {
        snackbar = null
        binding = null
        super.onDestroyView()
    }

    private fun FragmentBillingExampleBinding.initViews() {
        productsRecycler.adapter = productsAdapter
        swipeRefreshLayout.setOnRefreshListener { viewModel.getProducts() }
    }

    private fun FragmentBillingExampleBinding.updateState(state: BillingState) {
        swipeRefreshLayout.isRefreshing = state.isLoading

        emptyProductsView.isVisible = state.isEmpty

        productsAdapter.submitList(state.products)

        if (state.snackbarResId != null) {
            snackbar = Snackbar.make(root, state.snackbarResId, Snackbar.LENGTH_INDEFINITE)
            snackbar?.show()
        } else {
            snackbar?.dismiss()
        }
    }

    private fun handleEvent(event: BillingEvent) {
        when (event) {
            is BillingEvent.ShowDialog -> {
                requireContext().showAlertDialog(
                    title = getString(event.dialogInfo.titleRes),
                    message = event.dialogInfo.message,
                )
            }

            is BillingEvent.ShowError -> {
                if (event.error is RuStoreException) {
                    event.error.resolveForBilling(requireContext())
                }
                showToast(
                    message = "${getString(R.string.billing_general_error)}: ${event.error.message.orEmpty()}",
                    lengthLong = true
                )
            }
        }
    }
}
