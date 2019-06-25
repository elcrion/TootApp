package xyz.gsora.toot

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Environment
import android.provider.MediaStore
import android.support.v4.content.FileProvider
import java.io.File
import java.io.IOException
import java.text.SimpleDateFormat
import java.util.*

object TakePhotoHelper {

    private val INTENT_BUNDLE = "intent_bundle"
    private val BUNDLE_PHOTO_PATH = "bundle_photo_path"

    @Throws(IOException::class)
    fun createImageFile(context: Context): Uri {
        // Create an image file name
        val timeStamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.US).format(Date())
        val imageFileName = "JPEG_" + timeStamp
        val storageDir = context.getExternalFilesDir(Environment.DIRECTORY_PICTURES)
        val image = File.createTempFile(imageFileName, ".jpg", storageDir)
        val AUTHORITY = context.applicationInfo.packageName +".fileprovider"
        val photoUri = FileProvider.getUriForFile(
                context,
                AUTHORITY,
                image)

        return photoUri
    }

    fun getTakePictureIntent(uri: Uri, context: Context): Intent? {
        val intent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
        if (intent.resolveActivity(context.packageManager) != null) {
            return intent.apply {
                putExtra(MediaStore.EXTRA_OUTPUT, uri)
            }
        }

        return null
    }

    fun addPhotoToGallery(uri: Uri,context: Context) {
        val intent = Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE)
        intent.data = uri
        context.sendBroadcast(intent)
    }
}