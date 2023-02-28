package com.st.blesensor.cloud.AzureIoTCentralPnP.CloudAppConfiguration

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.st.BlueSTSDK.Node
import com.st.blesensor.cloud.AzureIoTCentralPnP.CloudAppConfigured
import com.st.blesensor.cloud.databinding.CloudConfigAzureIotCentralPnpCloudAppElementBinding
import java.text.SimpleDateFormat

class AzureIoTCentralPnPCloudAdapter(private val listener: OnItemClickListener) :
    RecyclerView.Adapter<AzureIoTCentralPnPCloudAdapter.CloudApplicationViewHolder>() {

    private var cloudApp = listOf<CloudAppConfigured>()

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): CloudApplicationViewHolder {
        val binding = CloudConfigAzureIotCentralPnpCloudAppElementBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return CloudApplicationViewHolder(binding)
    }

    override fun onBindViewHolder(
        holder: CloudApplicationViewHolder,
        position: Int
    ) {
        val currentItem = cloudApp[position]

        if (currentItem.configurationDone) {
            //if the CloudApp is configured, we show the App Name and the URL
            holder.nameTextView.text = currentItem.cloudApp.url
                .toString()
                .removePrefix("https://").removeSuffix(".azureiotcentral.com")

            holder.descTextView.text = currentItem.cloudApp.url

            holder.descTextView.setTextColor(holder.nameTextView.textColors)
            holder.descTextView.visibility = View.VISIBLE
            holder.descTextView.isSelected = true
        } else {
            //if the CloudApp is not configured, we show the App Name and the Description taken from Fw Database
            holder.nameTextView.text = currentItem.cloudApp.name
            holder.descTextView.text = currentItem.cloudApp.description

            holder.descTextView.setTextColor(holder.nameTextView.textColors)

            if (currentItem.cloudApp.description != null) {
                holder.descTextView.visibility = View.VISIBLE
                holder.descTextView.isSelected = true
            } else {
                holder.descTextView.visibility = View.INVISIBLE
                holder.descTextView.isSelected = false
            }
        }

        if(currentItem.apiToken!=null) {
            holder.tokenExpirationTextView.visibility = View.VISIBLE
            val dateFormat = SimpleDateFormat("dd/MM/yyyy hh:mm:ss").format(currentItem.apiToken!!.expire)
            val string = "Token Expiry $dateFormat"
            holder.tokenExpirationTextView.text = string
            holder.tokenExpirationTextView.isSelected=true
        } else {
            holder.tokenExpirationTextView.visibility = View.GONE
            holder.tokenExpirationTextView.isSelected=false
        }
    }

    override fun getItemCount() = cloudApp.size

    fun updateCloudAppList(newList: List<CloudAppConfigured>) {
        cloudApp = newList

        // for updating the recycler view
        notifyDataSetChanged()
    }

    inner class CloudApplicationViewHolder(binding: CloudConfigAzureIotCentralPnpCloudAppElementBinding) :
        RecyclerView.ViewHolder(binding.root),
        View.OnClickListener {
        val nameTextView: TextView = binding.cloudConfigAzureIotCentralPnpElementName
        val descTextView: TextView = binding.cloudConfigAzureIotCentralPnpElementDescription
        val tokenExpirationTextView: TextView = binding.cloudConfigAzureIotCentralPnpElementExpiration

        init {
            itemView.setOnClickListener(this)
        }

        override fun onClick(v: View?) {
            //val position = adapterPosition //LP
            val position = bindingAdapterPosition
            if (position != RecyclerView.NO_POSITION) {
                listener.onItemClick(position)
            }
        }
    }

    interface OnItemClickListener {
        fun onItemClick(position: Int)
    }
}
