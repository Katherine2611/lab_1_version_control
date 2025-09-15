package ua.opu.mycoolapp

import android.os.Environment
import androidx.core.content.FileProvider
import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.widget.Toast
import android.widget.Button
import android.widget.ImageView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import java.io.File
import java.io.FileOutputStream
import java.io.IOException

class MainActivity : AppCompatActivity() {

    private var imageView: ImageView? = null
    private var fileUri: Uri? = null
    private val CAMERA_REQUEST = 1888

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        imageView = findViewById(R.id.imageView)

        val btnTakeSelfie: Button = findViewById(R.id.btnTakeSelfie)
        val btnSendSelfie: Button = findViewById(R.id.btnSendSelfie)

        btnTakeSelfie.setOnClickListener { openCamera() }

        btnSendSelfie.setOnClickListener { sendEmail() }

        // Перевірка дозволів
        checkPermissions()
    }

    // Перевірка дозволів на камеру та збереження
    private fun checkPermissions() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED ||
            ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED ||
            ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {

            ActivityCompat.requestPermissions(this,
                arrayOf(Manifest.permission.CAMERA, Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.READ_EXTERNAL_STORAGE),
                CAMERA_REQUEST)
        }
    }

    // Відкриваємо камеру для зйомки селфі
    private fun openCamera() {
        val cameraIntent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
        startActivityForResult(cameraIntent, CAMERA_REQUEST)
    }

    // Обробка результату з камери
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == CAMERA_REQUEST && resultCode == RESULT_OK) {
            val photo = data!!.extras!!["data"] as Bitmap?
            imageView!!.setImageBitmap(photo)
            fileUri = saveImage(photo)
        }
    }

    // Збереження зображення у файл
    private fun saveImage(bitmap: Bitmap?): Uri {
        val file = File(getExternalFilesDir(null), "selfie.jpg")
        try {
            FileOutputStream(file).use { fos ->
                bitmap!!.compress(Bitmap.CompressFormat.JPEG, 100, fos)
            }
        } catch (e: IOException) {
            e.printStackTrace()
        }
        return FileProvider.getUriForFile(this, "${packageName}.fileprovider", file)
    }


    // Відправка електронної пошти
    private fun sendEmail() {
        // Перевірка наявності файлу (селфі)
        if (fileUri == null) {
            Toast.makeText(this, "Спочатку зробіть селфі!", Toast.LENGTH_SHORT).show()
            return
        }

        // Створюємо інтерфейс для відправки електронної пошти
        val emailIntent = Intent(Intent.ACTION_SEND).apply {
            type = "message/rfc822"  // вказуємо MIME-тип для електронної пошти
            putExtra(Intent.EXTRA_EMAIL, arrayOf("hodovychenko@op.edu.ua"))  // Адреса відправника
            putExtra(Intent.EXTRA_SUBJECT, "DigiJED [Донець Катерина]")  // Тема листа
            putExtra(Intent.EXTRA_TEXT, "Посилання на репозиторій проекту: https://github.com/DigiJED-3-Android/practice.git")  // Текст листа

            // Додаємо вкладення (селфі)
            putExtra(Intent.EXTRA_STREAM, fileUri)
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)  // Дозволяємо доступ до файлу

        }

        // Перевірка, чи є доступні поштові клієнти для відправки листа
        if (emailIntent.resolveActivity(packageManager) != null) {
            startActivity(Intent.createChooser(emailIntent, "Вибір поштового клієнта"))
        } else {
            Toast.makeText(this, "Немає доступних поштових клієнтів", Toast.LENGTH_SHORT).show()
        }
    }

}
