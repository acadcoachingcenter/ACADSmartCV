package com.acad.smartcv.ui.screens

import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import android.view.*
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import com.acad.smartcv.R
import com.acad.smartcv.databinding.FragmentProfileBinding
import com.acad.smartcv.data.model.*
import com.acad.smartcv.ui.components.*
import com.acad.smartcv.viewmodel.SmartCVViewModel
import com.acad.smartcv.viewmodel.SmartCVViewModelFactory
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import kotlinx.coroutines.*
import java.io.File

class ProfileFragment : Fragment() {

    private var _binding: FragmentProfileBinding? = null
    private val binding get() = _binding!!
    private val vm: SmartCVViewModel by activityViewModels {
        SmartCVViewModelFactory(requireActivity().application)
    }

    private var currentPhotoPath: String? = null
    private var cameraImageUri: Uri? = null

    // ── Photo launchers ────────────────────────────────────────────────────
    private val galleryLauncher = registerForActivityResult(
        ActivityResultContracts.GetContent()
    ) { uri -> uri?.let { handlePhotoUri(it) } }

    private val cameraLauncher = registerForActivityResult(
        ActivityResultContracts.TakePicture()
    ) { success -> if (success) cameraImageUri?.let { handlePhotoUri(it) } }

    private val documentPhotoLauncher = registerForActivityResult(
        ActivityResultContracts.GetContent()
    ) { uri -> uri?.let { extractPhotoFromDocument(it) } }

    // ── Camera permission launcher ─────────────────────────────────────────
    private val cameraPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { granted ->
        if (granted) {
            launchCamera()
        } else {
            Toast.makeText(
                requireContext(),
                "Camera permission denied. Please enable it in Settings.",
                Toast.LENGTH_LONG
            ).show()
        }
    }

    override fun onCreateView(i: LayoutInflater, c: ViewGroup?, s: Bundle?): View {
        _binding = FragmentProfileBinding.inflate(i, c, false)
        setHasOptionsMenu(true)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // ── Import CV button ───────────────────────────────────────────────
        binding.btnUploadCV.setOnClickListener {
            findNavController().navigate(R.id.action_to_cvImport)
        }

        // ── Profile photo ──────────────────────────────────────────────────
        binding.ivProfilePhoto.setOnClickListener { showPhotoOptions() }
        binding.btnChangePhoto.setOnClickListener { showPhotoOptions() }

        // ── Profile basic info ─────────────────────────────────────────────
        vm.fullProfile.observe(viewLifecycleOwner) { fp ->
            fp?.profile?.let { p ->
                binding.etFirstName.setText(p.firstName)
                binding.etLastName.setText(p.lastName)
                binding.etEmail.setText(p.email)
                binding.etPhone.setText(p.phone)
                binding.etTitle.setText(p.title)
                binding.etOrg.setText(p.organization)
                binding.etLocation.setText(p.location)
                binding.etBio.setText(p.bio)
                binding.etSkills.setText(p.skills)
                binding.etOrcid.setText(p.orcidId)
                binding.etLinkedIn.setText(p.linkedIn)
                binding.etWebsite.setText(p.website)

                // Load saved photo
                p.profilePhotoPath.takeIf { it.isNotBlank() }?.let { path ->
                    currentPhotoPath = path
                    ProfilePhotoHelper.loadPhoto(path)?.let {
                        binding.ivProfilePhoto.setImageBitmap(it)
                    }
                }
            }
        }

        binding.btnSaveProfile.setOnClickListener { saveProfile() }

        // ── Projects ───────────────────────────────────────────────────────
        vm.projects.observe(viewLifecycleOwner) { list ->
            binding.tvProjectCount.text = "(${list.size})"
            binding.rvProjects.adapter = SectionCardAdapter(list,
                title = { it.title },
                sub = { "${it.role} · ${it.duration}".trim(' ', '·') },
                badge = { "Project" },
                badgeColor = R.color.acad_blue_light,
                onDelete = { vm.deleteProject(it) }
            )
        }
        binding.btnAddProject.setOnClickListener {
            findNavController().navigate(R.id.action_profile_to_addProject)
        }

        // ── Publications ───────────────────────────────────────────────────
        vm.publications.observe(viewLifecycleOwner) { list ->
            binding.tvPubCount.text = "(${list.size})"
            binding.rvPublications.adapter = SectionCardAdapter(list,
                title = { it.title },
                sub = { "${it.journal} · ${it.year}".trim(' ', '·') },
                badge = { it.type.name.replace('_', ' ') },
                badgeColor = R.color.coral_light,
                onDelete = { vm.deletePublication(it) }
            )
        }
        binding.btnAddPublication.setOnClickListener {
            findNavController().navigate(R.id.action_profile_to_addPublication)
        }

        // ── Awards ─────────────────────────────────────────────────────────
        vm.awards.observe(viewLifecycleOwner) { list ->
            binding.tvAwardCount.text = "(${list.size})"
            binding.rvAwards.adapter = SectionCardAdapter(list,
                title = { it.name },
                sub = { "${it.awardingBody} · ${it.year}".trim(' ', '·') },
                badge = { "Award" },
                badgeColor = R.color.amber_light,
                onDelete = { vm.deleteAward(it) }
            )
        }
        binding.btnAddAward.setOnClickListener {
            findNavController().navigate(R.id.action_profile_to_addAward)
        }

        // ── Grants ─────────────────────────────────────────────────────────
        vm.grants.observe(viewLifecycleOwner) { list ->
            binding.tvGrantCount.text = "(${list.size})"
            binding.rvGrants.adapter = SectionCardAdapter(list,
                title = { it.title },
                sub = { "${it.agency} · ${it.period} · ${it.amount}".trim(' ', '·') },
                badge = { it.role.ifBlank { "Grant" } },
                badgeColor = R.color.green_light,
                onDelete = { vm.deleteGrant(it) }
            )
        }
        binding.btnAddGrant.setOnClickListener {
            findNavController().navigate(R.id.action_profile_to_addGrant)
        }

        // ── Education ──────────────────────────────────────────────────────
        vm.education.observe(viewLifecycleOwner) { list ->
            binding.tvEduCount.text = "(${list.size})"
            binding.rvEducation.adapter = SectionCardAdapter(list,
                title = { it.degree },
                sub = { "${it.institution} · ${it.year}".trim(' ', '·') },
                badge = { "Education" },
                badgeColor = R.color.purple_light,
                onDelete = { vm.deleteEducation(it) }
            )
        }
        binding.btnAddEducation.setOnClickListener {
            findNavController().navigate(R.id.action_profile_to_addEducation)
        }

        // ── Export PDF ─────────────────────────────────────────────────────
        binding.btnExportPdf.setOnClickListener { exportPdf() }
    }

