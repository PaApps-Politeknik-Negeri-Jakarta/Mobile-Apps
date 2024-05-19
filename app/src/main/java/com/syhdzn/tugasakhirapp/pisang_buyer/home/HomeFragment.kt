package com.syhdzn.tugasakhirapp.pisang_buyer.home

import android.os.Bundle
import android.os.Handler
import android.os.Looper
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.lifecycle.ViewModelProvider
import androidx.viewpager2.widget.ViewPager2
import com.syhdzn.tugasakhirapp.R
import com.syhdzn.tugasakhirapp.databinding.FragmentHomeBinding

class HomeFragment : Fragment() {

    private lateinit var binding: FragmentHomeBinding
    private lateinit var viewModel: HomeViewModel

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
    ): View? {
        binding = FragmentHomeBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        viewModel = ViewModelProvider(this).get(HomeViewModel::class.java)

        setupViewPager()
        setupPageIndicator()
        observeCurrentPage()
    }

    private fun setupViewPager() {
        val viewPager: ViewPager2 = binding.vpHomeCarousel
        val adapter = CarouselAdapter(imageIds)
        viewPager.adapter = adapter
        viewPager.registerOnPageChangeCallback(object : ViewPager2.OnPageChangeCallback() {
            override fun onPageSelected(position: Int) {
                viewModel.setCurrentPage(position)
                startAutoScroll()
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
