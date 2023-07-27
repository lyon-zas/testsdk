package com.nearpays.nearpayssdk

import android.app.Activity
import android.app.PendingIntent
import android.app.PendingIntent.FLAG_IMMUTABLE
import android.content.Intent
import android.content.IntentFilter
import android.nfc.NfcAdapter
import android.nfc.tech.IsoDep
import android.nfc.tech.NfcA
import android.os.Build
import androidx.annotation.RequiresApi

class NearPaysNFCUtils(private val mActivity: Activity) {

    private val mNfcAdapter: NfcAdapter? = NfcAdapter.getDefaultAdapter(mActivity)

    private val mPendingIntent: PendingIntent = PendingIntent.getActivity(
        mActivity,
        0,
        Intent(mActivity, mActivity.javaClass).addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP),
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) PendingIntent.FLAG_MUTABLE else 0
    )

    fun disableDispatch() {
        mNfcAdapter?.disableForegroundDispatch(mActivity)
    }

    fun enableDispatch() {
        mNfcAdapter?.enableForegroundDispatch(mActivity, mPendingIntent, INTENT_FILTER, TECH_LIST)
    }

    companion object {
        private val INTENT_FILTER = arrayOf(
            IntentFilter(NfcAdapter.ACTION_TECH_DISCOVERED),
            IntentFilter(NfcAdapter.ACTION_TAG_DISCOVERED)
        )
        private val TECH_LIST = arrayOf(
            arrayOf(
                NfcA::class.java.name, IsoDep::class.java.name
            )
        )
    }

}