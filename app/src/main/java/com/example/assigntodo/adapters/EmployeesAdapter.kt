package com.example.assigntodo.adapters

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.navigation.Navigation
import androidx.recyclerview.widget.AsyncListDiffer

import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.RecyclerView.ViewHolder
import com.bumptech.glide.Glide
import com.example.assigntodo.R
import com.example.assigntodo.models.Users
import com.example.assigntodo.databinding.ItemViewEmplyeesProfileBinding
import com.example.assigntodo.ui.boss.EmployeesFragmentDirections

class EmployeesAdapter : RecyclerView.Adapter<EmployeesAdapter.EmployeesViewHolder>() {
    class EmployeesViewHolder(val binding : ItemViewEmplyeesProfileBinding) : ViewHolder(binding.root)

    val diffUtil  = object : DiffUtil.ItemCallback<Users>(){
        override fun areItemsTheSame(oldItem: Users, newItem: Users): Boolean {
            return oldItem.id == newItem.id
        }
        override fun areContentsTheSame(oldItem: Users, newItem: Users): Boolean {
            return oldItem == newItem
        }
    }
    val differ = AsyncListDiffer(this, diffUtil)
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): EmployeesViewHolder {
        return EmployeesViewHolder(ItemViewEmplyeesProfileBinding.inflate(LayoutInflater.from(parent.context) , parent , false))
    }

    override fun getItemCount(): Int {
        return differ.currentList.size
    }

    override fun onBindViewHolder(holder: EmployeesViewHolder, position: Int) {
        val empData = differ.currentList[position]
        holder.binding.apply {
            Glide.with(holder.itemView).load(empData.userImage).into(ivEmployeeProfile)
            tvEmployeeName.text = empData.userName
        }
        holder.itemView.setOnClickListener{
            val action = EmployeesFragmentDirections.actionEmployeesFragmentToWorksFragment(empData)
            Navigation.findNavController(it).popBackStack(R.id.employeesFragment , false)
            Navigation.findNavController(it).navigate(action)
        }
    }


}