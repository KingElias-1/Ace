package com.kingelias.ace.fragments

import android.app.ProgressDialog
import android.content.Intent
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.firebase.auth.FirebaseAuth
import com.kingelias.ace.R
import com.kingelias.ace.activities.AuthActivity
import com.kingelias.ace.data.User
import com.kingelias.ace.databinding.FragmentSettingsBinding
import com.kingelias.ace.viewmodels.UserVM


class SettingsFragment : Fragment() {
    private lateinit var settingsBinding: FragmentSettingsBinding

    private var auth = FirebaseAuth.getInstance()
    private lateinit var googleSignInClient: GoogleSignInClient

    //progress dialog
    private lateinit var logoutInDialog: ProgressDialog

    private val usersVM by lazy {
        ViewModelProvider(this)[UserVM::class.java]
    }
    private lateinit var user: User

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        settingsBinding = FragmentSettingsBinding.inflate(inflater, container, false)

        logoutInDialog = ProgressDialog(requireActivity())
        logoutInDialog.setTitle("Please wait")
        logoutInDialog.setMessage("Logging you out...")
        logoutInDialog.setCanceledOnTouchOutside(false)

        usersVM.getUser()
        usersVM.user.observe(viewLifecycleOwner){
            user = it
        }

        usersVM.result.observe(viewLifecycleOwner){ exception ->
            Toast.makeText(requireContext(), exception?.message.toString(), Toast.LENGTH_LONG).show()
        }

        settingsBinding.logoutBn.setOnClickListener {
            logoutInDialog.show()
            handleLogout()
        }

        settingsBinding.personalDetailsBn.setOnClickListener {
            findNavController().navigate(SettingsFragmentDirections.actionSettingsFragmentToUserDetailsFragment(false))
        }

        settingsBinding.businessDetailsBn.setOnClickListener {
            findNavController().navigate(SettingsFragmentDirections.actionSettingsFragmentToUserDetailsFragment( true))
        }

        settingsBinding.myAdsBn.setOnClickListener {
            findNavController().navigate(SettingsFragmentDirections.actionSettingsFragmentToMyAdsFragment())
        }

        settingsBinding.myFeedbackBN.setOnClickListener {
            findNavController().navigate(SettingsFragmentDirections.actionSettingsFragmentToMyFeedbackFragment( true, null))
        }

        settingsBinding.aboutUsBn.setOnClickListener {
            findNavController().navigate(SettingsFragmentDirections.actionSettingsFragmentToAboutFragment())
        }

        //handling sending reset password link
        settingsBinding.changePasswordBn.setOnClickListener{
            val user = usersVM.user.value
            sendPasswordResetEmail(user?.email.toString().trim())
        }

        settingsBinding.changeEmailBn.setOnClickListener{
            AlertDialog.Builder(requireActivity()).also{
                it.setTitle("Sorry :(")
                it.setMessage("This feature is currently unavailable")
                it.setPositiveButton(getString(R.string.ok)){ _, _ ->
                }
            }.create().show()
        }

        settingsBinding.giveFeedbackBn.setOnClickListener{
            AlertDialog.Builder(requireActivity()).also{
                it.setTitle("Sorry :(")
                it.setMessage("This feature is currently unavailable")
                it.setPositiveButton(getString(R.string.ok)){ _, _ ->
                }
            }.create().show()
        }

        //handling deleting user account
        settingsBinding.deleteAccountBn.setOnClickListener{
            AlertDialog.Builder(requireActivity()).also{
                it.setTitle("Are you sure you want to delete your account?")
                it.setMessage("Deleting your account will get rid of all your data")
                it.setNegativeButton(getString(R.string.cancel)){ _, _ ->
                }
                it.setPositiveButton(getString(R.string.yes)){ _, _ ->
                    usersVM.deleteUser()
                    startActivity(Intent(requireContext(), AuthActivity::class.java))
                }
            }.create().show()
        }

        return settingsBinding.root
    }

    private fun sendPasswordResetEmail(email: String) {
        auth.sendPasswordResetEmail(email)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    // Password reset email sent successfully
                    AlertDialog.Builder(requireActivity()).also{
                        it.setTitle("Password Reset Link sent!")
                        it.setMessage("Please use the link sent to your mail to Update your password")
                        it.setPositiveButton(getString(R.string.ok)){ _, _ ->
                        }
                    }.create().show()
                } else {
                    // Failed to send password reset email
                    AlertDialog.Builder(requireActivity()).also{
                        it.setTitle("Failed to send Password Reset Link sent!")
                        it.setMessage(task.exception?.message.toString())
                        it.setPositiveButton(getString(R.string.ok)){ _, _ ->
                        }
                    }.create().show()
                }
            }

    }

    override fun onResume() {
        super.onResume()
        //get users again when resumed
        usersVM.getUser()
    }

    fun handleLogout() {
        //logging out
        signOutFromGoogle()
        auth.signOut()

        val firebaseUser = auth.currentUser
        if (firebaseUser != null) {
            //user still logged in
            logoutInDialog.dismiss()
        } else {
            logoutInDialog.dismiss()
            startActivity(Intent(requireContext(), AuthActivity::class.java))
        }
    }

    private fun signOutFromGoogle() {
        // generating Google Sign in client
        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken(getString(R.string.default_web_client_id))
            .requestEmail()
            .build()
        googleSignInClient = GoogleSignIn.getClient(requireContext(), gso)

        //revoking google Sign in token
        googleSignInClient.revokeAccess()
    }

}