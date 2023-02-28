package com.st.trilobyte.ui.adapter

import androidx.recyclerview.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.st.trilobyte.R
import com.st.trilobyte.databinding.RadioAdapterCellBinding
import com.st.trilobyte.models.Flow

class RadioAdapter : RecyclerView.Adapter<RadioAdapter.ViewHolder>() {

    private var itemList = listOf<Flow>()

    private var selectedItemPosition = -1

//    override fun onCreateViewHolder(parent: ViewGroup, position: Int): ViewHolder {
//        val view = LayoutInflater.from(parent.context).inflate(R.layout.radio_adapter_cell, parent, false)
//        return ViewHolder(view)
//    }
    override fun onCreateViewHolder(parent: ViewGroup, position: Int): ViewHolder {
        val binding = RadioAdapterCellBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ViewHolder(binding)
    }

    override fun getItemCount(): Int {
        return itemList.size
    }

//    override fun onBindViewHolder(viewHolder: ViewHolder, position: Int) {
//
//        val item = itemList[position]
//
//        viewHolder.view.radio_button.setOnCheckedChangeListener(null)
//
//        viewHolder.view.item_description.text = item.description
//        viewHolder.view.radio_button.isChecked = position == selectedItemPosition
//
//        viewHolder.view.radio_button.setOnCheckedChangeListener { _, _ ->
//            selectedItemPosition = viewHolder.adapterPosition
//            notifyDataSetChanged()
//        }
//    }
    override fun onBindViewHolder(viewHolder: ViewHolder, position: Int) {

        val item = itemList[position]

        viewHolder.binding.radioButton.setOnCheckedChangeListener(null)

        viewHolder.binding.itemDescription.text = item.description
        viewHolder.binding.radioButton.isChecked = position == selectedItemPosition

        viewHolder.binding.radioButton.setOnCheckedChangeListener { _, _ ->
            selectedItemPosition = viewHolder.adapterPosition
            notifyDataSetChanged()
        }
    }

    fun setItems(items: List<Flow>) {
        itemList = items

        if (!items.isEmpty()) {
            selectedItemPosition = 0
            notifyDataSetChanged()
        } else {
            selectedItemPosition = -1
        }
    }

    fun getSelectedItem(): Flow? {
        if (selectedItemPosition < 0)
            return null

        return itemList[selectedItemPosition]
    }

    fun setSelectedItem(flow: Flow) {
        selectedItemPosition = itemList.indexOf(flow)
        notifyDataSetChanged()
    }

    data class ViewHolder(val binding: RadioAdapterCellBinding) : RecyclerView.ViewHolder(binding.root)
}