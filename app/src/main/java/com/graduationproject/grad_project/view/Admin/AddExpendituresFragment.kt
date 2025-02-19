package com.graduationproject.grad_project.view.admin

import android.Manifest
import android.app.Activity.RESULT_OK
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import com.google.android.material.snackbar.Snackbar
import com.graduationproject.grad_project.R
import com.graduationproject.grad_project.databinding.FragmentAddExpendituresBinding
import com.graduationproject.grad_project.viewmodel.AddExpendituresViewModel

class AddExpendituresFragment : Fragment() {

    private var _binding: FragmentAddExpendituresBinding? = null
    private val binding get() = _binding!!
    private lateinit var activityResultLauncher: ActivityResultLauncher<Intent>
    private lateinit var permissionLauncher: ActivityResultLauncher<String>
    private var selectedPicture: Uri? = null
    private val viewModel: AddExpendituresViewModel by viewModels()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        // Inflate the layout for this fragment
        _binding = FragmentAddExpendituresBinding.inflate(inflater, container, false)
        binding.billAddText.visibility = View.GONE
        binding.viewModel = viewModel
        binding.lifecycleOwner = viewLifecycleOwner
        registerLauncher()
        binding.addDocument.setOnClickListener {
            selectDocument()
        }
        binding.shareButton.setOnClickListener {
            if (selectedPicture == null) {
                Snackbar.make(
                    requireView(),
                    "Lütfen bir fatura ekleyiniz!",
                    Snackbar.LENGTH_LONG
                ).show()
            } else {
                shareButtonClicked()
            }
        }
        viewModel.selectedImage.observe(viewLifecycleOwner) {
            if (it == null) {
                binding.billAddText.visibility = View.GONE
            }
            it?.let {
                binding.billAddText.visibility = View.VISIBLE
            }
        }
        binding.backButtonToAnnouncement.setOnClickListener { backToExpendituresFragment() }
        return binding.root
    }

    private fun backToExpendituresFragment() {
        val action = AddExpendituresFragmentDirections.actionAddExpendituresFragmentToExpendituresFragment()
        findNavController().navigate(action)
    }

    private fun shareButtonClicked() {
        if (!viewModel.isNull()) {
            viewModel.uploadImageAndShareExpenditure(selectedPicture)
            Snackbar.make(
                requireView(),
                "Gider sakinlerle paylaşılıyor...",
                Snackbar.LENGTH_LONG
            ).show()
            backToExpendituresFragment()
        } else {
            Snackbar.make(
                requireView(),
                R.string.boşluklarıDoldur,
                Snackbar.LENGTH_LONG
            ).show()
        }
    }

    private fun selectDocument() {
        if (this.context?.let { ContextCompat.checkSelfPermission(it, Manifest.permission.READ_EXTERNAL_STORAGE) } != PackageManager.PERMISSION_GRANTED) {
            if (activity?.let { ActivityCompat.shouldShowRequestPermissionRationale(it, Manifest.permission.READ_EXTERNAL_STORAGE) } == true) {
                view?.let {
                    Snackbar.make(
                        it,
                        "Permission needed for gallery",
                        Snackbar.LENGTH_INDEFINITE
                    ).setAction("Give permission") {
                        permissionLauncher.launch(Manifest.permission.READ_EXTERNAL_STORAGE)
                    }.show()
                }
            } else {
                permissionLauncher.launch(Manifest.permission.READ_EXTERNAL_STORAGE)
            }
        } else {
            val intentToGallery = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
            activityResultLauncher.launch(intentToGallery)
        }
    }

    private fun registerLauncher() {
        activityResultLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == RESULT_OK) {
                val intentFromResult = result.data
                if (intentFromResult != null) {
                    selectedPicture = intentFromResult.data
                }
            }
        }

        permissionLauncher = registerForActivityResult(ActivityResultContracts.RequestPermission()) { result ->
            if (result) {
                val intentToGallery = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
                activityResultLauncher.launch(intentToGallery)
            } else {
                Toast.makeText(this.requireContext(), "Permission needed!", Toast.LENGTH_SHORT).show()
            }
        }
    }
}