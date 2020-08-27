package com.siddhesh.mad

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import kotlinx.android.synthetic.main.calculation_row.view.*

class HistoryAdapter(): RecyclerView.Adapter<HistoryAdapter.HistoryViewHolder>() {
    private lateinit var historyList: List<History>

    constructor(historyList: ArrayList<History>): this(){
        this.historyList = historyList
    }

    fun setHistoryList(historyList: ArrayList<History>){
        this.historyList = historyList
    }

    override fun getItemId(position: Int): Long = position.toLong()

    override fun getItemViewType(position: Int): Int = position

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): HistoryViewHolder {
        val view =
            LayoutInflater.from(parent.context).inflate(R.layout.calculation_row, parent, false)
        return HistoryViewHolder(view)
    }

    override fun onBindViewHolder(holder: HistoryViewHolder, position: Int) {
        holder.question.text = "Calculations: ${historyList[position].question}"
        holder.answer.text = "Answer: ${historyList[position].answer}"
    }

    override fun getItemCount(): Int = historyList.size

    class HistoryViewHolder(itemView: View): RecyclerView.ViewHolder(itemView) {
        val question = itemView.question!!
        val answer = itemView.answer!!
    }
}