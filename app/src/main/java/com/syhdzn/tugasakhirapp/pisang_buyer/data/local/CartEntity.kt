package com.syhdzn.tugasakhirapp.pisang_buyer.data.local

import android.os.Parcel
import android.os.Parcelable
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

    @field:ColumnInfo(name = "harga_pisang")
    var price: Double,

    @field:ColumnInfo(name = "id_pisang")
    var idbarang: String = "",

    @field:ColumnInfo(name = "image_url")
    var imageUrl: String = "",

    @field:ColumnInfo(name = "amount")
    val amount: Int,

    @field:ColumnInfo(name = "ignore_check")
    var ignoreCheck: Boolean = false
) : Parcelable {
    constructor(parcel: Parcel) : this(
        parcel.readLong(),
        parcel.readString() ?: "",
        parcel.readDouble(),
        parcel.readString() ?: "",
        parcel.readString() ?: "",
        parcel.readInt(),
        parcel.readByte() != 0.toByte()
    )

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeLong(id)
        parcel.writeString(name)
        parcel.writeDouble(price)
        parcel.writeString(idbarang)
        parcel.writeString(imageUrl)
        parcel.writeInt(amount)
        parcel.writeByte(if (ignoreCheck) 1 else 0)
    }

    override fun describeContents(): Int {
        return 0
    }

    companion object CREATOR : Parcelable.Creator<CartEntity> {
        override fun createFromParcel(parcel: Parcel): CartEntity {
            return CartEntity(parcel)
        }

        override fun newArray(size: Int): Array<CartEntity?> {
            return arrayOfNulls(size)
        }
    }
}
