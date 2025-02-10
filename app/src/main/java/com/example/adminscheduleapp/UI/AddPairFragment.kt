package com.example.adminscheduleapp.UI

import android.annotation.SuppressLint
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.navigation.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.adminscheduleapp.adapters.AddPairRecyclerViewAdapter
import com.example.adminscheduleapp.data.Constants.APP_ADMIN_BASE_SCHEDULE_EDIT_MODE
import com.example.adminscheduleapp.data.Constants.APP_ADMIN_CURRENT_SCHEDULE_EDIT_MODE
import com.example.adminscheduleapp.data.Constants.APP_ADMIN_EDIT_PAIR_ARRAY
import com.example.adminscheduleapp.data.Constants.APP_ADMIN_PARAMETERS_CABINET_NAME
import com.example.adminscheduleapp.data.Constants.APP_ADMIN_PARAMETERS_DISCIPLINE_NAME
import com.example.adminscheduleapp.data.Constants.APP_ADMIN_PARAMETERS_TEACHER_NAME
import com.example.adminscheduleapp.databinding.FragmentAddPairBinding
import com.example.adminscheduleapp.utils.Utils.checkIfItemArraysAreEqual
import com.example.adminscheduleapp.utils.Utils.getItemArrayDeepCopy
import com.example.adminscheduleapp.viewmodels.MainActivityViewModel
import com.example.adminscheduleapp.viewmodels.ScheduleViewModel

class AddPairFragment() : Fragment() {
    private lateinit var binding: FragmentAddPairBinding
    private val mainViewModel: MainActivityViewModel by activityViewModels()
    private val scheduleViewModel: ScheduleViewModel by activityViewModels()
    private lateinit var addPairRecyclerViewAdapter: AddPairRecyclerViewAdapter

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View? {
        binding = FragmentAddPairBinding.inflate(layoutInflater)
        return binding.root
    }

    @SuppressLint("SetTextI18n")
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        setupRecyclerView()

        binding.subPairEnable.setOnCheckedChangeListener { v, checked -> updateRecyclerView() }
        binding.optionalEnable.setOnCheckedChangeListener { v, checked -> updateRecyclerView() }

        binding.saveButton.setOnClickListener {
            scheduleViewModel.saveNewPair(ArrayList(addPairRecyclerViewAdapter.differ.currentList))
            mainViewModel.setNextUpload(true)
            requireView().findNavController().navigate(AddPairFragmentDirections.actionAddPairFragmentToFragmentContainer())
            it.isEnabled = false
        }

        binding.pair.text = (scheduleViewModel.getChosenPairNum()!! + 1).toString() + "-я пара"
        binding.group.text = scheduleViewModel.getChosenGroup()
        binding.date.text = mainViewModel.getDayToTab().replace(System.lineSeparator(), ", ")

