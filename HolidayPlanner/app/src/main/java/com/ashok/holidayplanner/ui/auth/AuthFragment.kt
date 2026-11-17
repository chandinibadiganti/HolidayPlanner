package com.ashok.holidayplanner.ui.auth

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.credentials.CredentialManager
import androidx.credentials.CustomCredential
import androidx.credentials.GetCredentialRequest
import androidx.credentials.exceptions.GetCredentialCancellationException
import androidx.credentials.exceptions.GetCredentialException
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.ashok.holidayplanner.HolidayApplication
import com.ashok.holidayplanner.R
import com.ashok.holidayplanner.databinding.FragmentAuthBinding
import com.ashok.holidayplanner.ui.common.UiState
import com.ashok.holidayplanner.ui.home.HomeFragment
import com.google.android.libraries.identity.googleid.GetGoogleIdOption
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential
import com.google.android.material.snackbar.Snackbar
import kotlinx.coroutines.launch

class AuthFragment : Fragment() {

    private var _binding: FragmentAuthBinding? = null
    private val binding get() = _binding!!

    private val authViewModel: AuthViewModel by activityViewModels {
        (requireActivity().application as HolidayApplication).container.viewModelFactory
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentAuthBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // If already signed in, go directly to Home
        if (authViewModel.isUserSignedIn()) {
            navigateToHome()
            return
        }

        setupListeners()
        observeAuthState()
    }

    private fun setupListeners() {
        binding.btnSignInGoogle.setOnClickListener {
            launchGoogleSignIn()
        }

        binding.btnContinueGuest.setOnClickListener {
            authViewModel.continueAsGuest("Guest Explorer")
        }
    }

    private fun launchGoogleSignIn() {
        binding.tvAuthError.visibility = View.GONE
        binding.authProgress.visibility = View.VISIBLE
        binding.btnSignInGoogle.isEnabled = false
        binding.btnContinueGuest.isEnabled = false

        val serverClientId = getServerClientId()
        if (serverClientId.isNullOrBlank() || serverClientId.startsWith("123456789012-mock")) {
            // Unconfigured or demo Web Client ID
            binding.authProgress.visibility = View.GONE
            binding.btnSignInGoogle.isEnabled = true
            binding.btnContinueGuest.isEnabled = true
            Snackbar.make(
                binding.root,
                "Firebase Web Client ID is not configured yet. You can continue as a Guest, or follow the README to set up Firebase.",
                Snackbar.LENGTH_LONG
            ).setAction("As Guest") {
                authViewModel.continueAsGuest("Guest Explorer")
            }.show()
            return
        }

        val credentialManager = CredentialManager.create(requireContext())
        val googleIdOption = GetGoogleIdOption.Builder()
            .setFilterByAuthorizedAccounts(false)
            .setServerClientId(serverClientId)
            .setAutoSelectEnabled(false)
            .build()

        val request = GetCredentialRequest.Builder()
            .addCredentialOption(googleIdOption)
            .build()

        viewLifecycleOwner.lifecycleScope.launch {
            try {
                val result = credentialManager.getCredential(
                    request = request,
                    context = requireActivity()
                )

                val credential = result.credential
                if (credential is CustomCredential &&
                    credential.type == GoogleIdTokenCredential.TYPE_GOOGLE_ID_TOKEN_CREDENTIAL
                ) {
                    val googleIdTokenCredential = GoogleIdTokenCredential.createFrom(credential.data)
                    val idToken = googleIdTokenCredential.idToken
                    authViewModel.handleGoogleIdToken(idToken)
                } else {
                    showError("Unsupported credential returned.")
                }
            } catch (e: GetCredentialCancellationException) {
                // User cancelled sign in
                binding.authProgress.visibility = View.GONE
                binding.btnSignInGoogle.isEnabled = true
                binding.btnContinueGuest.isEnabled = true
            } catch (e: GetCredentialException) {
                showError("Sign in failed: ${e.message}. You can continue as a Guest.")
            } catch (e: Exception) {
                showError("Authentication error: ${e.localizedMessage}")
            }
        }
    }

    private fun getServerClientId(): String? {
        return try {
            val resId = resources.getIdentifier("default_web_client_id", "string", requireContext().packageName)
            if (resId != 0) getString(resId) else null
        } catch (e: Exception) {
            null
        }
    }

    private fun observeAuthState() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                authViewModel.authState.collect { state ->
                    when (state) {
                        is UiState.Loading -> {
                            binding.authProgress.visibility = View.VISIBLE
                            binding.btnSignInGoogle.isEnabled = false
                            binding.btnContinueGuest.isEnabled = false
                            binding.tvAuthError.visibility = View.GONE
                        }
                        is UiState.Success -> {
                            binding.authProgress.visibility = View.GONE
                            navigateToHome()
                        }
                        is UiState.Error -> {
                            showError(state.message)
                        }
                        UiState.Empty -> {
                            binding.authProgress.visibility = View.GONE
                            binding.btnSignInGoogle.isEnabled = true
                            binding.btnContinueGuest.isEnabled = true
                            binding.tvAuthError.visibility = View.GONE
                        }
                    }
                }
            }
        }
    }

    private fun showError(message: String) {
        binding.authProgress.visibility = View.GONE
        binding.btnSignInGoogle.isEnabled = true
        binding.btnContinueGuest.isEnabled = true
        binding.tvAuthError.text = message
        binding.tvAuthError.visibility = View.VISIBLE
        Snackbar.make(binding.root, message, Snackbar.LENGTH_LONG).show()
    }

    private fun navigateToHome() {
        parentFragmentManager.beginTransaction()
            .replace(R.id.fragment_container, HomeFragment())
            .commit()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
