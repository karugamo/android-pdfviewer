package androidpdfviewer.com.danjdt.sample

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import androidpdfviewer.com.danjdt.sample.databinding.ActivityMainBinding
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.danjdt.pdfviewer.PdfViewer
import com.danjdt.pdfviewer.interfaces.OnErrorListener
import com.danjdt.pdfviewer.interfaces.OnPageChangedListener
import com.danjdt.pdfviewer.utils.PdfPageQuality
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.io.IOException


class SampleActivity : AppCompatActivity(), OnPageChangedListener , OnErrorListener {
    private val REQUEST_CODE_LOAD = 367
    private lateinit var binding: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val view = PdfViewer.Builder(binding.rootView, lifecycleScope)
            .setMaxZoom(3f)
            .setZoomEnabled(true)
            .quality(PdfPageQuality.QUALITY_1440)
            .setOnErrorListener(this)
            .setOnPageChangedListener(this)
            .setRenderDispatcher(Dispatchers.Default)
            .build()


//        lifecycleScope.launch {
//            delay(200)
//            view.viewController.goToPosition(10)
//            delay(300) // Asynchronous delay that does not block the main thread
//            view.viewController.goToPosition(0)
//        }

        view.load(R.raw.sample)
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.menu, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (item.itemId == R.id.action_open) {
            val intent = Intent(Intent.ACTION_OPEN_DOCUMENT).apply {
                addCategory(Intent.CATEGORY_OPENABLE)
                type = "application/pdf"
            }
            startActivityForResult(intent,REQUEST_CODE_LOAD)
            return true
        }
        return super.onOptionsItemSelected(item)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == REQUEST_CODE_LOAD && resultCode == Activity.RESULT_OK) {
            data?.data?.let { uri ->
                binding.rootView.removeAllViews()
                PdfViewer.Builder(binding.rootView, lifecycleScope)
                    .setMaxZoom(3f)
                    .setZoomEnabled(true)
                    .quality(PdfPageQuality.QUALITY_1080)
                    .setOnErrorListener(this)
                    .setOnPageChangedListener(this)
                    .setRenderDispatcher(Dispatchers.Default)
                    .build()
                    .load(uri)
            }
        }
    }

    override fun onPageChanged(page: Int, total: Int) {
        binding.tvCounter.text = getString(R.string.pdf_page_counter, page, total)
    }

    override fun onFileLoadError(e: Exception) {
        //Handle error ...
        e.printStackTrace()
    }

    override fun onAttachViewError(e: Exception) {
        //Handle error ...
        e.printStackTrace()
    }

    override fun onPdfRendererError(e: IOException) {
        //Handle error ...
        e.printStackTrace()
    }
}
