package com.syhdzn.tugasakhirapp.PisangBuyer.home

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel

class HomeViewModel : ViewModel() {

    private val _currentPage = MutableLiveData<Int>()
    val currentPage: LiveData<Int>
        get() = _currentPage

    fun setCurrentPage(position: Int) {
        _currentPage.value = position
    }
}
