package com.syhdzn.tugasakhirapp.pisang_buyer.search

import android.content.Context
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.recyclerview.widget.GridLayoutManager
import androidx.activity.OnBackPressedCallback
import androidx.appcompat.widget.SearchView
import com.google.firebase.database.*
import com.syhdzn.tugasakhirapp.R
import com.syhdzn.tugasakhirapp.databinding.ActivitySearchBinding
import com.syhdzn.tugasakhirapp.pisang_buyer.dashboard.BuyerDashboardActivity
import org.tensorflow.lite.Interpreter
import org.tensorflow.lite.flex.FlexDelegate
import java.io.FileInputStream
import java.nio.ByteBuffer
import java.nio.ByteOrder
import java.nio.channels.FileChannel
import java.nio.MappedByteBuffer

class SearchActivity : AppCompatActivity() {

    private lateinit var binding: ActivitySearchBinding
    private lateinit var pisangAdapter: PisangAdapter
    private lateinit var firebaseDatabase: FirebaseDatabase
    private lateinit var pisangList: MutableList<Pisang>
    private lateinit var interpreter: Interpreter

    private val pisangInfoList = mapOf(
        "Pisang Kepok" to "Pisang yang cocok untuk gorengan adalah pisang kepok.",
        "Pisang Ambon" to "Pisang yang cocok untuk smoothie adalah pisang ambon.",
        "Pisang Tanduk" to "Pisang yang cocok untuk direbus adalah pisang tanduk.",
        "Pisang Uli" to "Pisang yang cocok untuk nugget pisang adalah pisang uli.",
        "Pisang Raja" to "Pisang yang cocok untuk bubur pisang adalah pisang raja."
    )

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySearchBinding.inflate(layoutInflater)
        setContentView(binding.root)

        ViewCompat.setOnApplyWindowInsetsListener(binding.main) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        binding.rvSearch.layoutManager = GridLayoutManager(this, 2)
        firebaseDatabase = FirebaseDatabase.getInstance("https://tugasakhirapp-c5669-default-rtdb.asia-southeast1.firebasedatabase.app")
        pisangList = mutableListOf()

        interpreter = Interpreter(loadModelFile(this, "model_kuliner_pisang.tflite"), Interpreter.Options().addDelegate(FlexDelegate()))

        loadAllData()
        setupAction()
    }

    private fun setupAction() {
        onBackPressedDispatcher.addCallback(this, object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {

            }
        })

        binding.btnBack.setOnClickListener{
            val intent = Intent(this, BuyerDashboardActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_NO_ANIMATION
            startActivity(intent)
        }

        binding.searchView.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String?): Boolean {
                if (query != null) {
                    searchPisang(query)
                }
                return true
            }

            override fun onQueryTextChange(newText: String?): Boolean {
                if (newText.isNullOrBlank()) {
                    loadAllData()
                } else {
                    searchPisang(newText)
                }
                return true
            }
        })
    }

    private fun loadAllData() {
        val reference = firebaseDatabase.getReference("product").orderByChild("timestamp")
        reference.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                pisangList.clear()
                for (dataSnapshot in snapshot.children.reversed()) {
                    val pisang = dataSnapshot.getValue(Pisang::class.java)
                    if (pisang != null) {
                        pisangList.add(pisang)
                    }
                }
                pisangAdapter = PisangAdapter(pisangList)
                binding.rvSearch.adapter = pisangAdapter
                pisangAdapter.notifyDataSetChanged()
            }

            override fun onCancelled(error: DatabaseError) {
                Log.e("SearchActivity", "Firebase error: ${error.message}")
            }
        })
    }

    private fun searchPisang(query: String) {
        val resultText = predict(query)
        val pisangType = getPisangType(query)
        val filteredList = pisangList.filter {
            it.nama_pisang.contains(query, ignoreCase = true) ||
                    it.nama_pisang.equals(pisangType, ignoreCase = true)
        }

        binding.resultTextView.text = resultText
        pisangAdapter = PisangAdapter(filteredList)
        binding.rvSearch.adapter = pisangAdapter
        pisangAdapter.notifyDataSetChanged()
    }

    private fun getPisangType(query: String): String {
        return when {
            query.contains("goreng", ignoreCase = true) -> "Pisang Kepok"
            query.contains("smoothie", ignoreCase = true) -> "Pisang Ambon"
            query.contains("rebus", ignoreCase = true) -> "Pisang Tanduk"
            query.contains("nugget", ignoreCase = true) -> "Pisang Uli"
            query.contains("bubur", ignoreCase = true) -> "Pisang Raja"
            else -> ""
        }
    }

    private fun loadModelFile(context: Context, modelName: String): MappedByteBuffer {
        val assetFileDescriptor = context.assets.openFd(modelName)
        val fileInputStream = FileInputStream(assetFileDescriptor.fileDescriptor)
        val fileChannel = fileInputStream.channel
        val startOffset = assetFileDescriptor.startOffset
        val declaredLength = assetFileDescriptor.declaredLength
        return fileChannel.map(FileChannel.MapMode.READ_ONLY, startOffset, declaredLength)
    }

    private fun preprocessText(text: String): FloatArray {
        val cleanedText = text.toLowerCase().replace(Regex("[^a-zA-Z\\d\\s]"), "").split(" ")
        val wordIndex = mutableMapOf<String, Int>()
        var index = 1
        cleanedText.forEach { word ->
            if (word.isNotBlank() && !wordIndex.containsKey(word)) {
                wordIndex[word] = index++
            }
        }

        val sequence = cleanedText.map { wordIndex[it] ?: 0 }
        val maxlen = 100
        val paddedSequence = if (sequence.size > maxlen) {
            sequence.take(maxlen)
        } else {
            sequence + List(maxlen - sequence.size) { 0 }
        }

        return paddedSequence.map { it.toFloat() }.toFloatArray()
    }

    private fun predict(inputText: String): String {
        val input = preprocessText(inputText)
        val inputBuffer = ByteBuffer.allocateDirect(4 * input.size).order(ByteOrder.nativeOrder())
        for (value in input) {
            inputBuffer.putFloat(value)
        }
        val outputBuffer = ByteBuffer.allocateDirect(4).order(ByteOrder.nativeOrder())
        interpreter.run(inputBuffer, outputBuffer)
        outputBuffer.rewind()
        val prediction = outputBuffer.float

        return if (prediction > 0.5) pisangInfoList[getPisangType(inputText)] ?: "Informasi mengenai $inputText tidak ditemukan."
        else "Informasi mengenai $inputText tidak ditemukan."
    }
}