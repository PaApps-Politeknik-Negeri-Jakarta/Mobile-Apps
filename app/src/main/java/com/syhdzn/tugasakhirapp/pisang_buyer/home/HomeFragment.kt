package com.syhdzn.tugasakhirapp.pisang_buyer.home


import android.animation.AnimatorListenerAdapter
import android.animation.ObjectAnimator
import android.content.Intent
import android.graphics.Color
import android.graphics.PorterDuff
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.AnimationUtils
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.StaggeredGridLayoutManager
import androidx.viewpager2.widget.ViewPager2
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.syhdzn.tugasakhirapp.R
import com.syhdzn.tugasakhirapp.databinding.FragmentHomeBinding
import com.syhdzn.tugasakhirapp.pisang_buyer.UserUtils
import com.syhdzn.tugasakhirapp.pisang_buyer.adapter.ProductAdapter
import com.syhdzn.tugasakhirapp.pisang_buyer.data.Product
import com.syhdzn.tugasakhirapp.pisang_buyer.search.SearchActivity

class HomeFragment : Fragment() {
    private lateinit var binding: FragmentHomeBinding
    private val viewModel: HomeViewModel by viewModels()
    private lateinit var productList: ArrayList<Product>
    private lateinit var originalProductList: ArrayList<Product>
    private lateinit var firebaseRef: DatabaseReference
    private lateinit var mAuth: FirebaseAuth
    private val selectedFilters = mutableSetOf<String>()

    private val imageIds = listOf(
        R.drawable.banner_1,
        R.drawable.banner_2,
        R.drawable.banner_3,
        R.drawable.banner_4,
        R.drawable.banner_5
    )

    private val autoScrollHandler = Handler(Looper.getMainLooper())
    private val autoScrollRunnable = Runnable {
        val currentPosition = binding.vpHomeCarousel.currentItem
        val newPosition = (currentPosition + 1) % imageIds.size
        binding.vpHomeCarousel.currentItem = newPosition
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentHomeBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        firebaseRef = FirebaseDatabase.getInstance("https://tugasakhirapp-c5669-default-rtdb.asia-southeast1.firebasedatabase.app").reference
        productList = arrayListOf()
        originalProductList = arrayListOf()
        mAuth = FirebaseAuth.getInstance()

        setupViewPager()
        setupPageIndicator()
        observeCurrentPage()
        setupRecyclerView()
        fetchData()
        setupAction()
        loadUserData()

        setupCategoryFilter(binding.pisangAmbon, binding.clearPisangAmbon, "Pisang Ambon")
        setupCategoryFilter(binding.pisangUli, binding.clearPisangUli, "Pisang Uli")
        setupCategoryFilter(binding.pisangBarangan, binding.clearPisangBarangan, "Pisang Barangan")
        setupCategoryFilter(binding.pisangKepok, binding.clearPisangKepok, "Pisang Kepok")
        setupCategoryFilter(binding.pisangTanduk, binding.clearPisangTanduk, "Pisang Tanduk")
        setupCategoryFilter(binding.pisangRaja, binding.clearPisangRaja, "Pisang Raja")
    }

    private fun setupCategoryFilter(layout: LinearLayout, clearImage: ImageView, name: String) {
        val color = Color.parseColor("#FF0000")
        clearImage.setColorFilter(color, PorterDuff.Mode.SRC_IN)
        clearImage.tag = name


        layout.setOnClickListener {
            if (selectedFilters.contains(name)) {
                selectedFilters.remove(name)
                animateFilterRemove(clearImage)
            } else {
                selectedFilters.add(name)
                animateFilterAdd(clearImage)
            }
            filterProducts()
        }

        clearImage.setOnClickListener {
            selectedFilters.remove(name)
            animateFilterRemove(clearImage)
            filterProducts()
        }
    }

    private fun animateFilterAdd(clearImage: ImageView) {
        clearImage.visibility = View.VISIBLE
        val rotate = AnimationUtils.loadAnimation(context, R.anim.rotate)
        clearImage.startAnimation(rotate)
    }

