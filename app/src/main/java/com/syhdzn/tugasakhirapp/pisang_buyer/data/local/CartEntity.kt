package com.syhdzn.tugasakhirapp.pisang_buyer.data.local

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "cart_table")
data class CartEntity(

    @PrimaryKey(autoGenerate = true)
    @field:ColumnInfo(name = "cartId")
    val id: Long = 0,

    @field:ColumnInfo(name = "nama_pisang")
    var name: String = "",

    @field:ColumnInfo(name = "image_url")
    var imageUrl: String = "",

    @field:ColumnInfo(name = "amount")
    val amount: Int
)
