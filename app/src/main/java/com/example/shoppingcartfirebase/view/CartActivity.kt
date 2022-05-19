package com.example.shoppingcartfirebase.view

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.shoppingcartfirebase.R
import com.example.shoppingcartfirebase.model.CartModel
import com.example.shoppingcartfirebase.viewmodel.CartAdapter
import com.example.shoppingcartfirebase.viewmodel.CartLoadListener
import com.example.shoppingcartfirebase.viewmodel.UpdateCart
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import kotlinx.android.synthetic.main.activity_cart.*
import kotlinx.android.synthetic.main.activity_main.*
import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe
import org.greenrobot.eventbus.ThreadMode

class CartActivity : AppCompatActivity(), CartLoadListener {

    var cartLoadListener: CartLoadListener? = null

    override fun onStart() {
        super.onStart()
        EventBus.getDefault().register(this)
    }

    override fun onStop() {
        super.onStop()
        if(EventBus.getDefault().hasSubscriberForEvent(UpdateCart::class.java))
            EventBus.getDefault().removeStickyEvent(UpdateCart::class.java)
        EventBus.getDefault().unregister(this)
    }

    @Subscribe(threadMode = ThreadMode.MAIN, sticky = true)
    fun updateCartEvent(event: UpdateCart) {
        loadCartFromFirebase()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_cart)
        init()
        loadCartFromFirebase()
    }

    private fun loadCartFromFirebase() {
        val cartModels: MutableList<CartModel> = ArrayList()
        FirebaseDatabase.getInstance()
            .getReference("Cart")
            .child("UNIQUE_USER_ID")
            .addListenerForSingleValueEvent(object: ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    for(cartSnapshot in snapshot.children) {
                        val cartModel = cartSnapshot.getValue(CartModel::class.java)
                        cartModel!!.key = cartSnapshot.key
                        cartModels.add(cartModel)
                    }
                    cartLoadListener!!.onLoadCartSuccess(cartModels)
                }

                override fun onCancelled(error: DatabaseError) {
                    cartLoadListener!!.onLoadCartFailed(error.message)
                }

            })
    }

    private fun init() {
        cartLoadListener = this
        val layoutManager = LinearLayoutManager(this)
        cartRecycler!!.layoutManager = layoutManager
        cartRecycler!!.addItemDecoration(DividerItemDecoration(this, layoutManager.orientation))
        backButton!!.setOnClickListener {
            finish()
        }
    }

    override fun onLoadCartSuccess(cartModelList: List<CartModel>) {
        var sum = 0.00
        for(cartModel in cartModelList!!) {
            sum += cartModel!!.totalPrice
            sum = String.format("%.2f", sum).toDouble()
        }
        totalText.text = StringBuilder("$").append(sum)
        val adapter = CartAdapter(this, cartModelList)
        cartRecycler!!.adapter = adapter
    }

    override fun onLoadCartFailed(message: String?) {
        Snackbar.make(mainLayout,message!!, Snackbar.LENGTH_LONG).show()
    }
}