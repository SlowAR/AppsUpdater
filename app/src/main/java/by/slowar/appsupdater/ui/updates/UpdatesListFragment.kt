package by.slowar.appsupdater.ui.updates

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import by.slowar.appsupdater.UpdaterApp
import by.slowar.appsupdater.databinding.FragmentUpdatesListBinding
import by.slowar.appsupdater.ui.updates.states.AppItemUiState
import javax.inject.Inject

class UpdatesListFragment : Fragment() {

    private var _binding: FragmentUpdatesListBinding? = null
    private val binding: FragmentUpdatesListBinding
        get() = _binding!!

    @Inject
    lateinit var viewModelFactory: UpdatesListViewModel.Factory
    private val viewModel: UpdatesListViewModel by viewModels { viewModelFactory }

    private lateinit var adapter: UpdateAppListAdapter

    override fun onAttach(context: Context) {
        super.onAttach(context)
        (requireActivity().applicationContext as UpdaterApp).appComponent
            .getUpdatesListComponent()
            .inject(this)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentUpdatesListBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        adapter = UpdateAppListAdapter()
        binding.appsRecyclerView.adapter = adapter

        binding.swipeRefreshLayout.setOnRefreshListener {
            viewModel.checkForUpdates(true)
        }

        viewModel.updateResult.observe(viewLifecycleOwner) { result ->
            handleCheckForUpdatesResult(result)
        }

        viewModel.updatingAppState.observe(viewLifecycleOwner) { itemState ->
            handleUpdateAppState(itemState)
        }

        viewModel.checkForUpdates()
    }

    private fun handleCheckForUpdatesResult(state: AppUpdateResult) {
        when (state) {
            is AppUpdateResult.Loading -> {
                changeLoadingVisibility(true)
            }
            AppUpdateResult.EmptyResult -> {
                changeLoadingVisibility(false)
            }
            is AppUpdateResult.ErrorResult -> {
                changeLoadingVisibility(false)
                Toast.makeText(context, state.errorId, Toast.LENGTH_LONG).show()
            }
            is AppUpdateResult.SuccessResult -> {
                changeLoadingVisibility(false)
                adapter.setNewAppList(state.result)
            }
        }
    }

    private fun changeLoadingVisibility(isVisible: Boolean) {
        binding.swipeRefreshLayout.isRefreshing = isVisible
    }

    private fun handleUpdateAppState(appUpdateState: AppItemUiState) {
        if (appUpdateState is AppItemUiState.CompletedResult) {
            adapter.removeUpdatingApp()
        } else {
            val payload = if (appUpdateState is AppItemUiState.Idle) {
                null
            } else {
                UpdateAppListAdapter.APP_UPDATE_PAYLOAD
            }
            adapter.updateAppItem(appUpdateState, payload)
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    companion object {

        @JvmStatic
        fun newInstance() = UpdatesListFragment()
    }
}