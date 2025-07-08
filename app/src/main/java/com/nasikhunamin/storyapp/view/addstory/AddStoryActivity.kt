package com.nasikhunamin.storyapp.view.addstory

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import com.google.android.gms.maps.model.LatLng
import com.nasikhunamin.storyapp.R
import com.nasikhunamin.storyapp.ViewModelFactory
import com.nasikhunamin.storyapp.data.repository.Result
import com.nasikhunamin.storyapp.data.retrofit.ApiConfig
import com.nasikhunamin.storyapp.databinding.ActivityAddStoryBinding
import com.nasikhunamin.storyapp.utils.getImageUri
import com.nasikhunamin.storyapp.utils.reduceFileImage
import com.nasikhunamin.storyapp.utils.uriToFile
import com.nasikhunamin.storyapp.view.addstory.maps.MapsPickedLocationActivity
import com.nasikhunamin.storyapp.view.main.MainActivity
import java.util.Locale

class AddStoryActivity : AppCompatActivity() {

    private lateinit var binding: ActivityAddStoryBinding
    private var currentImageUri: Uri? = null
    private var selectedLatLng: LatLng? = null

    private val locationPickerLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == RESULT_OK) {
            val data: Intent? = result.data
            val latitude = data?.getDoubleExtra(EXTRA_LATITUDE, 0.0)
            val longitude = data?.getDoubleExtra(EXTRA_LONGITUDE, 0.0)

            if (latitude != null && longitude != null) {
                selectedLatLng = LatLng(latitude, longitude)
                val locationText = String.format(Locale.US, "Lat: %.4f, Lon: %.4f", latitude, longitude)
                binding.tvLatitudeValue.text = locationText
                binding.tvLatitudeValue.visibility = View.VISIBLE
            }
        }
    }


    private val viewModel by viewModels<AddStoryViewModel> {
        ViewModelFactory.getInstance(this, ApiConfig.getApiService())
    }
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAddStoryBinding.inflate(layoutInflater)
        setContentView(binding.root)


        binding.galleryButton.setOnClickListener { startGallery() }
        binding.cameraButton.setOnClickListener { startCamera() }
        binding.uploadButton.setOnClickListener { uploadImage() }
        setupCheckboxListener()
        setupLocationButton()
    }

    private fun startGallery() {
        launcherGallery.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly))
    }

    private val launcherGallery = registerForActivityResult(
        ActivityResultContracts.PickVisualMedia()
    ) { uri: Uri? ->
        if (uri != null) {
            currentImageUri = uri
            showImage()
        } else {
            Log.d(MESSAGE_PHOTO_PICKER, NO_MEDIA_SELECTED)
        }
    }

    private fun startCamera() {
        currentImageUri = getImageUri(this)
        launcherIntentCamera.launch(currentImageUri!!)
    }

    private val launcherIntentCamera = registerForActivityResult(
        ActivityResultContracts.TakePicture()
    ) { isSuccess ->
        if (isSuccess) {
            showImage()
        } else {
            currentImageUri = null
        }
    }

    private fun showImage() {
        currentImageUri?.let {
            Log.d(IMAGE_URI, "$SHOW_IMAGE $it")
            binding.previewImageView.setImageURI(it)
        }
    }
    private fun setupCheckboxListener() {
        binding.locationCheckBox.setOnCheckedChangeListener { _, isChecked ->
            if (isChecked) {
                binding.locationButton.visibility = View.VISIBLE
                if (selectedLatLng != null) {
                    binding.tvLatitudeValue.visibility = View.VISIBLE
                }
            } else {
                binding.locationButton.visibility = View.GONE
                binding.tvLatitudeValue.visibility = View.GONE
                selectedLatLng = null
            }
        }
    }

    private fun setupLocationButton() {
        binding.locationButton.setOnClickListener {
            val intent = Intent(this, MapsPickedLocationActivity::class.java)
            locationPickerLauncher.launch(intent)
        }
    }

    private fun uploadImage() {
        currentImageUri?.let { uri ->
            val imageFile = uriToFile(uri, this).reduceFileImage()
            Log.d(IMAGE_FILE, "$SHOW_IMAGE ${imageFile.path}")
            val description = binding.edAddDescription.text.toString().trim()
            val lat = selectedLatLng?.latitude
            val lon = selectedLatLng?.longitude

            viewModel.uploadImage(imageFile, description, lat, lon).observe(this) { result ->
                if (result != null) {
                    when (result) {
                        is Result.Loading -> {
                            showLoading(true)
                        }

                        is Result.Success -> {
                            showToast(result.data.message.toString())
                            showLoading(false)
                            val intent = Intent(this, MainActivity::class.java)
                            intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TASK or Intent.FLAG_ACTIVITY_NEW_TASK
                            startActivity(intent)
                        }

                        is Result.Error -> {
                            showToast(result.error)
                            showLoading(false)
                        }
                    }
                }
            }
        } ?: showToast(getString(R.string.email_error))
    }

    private fun showLoading(isLoading: Boolean) {
        binding.progressIndicator.visibility = if (isLoading) View.VISIBLE else View.GONE
    }
    private fun showToast(message: String) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
    }

    companion object{
        const val MESSAGE_PHOTO_PICKER = "Photo Picker"
        const val NO_MEDIA_SELECTED = "No media selected"
        const val IMAGE_URI = "Image URI"
        const val SHOW_IMAGE = "showImage:"
        const val IMAGE_FILE = "Image File"
        const val EXTRA_LATITUDE = "extra_latitude"
        const val EXTRA_LONGITUDE = "extra_longitude"
    }
}