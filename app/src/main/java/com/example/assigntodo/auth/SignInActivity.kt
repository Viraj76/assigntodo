package com.example.assigntodo.auth

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.LayoutInflater
import androidx.appcompat.app.AlertDialog

import androidx.lifecycle.lifecycleScope
import com.example.assigntodo.ui.boss.BossMainActivity
import com.example.assigntodo.ui.employee.EmployeeMainActivity
import com.example.assigntodo.models.Users
import com.example.assigntodo.databinding.ActivitySignInBinding
import com.example.assigntodo.databinding.ForgotPasswordDialogBinding
import com.example.assigntodo.utils.Utils
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

class SignInActivity : AppCompatActivity() {
    private lateinit var binding : ActivitySignInBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySignInBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.btnLogin.setOnClickListener {
            val email = binding.etEmail.text.toString()
            val password = binding.etPassword.text.toString()
            loginUser(email, password)
        }
        binding.tvSignUp.setOnClickListener {
            startActivity(Intent(this , SignUpActivity::class.java))
            finish()
        }
        binding.tvForgotPassword.setOnClickListener { showForgotPasswordDialog() }

    }

    private fun showForgotPasswordDialog() {
        val dialog = ForgotPasswordDialogBinding.inflate(LayoutInflater.from(this))
        val alertDialog = AlertDialog.Builder(this)
            .setView(dialog.root)
            .create()
        alertDialog.show()
        dialog.etEmail.requestFocus()
        dialog.tvBackToLogin.setOnClickListener {
            startActivity(Intent(this , SignInActivity::class.java))
            finish()
        }
        dialog.btnForgotPassword.setOnClickListener {
            val email = dialog.etEmail.text.toString()
            alertDialog.dismiss()
            resetPassword(email)
        }
    }

    private fun resetPassword(email: String) {
        lifecycleScope.launch {
            try {
                FirebaseAuth.getInstance().sendPasswordResetEmail(email).await()
                Utils.showToast(this@SignInActivity , "Please check your email and reset the password")
            }
            catch (e : Exception){
                Utils.showToast(this@SignInActivity, e.message.toString())
            }
        }
    }


    private fun loginUser(email: String, password: String) {
        Utils.showDialog(this)
        val firebaseAuth = FirebaseAuth.getInstance()
        lifecycleScope.launch {
            try {
                val authResult = firebaseAuth.signInWithEmailAndPassword(email, password).await()
                val currentUser = authResult.user?.uid
                val verifyEmail = firebaseAuth.currentUser?.isEmailVerified
                if(verifyEmail == true){
                    if(currentUser != null){
                        // Users , uid
                        FirebaseDatabase.getInstance().getReference("Users").child(currentUser).addListenerForSingleValueEvent(object  : ValueEventListener{
                            override fun onDataChange(snapshot: DataSnapshot) {
                                val currentUserData = snapshot.getValue(Users::class.java)
                                when(currentUserData?.userType){
                                    "Boss" ->{
                                        Utils.hideDialog()
                                        startActivity(Intent(this@SignInActivity , BossMainActivity::class.java))
                                        finish()
                                    }
                                    "Employee" ->{
                                        Utils.hideDialog()
                                        startActivity(Intent(this@SignInActivity , EmployeeMainActivity::class.java))
                                        finish()
                                    }
                                    else -> {
                                        startActivity(Intent(this@SignInActivity , EmployeeMainActivity::class.java))
                                        finish()
                                    }
                                }
                            }

                            override fun onCancelled(error: DatabaseError) {
                                Utils.hideDialog()
                                Utils.showToast(this@SignInActivity , error.message)
                            }

                        })
                    }
                    else{
                        Utils.hideDialog()
                        Utils.showToast(this@SignInActivity , "User Not Found Please Sign UP first")
                    }
                }
                else{
                    Utils.hideDialog()
                    Utils.showToast(this@SignInActivity , "Email is not verified")
                }
            }
            catch (e : Exception){
                Utils.hideDialog()
                Utils.showToast(this@SignInActivity , e.message!!)
            }
        }
    }


}
