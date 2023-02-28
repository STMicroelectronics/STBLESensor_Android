package com.st.BlueSTSDK.gui

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.st.BlueSTSDK.gui.databinding.DemoElementBinding

class ListOfDemoAdapter(private val listener: OnItemClickListener) :
    RecyclerView.Adapter<ListOfDemoAdapter.ListOfDemoViewHolder>() {

    private var listOfDemo = listOf<DemoClass>()


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ListOfDemoViewHolder {
        val binding = DemoElementBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ListOfDemoViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ListOfDemoViewHolder, position: Int) {
        val currentItem = listOfDemo[position]

        holder.nameTextView.text = currentItem.name
        holder.iconView.setImageResource(currentItem.icon)
    }

    override fun getItemCount() = listOfDemo.size

    fun updateDemoList(newList: List<DemoClass>) {
        //We need to remove the replications due to Demo Classification
        listOfDemo =  newList.toHashSet().toList()
        notifyDataSetChanged()
    }

    inner class ListOfDemoViewHolder(binding: DemoElementBinding) : RecyclerView.ViewHolder(binding.root),
        View.OnClickListener {
        val nameTextView: TextView = binding.demoName
        val iconView: ImageView = binding.demoIcon


        init {
            itemView.setOnClickListener(this)
        }

        override fun onClick(v: View?) {
            //val position = adapterPosition //LP
            val position = bindingAdapterPosition
            if (position != RecyclerView.NO_POSITION) {
                //we need move to the right position inside the Viewpager
                listener.onItemClick(listOfDemo[position].number)
            }
        }
    }

    interface OnItemClickListener {
        fun onItemClick(position: Int)
    }
}