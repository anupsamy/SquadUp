package com.cpen321.usermanagement.utils

import android.content.Context
import android.net.Uri
import android.util.Log
import java.io.File

object MediaUtils {

    private const val TAG = "MediaUtils"

    fun uriToFile(context: Context, uri: Uri): File {
        return when (uri.scheme) {
            "file" -> handleFileUri(uri)
            "content" -> handleContentUri(context, uri)
            else -> {
                Log.e(TAG, "Unsupported URI scheme: ${uri.scheme}")
                throw IllegalArgumentException("Unsupported URI scheme: ${uri.scheme}")
            }
        }
    }

    private fun handleFileUri(uri: Uri): File {
        try {
            return File(uri.path!!)
        } catch (e: IllegalArgumentException) {
            Log.e(TAG, "Invalid file path: ${uri.path}", e)
            throw e
        } catch (e: SecurityException) {
            Log.e(TAG, "Permission denied to access file: ${uri.path}", e)
            throw e
        }
    }

    private fun handleContentUri(context: Context, uri: Uri): File {
        val inputStream = context.contentResolver.openInputStream(uri)
        if (inputStream == null) {
            Log.e(TAG, "Failed to open input stream for URI: $uri")
            throw IllegalArgumentException("Failed to open input stream for URI")
        }

        try {
            val file = File.createTempFile("profile_", ".jpg", context.cacheDir)
            copyInputStreamToFile(inputStream, file)
            return file
        } catch (e: java.io.IOException) {
            Log.e(TAG, "IO error while creating file from content URI: $uri", e)
            throw e
        } catch (e: SecurityException) {
            Log.e(TAG, "Permission denied to access content URI: $uri", e)
            throw e
        } catch (e: IllegalArgumentException) {
            Log.e(TAG, "Invalid content URI: $uri", e)
            throw e
        }
    }

    private fun copyInputStreamToFile(inputStream: java.io.InputStream, file: File) {
        inputStream.use { input ->
            file.outputStream().use { output ->
                input.copyTo(output)
            }
        }
    }
}
