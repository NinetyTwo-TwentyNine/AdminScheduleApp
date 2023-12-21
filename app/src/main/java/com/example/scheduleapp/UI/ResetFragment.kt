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
import com.example.scheduleapp.data.AuthenticationStatus
import com.example.scheduleapp.databinding.FragmentResetBinding
import com.example.scheduleapp.utils.Utils.getBlankStringsChecker
import com.example.scheduleapp.viewmodels.MainActivityViewModel
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class ResetFragment : Fragment() {
    private val viewModel: MainActivityViewModel by activityViewModels()
    private lateinit var binding: FragmentResetBinding
    private lateinit var setButtonVisibility: ()->Unit
    private lateinit var currentAuthStatus: MutableLiveData<AuthenticationStatus>

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        binding = FragmentResetBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.loginButton.setOnClickListener {
            view.findNavController()
                .navigate(ResetFragmentDirections.actionResetFragmentToLoginFragment())
        }

        setButtonVisibility = {
            if (currentAuthStatus.value != AuthenticationStatus.Progress) {
                binding.resetButton.isEnabled = !binding.userEmail.text.toString().isBlank()
            }
        }
        binding.userEmail.addTextChangedListener(getBlankStringsChecker(binding.userEmail, setButtonVisibility))

        binding.resetButton.setOnClickListener {
            viewModel.sendResetMessage(currentAuthStatus, binding.userEmail.text.toString())
        }

        initObservers()
    }

    private fun initObservers() {
        currentAuthStatus = MutableLiveData()
        currentAuthStatus.observe(viewLifecycleOwner) {authStatus->
            when (authStatus) {
                is AuthenticationStatus.Success -> {
                    setButtonVisibility()
                    binding.progressBar.visibility = View.GONE
                    Toast.makeText(activity, "Reset message sent successfully.", Toast.LENGTH_SHORT).show()
                    Log.d("TAG", "Successful send")
                }
                is AuthenticationStatus.Error -> {
                    setButtonVisibility()
                    binding.progressBar.visibility = View.GONE
                    Toast.makeText(activity, "Failed to send the reset message: ${authStatus.message}", Toast.LENGTH_LONG).show()
                    Log.d("TAG", authStatus.message)
                }
                is AuthenticationStatus.Progress -> {
                    binding.resetButton.isEnabled = false
                    binding.progressBar.visibility = View.VISIBLE
                }
            }
        }
    }
}