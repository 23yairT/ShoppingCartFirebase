package com.example.shoppingcartfirebase.viewmodel

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.shoppingcartfirebase.R
import com.example.shoppingcartfirebase.model.CartModel
import com.google.firebase.database.FirebaseDatabase
import org.greenrobot.eventbus.EventBus

class CartAdapter (

    private val context: Context,
    private val cartModelList: List<CartModel>

) : RecyclerView.Adapter<CartAdapter.CartViewHolder>() {

    class CartViewHolder(itemView: View): RecyclerView.ViewHolder(itemView) {

        var minusButton: ImageView? = null
        var plusButton: ImageView? = null
        var imageView: ImageView? = null
        var deleteButton: ImageView? = null
        var nameText: TextView? = null
        var priceText: TextView? = null
        var quantityText: TextView? = null

        init {
            minusButton = itemView.findViewById(R.id.minusButton) as ImageView
            plusButton = itemView.findViewById(R.id.plusButton) as ImageView
            imageView = itemView.findViewById(R.id.imageView) as ImageView
            deleteButton = itemView.findViewById(R.id.deleteButton) as ImageView
            nameText = itemView.findViewById(R.id.nameText) as TextView
            priceText = itemView.findViewById(R.id.priceText) as TextView
            quantityText = itemView.findViewById(R.id.quantityText) as TextView
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CartViewHolder {
        return CartViewHolder(
            LayoutInflater.from(context)
                .inflate(R.layout.layout_cart_item, parent, false)
        )
    }

    override fun onBindViewHolder(holder: CartViewHolder, position: Int) {
        Glide.with(context)
            .load(cartModelList[position].image)
            .into(holder.imageView!!)
        holder.nameText!!.text = StringBuilder().append(cartModelList[position].name)
        holder.priceText!!.text = java.lang.StringBuilder("$").append(cartModelList[position].price)
        holder.quantityText!!.text = java.lang.StringBuilder("").append(cartModelList[position].quantity)
        holder.minusButton!!.setOnClickListener {_ -> minusCartItem(holder, cartModelList[position]) }
        holder.plusButton!!.setOnClickListener {_ -> plusCartItem(holder, cartModelList[position]) }
        holder.deleteButton!!.setOnClickListener {_ ->
            val dialog = AlertDialog.Builder(context)
                .setTitle("Delete Item")
                .setMessage("Are you sure")
                .setNegativeButton("CANCEL") { dialog, _ -> dialog.dismiss()}
                .setPositiveButton("DELETE") { dialog, _ ->

                    notifyItemRemoved(position)
                    FirebaseDatabase.getInstance()
                        .getReference("Cart")
                        .child("UNIQUE_USER_ID")
                        .child(cartModelList[position].key!!)
                        .removeValue()
                        .addOnSuccessListener {
                            EventBus.getDefault().postSticky(UpdateCart())
                        }
                }
                .create()
                dialog.show()
        }
    }

    private fun plusCartItem(holder: CartAdapter.CartViewHolder, cartModel: CartModel) {
        cartModel.quantity += 1
        cartModel.totalPrice = cartModel.quantity * cartModel.price!!.toFloat()
        holder.quantityText!!.text = StringBuilder("").append(cartModel.quantity)
        updateFirebase(cartModel)
    }

    private fun minusCartItem(holder: CartAdapter.CartViewHolder, cartModel: CartModel) {
        if(cartModel.quantity > 1) {
            cartModel.quantity -= 1
            cartModel.totalPrice = cartModel.quantity * cartModel.price!!.toFloat()
            holder.quantityText!!.text = StringBuilder("").append(cartModel.quantity)
            updateFirebase(cartModel)
        }
    }

    private fun updateFirebase(cartModel: CartModel) {
        FirebaseDatabase.getInstance()
            .getReference("Cart")
            .child("UNIQUE_USER_ID")
            .child(cartModel.key!!)
            .setValue(cartModel)
            .addOnSuccessListener { EventBus.getDefault().postSticky(UpdateCart()) }
    }

    override fun getItemCount(): Int {
        return cartModelList.size
    }

}