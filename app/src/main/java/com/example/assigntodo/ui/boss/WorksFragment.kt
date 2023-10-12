package com.example.assigntodo.ui.boss

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AlertDialog
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.assigntodo.R
import com.example.assigntodo.models.Works
import com.example.assigntodo.adapters.WorksAdapter
import com.example.assigntodo.databinding.FragmentWorksBinding
import com.example.assigntodo.utils.Utils
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener


class WorksFragment : Fragment() {
    val empData by navArgs<WorksFragmentArgs>()
    private lateinit var worksAdapter: WorksAdapter
    private lateinit var binding : FragmentWorksBinding
    private lateinit var workRoom : String
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
       binding = FragmentWorksBinding.inflate(layoutInflater)
        val empDataToPass = empData.empData
        binding.fabAssignWork.setOnClickListener {
            val action = WorksFragmentDirections.actionWorksFragmentToAssignWorkFragment(empDataToPass)
            findNavController().navigate(action)

        }
        val empName = empData.empData.userName
        binding.tbEmpWorks.apply {
            title = empName
            setNavigationIcon(R.drawable.baseline_arrow_back_24)
            setNavigationOnClickListener {
                activity?.onBackPressed()
            }
        }
        prepareRvForWorksAdapter()


        showAllWorks()
        return binding.root
    }

    private fun showAllWorks() {
        Utils.showDialog(requireContext())
        val bossId = FirebaseAuth.getInstance().currentUser?.uid
        workRoom = bossId + empData.empData.userId
        FirebaseDatabase.getInstance().getReference("Works").child(workRoom)
            .addValueEventListener(object  : ValueEventListener{
                override fun onDataChange(snapshot: DataSnapshot) {
                    val workList = ArrayList<Works>()
                    for(allWorks in snapshot.children){
                        val work = allWorks.getValue(Works::class.java)
                        workList.add(work!!)
                    }
                    worksAdapter.differ.submitList(workList)
                    Utils.hideDialog()
                    binding.tvText.visibility = if (workList.isEmpty()) {
                        View.VISIBLE
                    } else {
                        View.GONE
                    }
                }

                override fun onCancelled(error: DatabaseError) {
                    TODO("Not yet implemented")
                }

            })
    }

    private fun prepareRvForWorksAdapter() {
        worksAdapter = WorksAdapter(::onUnassignedButtonClicked)
        binding.rvWorks.apply {
            layoutManager = LinearLayoutManager(requireContext() , LinearLayoutManager.VERTICAL, false)
            adapter = worksAdapter
        }
    }

    private fun onUnassignedButtonClicked(works : Works) {
        val builder = AlertDialog.Builder(requireContext())
        val alertDialog = builder.create()
        builder.setTitle("Unassigned Work")
            .setMessage("Are you sure you want to unassigned this work")
            .setPositiveButton("Yes"){_,_->
                unassignedWork(works)
            }
            .setNegativeButton("NO"){_,_->
                alertDialog.dismiss()
            }
            .show()
    }

    private fun unassignedWork(works: Works) {
        works.expanded = !works.expanded
        FirebaseDatabase.getInstance().getReference("Works").child(workRoom)
            .addValueEventListener(object  : ValueEventListener{
                override fun onDataChange(snapshot: DataSnapshot) {
                    for(allWorks in snapshot.children){
                        val currentWork  = allWorks.getValue(Works::class.java)
                        if(currentWork == works){
                            allWorks.ref.removeValue()
                            Utils.showToast(requireContext() ,   "Work has been Unassigned!")
                        }
                    }
                }

                override fun onCancelled(error: DatabaseError) {
                    TODO("Not yet implemented")
                }

            })
    }

}