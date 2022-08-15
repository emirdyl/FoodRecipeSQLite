package com.dyltech.foodrecipesqlite

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.navigation.Navigation
import androidx.recyclerview.widget.RecyclerView
import kotlinx.android.synthetic.main.recyler_row.view.*

class ListRecylerAdapter(val foodList: ArrayList<String>, val idList: ArrayList<Int>) :
    RecyclerView.Adapter<ListRecylerAdapter.FoodHolder>() {

    class FoodHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {


    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): FoodHolder {
        val inflater = LayoutInflater.from(parent.context)
        val view = inflater.inflate(R.layout.recyler_row, parent, false)
        return FoodHolder(view)

    }

    override fun onBindViewHolder(holder: FoodHolder, position: Int) {
        holder.itemView.recycler_row_text.text = foodList[position]
        holder.itemView.setOnClickListener{
         val action= ListFragmentDirections.actionListFragmentToRecipeFragment("recyclerdangeldim",idList[position])
            Navigation.findNavController(it).navigate(action)
        }

    }

    override fun getItemCount(): Int {
        return foodList.size

    }
}