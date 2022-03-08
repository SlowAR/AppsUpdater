package by.slowar.appsupdater.ui.updates

import android.annotation.SuppressLint
import android.os.Build
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.view.isVisible
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

    private var currentlyUpdatingAppId: Int = Constants.EMPTY

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding =
            AppUpdateItemBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(appsList[position], true)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int, payloads: MutableList<Any>) {
        if (payloads.isEmpty()) {
            super.onBindViewHolder(holder, position, payloads)
        } else {
            for (payload in payloads) {
                if (payload == APP_UPDATE_PAYLOAD) {
                    holder.bind(appsList[position])
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

    fun removeUpdatingApp() {
        if (currentlyUpdatingAppId != Constants.EMPTY) {
            appsList.removeAt(currentlyUpdatingAppId)
            notifyItemRemoved(currentlyUpdatingAppId)
            currentlyUpdatingAppId = Constants.EMPTY
        }
    }

    fun updateAppItem(appItem: AppItemUiState, payload: String? = null) {
        if (checkAppByPackage(appItem.packageName)) {
            appsList[currentlyUpdatingAppId] = appItem
        } else {
            val correctAppId = getAppIdByPackage(appItem.packageName)
            if (correctAppId != Constants.EMPTY) {
                appsList[correctAppId] = appItem
                currentlyUpdatingAppId = correctAppId
            } else {
                Log.e(Constants.LOG_TAG, "UpdateAppListAdapter: Couldn't find correct app")
                return
            }
        }
        notifyItemChanged(currentlyUpdatingAppId, payload)
    }

    private fun checkAppByPackage(packageName: String) =
        currentlyUpdatingAppId != Constants.EMPTY && appsList[currentlyUpdatingAppId].packageName == packageName

    private fun getAppIdByPackage(packageName: String) = appsList.indexOfFirst { state ->
        state.packageName == packageName
    }

    inner class ViewHolder(private val binding: AppUpdateItemBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(appItemState: AppItemUiState, refresh: Boolean = false) {
            when (appItemState) {
                is AppItemUiState.Empty -> Log.e(Constants.LOG_TAG, "bindPayload: empty")
                is AppItemUiState.Idle -> handleIdleState(appItemState)
                is AppItemUiState.Pending -> handlePendingState(appItemState)
                is AppItemUiState.Initializing -> handleInitializeState(appItemState)
                is AppItemUiState.Downloading -> handleDownloadingState(appItemState)
                is AppItemUiState.Installing -> handleInstallingState(appItemState)
                is AppItemUiState.CompletedResult -> handleCompletedState(appItemState)
                is AppItemUiState.ErrorResult -> handleErrorState(appItemState)
            }

            if (refresh) {
                binding.executePendingBindings()
            }
        }

        private fun handleIdleState(uiState: AppItemUiState.Idle) {
            binding.appItemState = uiState
            setStatusText("", uiState.updateSize)
            binding.showInfoButton.setOnClickListener { toggleDescriptionVisibility() }
            binding.updateButton.setOnClickListener { uiState.onUpdateAction() }
        }

        private fun handlePendingState(uiState: AppItemUiState.Pending) {
            handleDefaultStateData(uiState)
            setStatusText(R.string.pending_text)
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
            binding.appIcon.setImageDrawable(uiState.icon)

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
                binding.taskProgressBar.isVisible = uiState.taskProgressVisible
                binding.downloadProgressBar.isVisible = uiState.downloadProgressVisible
            }

            binding.updateButton.isVisible = uiState.updateAvailable
            binding.cancelButton.isVisible = uiState.cancelUpdateAvailable

            if (uiState.cancelUpdateAvailable) {
                binding.cancelButton.setOnClickListener { uiState.onCancelAction() }
            }
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
            binding.showInfoButton.rotation = 0f
            binding.showInfoButton.animate().rotation(180f)
        }

        private fun hideDescriptionText() {
            binding.updateInfoText.visibility = View.GONE
            binding.showInfoButton.animate().rotation(360f)
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