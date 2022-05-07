package com.redeyesncode.amazonchimekotlin.adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.redeyesncode.amazonchimekotlin.R
import com.redeyesncode.amazonchimekotlin.data.MetricData
import com.redeyesncode.amazonchimekotlin.databinding.RowMessageBinding
import com.redeyesncode.amazonchimekotlin.databinding.RowMetricBinding
import com.redeyesncode.amazonchimekotlin.utils.inflate

class MetricAdapter(
    private val metricsList: Collection<MetricData>, var context: Context
) :
    RecyclerView.Adapter<MetricHolder>() {

    private lateinit var binding :RowMetricBinding
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MetricHolder {

        binding = RowMetricBinding.inflate(LayoutInflater.from(context),parent,false)
        val inflatedView = parent.inflate(R.layout.row_metric, false)
        return MetricHolder(inflatedView,binding)
    }

    override fun getItemCount(): Int {
        return metricsList.size
    }

    override fun onBindViewHolder(holder: MetricHolder, position: Int) {
        holder.bindMetrics(metricsList.elementAt(position))
    }
}

class MetricHolder(inflatedView: View, binding: RowMetricBinding) :
    RecyclerView.ViewHolder(inflatedView) {

    private var view: View = inflatedView
    private var binding = RowMetricBinding.bind(inflatedView);

    fun bindMetrics(metric: MetricData) {
        val name = metric.metricName
        val value = metric.metricValue.toString()
        binding.metricName.text = name
        binding.metricValue.text = value
        binding.metricName.contentDescription = "$name metric"
        binding.metricValue.contentDescription = "$name value"
    }
}
