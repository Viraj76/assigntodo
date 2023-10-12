package com.example.assigntodo.ui.boss
import android.app.DatePickerDialog
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import com.example.assigntodo.R
import com.example.assigntodo.api.ApiUtilities
import com.example.assigntodo.models.Works
import com.example.assigntodo.databinding.FragmentAssignWorkBinding
import com.example.assigntodo.models.Notification
import com.example.assigntodo.models.NotificationData
import com.example.assigntodo.models.Users
import com.example.assigntodo.utils.Utils
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale
class  AssignWorkFragment : Fragment() {
    val empData by navArgs<AssignWorkFragmentArgs>()
    private lateinit var binding : FragmentAssignWorkBinding
    private var priority = "1"
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        binding = FragmentAssignWorkBinding.inflate(layoutInflater)
        binding.tbAssignWork.apply {
            setNavigationIcon(R.drawable.baseline_arrow_back_24)
            setNavigationOnClickListener {
//                val action = AssignWorkFragmentDirections.actionAssignWorkFragmentToWorksFragment(empData.empData)
//                findNavController().navigate(action)
                activity?.onBackPressed()
            }
        }
        setPriority()
        selectDate()
        binding.btnDone.setOnClickListener {
            Utils.showDialog(requireContext())
            assignWork()
        }
        return binding.root
    }

    private fun assignWork() {
        val workTitle = binding.etTitle.text.toString()
        val workDescription = binding.etWorkDesc.text.toString()
        val workLastDate = binding.tvDate.text.toString()

        if(workTitle.isEmpty()){
            Utils.apply {
                hideDialog()
                showToast(requireContext() , "Please select work title")
            }
        }

        else if(workLastDate == "Last Date : "){
            Utils.apply {
                hideDialog()
                showToast(requireContext() , "Please choose last date")
            }
        }
        else {
            val empId = empData.empData.userId
            val bossId = FirebaseAuth.getInstance().currentUser?.uid
            val workRoom = bossId + empId
            val randomId =(1..20).map { (('A'..'Z') + ('a'..'z') + ('0'..'9')).random() }.joinToString("")
            val work = Works(
                bossId = bossId,
                workID = randomId,
                workTitle = workTitle,
                workDesc = workDescription,
                workPriority = priority,
                workLastDate = workLastDate,
                workStatus = "1"
            )
            FirebaseDatabase.getInstance().getReference("Works").child(workRoom).child(randomId)
                .setValue(work)
                .addOnSuccessListener {
                    sendNotification(empId  , workTitle)
                    Utils.hideDialog()
                    Utils.showToast(requireContext() , "Work has been assigned to ${empData.empData.userName}")
                    val action = AssignWorkFragmentDirections.actionAssignWorkFragmentToWorksFragment(empData.empData)
                    findNavController().navigate(action)
                }
                }
        }

    private fun sendNotification(empId: String?, workTitle: String) {
        val getToken = FirebaseDatabase.getInstance().getReference("Users").child(empId!!).get()
        getToken.addOnSuccessListener {
            val empDetail = it.getValue(Users::class.java)
            val empToken = empDetail?.userToken
            val notification = Notification(empToken , NotificationData("WORK ASSIGNED", workTitle))
            ApiUtilities.api.sendNotification(notification).enqueue(object : Callback<Notification>{
                override fun onResponse(
                    call: Call<Notification>,
                    response: Response<Notification>
                ) {
                    if(response.isSuccessful){
//                        Utils.showToast(requireContext() , "Notification Sent")
//                        Utils.showToast(requireContext() , "$workTitle   $empId")
                    }
                }

                override fun onFailure(call: Call<Notification>, t: Throwable) {
                    TODO("Not yet implemented")
                }

            })
        }
    }

    private fun selectDate() {
        val myCalender = Calendar.getInstance()
        val datePicker = DatePickerDialog.OnDateSetListener { view, year, month, dayOfMonth ->
            myCalender.apply {
                set(Calendar.YEAR, year)
                set(Calendar.MONTH, month)
                set(Calendar.DAY_OF_MONTH, dayOfMonth)
                updateLable(myCalender)
            }
        }
        binding.greenOval.setImageResource(R.drawable.done)
        binding.datePicker.setOnClickListener {
            DatePickerDialog(requireContext(),datePicker,myCalender.get(Calendar.YEAR),myCalender.get(Calendar.MONTH),myCalender.get(
                Calendar.DAY_OF_MONTH)).show()
        }

    }

    private fun setPriority() {
        binding.apply {
            greenOval.setOnClickListener {
                Utils.showToast(requireContext() , "Priority : Low")
                priority = "1"
                binding.greenOval.setImageResource(R.drawable.done)
                binding.yellowOval.setImageResource(0)
                binding.redOval.setImageResource(0)
            }
            yellowOval.setOnClickListener {
                Utils.showToast(requireContext() , "Priority : Medium")
                priority = "2"
                binding.yellowOval.setImageResource(R.drawable.done)
                binding.greenOval.setImageResource(0)
                binding.redOval.setImageResource(0)
            }
            redOval.setOnClickListener {
                Utils.showToast(requireContext() , "Priority : High")
                priority = "3"
                binding.redOval.setImageResource(R.drawable.done)
                binding.yellowOval.setImageResource(0)
                binding.greenOval.setImageResource(0)
            }
        }
    }
    private fun updateLable(myCalender: Calendar) {
        val myFormat = "dd-MM-yyyy"
        val sdf = SimpleDateFormat(myFormat, Locale.UK)
        binding.tvDate.text = sdf.format(myCalender.time)
    }
}