package com.neonnoir.presentation.profile

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Matrix
import android.net.Uri
import androidx.exifinterface.media.ExifInterface
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.UserProfileChangeRequest
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.storage.FirebaseStorage
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext
import java.io.ByteArrayOutputStream
import javax.inject.Inject

@HiltViewModel
class EditProfileViewModel @Inject constructor() : ViewModel() {

    sealed class UiState {
        object Idle : UiState()
        object Loading : UiState()
        object Success : UiState()
        data class Error(val message: String) : UiState()
    }

    private val _uiState = MutableLiveData<UiState>(UiState.Idle)
    val uiState: LiveData<UiState> = _uiState

    // Current user snapshot exposed for pre-filling form fields
    val currentUser get() = FirebaseAuth.getInstance().currentUser
    val displayName: String get() = currentUser?.displayName ?: ""
    val email: String get() = currentUser?.email ?: ""

    // Reads additional fields stored in Realtime Database
    private val dbRef = currentUser?.uid?.let {
        FirebaseDatabase.getInstance().getReference("users").child(it)
    }

    private val _bio = MutableLiveData<String>()
    val bio: LiveData<String> = _bio

    private val _phone = MutableLiveData<String>()
    val phone: LiveData<String> = _phone

    init {
        loadExtendedProfile()
    }

    private fun loadExtendedProfile() {
        viewModelScope.launch {
            try {
                val snapshot = dbRef?.get()?.await() ?: return@launch
                _bio.value   = snapshot.child("bio").getValue(String::class.java) ?: ""
                _phone.value = snapshot.child("phone").getValue(String::class.java) ?: ""
            } catch (_: Exception) { /* no-op — fields stay empty */ }
        }
    }

    // ──────────────────────────────────────────────
    // IMAGE PROCESSING
    // ──────────────────────────────────────────────

    /**
     * Reads a URI, corrects EXIF orientation so the image is never flipped/rotated,
     * then compresses it iteratively until the JPEG byte array is under 1 MB.
     */
    fun processImage(context: Context, uri: Uri): ByteArray? = runCatching {
        val bitmap = decodeCorrected(context, uri)
        compressUnder1MB(bitmap)
    }.getOrNull()

    /**
     * Decodes the bitmap and applies any EXIF rotation/flip so what you see
     * in the gallery is exactly what gets uploaded.
     */
    private fun decodeCorrected(context: Context, uri: Uri): Bitmap {
        val raw = context.contentResolver.openInputStream(uri)!!.use { stream ->
            BitmapFactory.decodeStream(stream)
        }
        val orientation = context.contentResolver.openInputStream(uri)!!.use { stream ->
            ExifInterface(stream).getAttributeInt(
                ExifInterface.TAG_ORIENTATION,
                ExifInterface.ORIENTATION_UNDEFINED
            )
        }
        val matrix = Matrix()
        when (orientation) {
            ExifInterface.ORIENTATION_ROTATE_90           -> matrix.postRotate(90f)
            ExifInterface.ORIENTATION_ROTATE_180          -> matrix.postRotate(180f)
            ExifInterface.ORIENTATION_ROTATE_270          -> matrix.postRotate(270f)
            ExifInterface.ORIENTATION_FLIP_HORIZONTAL     -> matrix.postScale(-1f, 1f)
            ExifInterface.ORIENTATION_FLIP_VERTICAL       -> matrix.postScale(1f, -1f)
            ExifInterface.ORIENTATION_TRANSPOSE           -> { matrix.postRotate(90f); matrix.postScale(-1f, 1f) }
            ExifInterface.ORIENTATION_TRANSVERSE          -> { matrix.postRotate(270f); matrix.postScale(-1f, 1f) }
        }
        return if (matrix.isIdentity) raw
        else Bitmap.createBitmap(raw, 0, 0, raw.width, raw.height, matrix, true)
    }

    /**
     * Compresses the bitmap as JPEG, reducing quality in steps of 10
     * until the byte array is strictly under 1 MB (1,048,576 bytes).
     * Minimum quality floor is 20 to avoid artefact-heavy output.
     */
    private fun compressUnder1MB(bitmap: Bitmap): ByteArray {
        val maxBytes = 1 * 1024 * 1024  // 1 MB
        var quality  = 90
        var result: ByteArray
        do {
            val out = ByteArrayOutputStream()
            bitmap.compress(Bitmap.CompressFormat.JPEG, quality, out)
            result = out.toByteArray()
            quality -= 10
        } while (result.size > maxBytes && quality >= 20)
        return result
    }

    // ──────────────────────────────────────────────
    // SAVE PROFILE
    // ──────────────────────────────────────────────

    /**
     * Saves name + bio + phone to Firebase Auth / Realtime DB.
     * If [imageBytes] is provided, uploads to Firebase Storage first and
     * attaches the download URL to the Auth profile.
     */
    fun saveProfile(name: String, bio: String, phone: String, imageBytes: ByteArray?) {
        val user = currentUser ?: return
        _uiState.value = UiState.Loading

        viewModelScope.launch {
            try {
                // 1. Upload avatar if a new image was chosen
                val photoUrl: String? = if (imageBytes != null) {
                    uploadAvatar(user.uid, imageBytes)
                } else null

                // 2. Update Firebase Auth displayName (and optionally photoUrl)
                withContext(Dispatchers.IO) {
                    val builder = UserProfileChangeRequest.Builder()
                        .setDisplayName(name.trim())
                    if (photoUrl != null) {
                        builder.setPhotoUri(android.net.Uri.parse(photoUrl))
                    }
                    user.updateProfile(builder.build()).await()
                }

                // 3. Persist extended fields in Realtime Database
                withContext(Dispatchers.IO) {
                    val updates = mutableMapOf<String, Any>(
                        "displayName" to name.trim(),
                        "bio"         to bio.trim(),
                        "phone"       to phone.trim()
                    )
                    if (photoUrl != null) updates["photoUrl"] = photoUrl
                    dbRef?.updateChildren(updates)?.await()
                }

                _uiState.value = UiState.Success
            } catch (e: Exception) {
                _uiState.value = UiState.Error(e.message ?: "Failed to save profile")
            }
        }
    }

    /**
     * Uploads [bytes] to Firebase Storage at avatars/{uid}.jpg and
     * returns the public download URL.
     */
    private suspend fun uploadAvatar(uid: String, bytes: ByteArray): String =
        withContext(Dispatchers.IO) {
            val ref = FirebaseStorage.getInstance()
                .reference
                .child("avatars/$uid.jpg")

            // putBytes suspends until the upload is complete
            ref.putBytes(bytes).await()

            // Retrieve and return the permanent download URL
            ref.downloadUrl.await().toString()
        }
}
