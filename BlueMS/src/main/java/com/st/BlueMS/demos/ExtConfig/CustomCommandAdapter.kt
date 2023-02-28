package com.st.BlueMS.demos.ExtConfig

import android.graphics.Color
import android.os.Handler
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.st.BlueMS.databinding.CustomCommandElementBinding
import com.st.BlueSTSDK.Features.ExtConfiguration.CustomCommand

class CustomCommandAdapter(private val listener: OnItemClickListener) :
        RecyclerView.Adapter<CustomCommandAdapter.CustomCommandViewHolder>() {

    private var customCommand = listOf<CustomCommand>()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CustomCommandViewHolder {
        val binding = CustomCommandElementBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return CustomCommandViewHolder(binding)
    }

    override fun onBindViewHolder(holder: CustomCommandViewHolder, position: Int) {
        val currentItem = customCommand[position]

        holder.nameTextView.text = currentItem.name

        //Because we disable the custom command element when they are pressed for providing a feedback to the user...
        //we re-enable the element when it created without waiting the timer
        holder.nameTextView.isEnabled=true


        // Make a control on the types supported
        if((currentItem.type!="String") && (currentItem.type!="Integer") &&
                (currentItem.type!="Void") && (currentItem.type!="Boolean") &&
                (currentItem.type!="EnumInteger") && (currentItem.type!="EnumString")) {
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
                holder.descTextView.isSelected=true
            } else {
                holder.descTextView.visibility=View.INVISIBLE
                holder.descTextView.isSelected=false
            }
        }
    }

    override fun getItemCount() = customCommand.size

    fun updateCustomCommandList(newList: List<CustomCommand>) {
        customCommand = newList
        // for updating the recycler view
        notifyDataSetChanged()
    }

    inner class CustomCommandViewHolder(binding: CustomCommandElementBinding) : RecyclerView.ViewHolder(binding.root),
            View.OnClickListener {
        val nameTextView: TextView = binding.customCommandName
        val descTextView: TextView = binding.customCommandDescription

        init {
            itemView.setOnClickListener(this)
        }

        override fun onClick(v: View?) {
            //val position = adapterPosition //LP
            val position = bindingAdapterPosition
            if (position != RecyclerView.NO_POSITION) {
                listener.onItemClick(position)
                //For Providing a feedback to the user that the element is selected
                timer(nameTextView)
            }
        }

        private fun timer(element: TextView) {
            element.isEnabled=false
            val handler = Handler()
            handler.postDelayed(Runnable { element.isEnabled=true}, 200)
        }
    }

    interface OnItemClickListener {
        fun onItemClick(position: Int)
    }
}