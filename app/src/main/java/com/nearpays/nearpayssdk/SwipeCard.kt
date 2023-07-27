//package com.nearpays.nearpayssdk
//
//import android.app.Activity
//import android.content.Intent
//import android.nfc.NfcAdapter
//import android.os.Bundle
//import android.provider.Settings
//import android.util.Log
//import android.view.LayoutInflater
//import android.view.View
//import android.view.ViewGroup
//import android.widget.Toast
//import androidx.fragment.app.Fragment
//import androidx.navigation.fragment.findNavController
//import com.bumptech.glide.Glide
//import com.google.android.material.snackbar.Snackbar
//import com.google.gson.Gson
//import com.nateshmbhat.credit_card_scanner.scanner_core.models.CardDetails
//package com.nearpays.nearpayssdk.R
//import com.nearpays.nearpays.databinding.FragmentSwipeCardBinding
//import com.pro100svitlo.creditCardNfcReader.CardNfcAsyncTask
//import com.pro100svitlo.creditCardNfcReader.CardNfcAsyncTask.CardNfcInterface
//import com.pro100svitlo.creditCardNfcReader.utils.CardNfcUtils
//import kotlin.math.exp
//
//class SwipeCard : Fragment(), View.OnClickListener, CardNfcInterface {
//
//    lateinit var binding: FragmentSwipeCardBinding
//    private lateinit var mNfcAdapter: NfcAdapter
//    private lateinit var mCardNfcUtils: NearPaysNFCUtils
//    private var mIntentFromCreate = false
//    private var mCardNfcAsyncTask: CardNfcAsyncTask? = null
//
//    override fun onCreate(savedInstanceState: Bundle?) {
//        super.onCreate(savedInstanceState)
//        mNfcAdapter = NfcAdapter.getDefaultAdapter(requireContext())
//    }
//
//    private fun showSnackBar(message: String) {
//        Snackbar.make(binding.root, message, Snackbar.LENGTH_SHORT).show()
//    }
//
//    override fun onCreateView(
//        inflater: LayoutInflater, container: ViewGroup?,
//        savedInstanceState: Bundle?
//    ): View {
//        // Inflate the layout for this fragment
//        val root = inflater.inflate(R.layout.fragment_swipe_card, container, false)
//        binding = FragmentSwipeCardBinding.bind(root)
//
//
//        Glide.with(requireContext())
//            .load(R.drawable.nearpays)
//            .into(binding.cardSwipeGif)
//
//        mCardNfcUtils = NearPaysNFCUtils(requireActivity())
//        mIntentFromCreate = true
//        binding.toolbar.setNavigationOnClickListener {
//            requireActivity().onBackPressed()
//        }
//
//        binding.tapToScan.setOnClickListener {
//            findNavController().navigate(SwipeCardDirections.actionSwipeCardToCameraScanFragment(false))
//        }
//
//        requireActivity().addOnNewIntentListener {
//            if (mNfcAdapter.isEnabled)
//                mCardNfcAsyncTask = CardNfcAsyncTask.Builder(this, it, mIntentFromCreate)
//                    .build()
//        }
//
//
//        return binding.root
//    }
//
//    override fun onResume() {
//        super.onResume()
//        if (!mNfcAdapter.isEnabled) {
//
//            /* Intent(Settings.ACTION_NFC_SETTINGS)
//                 .also {
//                     startActivity(it)
//                 }*/
//        } else {
//            mCardNfcUtils.enableDispatch()
//        }
//    }
//
//    override fun onPause() {
//        super.onPause()
//        mCardNfcUtils.disableDispatch();
//    }
//
//    override fun onClick(v: View?) {
//
//    }
//
//    override fun startNfcReadCard() {
//        showSnackBar("Scanning started")
//    }
//
//    override fun cardIsReadyToRead() {
//        val card: String = mCardNfcAsyncTask!!.cardNumber ?: return
//        // card = getPrettyCardNumber(card)
//        val expiredDate = mCardNfcAsyncTask!!.cardExpireDate
//        val cardType = mCardNfcAsyncTask!!.cardType
//
//        val type = parseCardType(cardType)
//
//        val cardDetails = CardDetails(card, expiredDate, "GTB", type)
//
//        val gson = Gson()
//        val returnIntent = Intent()
//        returnIntent.putExtra("scan", gson.toJson(cardDetails))
//        requireActivity().setResult(Activity.RESULT_OK, returnIntent)
//        requireActivity().finish()
//
//
//    }
//
//
//    private fun parseCardType(cardType: String): String {
//        when (cardType) {
//            CardNfcAsyncTask.CARD_UNKNOWN -> {
//                log("unknown card type")
//                return "Unknown"
//            }
//            CardNfcAsyncTask.CARD_VISA -> {
//                log("Visa card")
//                return "Visa"
//            }
//            CardNfcAsyncTask.CARD_MASTER_CARD -> {
//                log("Master card")
//                return "Master Card"
//            }
//            CardNfcAsyncTask.CARD_VERVE -> {
//                log("verve")
//                return "Verve"
//            }
//            else -> return "Unknown"
//        }
//    }
//
//    private fun log(message: String) {
//        Log.d("MainActivity", message)
//    }
//
//
//    override fun doNotMoveCardSoFast() {
//        showSnackBar("don't move card too fast")
//    }
//
//    override fun unknownEmvCard() {
//        showSnackBar("unknown emvc")
//    }
//
//    override fun cardWithLockedNfc() {
//        showSnackBar("Nfc locked")
//    }
//
//    override fun finishNfcReadCard() {
//        //mCardNfcAsyncTask = null
//    }
//
//}
//
//
