package com.st.blesensor.cloud.AzureIoTCentralPnP.CloudAppConfiguration

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.st.BlueSTSDK.Node
import com.st.blesensor.cloud.AzureIoTCentralPnP.DeviceConfiguration.AzureCloudDevice
import com.st.blesensor.cloud.databinding.CloudConfigAzureIotCentralPnpDevElementBinding
import android.R
import android.content.Context
import android.graphics.Typeface


class AzureIoTPnPDeviceAdapter (private val listener: OnItemClickListener) :
    RecyclerView.Adapter<AzureIoTPnPDeviceAdapter.CloudDeviceViewHolder>() {

    private var cloudDevice = listOf<AzureCloudDevice>()

    private var mNode: Node?=null
    private var mMcuId: String?=null
    private var mDispCloudUri: Boolean=false

    private var selectedPosition = -1

    private lateinit  var context: Context

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): CloudDeviceViewHolder {
        val binding = CloudConfigAzureIotCentralPnpDevElementBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        context = parent.context
        return CloudDeviceViewHolder(binding)
    }

    override fun onBindViewHolder(
        holder: CloudDeviceViewHolder,
        position: Int
    ) {
        val currentItem = cloudDevice[position]

        holder.nameNameTextView.text = currentItem.displayName
        holder.deviceIdTextView.text = currentItem.id

        //We enable each Cloud APP only for the owner node
        if (mMcuId != null) {
            if (mMcuId == currentItem.id) {
                holder.itemView.isEnabled = true
                holder.itemView.alpha = 1.0f
            } else {
                holder.itemView.isEnabled = false
                holder.itemView.alpha = 0.3f
            }
        } else if(mNode != null) {
            if (mNode!!.tag == currentItem.macAdd) {
                holder.itemView.isEnabled = true
                holder.itemView.alpha = 1.0f
            } else {
                holder.itemView.isEnabled = false
                holder.itemView.alpha = 0.3f
            }
        } else {
            holder.itemView.isEnabled = true
            holder.itemView.alpha = 1.0f
        }

        if(position==selectedPosition) {
            //holder.nameNameTextView.setTextColor(context.resources.getColor(com.st.BlueSTSDK.gui.R.color.colorPrimary))
            holder.deviceIdTextView.apply {
                setTypeface(typeface, Typeface.BOLD)
            }
        } else {
            //holder.nameNameTextView.setTextColor(context.resources.getColor(com.st.BlueSTSDK.gui.R.color.labelPlotContrast))
            holder.deviceIdTextView.apply {
                setTypeface(typeface, Typeface.ITALIC)
            }
        }

        if(mDispCloudUri) {
            if(currentItem.connectedCloudAppUrl!=null) {
                holder.cloudAppTextView.visibility = View.VISIBLE
                holder.cloudAppTextView.text = currentItem.connectedCloudAppUrl

                if(position==selectedPosition) {
                    holder.cloudAppTextView.apply {
                        setTypeface(typeface, Typeface.BOLD)
                    }
                } else {
                    holder.cloudAppTextView.apply {
                        setTypeface(typeface, Typeface.ITALIC)
                    }
                }

            } else {
                holder.cloudAppTextView.visibility=View.GONE
            }
        } else {
            holder.cloudAppTextView.visibility=View.GONE
        }
    }

    override fun getItemCount() = cloudDevice.size

    fun updateAzureDeviceList(newList: List<AzureCloudDevice>,node: Node?=null,dispCloudUri: Boolean=false,mcuId: String?=null) {
        cloudDevice = newList
        mNode = node
        mMcuId = mcuId
        mDispCloudUri= dispCloudUri
        // for updating the recycler view
        notifyDataSetChanged()

    }

    inner class CloudDeviceViewHolder(binding: CloudConfigAzureIotCentralPnpDevElementBinding) :
        RecyclerView.ViewHolder(binding.root),
        View.OnClickListener {
        val nameNameTextView: TextView = binding.cloudConfigAzureIotCentralPnpDeviceName
        val deviceIdTextView: TextView = binding.cloudConfigAzureIotCentralPnpDeviceId
        val cloudAppTextView: TextView = binding.cloudConfigAzureIotCentralPnpDeviceUri

        init {
            itemView.setOnClickListener(this)
        }

        override fun onClick(v: View?) {
            //val position = adapterPosition //LP
            val position = bindingAdapterPosition
            if (position != RecyclerView.NO_POSITION) {
                listener.onItemClick(position)
                selectedPosition = position;
                notifyDataSetChanged();
            }
        }
    }

    interface OnItemClickListener {
        fun onItemClick(position: Int)
    }
}