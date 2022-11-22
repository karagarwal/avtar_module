package com.example.avtar_module

import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.avtar_module.databinding.AvatarItemBinding

open class AvtarAdapter(
    val contexts: Context,
    val homeArrayList: ArrayList<AvtarModel>,
    val callback: (temp: AvtarModel) -> Unit
) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    lateinit var rowBinding: AvatarItemBinding

    override fun onCreateViewHolder(container: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        rowBinding =
            AvatarItemBinding.inflate(
                LayoutInflater.from(container.context),
                container,
                false
            )
        return ViewHolder(rowBinding)
    }

    override fun getItemCount(): Int {
        return homeArrayList.size
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        holder.setIsRecyclable(false)

        rowBinding.image.setImageResource(homeArrayList[position].resource_id)

        holder.itemView.setOnClickListener {
            callback.invoke(homeArrayList[position])
        }

    }

    inner class ViewHolder(binding: AvatarItemBinding) :
        RecyclerView.ViewHolder(binding.root) {
    }

}