package by.slowar.appsupdater.ui.updates

import android.annotation.SuppressLint
import android.os.Build
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import by.slowar.appsupdater.R
import by.slowar.appsupdater.common.Constants
import by.slowar.appsupdater.data.AppUpdateItemBinding
import by.slowar.appsupdater.ui.updates.states.AppItemUiState
import by.slowar.appsupdater.ui.updates.states.utils.animateHideProgress
import by.slowar.appsupdater.ui.updates.states.utils.animateShowProgress
import by.slowar.appsupdater.utils.formatBytesValue

class UpdateAppListAdapter(private val appsList: MutableList<AppItemUiState> = ArrayList()) :
    RecyclerView.Adapter<UpdateAppListAdapter.ViewHolder>() {

    companion object {
        const val APP_UPDATE_PAYLOAD = "AppUpdatePayload"
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding =
            AppUpdateItemBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(appsList[position])
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int, payloads: MutableList<Any>) {
        if (payloads.isEmpty()) {
            super.onBindViewHolder(holder, position, payloads)
        } else {
            for (payload in payloads) {
                if (payload == APP_UPDATE_PAYLOAD) {
                    holder.bindPayload(appsList[position])
                }
            }
        }
    }

    override fun getItemCount() = appsList.size

    @SuppressLint("NotifyDataSetChanged")
    fun setNewAppList(appsList: List<AppItemUiState>) {
        this.appsList.clear()
        this.appsList.addAll(appsList)
        notifyDataSetChanged()
    }

    fun removeAppItem(appId: Int) {
        appsList.removeAt(appId)
        notifyItemRemoved(appId)
    }

    fun updateAppItem(appId: Int, appItem: AppItemUiState, payload: String? = null) {
        appsList[appId] = appItem
        notifyItemChanged(appId, payload)
    }

    inner class ViewHolder(private val binding: AppUpdateItemBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(appItemState: AppItemUiState) {
            binding.appItemState = appItemState
            setStatusText("", appItemState.updateSize)
            binding.showInfoButton.setOnClickListener { toggleDescriptionVisibility() }

            if (appItemState is AppItemUiState.Idle) {
                binding.updateButton.setOnClickListener { appItemState.onUpdateAction() }
            }

            binding.executePendingBindings()
        }

        fun bindPayload(appItemState: AppItemUiState) {
            when (appItemState) {
                is AppItemUiState.Idle -> {
                    Log.e(
                        Constants.LOG_TAG,
                        "bindPayload: idle (shouldn't be here - notify without payload tag)"
                    )
                }
                is AppItemUiState.Initializing -> handleInitializeState(appItemState)
                is AppItemUiState.Downloading -> handleDownloadingState(appItemState)
                is AppItemUiState.Installing -> handleInstallingState(appItemState)
                is AppItemUiState.CompletedResult -> handleCompletedState(appItemState)
                is AppItemUiState.ErrorResult -> handleErrorState(appItemState)
            }
        }

        private fun handleInitializeState(uiState: AppItemUiState.Initializing) {
            handleDefaultStateData(uiState)
            setStatusText(R.string.initializing_text, uiState.updateSize)
        }

        private fun handleDownloadingState(uiState: AppItemUiState.Downloading) {
            handleDefaultStateData(uiState)

            val downloadedSizeText = formatBytesValue(uiState.downloadedSize, binding.root.context)
            setStatusText(downloadedSizeText, uiState.updateSize)

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                binding.downloadProgressBar.setProgress(uiState.getProgressPercent(), true)
            } else {
                binding.downloadProgressBar.progress = uiState.getProgressPercent()
            }
        }

        private fun handleInstallingState(uiState: AppItemUiState.Installing) {
            handleDefaultStateData(uiState)
            setStatusText(R.string.installing_text)
        }

        private fun handleCompletedState(uiState: AppItemUiState.CompletedResult) {
            handleDefaultStateData(uiState)
        }

        private fun handleErrorState(uiState: AppItemUiState.ErrorResult) {
            handleDefaultStateData(uiState)
            setStatusText(R.string.update_error, uiState.updateSize)
        }

        private fun handleDefaultStateData(uiState: AppItemUiState) {
            val isShowingFirstProgress = binding.taskProgressBar.visibility != View.VISIBLE &&
                    binding.downloadProgressBar.visibility != View.VISIBLE &&
                    uiState.taskProgressVisible

            val isHidingLastProgress =
                !uiState.taskProgressVisible && !uiState.downloadProgressVisible

            if (isShowingFirstProgress) {
                animateShowProgress(binding.taskProgressBar, binding.appIcon)
            } else if (isHidingLastProgress) {
                if (binding.taskProgressBar.visibility == View.VISIBLE) {
                    animateHideProgress(binding.downloadProgressBar, binding.appIcon)
                } else {
                    animateHideProgress(binding.downloadProgressBar, binding.appIcon)
                }
            } else {
                binding.taskProgressBar.visibility = isVisible(uiState.taskProgressVisible)
                binding.downloadProgressBar.visibility =
                    isVisible(uiState.downloadProgressVisible)
            }

            binding.updateButton.visibility = isVisible(uiState.updateAvailable)
        }

        private fun isVisible(isVisible: Boolean) = if (isVisible) View.VISIBLE else View.INVISIBLE

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

        private fun setStatusText(status: String, updateSize: Long = 0) {
            val context = binding.root.context
            val appSize = if (updateSize != 0L) formatBytesValue(updateSize, context) else ""
            val separator = if (status.isEmpty() || appSize.isEmpty()) "" else "-"
            val statusFull = if (status.isEmpty()) appSize else "$status $separator $appSize"
            binding.updateSizeText.text = statusFull
        }

        private fun setStatusText(statusStringId: Int, updateSize: Long = 0) {
            val status = binding.root.context.getString(statusStringId)
            setStatusText(status, updateSize)
        }
    }
}