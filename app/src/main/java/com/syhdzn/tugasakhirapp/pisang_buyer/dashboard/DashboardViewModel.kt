package com.syhdzn.tugasakhirapp.pisang_buyer.dashboard

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel

class DashboardViewModel() : ViewModel() {
    private val _selectedItemId = MutableLiveData<Int>()
    val selectedItemId: LiveData<Int>
        get() = _selectedItemId

    fun setSelectedItemId(itemId: Int) {
        _selectedItemId.value = itemId
    }
}