    private fun animateFilterRemove(clearImage: ImageView) {
        val fadeOut = ObjectAnimator.ofFloat(clearImage, "alpha", 1f, 0f)
        fadeOut.addListener(object : AnimatorListenerAdapter() {
            override fun onAnimationEnd(animation: android.animation.Animator) {
                if (!selectedFilters.contains(clearImage.tag.toString())) {
                    clearImage.alpha = 1f
                    clearImage.visibility = View.GONE
                }
            }
        })
        fadeOut.start()
    }

    private fun filterProducts() {
        if (selectedFilters.isEmpty()) {
            productList.clear()
            productList.addAll(originalProductList)
        } else {
            productList.clear()
            productList.addAll(originalProductList.filter { selectedFilters.contains(it.nama_pisang) })
        }
        binding.rvProduct.adapter?.notifyDataSetChanged()
        Log.d("HomeFragment", "Filtered ${productList.size} products by filters: $selectedFilters")
    }

    private fun setupAction() {
        binding.imageView9.setOnClickListener {
            val intent = Intent(requireContext(), SearchActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_NO_ANIMATION
            startActivity(intent)
        }
    }

    private fun loadUserData() {
        UserUtils.loadUserData(mAuth, firebaseRef) { fullname ->
            if (fullname != null) {
                Log.d("HomeFragment", fullname)
                val sharedPreferences = requireActivity().getSharedPreferences("app_prefs", AppCompatActivity.MODE_PRIVATE)
                val editor = sharedPreferences.edit()
                editor.putString("FULL_NAME", fullname)
                editor.apply()
            } else {
                Log.d("UserFragment", "Failed to load user data")
            }
        }
    }

    private fun setupViewPager() {
        val adapter = CarouselAdapter(imageIds)
        binding.vpHomeCarousel.adapter = adapter
        binding.vpHomeCarousel.registerOnPageChangeCallback(object : ViewPager2.OnPageChangeCallback() {
            override fun onPageSelected(position: Int) {
                viewModel.setCurrentPage(position)
                startAutoScroll()
            }
        })
    }

    private fun setupRecyclerView() {
        binding.rvProduct.apply {
            setHasFixedSize(true)
            layoutManager = StaggeredGridLayoutManager(2, StaggeredGridLayoutManager.VERTICAL)
            adapter = ProductAdapter(productList)
        }
    }

    private fun fetchData() {
        firebaseRef.child("product").addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                productList.clear()
                originalProductList.clear()
                snapshot.children.mapNotNullTo(productList) { it.getValue(Product::class.java) }
                originalProductList.addAll(productList)
                binding.rvProduct.adapter?.notifyDataSetChanged()
                Log.d("HomeFragment", "Fetched ${productList.size} products")
            }

            override fun onCancelled(error: DatabaseError) {
                Toast.makeText(context, "Error: ${error.message}", Toast.LENGTH_SHORT).show()
            }
        })
    }

    private fun observeCurrentPage() {
        viewModel.currentPage.observe(viewLifecycleOwner, { position ->
            updatePageIndicator(position)
        })
    }

    private fun setupPageIndicator() {
        val indicatorContainer = binding.indicatorContainer
        val context = indicatorContainer.context

        for (i in imageIds.indices) {
            val indicator = LayoutInflater.from(context).inflate(
                R.layout.carousel_indicator, indicatorContainer, false
            ) as ImageView
            indicatorContainer.addView(indicator)
        }
    }

    private fun updatePageIndicator(position: Int) {
        val indicatorContainer = binding.indicatorContainer
        for (i in 0 until indicatorContainer.childCount) {
            val indicator = indicatorContainer.getChildAt(i) as ImageView
            indicator.setImageResource(
                if (i == position % imageIds.size) R.drawable.indicator_dot_selected
                else R.drawable.indicator_dot_unselected
            )
        }
    }

    private fun startAutoScroll() {
        stopAutoScroll()
        val autoScrollInterval = 4000L
        autoScrollHandler.postDelayed(autoScrollRunnable, autoScrollInterval)
    }

    private fun stopAutoScroll() {
        autoScrollHandler.removeCallbacks(autoScrollRunnable)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        stopAutoScroll()
    }
}
