package com.example.shoppingcartfirebase.viewmodel

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.shoppingcartfirebase.R
import com.example.shoppingcartfirebase.model.CartModel
import com.example.shoppingcartfirebase.model.MenuModel
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import org.greenrobot.eventbus.EventBus

class MenuAdapter (
    private val context: Context,
    private val list: List<MenuModel>,
    private val cartListener: CartLoadListener
        ): RecyclerView.Adapter<MenuAdapter.MenuViewHolder>() {

    class MenuViewHolder (itemView: View): RecyclerView.ViewHolder(itemView), View.OnClickListener {
        var menuImage: ImageView? = null
        var menuName: TextView? = null
        var menuPrice: TextView? = null

        private var clickListener: RecyclerClickListener? = null

        fun setClickListener(clickListener: RecyclerClickListener) {
            this.clickListener = clickListener
        }

        init {
            menuImage = itemView.findViewById(R.id.imageView) as ImageView
            menuName = itemView.findViewById(R.id.menuItemText) as TextView
            menuPrice = itemView.findViewById(R.id.priceText) as TextView

            itemView.setOnClickListener(this)
        }

        override fun onClick(v: View?) {
            clickListener!!.onItemClickListener(v, adapterPosition)
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MenuViewHolder {
        return MenuViewHolder(
            LayoutInflater.from(context)
                .inflate(R.layout.layout_menu_item, parent, false)
        )
    }

    override fun onBindViewHolder(holder: MenuViewHolder, position: Int) {
        Glide.with(context)
            .load(list[position].image)
            .into(holder.menuImage!!)
        holder.menuName!!.text = StringBuilder().append(list[position].name)
        holder.menuPrice!!.text = java.lang.StringBuilder("$").append(list[position].price)

        holder.setClickListener(object: RecyclerClickListener {
            override fun onItemClickListener(view: View?, position: Int) {
                addToCart(list[position])
            }

        })
    }

    private fun addToCart(menuModel: MenuModel) {
        val userCart = FirebaseDatabase.getInstance()
            .getReference("Cart")
            .child("UNIQUE_USER_ID")

        userCart.child(menuModel.key!!)
            .addListenerForSingleValueEvent(object: ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    if(snapshot.exists()) {
                        val cartModel = snapshot.getValue(CartModel::class.java)
                        val updateData: MutableMap<String, Any> = HashMap()
                        updateData["quantity"] = cartModel!!.quantity
                        updateData["totalPrice"] = cartModel!!.quantity * cartModel.price!!.toFloat()

                        userCart.child(menuModel.key!!)
                            .updateChildren(updateData)
                            .addOnSuccessListener {
                                cartListener.onLoadCartFailed("Added to Cart")
                                EventBus.getDefault().postSticky(UpdateCart())
                            }
                            .addOnFailureListener { e -> cartListener.onLoadCartFailed(e.message) }
                    }
                    else {
                        val cartModel = CartModel()
                        cartModel.key = menuModel.key
                        cartModel.name = menuModel.name
                        cartModel.image = menuModel.image
                        cartModel.price = menuModel.price
                        cartModel.quantity = 1
                        cartModel.totalPrice = menuModel.price!!.toFloat()

                        userCart.child(menuModel.key!!)
                            .setValue(cartModel)
                            .addOnSuccessListener {
                                cartListener.onLoadCartFailed("Added to cart")
                                EventBus.getDefault().postSticky(UpdateCart())
                            }
                    }
                }

                override fun onCancelled(error: DatabaseError) {
                    cartListener.onLoadCartFailed(error.message)
                }

            })
    }

    override fun getItemCount(): Int {
        return list.size
    }

}