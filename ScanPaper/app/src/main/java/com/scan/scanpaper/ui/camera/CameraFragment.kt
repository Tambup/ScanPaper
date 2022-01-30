package com.scan.scanpaper.ui.camera

import android.app.Activity
import android.content.Intent
import android.graphics.Bitmap
import android.os.Bundle
import android.provider.MediaStore
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.webkit.URLUtil
import android.widget.Button
import android.widget.EditText
import androidx.activity.result.contract.ActivityResultContracts
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import com.scan.scanpaper.R
import com.scan.scanpaper.databinding.FragmentCameraBinding
import java.io.ByteArrayOutputStream
import java.net.URL
import java.io.DataOutputStream
import java.io.OutputStream
import java.net.HttpURLConnection


class CameraFragment : Fragment() {
    private lateinit var cameraViewModel: CameraViewModel
    private var _binding: FragmentCameraBinding? = null

    // This property is only valid between onCreateView and
    // onDestroyView.
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        cameraViewModel = ViewModelProvider(this)[CameraViewModel::class.java]

        _binding = FragmentCameraBinding.inflate(inflater, container, false)

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val launchSomeActivity = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == Activity.RESULT_OK) {
                val image: Bitmap = result.data?.extras?.get("data") as Bitmap
                val byteArray = ByteArrayOutputStream()
                image.compress(Bitmap.CompressFormat.PNG, 100, byteArray)
                val bytes: ByteArray = byteArray.toByteArray()

                sendHttpRequest(bytes)
            }
        }

        val button = view.findViewById<Button>(R.id.takePhoto)
        button.setOnClickListener {
            val takePictureIntent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
            launchSomeActivity.launch(takePictureIntent)
        }
    }

    private fun sendHttpRequest(bytes: ByteArray) {
        var httpConn: HttpURLConnection
        var request: OutputStream
        val stringUrl = view?.findViewById<EditText>(R.id.url)?.text.toString()
        if (URLUtil.isValidUrl(stringUrl)) {
            val url = URL(stringUrl)
            Thread {
                httpConn = url.openConnection() as HttpURLConnection
                httpConn.useCaches = false
                httpConn.doOutput = true
                httpConn.doInput = false

                httpConn.requestMethod = "POST"
                httpConn.setRequestProperty("Cache-Control", "no-cache")
                httpConn.setRequestProperty("Content-Type", "image/png")

                request = DataOutputStream(httpConn.outputStream)
                request.write(bytes)
                request.flush()
                httpConn.responseCode
            }.start()
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}