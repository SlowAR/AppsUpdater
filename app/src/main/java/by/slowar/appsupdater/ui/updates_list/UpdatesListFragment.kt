package by.slowar.appsupdater.ui.updates_list

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import by.slowar.appsupdater.databinding.FragmentUpdatesListBinding

class UpdatesListFragment : Fragment() {

    private var _binding: FragmentUpdatesListBinding? = null
    private val binding: FragmentUpdatesListBinding
        get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentUpdatesListBinding.inflate(inflater, container, false)
        return binding.root
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