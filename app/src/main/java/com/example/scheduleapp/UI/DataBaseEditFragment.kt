package com.example.scheduleapp.UI

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.MutableLiveData
import androidx.navigation.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.scheduleapp.R
import com.example.scheduleapp.adapters.AdminDBEditorRecyclerViewAdapter
import com.example.scheduleapp.data.AuthenticationStatus
import com.example.scheduleapp.data.Constants
import com.example.scheduleapp.data.Constants.APP_ADMIN_PARAMETERS_LIST
import com.example.scheduleapp.data.Data_IntString
import com.example.scheduleapp.databinding.FragmentDataBaseEditBinding
import com.example.scheduleapp.viewmodels.MainActivityViewModel
import com.example.scheduleapp.viewmodels.ScheduleFragmentViewModel

class DataBaseEditFragment: Fragment() {
    private var index: Int? = null
    private lateinit var dbEditorRecyclerViewAdapter: AdminDBEditorRecyclerViewAdapter
    private lateinit var binding: FragmentDataBaseEditBinding
    private val mainViewModel: MainActivityViewModel by activityViewModels()
    private val scheduleViewModel: ScheduleFragmentViewModel by activityViewModels()

    private lateinit var buttonCheck: (ArrayList<Int>)->Unit
    private lateinit var deleteOneView: (Int)->Unit
    private lateinit var editOneView: (Int, String)->Unit

    private lateinit var currentRecyclerList: ArrayList<Data_IntString>

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        binding = FragmentDataBaseEditBinding.inflate(layoutInflater)
        return binding.root
    }

    override fun onViewCreated(itemView: View, savedInstanceState: Bundle?) {
        val args = arguments
        index = args?.getInt("index", 0)

        if (index != null) {
            binding.addButton.setOnClickListener {
                val currentId = scheduleViewModel.getPossibleId(currentRecyclerList)
                currentRecyclerList.add(Data_IntString(currentId, ""))
                setupRecyclerView()
            }

            currentRecyclerList = mainViewModel.getParametersByName(APP_ADMIN_PARAMETERS_LIST[index!!])

            buttonCheck = {array ->
                binding.addButton.isEnabled = array.isEmpty()
            }
            deleteOneView = {id ->
                for (e: Data_IntString in currentRecyclerList) {
                    if (e.id == id)  {
                        currentRecyclerList.remove(e)
                        break
                    }
                }
                setupRecyclerView()
            }
            editOneView = {id, title ->
                for (e: Data_IntString in currentRecyclerList) {
                    if (e.id == id)  {
                        e.title = title
                        break
                    }
                }
                //setupRecyclerView()
            }

            dbEditorRecyclerViewAdapter = AdminDBEditorRecyclerViewAdapter(buttonCheck, deleteOneView, editOneView)
            setupRecyclerView()
        }
    }

    fun setupRecyclerView() {
        dbEditorRecyclerViewAdapter.differ.submitList(currentRecyclerList)
        binding.apply {
            parametersRecyclerView.apply {
                layoutManager = LinearLayoutManager(activity)
                adapter = dbEditorRecyclerViewAdapter
            }
        }
    }

    companion object {
        fun newInstance(position: Int): DataBaseEditFragment {
            val fragment = DataBaseEditFragment()
            val args = Bundle()
            args.putInt("index", position)
            fragment.arguments = args
            return fragment
        }
    }
}