    // ── Photo options dialog ───────────────────────────────────────────────
    private fun showPhotoOptions() {
        MaterialAlertDialogBuilder(requireContext())
            .setTitle("Set Profile Photo")
            .setItems(arrayOf(
                "📷  Take Photo",
                "🖼️  Choose from Gallery",
                "📄  Extract from CV/Document"
            )) { _, which ->
                when (which) {
                    0 -> openCamera()
                    1 -> galleryLauncher.launch("image/*")
                    2 -> documentPhotoLauncher.launch("*/*")
                }
            }
            .show()
    }

    // ── Check permission then open camera ─────────────────────────────────
    private fun openCamera() {
        when {
            ContextCompat.checkSelfPermission(
                requireContext(),
                android.Manifest.permission.CAMERA
            ) == PackageManager.PERMISSION_GRANTED -> {
                // Permission already granted — launch camera directly
                launchCamera()
            }
            shouldShowRequestPermissionRationale(android.Manifest.permission.CAMERA) -> {
                // Show explanation then ask
                MaterialAlertDialogBuilder(requireContext())
                    .setTitle("Camera Permission Needed")
                    .setMessage("Camera access is needed to take your profile photo.")
                    .setPositiveButton("Allow") { _, _ ->
                        cameraPermissionLauncher.launch(android.Manifest.permission.CAMERA)
                    }
                    .setNegativeButton("Cancel", null)
                    .show()
            }
            else -> {
                // Ask for permission
                cameraPermissionLauncher.launch(android.Manifest.permission.CAMERA)
            }
        }
    }

    // ── Actually launch camera after permission granted ────────────────────
    private fun launchCamera() {
        val photoFile = File(
            requireContext().filesDir,
            "camera_photo_${System.currentTimeMillis()}.jpg"
        )
        cameraImageUri = FileProvider.getUriForFile(
            requireContext(),
            "${requireContext().packageName}.fileprovider",
            photoFile
        )
        cameraLauncher.launch(cameraImageUri!!)
    }

