package com.neonnoir.presentation.profile

import android.Manifest
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import com.google.android.material.snackbar.Snackbar
import com.neonnoir.databinding.FragmentEditProfileBinding
import com.neonnoir.util.loadCurrentUserAvatar
import dagger.hilt.android.AndroidEntryPoint
import java.io.File

@AndroidEntryPoint
class EditProfileFragment : Fragment() {

    private var _binding: FragmentEditProfileBinding? = null
    private val binding get() = _binding!!

    private val viewModel: EditProfileViewModel by viewModels()

    // Holds the processed bytes ready to upload on save
    private var pendingImageBytes: ByteArray? = null

    // Temporary URI for camera capture
    private var cameraImageUri: Uri? = null

    // ──────────────────────────────────────────────
    // ACTIVITY RESULT LAUNCHERS
    // ──────────────────────────────────────────────

    private val galleryLauncher = registerForActivityResult(
        ActivityResultContracts.GetContent()
    ) { uri ->
        uri?.let { handleImageUri(it) }
    }

    private val cameraLauncher = registerForActivityResult(
        ActivityResultContracts.TakePicture()
    ) { success ->
        if (success) cameraImageUri?.let { handleImageUri(it) }
    }

    private val permissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { grants ->
        val allGranted = grants.values.all { it }
        if (allGranted) showImageSourceDialog()
        else Snackbar.make(binding.root, "Permission required to change photo", Snackbar.LENGTH_SHORT).show()
    }

    // ──────────────────────────────────────────────
    // LIFECYCLE
    // ──────────────────────────────────────────────

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        _binding = FragmentEditProfileBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        prefillForm()
        observeViewModel()
        setupClickListeners()
    }

    // ──────────────────────────────────────────────
    // SETUP
    // ──────────────────────────────────────────────

    private fun prefillForm() {
        binding.etName.setText(viewModel.displayName)
        binding.etEmail.setText(viewModel.email)
        // Email from Firebase Auth is not editable (requires re-auth)
        binding.etEmail.isEnabled = false

        // Load current avatar using the shared extension (circleCrop, crossfade)
        binding.ivAvatar.loadCurrentUserAvatar()

        // Observe bio/phone loaded from Realtime DB
        viewModel.bio.observe(viewLifecycleOwner) { bio ->
            if (binding.etBio.text.isNullOrEmpty()) binding.etBio.setText(bio)
        }
        viewModel.phone.observe(viewLifecycleOwner) { phone ->
            if (binding.etPhone.text.isNullOrEmpty()) binding.etPhone.setText(phone)
        }
    }

    private fun observeViewModel() {
        viewModel.uiState.observe(viewLifecycleOwner) { state ->
            when (state) {
                is EditProfileViewModel.UiState.Loading -> {
                    binding.pbUpload.visibility = View.VISIBLE
                    binding.btnSave.isEnabled   = false
                }
                is EditProfileViewModel.UiState.Success -> {
                    binding.pbUpload.visibility = View.GONE
                    binding.btnSave.isEnabled   = true
                    Snackbar.make(binding.root, "Profile updated!", Snackbar.LENGTH_SHORT).show()
                    findNavController().popBackStack()
                }
                is EditProfileViewModel.UiState.Error -> {
                    binding.pbUpload.visibility = View.GONE
                    binding.btnSave.isEnabled   = true
                    Snackbar.make(binding.root, state.message, Snackbar.LENGTH_LONG).show()
                }
                else -> {
                    binding.pbUpload.visibility = View.GONE
                    binding.btnSave.isEnabled   = true
                }
            }
        }
    }

    private fun setupClickListeners() {
        binding.btnBack.setOnClickListener { findNavController().popBackStack() }
        binding.btnCancel.setOnClickListener { findNavController().popBackStack() }

        binding.btnChangePhoto.setOnClickListener { requestPhotoPermissions() }

        binding.btnSave.setOnClickListener {
            viewModel.saveProfile(
                name       = binding.etName.text.toString(),
                bio        = binding.etBio.text.toString(),
                phone      = binding.etPhone.text.toString(),
                imageBytes = pendingImageBytes
            )
        }

        binding.rowChangePassword.setOnClickListener {
            Snackbar.make(binding.root, "Password change coming soon", Snackbar.LENGTH_SHORT).show()
        }

        binding.rowTwoFactor.setOnClickListener {
            Snackbar.make(binding.root, "Two-factor auth coming soon", Snackbar.LENGTH_SHORT).show()
        }
    }

    // ──────────────────────────────────────────────
    // PERMISSIONS + IMAGE PICKING
    // ──────────────────────────────────────────────

    private fun requestPhotoPermissions() {
        val perms = buildList {
            add(Manifest.permission.CAMERA)
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU)
                add(Manifest.permission.READ_MEDIA_IMAGES)
            else
                add(Manifest.permission.READ_EXTERNAL_STORAGE)
        }.toTypedArray()

        val allGranted = perms.all {
            ContextCompat.checkSelfPermission(requireContext(), it) == PackageManager.PERMISSION_GRANTED
        }

        if (allGranted) showImageSourceDialog()
        else permissionLauncher.launch(perms)
    }

    private fun showImageSourceDialog() {
        AlertDialog.Builder(requireContext())
            .setTitle("Change Photo")
            .setItems(arrayOf("Take Photo", "Choose from Gallery")) { _, which ->
                when (which) {
                    0 -> launchCamera()
                    1 -> galleryLauncher.launch("image/*")
                }
            }
            .show()
    }

    private fun launchCamera() {
        val imageFile = File(
            requireContext().cacheDir.resolve("images").also { it.mkdirs() },
            "avatar_${System.currentTimeMillis()}.jpg"
        )
        cameraImageUri = FileProvider.getUriForFile(
            requireContext(),
            "${requireContext().packageName}.fileprovider",
            imageFile
        )
        cameraLauncher.launch(cameraImageUri)
    }

    // ──────────────────────────────────────────────
    // IMAGE PROCESSING
    // ──────────────────────────────────────────────

    /**
     * Corrects orientation, compresses to <1 MB, previews in the avatar,
     * and stores the bytes for upload on Save.
     */
    private fun handleImageUri(uri: Uri) {
        val bytes = viewModel.processImage(requireContext(), uri)
        if (bytes == null) {
            Snackbar.make(binding.root, "Failed to process image", Snackbar.LENGTH_SHORT).show()
            return
        }
        pendingImageBytes = bytes
        // Show corrected preview from the compressed bytes
        val bmp = android.graphics.BitmapFactory.decodeByteArray(bytes, 0, bytes.size)
        binding.ivAvatar.setImageBitmap(bmp)
    }

    // ──────────────────────────────────────────────
    // CLEANUP
    // ──────────────────────────────────────────────

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
