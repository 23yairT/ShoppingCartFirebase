package com.example.shoppingcartfirebase.viewmodel

import android.view.View

interface RecyclerClickListener {
    fun onItemClickListener(view: View?, position: Int)
}