        if (mainViewModel.getEditMode() == APP_ADMIN_BASE_SCHEDULE_EDIT_MODE) {
            binding.baseSchedule.text = scheduleViewModel.getBaseScheduleName()
        }
    }

    private fun setupRecyclerView() {
        val discipline_params_list = mainViewModel.getParametersList(APP_ADMIN_PARAMETERS_DISCIPLINE_NAME)
        val teacher_params_list = mainViewModel.getParametersList(APP_ADMIN_PARAMETERS_TEACHER_NAME)
        val cabinet_params_list = mainViewModel.getParametersList(APP_ADMIN_PARAMETERS_CABINET_NAME)

        teacher_params_list.sort()
        discipline_params_list.sort()
        cabinet_params_list.sort()

        addPairRecyclerViewAdapter = AddPairRecyclerViewAdapter(
            discipline_params_list,
            teacher_params_list,
            cabinet_params_list
        ) { mainViewModel.performTimerEvent({ updateSaveButton() }, 50L) }

        val addPairArray = getItemArrayDeepCopy(scheduleViewModel.getChosenScheduleItem()!!)
        addPairRecyclerViewAdapter.differ.submitList(addPairArray)
        binding.apply {
            schedulesRecyclerView.apply {
                layoutManager = LinearLayoutManager(activity)
                adapter = addPairRecyclerViewAdapter
            }
        }

        binding.optionalEnable.isChecked = addPairArray[1].visibility
        binding.subPairEnable.isChecked = addPairArray[2].visibility
    }

    private fun updateRecyclerView() {
        val addPairArray = ArrayList(addPairRecyclerViewAdapter.differ.currentList)
        val defaultArray = ArrayList(APP_ADMIN_EDIT_PAIR_ARRAY)

        addPairArray[1].visibility = binding.optionalEnable.isChecked
        addPairArray[2].visibility = binding.subPairEnable.isChecked
        addPairArray[3].visibility = binding.optionalEnable.isChecked && binding.subPairEnable.isChecked

        for (i in 1 until addPairArray.size) {
            if (!addPairArray[i].visibility) {
                addPairArray[i] = defaultArray[i]
            }
        }

        addPairRecyclerViewAdapter.run {
            differ.submitList(addPairArray)
            notifyDataSetChanged()
        }

        updateSaveButton()
    }

    private fun updateSaveButton() {
        Log.d("ADMIN_PAIR_FORMAT_CHECKER", "")
        Log.d("ADMIN_PAIR_FORMAT_CHECKER", "Update function was called.")
        val addPairArray = ArrayList(addPairRecyclerViewAdapter.differ.currentList)

        var formatCheck = true
        var allElementsAreEmpty = true
        var baseElementIsEmpty: Boolean? = null
        addPairArray.forEach {
            if (it.visibility && formatCheck) {
                if (it.id % 2 != 0) {
                    val subGroupIsEmpty = it.subGroup.isEmpty()
                    val emptyStrings = (it.cabinetSecond.isEmpty() && it.teacherSecond.isEmpty() && it.cabinetThird.isEmpty() && it.teacherThird.isEmpty())
                    val wrongChoices = ( (it.cabinetSecond.isEmpty() xor it.teacherSecond.isEmpty()) || (it.cabinetThird.isEmpty() xor it.teacherThird.isEmpty()) )
                    if ( (subGroupIsEmpty && emptyStrings && !baseElementIsEmpty!!) || (!subGroupIsEmpty && !emptyStrings) || wrongChoices ) {
                        formatCheck = false
                        Log.d("ADMIN_PAIR_FORMAT_CHECKER", "Format of element number ${it.id} is wrong.")
                    } else if (!emptyStrings) {
                        allElementsAreEmpty = false
                    }
                } else {
                    val someEmptyStrings = (it.cabinet.isEmpty() || it.teacher.isEmpty() || it.pairName.isEmpty())
                    val someNonemptyStrings = (it.cabinet.isNotEmpty() || it.teacher.isNotEmpty() || it.pairName.isNotEmpty())
                    if (someEmptyStrings && someNonemptyStrings) {
                        formatCheck = false
                        Log.d("ADMIN_PAIR_FORMAT_CHECKER", "Format of element number ${it.id} is wrong.")
                    } else {
                        baseElementIsEmpty = someEmptyStrings
                        if (someNonemptyStrings) {
                            allElementsAreEmpty = false
                        }
                    }
                }
            }
        }
        if ((binding.optionalEnable.isChecked || binding.subPairEnable.isChecked) && allElementsAreEmpty) {
            formatCheck = false
            Log.d("ADMIN_PAIR_FORMAT_CHECKER", "Format is wrong: All elements are empty despite there being several of them. (Why would you need more than one of them then?)")
        }

        if (!formatCheck) {
            binding.saveButton.isEnabled = false
            return
        }

        Log.d("ADMIN_PAIR_FORMAT_CHECKER", "Base array: " + System.lineSeparator() + scheduleViewModel.getChosenScheduleItem().toString())
        Log.d("ADMIN_PAIR_FORMAT_CHECKER", "Current array: " + System.lineSeparator() + addPairArray.toString())
        binding.saveButton.isEnabled = !checkIfItemArraysAreEqual(addPairArray, scheduleViewModel.getChosenScheduleItem()!!)
        Log.d("ADMIN_PAIR_FORMAT_CHECKER", "Are they the same? ${!binding.saveButton.isEnabled}.")
    }
}