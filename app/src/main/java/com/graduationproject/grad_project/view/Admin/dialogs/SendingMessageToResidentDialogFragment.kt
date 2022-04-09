package com.graduationproject.grad_project.view.admin.dialogs

import android.app.Dialog
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.FragmentTransaction
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.Timestamp
import com.graduationproject.grad_project.R
import com.graduationproject.grad_project.databinding.FragmentSendingMessageToResidentDialogBinding
import com.graduationproject.grad_project.model.Message
import com.graduationproject.grad_project.model.SiteResident
import com.graduationproject.grad_project.viewmodel.dialogs.SendingMessageToResidentDialogViewModel
import kotlinx.coroutines.*
import kotlinx.serialization.builtins.serializer
import java.util.*


class SendingMessageToResidentDialogFragment(
    private val resident: SiteResident,
    private val ioDispatcher: CoroutineDispatcher = Dispatchers.IO
) : DialogFragment() {

    private var _binding: FragmentSendingMessageToResidentDialogBinding? = null
    private val binding get() = _binding!!

    private val viewModel: SendingMessageToResidentDialogViewModel by viewModels()

    companion object {
        const val TAG = "AddingDebtDialogFragment"
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {

        return activity?.let {
            _binding = FragmentSendingMessageToResidentDialogBinding.inflate(layoutInflater)
            val view = binding.root
            /*  val inflater = requireActivity().layoutInflater
            val view = inflater.inflate(R.layout.fragment_adding_debt_dialog, null)*/
            val builder = MaterialAlertDialogBuilder(it)
                .setView(view)
                .setPositiveButton("Gönder") { _, _ ->
                    lifecycleScope.launch {
                        setPositiveButton()
                        dismiss()
                    }
                }.setNegativeButton("İptal") { _, _ ->
                    setNegativeButton()
                }
            builder.create()
        } ?: throw IllegalStateException("Activity cannot be null")
    }

    private suspend fun setPositiveButton() {
        CoroutineScope(ioDispatcher).launch {
            viewModel.setTitle(binding.titleOfSendMessage.text.toString())
            viewModel.setContent(binding.contentOfSendMessage.text.toString())
            val uuid = UUID.randomUUID()
            val message = Message(
                viewModel.title,
                viewModel.content,
                uuid.toString(),
                Timestamp.now()
            )
            viewModel.saveMessageIntoDB(resident.email, message)
            if (viewModel.isMessageSaved) {
                viewModel.takePlayerIdAndSendPostNotification(resident)
            } else {
                Log.e(TAG, "onCreateDialog ---> viewModel.isMessageSaved == false")
            }
        }.join()
    }

    private fun setNegativeButton() {
        view?.let { view ->
            Snackbar.make(
                view,
                "Borç ekleme iptal edildi!",
                Snackbar.LENGTH_LONG
            ).show()
        }
    }

}