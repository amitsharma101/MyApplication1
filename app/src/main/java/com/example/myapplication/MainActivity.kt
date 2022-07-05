package com.example.myapplication

import android.annotation.SuppressLint
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.Bundle
import com.google.android.material.snackbar.Snackbar
import androidx.appcompat.app.AppCompatActivity
import androidx.navigation.findNavController
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.navigateUp
import androidx.navigation.ui.setupActionBarWithNavController
import android.view.Menu
import android.view.MenuItem
import android.view.View
import com.example.myapplication.databinding.ActivityMainBinding

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding

    private lateinit var statusReceiver: BroadcastReceiver
    private lateinit var timeReceiver: BroadcastReceiver

    private var isStopwatchRunning = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.btbnCta.setOnClickListener {
            if (isStopwatchRunning) resetStopwatch() else startStopwatch()
        }

    }

    override fun onStart() {
        super.onStart()
        moveToBackground()
    }

    override fun onPause() {
        super.onPause()
        unregisterReceiver(statusReceiver)
        unregisterReceiver(timeReceiver)
        moveToForeground()
    }

    override fun onResume() {
        super.onResume()

        getStopwatchStatus()

        // Receiving stopwatch status from service
        val statusFilter = IntentFilter()
        statusFilter.addAction(StopwatchService.STOPWATCH_STATUS)
        statusReceiver = object : BroadcastReceiver() {
            override fun onReceive(p0: Context?, p1: Intent?) {
                val isRunning = p1?.getBooleanExtra(StopwatchService.IS_STOPWATCH_RUNNING, false)!!
                isStopwatchRunning = isRunning
                val timeElapsed = p1.getIntExtra(StopwatchService.TIME_ELAPSED, 0)
                val timeToAchieve = p1.getStringExtra(StopwatchService.TIME_TO_ACHIEVE)

                updateLayout(isStopwatchRunning)
                updateStopwatchValue(timeElapsed, timeToAchieve)
            }
        }
        registerReceiver(statusReceiver, statusFilter)

        // Receiving time values from service
        val timeFilter = IntentFilter()
        timeFilter.addAction(StopwatchService.STOPWATCH_TICK)
        timeReceiver = object : BroadcastReceiver() {
            override fun onReceive(p0: Context?, p1: Intent?) {
                val timeElapsed = p1?.getIntExtra(StopwatchService.TIME_ELAPSED, 0)!!
                val timeToAchieve = p1?.getStringExtra(StopwatchService.TIME_TO_ACHIEVE)
                updateStopwatchValue(timeElapsed, timeToAchieve)
            }
        }
        registerReceiver(timeReceiver, timeFilter)
    }


    private fun updateStopwatchValue(timeElapsed: Int, timeToAchieve: String?) {
        val timeToAchieveInSeconds = (timeToAchieve?.toInt() ?: 0) * 60
        if(timeElapsed > timeToAchieveInSeconds) resetStopwatch()
        val remainingTime = timeToAchieveInSeconds - timeElapsed

        val minutes: Int = remainingTime / 60
        val seconds: Int = remainingTime % 60

//        binding.tvTime.text = "${"%02d".format(hours)}:${"%02d".format(minutes)}:${"%02d".format(seconds)}"
        binding.tvTime.text = "${"%02d".format(minutes)}:${"%02d".format(seconds)}"
    }

    private fun updateLayout(isStopwatchRunning: Boolean) {
        if (isStopwatchRunning) {
            binding.etTime.visibility = View.GONE
            binding.btbnCta.text = "Reset"
        } else {
            binding.etTime.visibility = View.VISIBLE
            binding.btbnCta.text = "Start"
        }
    }

    private fun getStopwatchStatus() {
        val stopwatchService = Intent(this, StopwatchService::class.java)
        stopwatchService.putExtra(StopwatchService.STOPWATCH_ACTION, StopwatchService.GET_STATUS)
        startService(stopwatchService)
    }

    private fun startStopwatch() {
        val text = binding.etTime.text ?: return
        if(text.toString().isEmpty())return
        val stopwatchService = Intent(this, StopwatchService::class.java)
        stopwatchService.putExtra(StopwatchService.STOPWATCH_ACTION, StopwatchService.START)
        stopwatchService.putExtra(StopwatchService.TIME_TO_ACHIEVE, text.toString())
        startService(stopwatchService)
    }

    private fun pauseStopwatch() {
        val stopwatchService = Intent(this, StopwatchService::class.java)
        stopwatchService.putExtra(StopwatchService.STOPWATCH_ACTION, StopwatchService.PAUSE)
        startService(stopwatchService)
    }

    private fun resetStopwatch() {
        val stopwatchService = Intent(this, StopwatchService::class.java)
        stopwatchService.putExtra(StopwatchService.STOPWATCH_ACTION, StopwatchService.RESET)
        startService(stopwatchService)
    }

    private fun moveToForeground() {
        val stopwatchService = Intent(this, StopwatchService::class.java)
        stopwatchService.putExtra(
            StopwatchService.STOPWATCH_ACTION,
            StopwatchService.MOVE_TO_FOREGROUND
        )
        startService(stopwatchService)
    }

    private fun moveToBackground() {
        val stopwatchService = Intent(this, StopwatchService::class.java)
        stopwatchService.putExtra(
            StopwatchService.STOPWATCH_ACTION,
            StopwatchService.MOVE_TO_BACKGROUND
        )
        startService(stopwatchService)
    }

}