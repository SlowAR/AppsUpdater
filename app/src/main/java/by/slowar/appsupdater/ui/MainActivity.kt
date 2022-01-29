package by.slowar.appsupdater.ui

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import by.slowar.appsupdater.databinding.ActivityMainBinding

class MainActivity : AppCompatActivity() {

    lateinit var binding: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.updateAllButton.setOnClickListener {
        }
    }
}