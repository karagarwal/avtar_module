package com.example.avtar_module

import android.app.Activity
import android.app.Dialog
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Environment
import android.provider.MediaStore
import android.util.Log
import android.view.Gravity
import android.view.LayoutInflater
import android.view.Window
import android.view.WindowManager
import android.widget.LinearLayout
import android.widget.Toast
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.FileProvider
import androidx.core.view.isVisible
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.avtar_module.databinding.ActivityMainBinding
import com.example.avtar_module.databinding.DialogImageChooserBinding
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.asRequestBody
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.text.SimpleDateFormat
import java.util.*

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    var avtayList = arrayListOf<AvtarModel>()
    private lateinit var avtarAdapter: AvtarAdapter
    var launchGallery: ActivityResultLauncher<Intent>? = null
    var launchCamera: ActivityResultLauncher<Intent>? = null
    lateinit var fileToUpload: File
    private var mImageCaptureUri: Uri? = null
    private var imageCaptureUri: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        avtayList.clear()


        launchGallery =
            registerForActivityResult(ActivityResultContracts.StartActivityForResult()) {
                if (it.resultCode == Activity.RESULT_OK) {

                    if (it.data != null) {
                        try {
                            var uri = it.data!!.data!!
                            binding.finalImg.setImageURI(uri)
                            fileToUpload = FileUtil.from(this, uri)
                            uploadImageAssets()
                        } catch (e: Exception) {
                            e.printStackTrace()
                        }
                        binding.bt.isVisible = true
                    }
                }
            }


        launchCamera = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) {
            if (it.resultCode == Activity.RESULT_OK) {
                try {
                    val returnCursor = contentResolver.query(
                        mImageCaptureUri!!, null, null, null, null
                    )
                    returnCursor!!.moveToFirst()

                    //var res=  imageCaptureUri.toString()
                    binding.finalImg.setImageURI(Uri.parse(imageCaptureUri))
                    try {
                        fileToUpload = FileUtil.from(this, mImageCaptureUri!!)

                        binding.bt.isVisible = true
                        uploadImageAssets()
                    } catch (e: Exception) {
                        e.printStackTrace()
                    }
                } catch (ex: Exception) {
                    ex.printStackTrace()
                }
            }
        }


        avtayList.add(AvtarModel().apply {
            resource_id = R.drawable.add
            is_avtar = false
        })
        avtayList.add(AvtarModel().apply {
            id = "0"
            resource_id = R.drawable.avatar_0
        })
        avtayList.add(AvtarModel().apply {
            id = "1"
            resource_id = R.drawable.avatar_1
        })
        avtayList.add(AvtarModel().apply {
            id = "2"
            resource_id = R.drawable.avatar_2
        })
        avtayList.add(AvtarModel().apply {
            id = "3"
            resource_id = R.drawable.avatar_3
        })
        avtayList.add(AvtarModel().apply {
            id = "4"
            resource_id = R.drawable.avatar_4
        })

        avtarAdapter = AvtarAdapter(this, avtayList) {
            if (it.is_avtar) {
                binding.finalImg.setImageResource(it.resource_id)

                val bm: Bitmap = BitmapFactory.decodeResource(
                    resources, it.resource_id
                )

                val fileAvtar = File(
                    getExternalFilesDir(null)?.absolutePath
                            + File.separator + "drawable_avtar"
                )

                saveBitmapToFile(fileAvtar, "avtar.png", bm, Bitmap.CompressFormat.PNG, 100);

                if (!fileAvtar.exists()) {
                    fileAvtar.mkdirs()
                }

                binding.bt.isVisible = true

                fileToUpload = File(
                    getExternalFilesDir(null)?.absolutePath
                            + File.separator + "drawable_avtar" +
                            File.separator + "avtar.png"
                )

                uploadImageAssets()
            } else {
                imagePickerDialog()
            }
        }

        binding.recyclerView.apply {
            layoutManager = GridLayoutManager(
                this@MainActivity, 4, RecyclerView.VERTICAL, false
            )
            adapter = avtarAdapter
        }
    }

    fun saveBitmapToFile(
        file: File?, fileName: String?, bm: Bitmap,
        format: Bitmap.CompressFormat?, quality: Int
    ): Boolean {
        val imageFile = File(file, fileName)
        var fos: FileOutputStream? = null
        try {
            fos = FileOutputStream(imageFile)
            bm.compress(format, quality, fos)
            fos.close()
            return true
        } catch (e: IOException) {
            Log.e("app", e.message ?: "")
            if (fos != null) {
                try {
                    fos.close()
                } catch (e1: IOException) {
                    e1.printStackTrace()
                }
            }
        }
        return false
    }


    private fun uploadImageAssets() {
        var imageBody: MultipartBody.Part? = null

        imageBody = MultipartBody.Part.createFormData(
            "key_name",
            fileToUpload.name,
            fileToUpload.asRequestBody("image/*".toMediaTypeOrNull())
        )

        //JUST SEND THIS IMAGE BODY IN YOUR RETROFIT API SUPPORTING MULTIPART
    }

    private fun imagePickerDialog() {
        val dialog = Dialog(this)
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE)

        val dialogBinding = DialogImageChooserBinding.inflate(LayoutInflater.from(this))
        dialog.setContentView(dialogBinding.root)
        dialog.setCanceledOnTouchOutside(true)
        dialog.setCancelable(true)


        dialogBinding.camera.setOnClickListener {
            val intent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
            try {
                mImageCaptureUri = FileProvider.getUriForFile(
                    this, "${BuildConfig.APPLICATION_ID}.fileprovider", createImageFile()!!
                )
            } catch (e: IOException) {
                e.printStackTrace()
            }

            intent.putExtra(MediaStore.EXTRA_OUTPUT, mImageCaptureUri)
            launchCamera!!.launch(intent)

            dialog.dismiss()

        }

        dialogBinding.gallery.setOnClickListener {
            dialog.dismiss()

            val collection: Uri = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                MediaStore.Images.Media.getContentUri(
                    MediaStore.VOLUME_EXTERNAL
                )
            } else {
                MediaStore.Images.Media.EXTERNAL_CONTENT_URI
            }

            val galleryIntent = Intent(Intent.ACTION_PICK, collection)
            launchGallery!!.launch(galleryIntent)
        }


        dialog.window?.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN)
        dialog.window?.setLayout(
            LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.MATCH_PARENT
        )
        dialog.window?.setGravity(Gravity.CENTER)
        dialog.window?.setBackgroundDrawableResource(R.color.transparent)
        dialog.show()
    }


    private fun createImageFile(): File? {
        val timeStamp = SimpleDateFormat("yyyyMMdd_HHmmss").format(Date())
        val imageFileName = "JPEG_" + timeStamp + "_"
        val storageDir = File(getExternalFilesDir(Environment.DIRECTORY_DCIM), "Camera")
        val image = File.createTempFile(
            imageFileName, ".jpg", getExternalFilesDir(Environment.DIRECTORY_DCIM)
        )
        imageCaptureUri = "file:" + image.absolutePath
        return image
    }

}