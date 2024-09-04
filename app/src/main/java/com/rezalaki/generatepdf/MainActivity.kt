package com.rezalaki.generatepdf

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Environment
import android.provider.Settings
import android.util.Log
import android.view.View
import android.widget.TextView
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat

class MainActivity : AppCompatActivity() {

    companion object {
        private const val CODE_PERMISSION_WRITE = 555
    }

    private val multiplePermissionContract =
        registerForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) { permissionsStatusMap ->
            if (!permissionsStatusMap.containsValue(false)) {
                generatePdf()
            } else {
                Toast.makeText(this, "required permissions are NOT granted", Toast.LENGTH_SHORT).show()
            }
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        findViewById<View>(R.id.btnGeneratePdf).setOnClickListener {
            // handle Permissions
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) { // android 11, 12, 13, 14
                if (!Environment.isExternalStorageManager()) {
                    try {
                        val intent = Intent(Settings.ACTION_MANAGE_APP_ALL_FILES_ACCESS_PERMISSION)
                        intent.addCategory("android.intent.category.DEFAULT")
                        intent.data = Uri.fromParts("package", packageName, null)
                        startActivity(intent)
                    } catch (e: Exception) {
                        e.printStackTrace()
                        val intent = Intent(Settings.ACTION_MANAGE_APP_ALL_FILES_ACCESS_PERMISSION)
                        startActivity(intent)
                    }
                    return@setOnClickListener
                } else {
                    generatePdf()
                }
            } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) { // android 10
                multiplePermissionContract.launch(
                    arrayOf(Manifest.permission.WRITE_EXTERNAL_STORAGE)
                )
            } else {  // android 9, 8, ...
                val result = ActivityCompat.checkSelfPermission(
                    this, Manifest.permission.WRITE_EXTERNAL_STORAGE
                )
                if (result == PackageManager.PERMISSION_GRANTED) {
                    generatePdf()
                } else {
                    ActivityCompat.requestPermissions(
                        this,
                        arrayOf(Manifest.permission.WRITE_EXTERNAL_STORAGE),
                        CODE_PERMISSION_WRITE
                    )
                }
            }

        }

    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == CODE_PERMISSION_WRITE) {
            if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                generatePdf()
            } else {
                Toast.makeText(this, "required permissions are NOT granted", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun generatePdf() {
        PdfGenerator(
            context = this,
            onlyPersianLanguage = true,
            onSuccess = {
                Toast.makeText(this, "SUCCESS", Toast.LENGTH_SHORT).show()
                findViewById<TextView>(R.id.tvResult).text = "pdf file path => $it"
            },
            onFailure = {
                Toast.makeText(this, "FAILED", Toast.LENGTH_SHORT).show()
                findViewById<TextView>(R.id.tvResult).text = "error happened => $it"
            }

        ).generate(FakeData.people)
    }

}