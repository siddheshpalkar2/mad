package com.siddhesh.mad

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import kotlinx.android.synthetic.main.activity_login.*

class LoginActivity: AppCompatActivity() {
    private val userName1 = "siddhesh"
    private val password1 = "mad@123"
    private val userName2 = "flytbase"
    private val password2 = "flytbase@123"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)
        login_btn.setOnClickListener {
            if (username.text.toString() == userName1 && pass.text.toString() == password1) {
                Constants.USERNAME = "siddhesh"
                startActivity(Intent(this@LoginActivity, MainActivity::class.java))
                finish()
            } else if (username.text.toString() == userName2 && pass.text.toString() == password2) {
                Constants.USERNAME = "flytbase"
                startActivity(Intent(this@LoginActivity, MainActivity::class.java))
                finish()
            } else Toast.makeText(this@LoginActivity, "Invalid credentials.", Toast.LENGTH_SHORT)
                .show()
        }
    }
}