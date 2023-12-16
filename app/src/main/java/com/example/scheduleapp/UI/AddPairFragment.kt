package com.example.scheduleapp.UI

import android.annotation.SuppressLint
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.scheduleapp.adapters.AddPairRecyclerViewAdapter
import com.example.scheduleapp.data.Constants.APP_ADMIN_BASE_SCHEDULE_EDIT_MODE
import com.example.scheduleapp.data.Constants.APP_ADMIN_CURRENT_SCHEDULE_EDIT_MODE
import com.example.scheduleapp.data.Constants.APP_ADMIN_EDIT_PAIR_ARRAY
import com.example.scheduleapp.data.Constants.APP_BD_PATHS_CABINET_LIST
import com.example.scheduleapp.data.Constants.APP_BD_PATHS_DISCIPLINE_LIST
import com.example.scheduleapp.data.Constants.APP_BD_PATHS_TEACHER_LIST
import com.example.scheduleapp.data.ScheduleDetailed
import com.example.scheduleapp.databinding.FragmentAddPairBinding
import com.example.scheduleapp.utils.Utils.checkIfItemArraysAreEqual
import com.example.scheduleapp.utils.Utils.convertArrayOfAddPairItemToPair
import com.example.scheduleapp.utils.Utils.getById
import com.example.scheduleapp.utils.Utils.getItemArrayDeepCopy
import com.example.scheduleapp.viewmodels.MainActivityViewModel
import com.example.scheduleapp.viewmodels.ScheduleFragmentViewModel

class AddPairFragment() : Fragment() {
    private lateinit var binding: FragmentAddPairBinding
    private val mainViewModel: MainActivityViewModel by activityViewModels()
    private val scheduleViewModel: ScheduleFragmentViewModel by activityViewModels()
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
            val subPair1 = ScheduleDetailed(scheduleViewModel.getChosenPairNum()!! * 2 + 1)
            val subPair2 = ScheduleDetailed(scheduleViewModel.getChosenPairNum()!! * 2 + 2)

            convertArrayOfAddPairItemToPair(ArrayList(addPairRecyclerViewAdapter.differ.currentList), Pair(subPair1, subPair2))
            scheduleViewModel.saveScheduleEdits(mainViewModel.getParameters(), Pair(subPair1, subPair2), mainViewModel.getEditMode())
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
        val discipline_params_list = mainViewModel.getParametersList(APP_BD_PATHS_DISCIPLINE_LIST)
        val teacher_params_list = mainViewModel.getParametersList(APP_BD_PATHS_TEACHER_LIST)
        val cabinet_params_list = mainViewModel.getParametersList(APP_BD_PATHS_CABINET_LIST)

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

        addPairRecyclerViewAdapter.differ.submitList(addPairArray)
        binding.apply {
            schedulesRecyclerView.apply {
                layoutManager = LinearLayoutManager(activity)
                adapter = addPairRecyclerViewAdapter
            }
        }

        updateSaveButton()
    }

    private fun updateSaveButton() {
        Log.d("ADMIN_PAIR_FORMAT_CHECKER", "")
        Log.d("ADMIN_PAIR_FORMAT_CHECKER", "Update function was called.")
        val addPairArray = ArrayList(addPairRecyclerViewAdapter.differ.currentList)

        var formatCheck = true
        addPairArray.forEach {
            if (it.visibility && formatCheck) {
                if ((it.id + 1) % 2 == 0) {
                    val subGroupIsEmpty = it.subGroup.isEmpty()
                    val emptyStrings = (it.cabinetSecond.isEmpty() && it.teacherSecond.isEmpty() && it.cabinetThird.isEmpty() && it.teacherThird.isEmpty())
                    val wrongChoices = ( (it.cabinetSecond.isEmpty() && it.cabinetThird.isNotEmpty()) || (it.teacherSecond.isEmpty() && it.teacherThird.isNotEmpty())
                                        || (it.cabinetThird.isNotEmpty() && it.teacherThird.isEmpty()) || (it.cabinetThird.isEmpty() && it.teacherThird.isNotEmpty()))
                    if ( (subGroupIsEmpty && emptyStrings) || (!subGroupIsEmpty && !emptyStrings) || wrongChoices) {
                        formatCheck = false
                        Log.d("ADMIN_PAIR_FORMAT_CHECKER", "Format of element number ${it.id} is wrong.")
                    }
                } else {
                    val someEmptyStrings = (it.cabinet.isEmpty() || it.teacher.isEmpty() || it.pairName.isEmpty())
                    val someNonemptyStrings = (it.cabinet.isNotEmpty() || it.teacher.isNotEmpty() || it.pairName.isNotEmpty())
                    if (someEmptyStrings && someNonemptyStrings) {
                        formatCheck = false
                        Log.d("ADMIN_PAIR_FORMAT_CHECKER", "Format of element number ${it.id} is wrong.")
                    }
                }
            }
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

    override fun onDestroyView() {
        if (scheduleViewModel.chosenScheduleIdIsNew!!) {
            when(mainViewModel.getEditMode()) {
                APP_ADMIN_CURRENT_SCHEDULE_EDIT_MODE -> scheduleViewModel.removeScheduleItemCurrent(mainViewModel.getParameters(), mainViewModel.getDayWithOffset(), scheduleViewModel.getChosenGroup()!!, scheduleViewModel.getChosenPairNum()!!)
                APP_ADMIN_BASE_SCHEDULE_EDIT_MODE -> scheduleViewModel.removeScheduleItemBase(mainViewModel.getParameters(), mainViewModel.getChosenDayIndex(), scheduleViewModel.getChosenGroup()!!, scheduleViewModel.getChosenPairNum()!!)
            }
        }
        super.onDestroyView()
    }
}