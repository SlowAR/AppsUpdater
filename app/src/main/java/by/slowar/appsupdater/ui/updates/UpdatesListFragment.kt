package by.slowar.appsupdater.ui.updates

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import by.slowar.appsupdater.UpdaterApp
import by.slowar.appsupdater.databinding.FragmentUpdatesListBinding
import by.slowar.appsupdater.ui.HolderListener
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

    private lateinit var holderListener: HolderListener

    override fun onAttach(context: Context) {
        super.onAttach(context)

        (requireActivity().applicationContext as UpdaterApp).appComponent
            .getUpdatesListComponent()
            .inject(this)

        holderListener = context as HolderListener
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

        viewModel.hasUpdatingApps.observe(viewLifecycleOwner) { hasUpdatingApps ->
            holderListener.onHaveUpdatingApps(hasUpdatingApps)
        }

        viewModel.checkForUpdates()
    }

    private fun handleCheckForUpdatesResult(state: AppUpdateResult) {
        when (state) {
            is AppUpdateResult.Nothing -> {
                changeLoadingVisibility(false)
            }
            is AppUpdateResult.Loading -> {
                changeLoadingVisibility(true)
                changeUpdatesStatus(UpdatesStatus.List)
                holderListener.onUpdatesListRefresh()
            }
            AppUpdateResult.EmptyResult -> {
                changeLoadingVisibility(false)
                changeUpdatesStatus(UpdatesStatus.Empty)
                holderListener.onUpdatesListRefresh()
            }
            is AppUpdateResult.ErrorResult -> {
                changeLoadingVisibility(false)
                changeUpdatesStatus(UpdatesStatus.Error)
                holderListener.onUpdatesListRefresh()
                Toast.makeText(context, state.errorId, Toast.LENGTH_LONG).show()
            }
            is AppUpdateResult.SuccessResult -> {
                changeLoadingVisibility(false)
                changeUpdatesStatus(UpdatesStatus.List)
                adapter.setNewAppList(state.result)
                holderListener.onUpdatesListRefresh(state.result.size)
                holderListener.onHaveUpdatingApps(false)
            }
            is AppUpdateResult.RefreshAllResult -> {
                adapter.setNewAppList(state.result)
            }
        }
    }

    private fun changeLoadingVisibility(isVisible: Boolean) {
        binding.swipeRefreshLayout.isRefreshing = isVisible
    }

    private fun changeUpdatesStatus(status: UpdatesStatus) {
        when (status) {
            UpdatesStatus.Empty -> {
                binding.appsRecyclerView.isVisible = false
                binding.errorUpdatesGroup.isVisible = false
                binding.noUpdatesGroup.isVisible = true
            }
            UpdatesStatus.Error -> {
                binding.appsRecyclerView.isVisible = false
                binding.errorUpdatesGroup.isVisible = true
                binding.noUpdatesGroup.isVisible = false
            }
            UpdatesStatus.List -> {
                binding.appsRecyclerView.isVisible = true
                binding.errorUpdatesGroup.isVisible = false
                binding.noUpdatesGroup.isVisible = false
            }
        }
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

    fun onUpdateAllAppsClick() {
        viewModel.updateAllApps()
    }

    fun onCancelAllAppsClick() {
        viewModel.cancelAllUpdates()
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