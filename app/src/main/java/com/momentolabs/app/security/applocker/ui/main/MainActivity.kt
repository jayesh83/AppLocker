package com.momentolabs.app.security.applocker.ui.main

import android.Manifest
import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.MenuItem
import androidx.core.app.ActivityCompat
import androidx.core.view.GravityCompat
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.Observer
import androidx.viewpager.widget.ViewPager
import com.google.android.material.navigation.NavigationView
import com.momentolabs.app.security.applocker.R
import com.momentolabs.app.security.applocker.databinding.ActivityMainBinding
import com.momentolabs.app.security.applocker.service.CallReceiverService
import com.momentolabs.app.security.applocker.ui.BaseActivity
import com.momentolabs.app.security.applocker.ui.main.analytics.MainActivityAnalytics
import com.momentolabs.app.security.applocker.ui.newpattern.CreateNewPatternActivity
import com.momentolabs.app.security.applocker.ui.overlay.activity.OverlayValidationActivity
import com.momentolabs.app.security.applocker.ui.policydialog.PrivacyPolicyDialog
import com.momentolabs.app.security.applocker.util.helper.NavigationIntentHelper

private const val REQUEST_PERMISSIONS = 200

class MainActivity : BaseActivity<MainViewModel>(),
    NavigationView.OnNavigationItemSelectedListener {

    private lateinit var binding: ActivityMainBinding

    override fun getViewModel(): Class<MainViewModel> = MainViewModel::class.java

    private var allPermissionGranted = false
    private var permissions: Array<String> =
        arrayOf(Manifest.permission.RECORD_AUDIO, Manifest.permission.READ_PHONE_STATE)


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = DataBindingUtil.setContentView(this, R.layout.activity_main)
        binding.viewPager.adapter = MainPagerAdapter(this, supportFragmentManager)
        binding.tablayout.setupWithViewPager(binding.viewPager)
        binding.viewPager.addOnPageChangeListener(object : ViewPager.SimpleOnPageChangeListener() {
            override fun onPageSelected(position: Int) {
                when (position) {
                    0 -> MainActivityAnalytics.onSecurityTabSelected(this@MainActivity)
                    1 -> MainActivityAnalytics.onSettingsTabSelected(this@MainActivity)
                }
            }
        })

        binding.imageViewMenu.setOnClickListener { binding.drawerLayout.openDrawer(GravityCompat.START) }

        binding.navView.setNavigationItemSelectedListener(this)

        viewModel.getPatternCreationNeedLiveData().observe(this, Observer { isPatternCreateNeed ->
            when {
                isPatternCreateNeed -> {
                    startActivityForResult(
                        CreateNewPatternActivity.newIntent(this),
                        RC_CREATE_PATTERN
                    )
                }
                viewModel.isAppLaunchValidated().not() -> {
                    startActivityForResult(
                        OverlayValidationActivity.newIntent(this, this.packageName),
                        RC_VALIDATE_PATTERN
                    )
                }
            }
        })

        val recordPermission = ActivityCompat.checkSelfPermission(
            this,
            Manifest.permission.RECORD_AUDIO
        )

        val phoneStatePermission = ActivityCompat.checkSelfPermission(
            this,
            Manifest.permission.READ_PHONE_STATE
        )

        if (recordPermission != PackageManager.PERMISSION_GRANTED)
            askPermissions()

        if (phoneStatePermission != PackageManager.PERMISSION_GRANTED)
            askPermissions()

        val intent = Intent(this, CallReceiverService::class.java)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O)
            startForegroundService(intent)
        else
            startService(intent)
    }

    private fun askPermissions() {
        ActivityCompat.requestPermissions(
            this@MainActivity,
            permissions,
            REQUEST_PERMISSIONS
        )
    }

    override fun onBackPressed() {
        if (binding.drawerLayout.isDrawerOpen(GravityCompat.START)) {
            binding.drawerLayout.closeDrawer(GravityCompat.START)
        } else {
            super.onBackPressed()
        }
    }

    override fun onNavigationItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.nav_share -> startActivity(NavigationIntentHelper.getShareAppIntent())
            R.id.nav_rate_us -> startActivity(NavigationIntentHelper.getRateAppIntent())
            R.id.nav_feedback -> startActivity(NavigationIntentHelper.getFeedbackIntent())
        }
        binding.drawerLayout.closeDrawer(GravityCompat.START)
        return true
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        when (requestCode) {
            RC_CREATE_PATTERN -> {
                viewModel.onAppLaunchValidated()
                showPrivacyPolicyIfNeeded()
                if (resultCode != Activity.RESULT_OK) {
                    finish()
                }
            }
            RC_VALIDATE_PATTERN -> {
                if (resultCode == Activity.RESULT_OK) {
                    viewModel.onAppLaunchValidated()
                    showPrivacyPolicyIfNeeded()
                } else {
                    finish()
                }
            }
        }
    }

    private fun showPrivacyPolicyIfNeeded() {
        if (viewModel.isPrivacyPolicyAccepted().not()) {
            PrivacyPolicyDialog.newInstance().show(supportFragmentManager, "")
        }
    }

    companion object {
        private const val RC_CREATE_PATTERN = 2002
        private const val RC_VALIDATE_PATTERN = 2003
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        allPermissionGranted = if (requestCode == REQUEST_PERMISSIONS) {
            Log.e("GrandResults", "results: $grantResults")
            for (result in grantResults)
                if (result != PackageManager.PERMISSION_GRANTED)
                    return
            true
        } else {
            false
        }

        if (!allPermissionGranted)
            finish()
    }
}
