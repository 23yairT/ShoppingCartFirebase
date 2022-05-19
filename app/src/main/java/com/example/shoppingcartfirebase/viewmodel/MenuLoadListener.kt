package com.example.shoppingcartfirebase.viewmodel

import com.example.shoppingcartfirebase.model.MenuModel

interface MenuLoadListener {
    fun onMenuLoadSuccess(menuModelList:List<MenuModel>?)
    fun onMenuLoadFailed(message:String?)
}