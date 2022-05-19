package com.example.shoppingcartfirebase.viewmodel

import com.example.shoppingcartfirebase.model.CartModel

interface CartLoadListener {
    fun onLoadCartSuccess(cartModelList: List<CartModel>)
    fun onLoadCartFailed(message: String?)
}