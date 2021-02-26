package com.st.BlueMS.demos.ExtConfig

import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.st.BlueMS.R
import com.st.BlueSTSDK.Features.ExtConfiguration.CustomCommand
import kotlinx.android.synthetic.main.custom_command_element.view.*

class CustomCommandAdapter(private val listener: OnItemClickListener) :
        RecyclerView.Adapter<CustomCommandAdapter.CustomCommandViewHolder>() {

    private var customCommand = listOf<CustomCommand>()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CustomCommandViewHolder {
        val itemView = LayoutInflater.from(parent.context).inflate(R.layout.custom_command_element,
                parent, false)

        return CustomCommandViewHolder(itemView)
    }

    override fun onBindViewHolder(holder: CustomCommandViewHolder, position: Int) {
        val currentItem = customCommand[position]

        holder.nameTextView.text = currentItem.name
        // Make a control on the types supported

        if((currentItem.type!="String") && (currentItem.type!="Integer") && (currentItem.type!="Void") && (currentItem.type!="Boolean")) {
            holder.itemView.isEnabled=false
            holder.descTextView.visibility=View.VISIBLE
            //if there is one error... switch to red color for description and add a warning
            holder.descTextView.setTextColor(Color.RED)
            val string = "Wrong Custom Command Type=${currentItem.type}"
            holder.descTextView.text = string
        } else {
            holder.itemView.isEnabled=true
            holder.descTextView.text = currentItem.description
            // Revert to default text color
            holder.descTextView.setTextColor(holder.nameTextView.textColors)
            if(currentItem.description!=null) {
                holder.descTextView.visibility=View.VISIBLE
            } else {
                holder.descTextView.visibility=View.INVISIBLE
            }
        }
    }

    override fun getItemCount() = customCommand.size

    fun updateCustomCommandList(newList: List<CustomCommand>) {
        customCommand = newList
        // for updating the recycler view
        notifyDataSetChanged()
    }

    inner class CustomCommandViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView),
            View.OnClickListener {
        val nameTextView: TextView = itemView.custom_command_name
        val descTextView: TextView = itemView.custom_command_description


        init {
            itemView.setOnClickListener(this)
        }

        override fun onClick(v: View?) {
            val position = adapterPosition
            if (position != RecyclerView.NO_POSITION) {
                listener.onItemClick(position)
            }
        }
    }

    interface OnItemClickListener {
        fun onItemClick(position: Int)
    }
}