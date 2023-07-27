package com.nearpays.sdk//package com.nearpays.nearpayssdk

import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.media.Image
import android.os.Build
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.Surface
import android.view.View
import android.view.ViewGroup
import android.widget.CompoundButton
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.navigation.NavController
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import com.google.gson.Gson
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.text.TextRecognition
import com.google.mlkit.vision.text.TextRecognizer
import com.google.mlkit.vision.text.latin.TextRecognizerOptions
import com.nearpays.nearpayssdk.databinding.FragmentCameraScanBinding
import com.otaliastudios.cameraview.controls.Flash
import com.otaliastudios.cameraview.controls.WhiteBalance
import com.otaliastudios.cameraview.frame.Frame
import com.otaliastudios.cameraview.frame.FrameProcessor


class CameraScanFragment : Fragment(), FrameProcessor, CompoundButton.OnCheckedChangeListener {

    private lateinit var binding: FragmentCameraScanBinding
    private lateinit var navigator: NavController
    private var scannedCard: CardDetails? = null
    private lateinit var recognizer: TextRecognizer
    private var flash = Flash.OFF
    private val args: CameraScanFragmentArgs by navArgs()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        // Inflate the layout for this fragment
        val view = inflater.inflate(R.layout.fragment_camera_scan, container, false)
        binding = FragmentCameraScanBinding.bind(view)

        binding.toolbar.setNavigationOnClickListener {
            if (args.launch) {
                requireActivity().onBackPressed()
                return@setNavigationOnClickListener
            }
            findNavController().popBackStack()
        }
        navigator = findNavController()

        return binding.root
    }

    override fun onStart() {
        super.onStart()
        recognizer = TextRecognition.getClient(TextRecognizerOptions.DEFAULT_OPTIONS)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        if (allPermissionsGranted()) {
            binding.cameraView.setLifecycleOwner(viewLifecycleOwner)
            configuration()
            binding.toggleFlash.setOnCheckedChangeListener(this)
        } else {
            ActivityCompat.requestPermissions(
                requireActivity(),
                REQUIRED_PERMISSIONS,
                REQUEST_CODE_PERMISSIONS
            )
            binding.cameraView.setLifecycleOwner(viewLifecycleOwner)

            configuration()

            binding.toggleFlash.setOnCheckedChangeListener(this)

        }

    }

    private fun degreesToFirebaseRotation(orientation: Int): Int {
        val rotation: Int = when (orientation) {
            in 45..134 -> {
                Surface.ROTATION_270
            }
            in 135..224 -> {
                Surface.ROTATION_180
            }
            in 225..314 -> {
                Surface.ROTATION_90
            }
            else -> {
                Surface.ROTATION_0
            }
        }
        return rotation
    }

    private fun configuration() {
        binding.cameraView.whiteBalance = WhiteBalance.AUTO
        binding.cameraView.addFrameProcessor(this)
    }

    private fun handleImageAnalysis(image: InputImage) {

        recognizer.process(image)
            .addOnSuccessListener { result ->

                val resultText = result.textBlocks.map {
                    it.text
                }

                handleActionFromTextRecognizer(resultText)

            }.addOnFailureListener {

                println(it.localizedMessage)
            }


    }

    private fun frameProcessor(frame: Frame) {
        val size: com.otaliastudios.cameraview.size.Size = frame.size
        val userRotation: Int = frame.rotationToUser
        if (frame.dataClass === ByteArray::class.java) {
            val data: ByteArray = frame.getData()
            val inputImage = InputImage.fromByteArray(
                data, size.width, size.height,
                userRotation, InputImage.IMAGE_FORMAT_NV21
            )
            handleImageAnalysis(inputImage)
        } else if (frame.dataClass === Image::class.java) {
            val data: Image = frame.getData()
            val inputImage =
                InputImage.fromMediaImage(data, degreesToFirebaseRotation(userRotation))
            handleImageAnalysis(inputImage)
        }
        frame.release()
    }

    override fun onPause() {
        super.onPause()
        binding.cameraView.close()
    }

    private fun handleActionFromTextRecognizer(resultText: List<String>) {
        if (scannedCard != null) {
            return
        }
        val cardDetails = extractString(resultText)
        if (cardDetails == null) {
            return
        } else {
            scannedCard = cardDetails
            val gson = Gson()
            val returnIntent = Intent()
            returnIntent.putExtra("scan", gson.toJson(cardDetails))
            requireActivity().setResult(Activity.RESULT_OK, returnIntent)
            requireActivity().finish()
            recognizer.close()
            binding.cameraView.clearFrameProcessors()


        }
    }

    private fun allPermissionsGranted() = REQUIRED_PERMISSIONS.all {
        ContextCompat.checkSelfPermission(
            requireContext(), it
        ) == PackageManager.PERMISSION_GRANTED
    }

    companion object {
        private const val REQUEST_CODE_PERMISSIONS = 10
        private val REQUIRED_PERMISSIONS =
            mutableListOf(
                android.Manifest.permission.CAMERA,
            ).apply {
                if (Build.VERSION.SDK_INT <= Build.VERSION_CODES.P) {
                    add(android.Manifest.permission.WRITE_EXTERNAL_STORAGE)
                }
            }.toTypedArray()
    }

    override fun process(frame: Frame) {

        if (scannedCard == null) {
            frameProcessor(frame)
        }
    }

    override fun onCheckedChanged(p0: CompoundButton?, p1: Boolean) {
        flash = if (p1) Flash.TORCH else Flash.OFF
        binding.cameraView.flash = flash
    }


    fun extractString(input: List<String>): CardDetails? {
        val expectedCard = input.filter { textBlock ->
            val filteredText = textBlock.filter { it.isDigit() }
            filteredText.length == 16
        }.map { element -> element.filter { it.isDigit() } }

        if (expectedCard.isEmpty())
            return null
        val expectedExpiryDate =
            input.filter { textBlock -> textBlock.contains("/") && textBlock.length >= 5 }
        if (expectedExpiryDate.isEmpty())
            return null
        val dateSplitter = expectedExpiryDate.first().split("/")
        if (dateSplitter[0].length < 2 || dateSplitter[1].length < 2)
            return null
        val monthPart = dateSplitter[0]
        val yearPart = dateSplitter[1]
        val dateConcat =
            "${monthPart[monthPart.length - 2]}${monthPart[monthPart.length - 1]}/${yearPart[0]}${yearPart[1]}"

        val cardpan = expectedCard.first()
        println(cardpan)
        println(dateConcat)

        return CardDetails(
            cardpan, dateConcat, "unknown", "unknown"
        )
    }
}

