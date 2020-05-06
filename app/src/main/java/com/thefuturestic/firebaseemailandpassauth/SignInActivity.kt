package com.thefuturestic.firebaseemailandpassauth

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.EditText
import com.google.firebase.auth.FirebaseAuth
import com.wang.avi.AVLoadingIndicatorView
import rx.subscriptions.CompositeSubscription
import java.util.regex.Matcher

import android.text.TextUtils
import android.util.Log
import android.widget.*

import com.google.android.gms.tasks.OnCompleteListener

import com.jakewharton.rxbinding.widget.RxTextView
import rx.Observable
import rx.Observer
import rx.Subscriber
import rx.android.schedulers.AndroidSchedulers
import java.util.concurrent.TimeUnit
import com.google.firebase.auth.AuthResult
import org.w3c.dom.Text

class SignInActivity : AppCompatActivity() {
    private var  avi : AVLoadingIndicatorView? = null
    private var signInButton: Button? = null
    private var emailEditText: EditText? = null
    private var passwordEditText: EditText? = null
    private var mAuth: FirebaseAuth? = null
    //region Member Variables
    private val pattern = android.util.Patterns.EMAIL_ADDRESS
    private var matcher: Matcher? = null

    protected var compositeSubscription = CompositeSubscription()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_sign_in)
        signInButton = findViewById<View>(R.id.signInButton) as Button
        emailEditText = findViewById<View>(R.id.email_et) as EditText
        passwordEditText = findViewById<View>(R.id.password_et) as EditText
        avi = findViewById<View>(R.id.avi) as AVLoadingIndicatorView
        avi!!.hide()
        mAuth = FirebaseAuth.getInstance()
        //user check method
        userChecking()


        signInButton!!.setOnClickListener(View.OnClickListener {
            signInWithEmail()
        })//end of login button


        //defining val for obersevers
        val emailChangeObservable = RxTextView.textChanges(emailEditText!!)
        val passwordChangeObservable = RxTextView.textChanges(passwordEditText!!)

        // Checks for validity of the email input field

        val emailSubscription = emailChangeObservable
            .doOnNext { //hideEmailError()
            }
            .debounce(800, TimeUnit.MILLISECONDS)
            .filter { charSequence -> !TextUtils.isEmpty(charSequence) }
            .observeOn(AndroidSchedulers.mainThread()) // UI Thread
            .subscribe(object : Subscriber<CharSequence>() {
                override fun onCompleted() {

                }

                override fun onError(e: Throwable) {
                    e.printStackTrace()
                }

                override fun onNext(charSequence: CharSequence) {
                    val isEmailValid = validateEmail(charSequence.toString())
                    if (!isEmailValid) {
                        emailEditText!!.setError("Please enter a valid email address")
                    } else {
                        emailEditText!!.setError(null)
                    }
                }
            })

        compositeSubscription.add(emailSubscription)


        // Checks for validity of the password input field

        val passwordSubscription = passwordChangeObservable
            .doOnNext { //hidePasswordError()
            }
            .debounce(400, TimeUnit.MILLISECONDS)
            .filter { charSequence -> !TextUtils.isEmpty(charSequence) }
            .observeOn(AndroidSchedulers.mainThread()) // UI Thread
            .subscribe(object : Subscriber<CharSequence>() {
                override fun onCompleted() {

                }

                override fun onError(e: Throwable) {
                    e.printStackTrace()
                }

                override fun onNext(charSequence: CharSequence) {
                    val isPasswordValid = validatePassword(charSequence.toString())
                    if (!isPasswordValid) {
                        //showPasswordError()
                    } else {
                        //hidePasswordError()
                    }
                }
            })

        compositeSubscription.add(passwordSubscription)


        // Checks for validity of both input fields for button chacnging

        val signInFieldsSubscription = Observable.combineLatest(emailChangeObservable, passwordChangeObservable) { email, password ->
            val isEmailValid = validateEmail(email.toString())
            val isPasswordValid = validatePassword(password.toString())

            isEmailValid && isPasswordValid
        }.observeOn(AndroidSchedulers.mainThread()) // UI Thread
            .subscribe(object : Observer<Boolean> {
                override fun onCompleted() {

                }

                override fun onError(e: Throwable) {
                    e.printStackTrace()
                }

                override fun onNext(validFields: Boolean?) {
                    if (validFields!!) {
                        enableSignIn()
                    } else {
                        disableSignIn()
                    }
                }
            })

        compositeSubscription.add(signInFieldsSubscription)

    }//end of oncreate
    private fun validateEmail(email: String): Boolean {
        if (TextUtils.isEmpty(email))
            return false

        matcher = pattern.matcher(email)

        return matcher!!.matches()
    }

    private fun validatePassword(password: String): Boolean {
        return password.length > 0
    }
    private fun enableSignIn() {
        signInButton!!.isEnabled = true

    }

    private fun disableSignIn() {
        signInButton!!.isEnabled = false
    }

    private fun userChecking(){
        val user = FirebaseAuth.getInstance().currentUser
        //val facebookAccessToken = AccessToken.getCurrentAccessToken()
        if (user != null && user.isEmailVerified) {
            // User is signed in
            val intent = Intent(this, Home::class.java)
            startActivity(intent)
            finish()
        }
    }

    fun signInWithEmail() {
        avi!!.show()
        //var progressDialog = ProgressDialog.show(this, "please wait...", "processing..", true)
        val emailAdress = emailEditText!!.getText().toString()
        val password = passwordEditText!!.getText().toString()
        mAuth!!.signInWithEmailAndPassword(emailAdress, password).addOnCompleteListener(OnCompleteListener<AuthResult> { task ->
            // progressDialog!!.dismiss()
            avi!!.hide()
            if (task.isSuccessful) {
                    val i = Intent(this,Home::class.java)
                    startActivity(i)
                    finish()
                    emailEditText!!.setText("")
                    passwordEditText!!.setText("")
            } else {
                try {
                    throw task.getException()!!;
                } catch (e: Exception) {
                    Log.e("Error", task.exception!!.toString())
                    Toast.makeText(this, "Email or password is incorrect", Toast.LENGTH_SHORT).show()
                }
            }
        })
    }//end of email login

}//end of class
