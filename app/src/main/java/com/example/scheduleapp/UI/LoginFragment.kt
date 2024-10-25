package com.example.adminscheduleapp.UI

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
import com.example.adminscheduleapp.data.AuthenticationStatus
import com.example.adminscheduleapp.data.Constants.APP_MIN_PASSWORD_LENGTH
import com.example.adminscheduleapp.data.Constants.APP_PREFERENCES_STAY
import com.example.adminscheduleapp.data.Constants.APP_TOAST_DATA_DOWNLOAD_FAILED
import com.example.adminscheduleapp.data.Constants.APP_TOAST_LOGIN_FAILED
import com.example.adminscheduleapp.data.Constants.APP_TOAST_LOGIN_SUCCESS
import com.example.adminscheduleapp.data.DownloadStatus
import com.example.adminscheduleapp.data.FlatSchedule
import com.example.adminscheduleapp.databinding.FragmentLoginBinding
import com.example.adminscheduleapp.utils.Utils.getBlankStringsChecker
import com.example.adminscheduleapp.viewmodels.MainActivityViewModel
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class LoginFragment : Fragment() {
    private val viewModel: MainActivityViewModel by activityViewModels()
    private lateinit var binding: FragmentLoginBinding
    private lateinit var setButtonVisibility: ()->Unit
    private lateinit var currentAuthStatus: MutableLiveData<AuthenticationStatus>
    private lateinit var currentDownloadStatus: MutableLiveData<DownloadStatus<FlatSchedule>>

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        binding = FragmentLoginBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        if (viewModel.isUserSingedIn()) {
            if (!viewModel.getPreference(APP_PREFERENCES_STAY, false)) {
                viewModel.signOut()
            }
        }

        initDownloadObservers()
        //viewModel.downloadParametersList(currentDownloadStatus)
        viewModel.downloadEverything(currentDownloadStatus)
    }

    private fun initDownloadObservers() {
        currentDownloadStatus = MutableLiveData()
        currentDownloadStatus.observe(viewLifecycleOwner) { downloadStatus ->
            when (downloadStatus) {
                is DownloadStatus.Progress -> {
                    binding.progressBar.visibility = View.VISIBLE
                }
                is DownloadStatus.WeakProgress -> {
                    Toast.makeText(
                        activity,
                        downloadStatus.message,
                        Toast.LENGTH_LONG
                    ).show()
                }
                is DownloadStatus.Error -> {
                    binding.progressBar.visibility = View.GONE
                    currentDownloadStatus.removeObservers(viewLifecycleOwner)
                    Toast.makeText(
                        activity,
                        "$APP_TOAST_DATA_DOWNLOAD_FAILED: ${downloadStatus.message}",
                        Toast.LENGTH_LONG
                    ).show()
                }
                is DownloadStatus.Success<FlatSchedule> -> {
                    binding.progressBar.visibility = View.GONE
                    currentDownloadStatus.removeObservers(viewLifecycleOwner)

                    if (viewModel.isUserSingedIn()) {
                        requireView().findNavController()
                            .navigate(LoginFragmentDirections.actionLoginFragmentToAdminPanelFragment())
                    }
                    initializeView()
                    initAuthObservers()
                }
                else -> {
                    throw IllegalStateException()
                }
            }
        }
    }

    private fun initAuthObservers() {
        currentAuthStatus = MutableLiveData()
        currentAuthStatus.observe(viewLifecycleOwner) {authStatus->
            when (authStatus) {
                is AuthenticationStatus.Success -> {
                    setButtonVisibility()
                    binding.progressBar.visibility = View.GONE
                    Toast.makeText(activity, "$APP_TOAST_LOGIN_SUCCESS.", Toast.LENGTH_SHORT).show()
                    Log.d("TAG", "Successful login")
                    requireView().findNavController()
                        .navigate(LoginFragmentDirections.actionLoginFragmentToAdminPanelFragment())
                }
                is AuthenticationStatus.Error -> {
                    setButtonVisibility()
                    binding.progressBar.visibility = View.GONE
                    Toast.makeText(activity, "$APP_TOAST_LOGIN_FAILED: ${authStatus.message}", Toast.LENGTH_LONG).show()
                    Log.d("TAG", authStatus.message)
                }
                is AuthenticationStatus.Progress -> {
                    binding.loginButton.isEnabled = false
                    binding.progressBar.visibility = View.VISIBLE
                }
            }
        }
    }

    private fun initializeView() {
        binding.registerButton.setOnClickListener {
            requireView().findNavController()
                .navigate(LoginFragmentDirections.actionLoginFragmentToRegistrationFragment())
        }
        binding.forgotButton.setOnClickListener {
            requireView().findNavController()
                .navigate(LoginFragmentDirections.actionLoginFragmentToResetFragment())
        }

        setButtonVisibility = {
            if (currentAuthStatus.value != AuthenticationStatus.Progress) {
                binding.loginButton.isEnabled =
                    !(binding.userEmail.text.toString().isBlank() || binding.userPassword.text.toString().isBlank())
                            && binding.userPassword.text.toString().count() >= APP_MIN_PASSWORD_LENGTH
            }
        }

        binding.userEmail.isEnabled = true
        binding.userEmail.addTextChangedListener(getBlankStringsChecker(binding.userEmail, setButtonVisibility))
        binding.userPassword.isEnabled = true
        binding.userPassword.addTextChangedListener(getBlankStringsChecker(binding.userPassword, setButtonVisibility))

        binding.loginButton.setOnClickListener {
            viewModel.signIn(currentAuthStatus, binding.userEmail.text.toString(), binding.userPassword.text.toString(), false)
        }
    }
}