package com.syhdzn.tugasakhirapp.pisang_buyer.search

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.activity.OnBackPressedCallback
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.SearchView
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.recyclerview.widget.GridLayoutManager
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.syhdzn.tugasakhirapp.databinding.ActivitySearchBinding
import com.syhdzn.tugasakhirapp.pisang_buyer.dashboard.BuyerDashboardActivity
import org.tensorflow.lite.Interpreter
import org.tensorflow.lite.flex.FlexDelegate
import java.io.FileInputStream
import java.nio.ByteBuffer
import java.nio.ByteOrder
import java.nio.MappedByteBuffer
import java.nio.channels.FileChannel
import java.util.Locale


class SearchActivity : AppCompatActivity() {

    private lateinit var binding: ActivitySearchBinding
    private lateinit var pisangAdapter: PisangAdapter
    private lateinit var firebaseDatabase: FirebaseDatabase
    private lateinit var pisangList: MutableList<Pisang>
    private lateinit var interpreter: Interpreter

    private val pisangInfoList = mapOf(
        "Pisang Ambon" to "Pisang yang cocok untuk smoothie adalah pisang ambon.",
        "Pisang Barangan" to "Pisang yang cocok untuk dikonsumsi langsung adalah pisang barangan.",
        "Pisang Kepok" to "Pisang yang cocok untuk gorengan adalah pisang kepok.",
        "Pisang Raja" to "Pisang yang cocok untuk bubur pisang adalah pisang raja.",
        "Pisang Tanduk" to "Pisang yang cocok untuk direbus adalah pisang tanduk.",
        "Pisang Uli" to "Pisang yang cocok untuk nugget pisang adalah pisang uli.",
        "Pisang Ambon adalah salah satu varietas pisang yang sering digunakan untuk jus dan smoothies." to "Pisang Ambon adalah salah satu varietas pisang yang sering digunakan untuk jus dan smoothies.",
        "Pisang Barangan memiliki tekstur yang lembut dan manis, cocok untuk dimakan langsung." to "Pisang Barangan memiliki tekstur yang lembut dan manis, cocok untuk dimakan langsung.",
        "Pisang Kepok sering digunakan dalam olahan pisang goreng karena teksturnya yang padat." to "Pisang Kepok sering digunakan dalam olahan pisang goreng karena teksturnya yang padat.",
        "Pisang Raja sangat populer untuk dijadikan bahan dasar pisang bakar dan kolak." to "Pisang Raja sangat populer untuk dijadikan bahan dasar pisang bakar dan kolak.",
        "Pisang Tanduk memiliki ukuran yang besar dan sering digunakan dalam pembuatan keripik pisang." to "Pisang Tanduk memiliki ukuran yang besar dan sering digunakan dalam pembuatan keripik pisang.",
        "Pisang Uli memiliki rasa yang manis dan tekstur yang kenyal, cocok untuk dibuat pisang goreng." to "Pisang Uli memiliki rasa yang manis dan tekstur yang kenyal, cocok untuk dibuat pisang goreng.",
        // Tambahkan lebih banyak pengetahuan sesuai permintaan
        "Pisang Ambon cocok untuk dikonsumsi langsung maupun diolah menjadi berbagai hidangan." to "Pisang Ambon cocok untuk dikonsumsi langsung maupun diolah menjadi berbagai hidangan.",
        "Pisang Barangan dapat dibuat menjadi pisang bakar yang lezat." to "Pisang Barangan dapat dibuat menjadi pisang bakar yang lezat.",
        "Pisang Kepok juga enak dimakan langsung atau dibuat kolak." to "Pisang Kepok juga enak dimakan langsung atau dibuat kolak.",
        "Pisang Raja cocok untuk dibuat bubur pisang atau digoreng." to "Pisang Raja cocok untuk dibuat bubur pisang atau digoreng.",
        "Pisang Tanduk sangat cocok untuk dibuat pisang goreng." to "Pisang Tanduk sangat cocok untuk dibuat pisang goreng.",
        "Pisang Uli bisa diolah menjadi keripik pisang yang renyah." to "Pisang Uli bisa diolah menjadi keripik pisang yang renyah.",
        "Pisang Ambon banyak digunakan dalam pembuatan kue pisang." to "Pisang Ambon banyak digunakan dalam pembuatan kue pisang.",
        "Pisang Barangan sering dijadikan bahan utama dalam es campur." to "Pisang Barangan sering dijadikan bahan utama dalam es campur.",
        "Pisang Kepok memiliki rasa yang manis dan tekstur yang kenyal." to "Pisang Kepok memiliki rasa yang manis dan tekstur yang kenyal.",
        "Pisang Raja sering digunakan dalam pembuatan pisang molen." to "Pisang Raja sering digunakan dalam pembuatan pisang molen.",
        "Pisang Tanduk bisa dijadikan bahan dasar untuk pisang sale." to "Pisang Tanduk bisa dijadikan bahan dasar untuk pisang sale.",
        "Pisang Uli cocok untuk dibuat kolak pisang." to "Pisang Uli cocok untuk dibuat kolak pisang.",
        "Pisang Ambon sering diolah menjadi jus pisang yang segar." to "Pisang Ambon sering diolah menjadi jus pisang yang segar.",
        "Pisang Barangan bisa dimakan langsung sebagai buah segar." to "Pisang Barangan bisa dimakan langsung sebagai buah segar.",
        "Pisang Kepok sering digunakan dalam resep pisang bolen." to "Pisang Kepok sering digunakan dalam resep pisang bolen.",
        "Pisang Raja enak dimakan langsung atau sebagai tambahan pada es krim." to "Pisang Raja enak dimakan langsung atau sebagai tambahan pada es krim.",
        "Pisang Tanduk sering diolah menjadi pisang kremes yang lezat." to "Pisang Tanduk sering diolah menjadi pisang kremes yang lezat.",
        "Pisang Uli bisa diolah menjadi pisang goreng yang renyah." to "Pisang Uli bisa diolah menjadi pisang goreng yang renyah.",
        "Pisang Ambon cocok untuk dijadikan bahan dasar smoothies." to "Pisang Ambon cocok untuk dijadikan bahan dasar smoothies.",
        "Pisang Barangan memiliki rasa yang manis dan cocok untuk makanan penutup." to "Pisang Barangan memiliki rasa yang manis dan cocok untuk makanan penutup.",
        "Pisang Kepok sering digunakan dalam pembuatan getuk pisang." to "Pisang Kepok sering digunakan dalam pembuatan getuk pisang.",
        "Pisang Raja sangat cocok untuk dibuat pisang bakar keju." to "Pisang Raja sangat cocok untuk dibuat pisang bakar keju.",
        "Pisang Tanduk enak dimakan langsung atau direbus." to "Pisang Tanduk enak dimakan langsung atau direbus.",
        "Pisang Uli sering digunakan dalam resep pisang goreng madu." to "Pisang Uli sering digunakan dalam resep pisang goreng madu.",
        "Pisang Ambon dapat dijadikan bahan dasar kue bolu pisang." to "Pisang Ambon dapat dijadikan bahan dasar kue bolu pisang.",
        "Pisang Barangan enak dimakan langsung atau dijadikan topping es krim." to "Pisang Barangan enak dimakan langsung atau dijadikan topping es krim.",
        "Pisang Kepok bisa diolah menjadi pisang goreng krispi." to "Pisang Kepok bisa diolah menjadi pisang goreng krispi.",
        "Pisang Raja enak dimakan langsung atau sebagai campuran pada es campur." to "Pisang Raja enak dimakan langsung atau sebagai campuran pada es campur.",
        "Pisang Tanduk sering diolah menjadi pisang cokelat." to "Pisang Tanduk sering diolah menjadi pisang cokelat.",
        "Pisang Uli cocok untuk dijadikan pisang goreng tepung." to "Pisang Uli cocok untuk dijadikan pisang goreng tepung.",
        "Pisang Ambon sering digunakan dalam pembuatan pancake pisang." to "Pisang Ambon sering digunakan dalam pembuatan pancake pisang.",
        "Pisang Barangan memiliki tekstur lembut yang cocok untuk bayi." to "Pisang Barangan memiliki tekstur lembut yang cocok untuk bayi.",
        "Pisang Kepok sering diolah menjadi pisang goreng tepung roti." to "Pisang Kepok sering diolah menjadi pisang goreng tepung roti.",
        "Pisang Raja sangat cocok untuk dibuat pisang cokelat keju." to "Pisang Raja sangat cocok untuk dibuat pisang cokelat keju.",
        "Pisang Tanduk sering digunakan dalam pembuatan pisang sale." to "Pisang Tanduk sering digunakan dalam pembuatan pisang sale.",
        "Pisang Uli enak dimakan langsung atau diolah menjadi pisang goreng." to "Pisang Uli enak dimakan langsung atau diolah menjadi pisang goreng."
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
                // Handle back press
            }
        })

        binding.btnBack.setOnClickListener {
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
        val filteredList = pisangList.filter {
            it.nama_pisang.contains(query, ignoreCase = true) ||
                    it.nama_pisang.equals(getPisangType(resultText), ignoreCase = true)
        }

        binding.resultTextView.text = resultText
        pisangAdapter = PisangAdapter(filteredList)
        binding.rvSearch.adapter = pisangAdapter
        pisangAdapter.notifyDataSetChanged()
    }

    private fun getPisangType(query: String): String {
        for ((key, _) in pisangInfoList) {
            if (query.contains(key, ignoreCase = true)) {
                return key
            }
        }
        return ""
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

        return if (prediction > 0.5) {
            // Mencocokkan langsung dengan pengetahuan yang ada
            val bestMatch = pisangInfoList.keys.maxByOrNull { key ->
                fuzzyMatch(inputText, key)
            }
            pisangInfoList[bestMatch] ?: "Informasi mengenai $inputText tidak ditemukan."
        } else {
            "Informasi mengenai $inputText tidak ditemukan."
        }
    }

    private fun fuzzyMatch(query: String, key: String): Int {

        val levenshteinDistance = calculateLevenshteinDistance(
            query.lowercase(Locale.getDefault()),
            key.lowercase(Locale.getDefault())
        )


        return 100 - (levenshteinDistance * 100 / maxOf(query.length, key.length))
    }

    private fun calculateLevenshteinDistance(str1: String, str2: String): Int {
        val dp = Array(str1.length + 1) { IntArray(str2.length + 1) }

        for (i in 0..str1.length) {
            for (j in 0..str2.length) {
                if (i == 0) {
                    dp[i][j] = j
                } else if (j == 0) {
                    dp[i][j] = i
                } else {
                    dp[i][j] = minOf(
                        dp[i - 1][j - 1] + if (str1[i - 1] == str2[j - 1]) 0 else 1,
                        dp[i - 1][j] + 1,
                        dp[i][j - 1] + 1
                    )
                }
            }
        }

        return dp[str1.length][str2.length]
    }
}
