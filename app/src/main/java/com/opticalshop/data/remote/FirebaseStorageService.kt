package com.opticalshop.data.remote

import android.net.Uri
import com.google.firebase.storage.FirebaseStorage
import kotlinx.coroutines.tasks.await
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class FirebaseStorageService @Inject constructor(
    private val storage: FirebaseStorage
) {
    suspend fun uploadProfileImage(userId: String, imageUri: Uri): String {
        val ref = storage.reference.child("profile_images/$userId")
        ref.putFile(imageUri).await()
        return ref.downloadUrl.await().toString()
    }
}
