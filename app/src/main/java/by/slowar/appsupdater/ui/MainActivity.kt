package by.slowar.appsupdater.ui

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.findFragment
import by.slowar.appsupdater.databinding.ActivityMainBinding
import by.slowar.appsupdater.ui.updates.UpdatesListFragment

class MainActivity : AppCompatActivity() {

    lateinit var binding: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.updateAllButton.setOnClickListener {
            binding.fragmentContainer.getFragment<UpdatesListFragment>()
                .onUpdateAllAppsClick()
        }
    }
}