package com.example.assigntodo.ui.employee

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AlertDialog
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.assigntodo.adapters.EmployeeWorkAdapter
import com.example.assigntodo.R
import com.example.assigntodo.api.ApiUtilities
import com.example.assigntodo.models.Works
import com.example.assigntodo.auth.SignInActivity
import com.example.assigntodo.databinding.ActivityEmployeeMainBinding
import com.example.assigntodo.models.Notification
import com.example.assigntodo.models.NotificationData
import com.example.assigntodo.models.Users
import com.example.assigntodo.utils.Utils
import com.google.android.material.button.MaterialButton
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class EmployeeMainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityEmployeeMainBinding
    private lateinit var employeeWorkAdapter: EmployeeWorkAdapter
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityEmployeeMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        binding.apply {
            tbEmployee.setOnMenuItemClickListener {
                when (it.itemId) {
                    R.id.logOut -> {
                        showLogOutDialog()
                        true
                    }

                    else -> false
                }
            }

        }

        prepareRvForEmployeeWorksAdapter()
        showEmployeeWorks()

    }

    private fun prepareRvForEmployeeWorksAdapter() {
        employeeWorkAdapter =
            EmployeeWorkAdapter(this,::onProgressButtonClicked, ::onCompletedButtonClicked)
        binding.rvEmployeeWork.apply {
            layoutManager =
                LinearLayoutManager(this@EmployeeMainActivity, LinearLayoutManager.VERTICAL, false)
            adapter = employeeWorkAdapter
        }
    }

    private fun showEmployeeWorks() {
        Utils.showDialog(this)
        val empId = FirebaseAuth.getInstance().currentUser?.uid
        val workRef = FirebaseDatabase.getInstance()
        workRef.getReference("Works").addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                for (workRooms in snapshot.children) {
                    if (workRooms.key?.contains(empId!!) == true) {
                        val empWorkRef = workRef.getReference("Works").child(workRooms.key!!)
                        empWorkRef.addValueEventListener(object : ValueEventListener {
                            override fun onDataChange(snapshot: DataSnapshot) {
                                val workList = ArrayList<Works>()
                                for (works in snapshot.children) {
                                    val work = works.getValue(Works::class.java)
                                    workList.add(work!!)
                                }
                                employeeWorkAdapter.differ.submitList(workList)
                                binding.tvText.visibility = if (workList.isEmpty()) {
                                    View.VISIBLE
                                } else {
                                    View.GONE
                                }
                            }

                            override fun onCancelled(error: DatabaseError) {
                                // Handle onCancelled error
                            }
                        })
                    }
                }
                // Move the hideDialog() call here, outside of the inner ValueEventListener
                Utils.hideDialog()
            }

            override fun onCancelled(error: DatabaseError) {
                // Handle onCancelled error
            }
        })
    }


    private fun showLogOutDialog() {
        val builder = AlertDialog.Builder(this)
        val alertDialog = builder.create()
        builder.setTitle("Log Out")
            .setMessage("Are you sure you want to logout")
            .setPositiveButton("Yes") { _, _ ->
                FirebaseAuth.getInstance().signOut()
                startActivity(Intent(this, SignInActivity::class.java))
                this.finish()
            }
            .setNegativeButton("NO") { _, _ ->
                alertDialog.dismiss()
            }
            .show()
            .setCancelable(false)
    }

    private fun onProgressButtonClicked(works: Works, progressButton: MaterialButton) {
        if (progressButton.text != "In Progress") {
            val builder = AlertDialog.Builder(this)
            val alertDialog = builder.create()
            builder.setTitle("Starting Work")
                .setMessage("Are you starting this work?")
                .setPositiveButton("Yes") { _, _ ->
                    updateStatus(works, "2")
                    progressButton.apply {
                        text = "In Progress"
                        setTextColor(
                            ContextCompat.getColor(
                                this@EmployeeMainActivity,
                                R.color.Light5
                            )
                        )
                    }
                }
                .setNegativeButton("NO") { _, _ ->
                    alertDialog.dismiss()
                }
                .show()
                .setCancelable(false)
        }else Utils.showToast(this  , "Work is in progress or completed")
    }

    private fun updateStatus(works: Works, status: String) {
        val empId = FirebaseAuth.getInstance().currentUser?.uid
        val workRef = FirebaseDatabase.getInstance()
        workRef.getReference("Works").addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                for (workRooms in snapshot.children) {
                    if (workRooms.key?.contains(empId!!) == true) {
                        val empWorkRef = workRef.getReference("Works").child(workRooms.key!!)
                        empWorkRef.addListenerForSingleValueEvent(object : ValueEventListener {
                            override fun onDataChange(snapshot: DataSnapshot) {
                                for (allWorks in snapshot.children) {
                                    val work = allWorks.getValue(Works::class.java)
                                   if(works.workID == work?.workID){
                                       empWorkRef.child(allWorks.key!!).child("workStatus").setValue(status)
                                   }
                                }

                            }

                            override fun onCancelled(error: DatabaseError) {
                                Utils.hideDialog()
                                TODO("Not yet implemented")
                            }

                        })
                    }
                }
            }

            override fun onCancelled(error: DatabaseError) {
                TODO("Not yet implemented")
            }

        })
    }

    private fun onCompletedButtonClicked(works: Works, completedButton: MaterialButton) {
        if(completedButton.text != "Work Completed"){
           val builder = AlertDialog.Builder(this)
           val alertDialog = builder.create()
           builder.setTitle("Completed Work")
               .setMessage("Are you sure you completed this work?")
               .setPositiveButton("Yes") { _, _ ->
                   updateStatus(works, "3")
                   sendNotification(works.bossId , works.workTitle.toString())
                   completedButton.apply {
                       text = "Work Completed"
                       setTextColor(
                           ContextCompat.getColor(
                               this@EmployeeMainActivity,
                               R.color.Light5
                           )
                       )
                   }

               }
               .setNegativeButton("NO") { _, _ ->
                   alertDialog.dismiss()
               }
               .show()
               .setCancelable(false)
    }
        else Utils.showToast(this  , "Work is in progress or completed")
    }

    private fun sendNotification(bossID: String?, workTitle: String) {
        val getToken = FirebaseDatabase.getInstance().getReference("Users").child(bossID!!).get()
        getToken.addOnSuccessListener {
            val empDetail = it.getValue(Users::class.java)
            val empToken = empDetail?.userToken
            val notification = Notification(empToken , NotificationData("WORK COMPLETED", workTitle))
            ApiUtilities.api.sendNotification(notification).enqueue(object : Callback<Notification>{
                override fun onResponse(
                    call: Call<Notification>,
                    response: Response<Notification>
                ) {
                    if(response.isSuccessful){
                        Utils.showToast(this@EmployeeMainActivity , "Notification Sent")
                    }
                }

                override fun onFailure(call: Call<Notification>, t: Throwable) {
                    TODO("Not yet implemented")
                }

            })
        }
    }


}