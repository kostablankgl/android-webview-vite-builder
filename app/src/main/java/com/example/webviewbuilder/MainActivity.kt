package com.example.webviewbuilder

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.webkit.WebSettings
import android.webkit.WebView
import android.webkit.WebViewClient
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.documentfile.provider.DocumentFile
import com.example.webviewbuilder.databinding.ActivityMainBinding
import java.io.File
import java.io.FileOutputStream

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding

    private val openHtmlFileLauncher = registerForActivityResult(
        ActivityResultContracts.OpenDocument()
    ) { uri: Uri? ->
        if (uri == null) {
            updateStatus("Выбор HTML-файла отменён")
            return@registerForActivityResult
        }
        openSingleHtmlFile(uri)
    }

    private val openProjectFolderLauncher = registerForActivityResult(
        ActivityResultContracts.OpenDocumentTree()
    ) { uri: Uri? ->
        if (uri == null) {
            updateStatus("Выбор папки отменён")
            return@registerForActivityResult
        }
        openProjectFolder(uri)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupWebView()

        binding.btnOpenFile.setOnClickListener {
            openHtmlFilePicker()
        }

        binding.btnOpenFolder.setOnClickListener {
            openProjectFolderPicker()
        }

        binding.btnBack.setOnClickListener {
            if (binding.webView.canGoBack()) {
                binding.webView.goBack()
            }
        }

        binding.btnRefresh.setOnClickListener {
            binding.webView.reload()
        }
    }

    private fun setupWebView() {
        val webView = binding.webView
        webView.settings.javaScriptEnabled = true
        webView.settings.domStorageEnabled = true
        webView.settings.databaseEnabled = true
        webView.settings.allowFileAccess = true
        webView.settings.allowContentAccess = true
        webView.settings.allowUniversalAccessFromFileURLs = true
        webView.settings.loadsImagesAutomatically = true
        webView.settings.cacheMode = WebSettings.LOAD_DEFAULT
        webView.settings.builtInZoomControls = true
        webView.settings.displayZoomControls = false
        webView.settings.useWideViewPort = true
        webView.settings.loadWithOverviewMode = true
        webView.webViewClient = object : WebViewClient() {
            override fun onPageFinished(view: WebView?, url: String?) {
                super.onPageFinished(view, url)
                updateStatus("Проект загружен: $url")
            }
        }
    }

    private fun openHtmlFilePicker() {
        val intent = Intent(Intent.ACTION_OPEN_DOCUMENT).apply {
            addCategory(Intent.CATEGORY_OPENABLE)
            type = "text/html"
            putExtra(Intent.EXTRA_MIME_TYPES, arrayOf("text/html", "application/xhtml+xml"))
        }
        openHtmlFileLauncher.launch(arrayOf("text/html", "application/xhtml+xml"))
    }

    private fun openProjectFolderPicker() {
        try {
            openProjectFolderLauncher.launch(null)
        } catch (e: Exception) {
            Toast.makeText(this, "Ошибка выбора папки: ${e.message}", Toast.LENGTH_LONG).show()
        }
    }

    private fun openSingleHtmlFile(uri: Uri) {
        try {
            val targetFile = File(cacheDir, "single_page.html")
            contentResolver.openInputStream(uri)?.use { input ->
                FileOutputStream(targetFile).use { output ->
                    input.copyTo(output)
                }
            } ?: throw IllegalStateException("Не удалось прочитать файл")

            val url = "file://${targetFile.absolutePath}"
            binding.webView.loadUrl(url)
            updateStatus("Открыт HTML-файл: $url")
        } catch (e: Exception) {
            Toast.makeText(this, "Ошибка при открытии файла: ${e.message}", Toast.LENGTH_LONG).show()
            updateStatus("Ошибка: ${e.message}")
        }
    }

    private fun openProjectFolder(uri: Uri) {
        try {
            contentResolver.takePersistableUriPermission(
                uri,
                Intent.FLAG_GRANT_READ_URI_PERMISSION or Intent.FLAG_GRANT_WRITE_URI_PERMISSION
            )

            val sourceRoot = DocumentFile.fromTreeUri(this, uri)
                ?: throw IllegalStateException("Не удалось открыть папку проекта")

            val targetDir = File(cacheDir, "selected_project")
            if (targetDir.exists()) {
                targetDir.deleteRecursively()
            }
            targetDir.mkdirs()

            copyDocumentTree(sourceRoot, targetDir)

            val indexFile = findIndexHtml(targetDir)
            if (indexFile == null) {
                Toast.makeText(this, "В папке не найден index.html", Toast.LENGTH_LONG).show()
                updateStatus("Ошибка: index.html не найден")
                return
            }

            binding.webView.loadUrl("file://${indexFile.absolutePath}")
            updateStatus("Открыт проект: ${indexFile.absolutePath}")
        } catch (e: Exception) {
            Toast.makeText(this, "Ошибка при открытии папки: ${e.message}", Toast.LENGTH_LONG).show()
            updateStatus("Ошибка: ${e.message}")
        }
    }

    private fun copyDocumentTree(source: DocumentFile, targetDir: File) {
        if (source.isDirectory) {
            val list = source.listFiles()
            for (item in list) {
                val target = File(targetDir, item.name ?: "file")
                if (item.isDirectory) {
                    target.mkdirs()
                    copyDocumentTree(item, target)
                } else {
                    val inputStream = contentResolver.openInputStream(item.uri)
                    val outputStream = FileOutputStream(target)
                    inputStream?.copyTo(outputStream)
                    inputStream?.close()
                    outputStream.close()
                }
            }
        }
    }

    private fun findIndexHtml(root: File): File? {
        return root.walkTopDown()
            .filter { it.isFile && it.name.equals("index.html", ignoreCase = true) }
            .firstOrNull()
    }

    private fun updateStatus(message: String) {
        binding.statusText.text = message
    }

    override fun onBackPressed() {
        if (binding.webView.canGoBack()) {
            binding.webView.goBack()
        } else {
            super.onBackPressed()
        }
    }
}
