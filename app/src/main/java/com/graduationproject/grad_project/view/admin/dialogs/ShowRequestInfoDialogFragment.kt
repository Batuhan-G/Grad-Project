package com.graduationproject.grad_project.view.admin.dialogs

import android.app.Dialog
import android.app.DownloadManager
import android.content.Context
import android.net.Uri
import android.os.Bundle
import android.os.Environment
import android.view.View
import android.widget.Toast
import androidx.fragment.app.DialogFragment
import androidx.navigation.fragment.navArgs
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.graduationproject.grad_project.R
import com.graduationproject.grad_project.databinding.FragmentShowRequestInfoDialogBinding
import com.squareup.picasso.Picasso
import java.io.File
import java.util.*

class ShowRequestInfoDialogFragment : DialogFragment() {

    private var _binding : FragmentShowRequestInfoDialogBinding? = null
    private val binding get() = _binding!!
    private val args: ShowRequestInfoDialogFragmentArgs by navArgs()

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        return activity?.let {
            _binding = FragmentShowRequestInfoDialogBinding.inflate(layoutInflater)
            val view = binding.root
            binding.requestTitleText.text = args.title
            binding.requestContentText.text = args.content
            binding.requestTypeText.text = args.type
            showImage()
            val uuid = UUID.randomUUID()
            binding.imageOfRequest.setOnClickListener {
                downloadImageNew(uuid.toString(), args.downloadUri)
            }
            val materialDialog = MaterialAlertDialogBuilder(requireContext())
                .setView(view)
                .setPositiveButton(R.string.tamam) { _, _ -> }
                .create()

            materialDialog
        } ?: throw IllegalStateException("Activity cannot be null")
    }

    private fun showImage() {
        if (args.downloadUri.isNotEmpty()) {
            Picasso.get().load(args.downloadUri).into(binding.imageOfRequest)
        } else {
            binding.imageOfRequest.visibility = View.GONE
        }
    }

    private fun downloadImageNew(filename: String, downloadUrlOfImage: String) {
        try {
            val dm = activity?.getSystemService(Context.DOWNLOAD_SERVICE) as DownloadManager?
            val downloadUri: Uri = Uri.parse(downloadUrlOfImage)
            val request = DownloadManager.Request(downloadUri)
            request.setAllowedNetworkTypes(DownloadManager.Request.NETWORK_WIFI or DownloadManager.Request.NETWORK_MOBILE)
                .setAllowedOverRoaming(false)
                .setTitle(filename)
                .setMimeType("image/jpeg")
                .setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED)
                .setDestinationInExternalPublicDir(
                    Environment.DIRECTORY_PICTURES,
                    File.separator.toString() + filename + ".jpg"
                )
            dm!!.enqueue(request)
            Toast.makeText(requireContext(), R.string.dosyaİndiriliyor, Toast.LENGTH_SHORT).show()
        } catch (e: Exception) {
            Toast.makeText(requireContext(), R.string.dosyaİndirmeBaşarısız, Toast.LENGTH_SHORT).show()
        }
    }
}