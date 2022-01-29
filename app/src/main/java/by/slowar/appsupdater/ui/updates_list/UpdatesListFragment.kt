package by.slowar.appsupdater.ui.updates_list

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import by.slowar.appsupdater.UpdaterApp
import by.slowar.appsupdater.databinding.FragmentUpdatesListBinding
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

        viewModel.appsUiItems.observe(viewLifecycleOwner) { appsItemsList ->
            adapter.submitList(appsItemsList)
        }

        viewModel.checkForUpdates()
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