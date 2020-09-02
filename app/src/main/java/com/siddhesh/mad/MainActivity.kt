package com.siddhesh.mad

import android.os.Bundle
import android.util.Log
import android.view.KeyEvent
import android.view.View
import android.view.inputmethod.InputMethodManager
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.text.isDigitsOnly
import androidx.core.view.isVisible
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.firebase.database.ChildEventListener
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.ktx.database
import com.google.firebase.database.ktx.getValue
import com.google.firebase.ktx.Firebase
import kotlinx.android.synthetic.main.activity_main.*
import java.util.*
import kotlin.collections.ArrayList

class MainActivity : AppCompatActivity() {
    private var historyList = ArrayList<History>()
    private var historyAdapter: HistoryAdapter? = null
    private var priority = mapOf("*" to 4, "+" to 3, "/" to 2, "-" to 1)
    private var childEventListener = object : ChildEventListener {
        override fun onChildAdded(snapshot: DataSnapshot, previousChildName: String?) {
            val history = snapshot.getValue<History>()
            historyList.add(history!!)
            if (historyAdapter == null) {
                historyAdapter = HistoryAdapter(historyList)
                history_list.adapter = historyAdapter
            }
            Log.d("FB_VALUE", history.question)
        }

        override fun onChildChanged(snapshot: DataSnapshot, previousChildName: String?) {

        }

        override fun onChildRemoved(snapshot: DataSnapshot) {

        }

        override fun onChildMoved(snapshot: DataSnapshot, previousChildName: String?) {

        }

        override fun onCancelled(error: DatabaseError) {

        }

    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        history_list.layoutManager = LinearLayoutManager(this).apply {
            reverseLayout = true
            stackFromEnd = true
        }
        history_btn.setOnClickListener {
            history_list.visibility = if (history_list.isVisible) View.GONE else View.VISIBLE
        }
        calculations.setOnKeyListener { v, keyCode, event ->
            if (event.action == KeyEvent.ACTION_DOWN) {
                when (keyCode) {
                    KeyEvent.KEYCODE_DPAD_CENTER, KeyEvent.KEYCODE_ENTER -> {
                        val expression = calculations.text.toString()
                        if (expression.isNotEmpty()) {
                            if(expression[0].isDigit() && expression[expression.length-1].isDigit()) {
                                extractCalculations()
                                calculations.setText("")
                            } else Toast.makeText(
                                this@MainActivity,
                                "Invalid expression",
                                Toast.LENGTH_SHORT
                            ).show()
                        } else Toast.makeText(
                            this@MainActivity,
                            "Enter calculations",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                }
            }
            false
        }
        getHistory()
    }

    private fun extractCalculations() {
        var question = calculations.text.toString()
        question = question.replace("*", ",*,")
            .replace("+", ",+,")
            .replace("/", ",/,")
            .replace("-", ",-,")

        getAnswer(
            getPostFix(question.split(",").toTypedArray())
                .split(" ").toTypedArray()
        )
    }

    private fun uploadHistory(history: History) {
        val myRef = Firebase.database.getReference(Constants.USERNAME)
        myRef.removeEventListener(childEventListener)
        myRef.push().setValue(history)
    }

    private fun getHistory() {
        val myRef = Firebase.database.getReference(Constants.USERNAME).limitToLast(10)
        historyList.clear()
        myRef.addChildEventListener(childEventListener)
    }

    private fun getPostFix(tokens: Array<String>): String {
        var postFixStr = ""
        val operators = Stack<String>()
        tokens.forEach {
            if (it.isDigitsOnly()) postFixStr += "$it "
            else {
                if (operators.isNotEmpty()) {
                    if (priority.getValue(operators.peek()) < priority.getValue(it)) operators.push(
                        it
                    )
                    else {
                        postFixStr += "${operators.pop()} "
                        operators.push(it)
                    }
                } else operators.push(it)
            }
        }
        if (operators.isNotEmpty()) {
            while (!operators.isEmpty()) postFixStr += "${operators.pop()} "
        }
        return postFixStr.trim()
    }

    private fun getAnswer(postFix: Array<String>) {
        val stack = Stack<Double>()
        postFix.forEach {
            if (it.isDigitsOnly()) stack.push(it.toDouble())
            else {
                if (stack.size > 1) {
                    val right = stack.pop()
                    val left = stack.pop()
                    when (it) {
                        "*" -> stack.push(left * right)
                        "+" -> stack.push(left + right)
                        "/" -> {
                            try {
                                val value = left.toDouble() / right.toDouble()
                                stack.push(value)
                            } catch (e: Exception){
                                e.printStackTrace()
                                Toast.makeText(this@MainActivity, "Divide by zero not allowed.", Toast.LENGTH_SHORT).show()
                                return
                            }
                        }
                        "-" -> stack.push(left - right)
                    }
                    Log.d("STACK_VAL", stack.joinToString())
                }
            }
        }
        result.text = "Answer: ${stack.peek()}"
        val history = History(calculations.text.toString(), stack.peek().toString())
        historyList.add(history)
        if (historyAdapter == null) {
            historyAdapter = HistoryAdapter(historyList)
            history_list.adapter = historyAdapter
        } else {
            historyAdapter?.notifyDataSetChanged()
        }
        if(historyList.size > 10){
            historyList = ArrayList(historyList.takeLast(10))
            historyAdapter?.setHistoryList(historyList)
            historyAdapter?.notifyDataSetChanged()
        }
        history_list.smoothScrollToPosition(historyList.size-1)
        hideKeyboard()
        uploadHistory(history)
    }

    private fun hideKeyboard(){
        val imm: InputMethodManager = getSystemService(INPUT_METHOD_SERVICE) as InputMethodManager
        val view: View = currentFocus!!
        imm.hideSoftInputFromWindow(view.windowToken, 0)
    }
}