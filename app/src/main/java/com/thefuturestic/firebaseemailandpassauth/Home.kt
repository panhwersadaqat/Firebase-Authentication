package com.thefuturestic.firebaseemailandpassauth

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.widget.Button
import com.google.firebase.auth.FirebaseAuth

class Home : AppCompatActivity() {
    private lateinit var logout: Button
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_home)
        logout = findViewById<View>(R.id.logout) as Button

        logout!!.setOnClickListener(View.OnClickListener {
            val user = FirebaseAuth.getInstance().currentUser
            if (user != null) {
                FirebaseAuth.getInstance().signOut()
                val newIntent = Intent(this, SignInActivity::class.java)
                newIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
                newIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                finish()
                startActivity(newIntent)

            }//end of if
        })//end of click
    }
}//end of class
