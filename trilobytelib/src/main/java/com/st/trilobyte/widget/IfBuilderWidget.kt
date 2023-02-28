package com.st.trilobyte.widget

import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.View
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import com.st.trilobyte.R
import com.st.trilobyte.databinding.FlowBuilderWidgetBinding
import com.st.trilobyte.databinding.IfWidgetCellBinding
import com.st.trilobyte.models.Flow


class IfBuilderWidget @JvmOverloads constructor(
        context: Context,
        attrs: AttributeSet? = null,
        defStyleAttr: Int = 0) : LinearLayout(context, attrs, defStyleAttr) {

    private var listener: IfWidgetClickListener? = null

    private val flows: MutableList<Flow> = mutableListOf()

    private lateinit var binding : FlowBuilderWidgetBinding

    init {

        val title: String?
        val emptyMessage: String?

        val array = context.theme.obtainStyledAttributes(attrs, R.styleable.IfBuilderWidget, 0, 0)

        try {
            title = array.getString(R.styleable.IfBuilderWidget_if_widget_title)
            emptyMessage = array.getString(R.styleable.IfBuilderWidget_if_widget_empty_message)
        } finally {
            array.recycle()
        }

        val inflater = LayoutInflater.from(getContext())
        //val view = inflater.inflate(R.layout.flow_builder_widget, this, false)
        binding = FlowBuilderWidgetBinding.inflate(inflater)
        val view = binding.root

        binding.emptyTextview.text = emptyMessage
        binding.emptyTextview.setOnClickListener {
            listener?.onWidgetSelected()
        }

        binding.title.text = title ?: ""

        addView(view)
    }

    fun addExpression(exp: Flow?) {
        clear()

        exp?.let {
            addFlow(it, R.drawable.ic_accelerometer)
        }
    }

    fun addStatements(stats: List<Flow>?) {
        clear()

        stats?.let {
            stats.forEach { addFlow(it, R.drawable.ic_accelerometer) }
        }
    }

    private fun addFlow(flow: Flow, icon: Int) {

        flows.add(flow)

        binding.emptyTextview.visibility = View.GONE

        val row = LayoutInflater.from(context).inflate(R.layout.if_widget_cell, this, false)
        val item_image: ImageView = row.findViewById<ImageView>(R.id.item_image)
        item_image.setImageResource(icon)
        val item_textview: TextView = row.findViewById<TextView>(R.id.item_textview)
        item_textview.text = flow.description

        binding.itemContainer.addView(row)

        row.setOnClickListener {
            listener?.onWidgetSelected()
        }

        updateStatus()
    }

    fun clear() {
        binding.itemContainer.removeAllViews()
        binding.emptyTextview.visibility = View.VISIBLE
        updateStatus()
    }

    private fun updateStatus() {
        val isCompleted = !flows.isEmpty()
        val color = if (isCompleted) resources.getColor(R.color.widgetCompletedHeaderBgColor) else resources.getColor(R.color.widgetHeaderBgColor)
        binding.widgetHeader.setBackgroundColor(color)
        binding.completeIcon.visibility = if (isCompleted) View.VISIBLE else View.GONE
    }

    fun setWidgetListener(listener: IfWidgetClickListener) {
        this.listener = listener
    }

    interface IfWidgetClickListener {
        fun onWidgetSelected()
    }
}