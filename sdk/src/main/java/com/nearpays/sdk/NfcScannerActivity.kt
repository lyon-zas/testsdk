package com.nearpays.sdk//package com.nearpays.nearpayssdk

import android.app.Activity
import android.content.Intent
import android.nfc.NfcAdapter
import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import androidx.navigation.NavController
import androidx.navigation.fragment.findNavController
import com.google.gson.Gson
import com.nateshmbhat.credit_card_scanner.CardScannerCameraActivity
import com.nearpays.nearpays.databinding.ActivityNfcScannerPluginBinding
import com.pro100svitlo.creditCardNfcReader.CardNfcAsyncTask

class NfcScannerActivity : AppCompatActivity(){

    private val binding: ActivityNfcScannerPluginBinding by lazy {
        ActivityNfcScannerPluginBinding.inflate(layoutInflater)
    }

    private lateinit var navController: NavController
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)
        val hostFragment = supportFragmentManager.findFragmentById(R.id.navController)

        navController = hostFragment?.findNavController()!!
        val cameraScan = intent.getBooleanExtra("camera",false)
        if(cameraScan){
            navController.navigate(R.id.cameraScanFragment)
            return
        }
        navController.navigate(R.id.swipeCard)

    }

}