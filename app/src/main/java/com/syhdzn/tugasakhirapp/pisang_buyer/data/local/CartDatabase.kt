package com.syhdzn.tugasakhirapp.pisang_buyer.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

@Database(entities = [CartEntity::class], version = 1, exportSchema = false)
abstract class CartDatabase : RoomDatabase(){
    abstract fun cartDAO() : CartDao
    companion object{
        @Volatile
        private var INSTANCE: CartDatabase? = null

        @JvmStatic
        fun getInstance(context: Context): CartDatabase {
            if (INSTANCE == null) {
                synchronized(CartDatabase::class.java) {
                    INSTANCE = Room.databaseBuilder(
                        context.applicationContext,
                        CartDatabase::class.java, "cart_database"
                    )
                        .build()
                }
            }
            return INSTANCE as CartDatabase
        }
    }
}