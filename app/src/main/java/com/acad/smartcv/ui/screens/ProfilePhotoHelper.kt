package com.acad.smartcv.ui.screens

import org.apache.poi.xwpf.usermodel.XWPFDocument
import android.content.Context
import android.graphics.*
import android.net.Uri
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.FileOutputStream

object ProfilePhotoHelper {

    // Save photo to app internal storage and return file path
    fun savePhoto(context: Context, bitmap: Bitmap): String {
        val dir = File(context.filesDir, "profile_photos")
        dir.mkdirs()
        val file = File(dir, "profile_photo_${System.currentTimeMillis()}.jpg")
        FileOutputStream(file).use { out ->
            bitmap.compress(Bitmap.CompressFormat.JPEG, 95, out)
        }
        return file.absolutePath
    }

    // Load bitmap from saved path
    fun loadPhoto(path: String): Bitmap? {
        return try {
            BitmapFactory.decodeFile(path)
        } catch (e: Exception) {
            null
        }
    }

    // Load bitmap from URI (gallery/camera)
    fun loadFromUri(context: Context, uri: Uri): Bitmap? {
        return try {
            val stream = context.contentResolver.openInputStream(uri)
            BitmapFactory.decodeStream(stream)
        } catch (e: Exception) {
            null
        }
    }

    // Crop bitmap to square (center crop)
    fun cropToSquare(bitmap: Bitmap): Bitmap {
        val size = minOf(bitmap.width, bitmap.height)
        val x = (bitmap.width - size) / 2
        val y = (bitmap.height - size) / 2
        return Bitmap.createBitmap(bitmap, x, y, size, size)
    }

    // Resize bitmap to target size
    fun resize(bitmap: Bitmap, targetSize: Int = 512): Bitmap {
        return Bitmap.createScaledBitmap(bitmap, targetSize, targetSize, true)
    }

    // Create perfect circular crop with anti-aliasing
    fun toCircle(bitmap: Bitmap): Bitmap {
        val size = minOf(bitmap.width, bitmap.height)
        val output = Bitmap.createBitmap(size, size, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(output)

        val paint = Paint().apply {
            isAntiAlias = true
            isFilterBitmap = true
            isDither = true
        }

        // Draw circle mask
        canvas.drawARGB(0, 0, 0, 0)
        paint.color = Color.WHITE
        canvas.drawCircle(size / 2f, size / 2f, size / 2f, paint)

        // Draw bitmap with circle mask
        paint.xfermode = PorterDuffXfermode(PorterDuff.Mode.SRC_IN)
        val src = Bitmap.createScaledBitmap(bitmap, size, size, true)
        canvas.drawBitmap(src, 0f, 0f, paint)
        paint.xfermode = null

        // Draw subtle border
        val borderPaint = Paint().apply {
            isAntiAlias = true
            style = Paint.Style.STROKE
            color = Color.parseColor("#E4E8EE")
            strokeWidth = 3f
        }
        canvas.drawCircle(size / 2f, size / 2f, size / 2f - 2f, borderPaint)

        return output
    }

    // Convert bitmap to byte array for PDF embedding
    fun toByteArray(bitmap: Bitmap): ByteArray {
        val stream = ByteArrayOutputStream()
        bitmap.compress(Bitmap.CompressFormat.JPEG, 95, stream)
        return stream.toByteArray()
    }

    // Try to extract first image from DOCX
    fun extractFromDocx(context: Context, uri: Uri): Bitmap? {
        return try {
            val stream = context.contentResolver.openInputStream(uri) ?: return null
            val doc = org.apache.poi.xwpf.usermodel.XWPFDocument(stream)
            val pictures = doc.allPictures
            if (pictures.isNotEmpty()) {
                val picData = pictures[0].data
                BitmapFactory.decodeByteArray(picData, 0, picData.size)
            } else null
        } catch (e: Exception) {
            null
        }
    }

    // Try to extract first image from PDF
    fun extractFromPdf(context: Context, uri: Uri): Bitmap? {
        return try {
            val stream = context.contentResolver.openInputStream(uri) ?: return null
            val doc = com.tom_roush.pdfbox.pdmodel.PDDocument.load(stream)
            val page = doc.getPage(0)
            val resources = page.resources
            var bitmap: Bitmap? = null
            resources.xObjectNames.forEach { name ->
                if (bitmap == null) {
                    val xObject = resources.getXObject(name)
                    if (xObject is com.tom_roush.pdfbox.pdmodel.graphics.image.PDImageXObject) {
                        bitmap = xObject.image
                    }
                }
            }
            doc.close()
            bitmap
        } catch (e: Exception) {
            null
        }
    }
}