    private fun handlePhotoUri(uri: Uri) {
        val bitmap = ProfilePhotoHelper.loadFromUri(requireContext(), uri) ?: run {
            Toast.makeText(requireContext(), "Could not load image", Toast.LENGTH_SHORT).show()
            return
        }
        val path = ProfilePhotoHelper.savePhoto(
            requireContext(),
            ProfilePhotoHelper.toCircle(
                ProfilePhotoHelper.resize(
                    ProfilePhotoHelper.cropToSquare(bitmap), 512
                )
            )
        )
        currentPhotoPath = path
        binding.ivProfilePhoto.setImageBitmap(ProfilePhotoHelper.loadPhoto(path))
        Toast.makeText(requireContext(), "Photo updated!", Toast.LENGTH_SHORT).show()
    }

    private fun extractPhotoFromDocument(uri: Uri) {
        val mimeType = requireContext().contentResolver.getType(uri) ?: ""
        CoroutineScope(Dispatchers.IO).launch {
            val bitmap = when {
                mimeType.contains("pdf") || uri.toString().endsWith(".pdf", true) ->
                    ProfilePhotoHelper.extractFromPdf(requireContext(), uri)
                mimeType.contains("wordprocessingml") || uri.toString().endsWith(".docx", true) ->
                    ProfilePhotoHelper.extractFromDocx(requireContext(), uri)
                else -> null
            }
            withContext(Dispatchers.Main) {
                if (bitmap != null) {
                    val path = ProfilePhotoHelper.savePhoto(
                        requireContext(),
                        ProfilePhotoHelper.toCircle(
                            ProfilePhotoHelper.resize(
                                ProfilePhotoHelper.cropToSquare(bitmap), 512
                            )
                        )
                    )
                    currentPhotoPath = path
                    binding.ivProfilePhoto.setImageBitmap(ProfilePhotoHelper.loadPhoto(path))
                    Toast.makeText(requireContext(), "Photo extracted!", Toast.LENGTH_SHORT).show()
                } else {
                    Toast.makeText(
                        requireContext(),
                        "No photo found in document",
                        Toast.LENGTH_LONG
                    ).show()
                }
            }
        }
    }

    private fun saveProfile() {
        val profileId = vm.activeProfileId.value.takeIf { it > 0 } ?: 0L
        vm.saveProfile(Profile(
            id                = profileId,
            firstName         = binding.etFirstName.text.toString().trim(),
            lastName          = binding.etLastName.text.toString().trim(),
            email             = binding.etEmail.text.toString().trim(),
            phone             = binding.etPhone.text.toString().trim(),
            title             = binding.etTitle.text.toString().trim(),
            organization      = binding.etOrg.text.toString().trim(),
            location          = binding.etLocation.text.toString().trim(),
            bio               = binding.etBio.text.toString().trim(),
            skills            = binding.etSkills.text.toString().trim(),
            orcidId           = binding.etOrcid.text.toString().trim(),
            linkedIn          = binding.etLinkedIn.text.toString().trim(),
            website           = binding.etWebsite.text.toString().trim(),
            profilePhotoPath  = currentPhotoPath ?: "",
            updatedAt         = System.currentTimeMillis()
        )) {
            Toast.makeText(requireContext(), "Profile saved!", Toast.LENGTH_SHORT).show()
        }
    }

    private fun exportPdf() {
        val fp = vm.fullProfile.value ?: run {
            Toast.makeText(requireContext(), "Save your profile first", Toast.LENGTH_SHORT).show()
            return
        }
        CoroutineScope(Dispatchers.IO).launch {
            val file = PdfExporter.export(requireContext(), fp)
            withContext(Dispatchers.Main) {
                val uri = FileProvider.getUriForFile(
                    requireContext(),
                    "${requireContext().packageName}.fileprovider",
                    file
                )
                startActivity(Intent.createChooser(
                    Intent(Intent.ACTION_VIEW).apply {
                        setDataAndType(uri, "application/pdf")
                        addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                    }, "Open PDF"
                ))
            }
        }
    }

    override fun onDestroyView() { super.onDestroyView(); _binding = null }
}