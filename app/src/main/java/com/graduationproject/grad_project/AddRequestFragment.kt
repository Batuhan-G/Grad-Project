package com.graduationproject.grad_project

import android.os.Bundle
import android.text.InputType
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.ArrayAdapter
import androidx.core.view.get
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.google.android.material.snackbar.Snackbar
import com.graduationproject.grad_project.databinding.FragmentAddRequestBinding
import com.graduationproject.grad_project.viewmodel.dialogs.AddRequestViewModel
import kotlinx.coroutines.*

class AddRequestFragment : Fragment(), AdapterView.OnItemSelectedListener {

    private var _binding: FragmentAddRequestBinding? = null
    private val binding get() = _binding!!
    private val viewModel: AddRequestViewModel by viewModels()
    companion object {
        const val TAG = "AddRequestFragment"
    }
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentAddRequestBinding.inflate(inflater, container, false)
        binding.backButtonToRequests.setOnClickListener { goBackToRequests() }
        binding.sendRequestButton.setOnClickListener {
            lifecycleScope.launch {
                sendRequestButtonClicked()
            }
        }
        return binding.root
    }

    private suspend fun sendRequestButtonClicked(
        ioDispatcher: CoroutineDispatcher = Dispatchers.IO
    ) {
       try {
           if (!isEmpty()) {
               viewModel.setTitle(binding.titleInput.text.toString())
               viewModel.setContent(binding.contentInput.text.toString())
               CoroutineScope(ioDispatcher).launch {
                   viewModel.shareRequestWithAdmin()
               }.join()
               goBackToRequests()
           } else {
               showSpacesAreEmptySnackBar()
           }
       } catch (e: Exception) {
           Log.e(TAG, "sendRequestButtonClicked ---> $e")
       }
    }

    private fun goBackToRequests() {
        val action = AddRequestFragmentDirections.actionAddRequestFragmentToRequestFragment()
        findNavController().navigate(action)
    }

    override fun onResume() {
        super.onResume()
        val typeList = resources.getStringArray(R.array.type_list)
        val arrayAdapter =
            this.context?.let { ArrayAdapter(it, R.layout.request_dropdown_item, typeList) }
        binding.requestTypeText.inputType = InputType.TYPE_NULL
        binding.requestTypeText.setAdapter(arrayAdapter)
        binding.requestTypeText.onItemSelectedListener = this
    }

    private fun showSpacesAreEmptySnackBar() {
        view?.let {
            Snackbar.make(
                it,
                R.string.boşluklarıDoldur,
                Snackbar.LENGTH_LONG
            ).show()
        }
    }
    private fun isEmpty() = binding.titleInput.text.isNullOrEmpty() || binding.contentInput.text.isNullOrEmpty()

    override fun onDestroy() {
        super.onDestroy()
        _binding = null
    }

    override fun onItemSelected(p0: AdapterView<*>?, p1: View?, p2: Int, p3: Long) {
        println("seçiton")
        println(p0?.getItemAtPosition(p2).toString())
        viewModel.setContent(p0?.getItemAtPosition(p2).toString())
    }

    override fun onNothingSelected(p0: AdapterView<*>?) {
        println("birşey seçmedin")
    }
}