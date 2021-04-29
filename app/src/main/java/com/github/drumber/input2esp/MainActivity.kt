package com.github.drumber.input2esp

import android.app.ActivityManager
import android.content.Context
import android.graphics.BitmapFactory
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.util.TypedValue
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.navigation.NavController
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.setupActionBarWithNavController
import com.github.drumber.input2esp.kp2a.CredentialsData
import com.github.drumber.input2esp.ui.MainActivityViewModel
import keepass2android.pluginsdk.Strings

class MainActivity : AppCompatActivity() {

    private lateinit var navController: NavController
    val viewModel: MainActivityViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.main_activity)

        val navHostFragment = supportFragmentManager.findFragmentById(R.id.fragment) as NavHostFragment
        navController = navHostFragment.navController
        setupActionBarWithNavController(navController)

        // get surface color
        val typedValue = TypedValue()
        theme.resolveAttribute(R.attr.colorSurface, typedValue, true)
        // set app bar color in recent apps overview
        setAppTaskColor(typedValue.data)

        if(intent?.getStringExtra(Strings.EXTRA_ENTRY_OUTPUT_DATA) != null) {
            Log.d("MainActivity", "Got credentials from keepass2android.")
            val credentials = CredentialsData.fromIntent(intent)

            if(credentials.entries.isNotEmpty()) {
                Log.d("MainActivity", "Parsed credentials data.")
                viewModel.setCredentialsData(credentials)
            }
        }

        // set specific title for main fragment if credentials are available
        navController.addOnDestinationChangedListener { controller, destination, arguments ->
            if(destination.id == R.id.mainFragment && viewModel.getCredentialsData().value != null) {
                destination.label = getString(R.string.title_main_fragment_select)
                supportActionBar?.title = getString(R.string.title_main_fragment_select)
            }
        }
    }

    override fun onSupportNavigateUp(): Boolean {
        return navController.navigateUp() || super.onSupportNavigateUp()
    }

    /**
     * Change the color of the app bar that is visible in the recent
     * app overview. This does only change the color for the current
     * activity task.
     * @param color     the new color that should be applied
     */
    private fun setAppTaskColor(color: Int) {
        val activityManager = getSystemService(Context.ACTIVITY_SERVICE) as ActivityManager
        // find the app task that corresponds to this activity
        val appTask = activityManager.appTasks.firstOrNull {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                it.taskInfo.taskId == taskId
            } else {
                it.taskInfo.id == taskId
            }
        }
        // change the color of the task description, but keep the label and app icon
        appTask?.taskInfo?.taskDescription?.let {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                setTaskDescription(ActivityManager.TaskDescription(it.label, R.mipmap.ic_launcher, color))
            } else {
                val icon = BitmapFactory.decodeResource(resources, R.mipmap.ic_launcher)
                setTaskDescription(ActivityManager.TaskDescription(it.label, icon, color))
            }
        }
    }

}