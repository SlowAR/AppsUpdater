package by.slowar.appsupdater.ui.updates_list

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import by.slowar.appsupdater.data.AppUpdateItemBinding
import by.slowar.appsupdater.ui.updates_list.states.AppItemUiState

class UpdateAppListAdapter :
    ListAdapter<AppItemUiState, UpdateAppListAdapter.ViewHolder>(AppsDiffUtil()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding =
            AppUpdateItemBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        getItem(position)?.let {
            holder.bind(it)
        }
    }

    inner class ViewHolder(private val binding: AppUpdateItemBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(appItemState: AppItemUiState) {
            binding.appItemState = appItemState
            binding.showInfoButton.setOnClickListener { toggleDescriptionVisibility() }
            binding.updateButton.setOnClickListener { appItemState.onUpdateAction() }
            binding.executePendingBindings()
        }

        private fun toggleDescriptionVisibility() {
            if (binding.updateInfoText.visibility == View.VISIBLE) {
                hideDescriptionText()
            } else {
                showDescriptionText()
            }
        }

        private fun showDescriptionText() {
            binding.updateInfoText.visibility = View.VISIBLE
            binding.showInfoButton.animate().rotation(180f)
        }

        private fun hideDescriptionText() {
            binding.updateInfoText.visibility = View.GONE
            binding.showInfoButton.animate().rotation(0f)
        }
    }

    class AppsDiffUtil : DiffUtil.ItemCallback<AppItemUiState>() {

        override fun areItemsTheSame(oldItem: AppItemUiState, newItem: AppItemUiState): Boolean {
            return oldItem.packageName == newItem.packageName
        }

        override fun areContentsTheSame(oldItem: AppItemUiState, newItem: AppItemUiState): Boolean {
            return oldItem == newItem
        }
    }
}