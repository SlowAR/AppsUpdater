package by.slowar.appsupdater.ui

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import by.slowar.appsupdater.R
import by.slowar.appsupdater.databinding.ActivityMainBinding
import by.slowar.appsupdater.ui.updates.UpdatesListFragment

class MainActivity : AppCompatActivity(), HolderListener {

    private lateinit var binding: ActivityMainBinding

    private var hasUpdatingApps = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.updateAllButton.setOnClickListener {
            val fragment = binding.fragmentContainer.getFragment<UpdatesListFragment>()
            if (hasUpdatingApps) {
                fragment.onCancelAllAppsClick()
            } else {
                fragment.onUpdateAllAppsClick()
            }
        }
    }

    override fun onUpdatesListRefresh(updatesAmount: Int) {
        binding.updateAllButton.isEnabled = updatesAmount > 0
        val updatesAmountText = if (updatesAmount > 0) {
            String.format(getString(R.string.updates_found_amount), updatesAmount)
        } else {
            ""
        }
        binding.appsUpdatesText.text = updatesAmountText

    }

    override fun onHaveUpdatingApps(hasUpdatingApps: Boolean) {
        this.hasUpdatingApps = hasUpdatingApps
        val buttonTextId = if (hasUpdatingApps) R.string.cancel_all else R.string.update_all
        binding.updateAllButton.setText(buttonTextId)
    }
}

interface HolderListener {

    fun onUpdatesListRefresh(updatesAmount: Int = 0)

    fun onHaveUpdatingApps(hasUpdatingApps: Boolean)
}