package com.acad.smartcv.ui.screens

import android.os.Bundle
import android.view.*
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import com.acad.smartcv.databinding.*
import com.acad.smartcv.data.model.*
import com.acad.smartcv.viewmodel.SmartCVViewModel
import com.acad.smartcv.viewmodel.SmartCVViewModelFactory

// ── Add Project ────────────────────────────────────────────────────────────
class AddProjectFragment : Fragment() {
    private var _b: FragmentAddProjectBinding? = null
    private val b get() = _b!!
    private val vm: SmartCVViewModel by activityViewModels { SmartCVViewModelFactory(requireActivity().application) }

    override fun onCreateView(i: LayoutInflater, c: ViewGroup?, s: Bundle?) =
        FragmentAddProjectBinding.inflate(i, c, false).also { _b = it }.root

    override fun onViewCreated(v: View, s: Bundle?) {
        b.btnSave.setOnClickListener {
            val profileId = vm.activeProfileId.value.takeIf { it > 0 }
                ?: run { Toast.makeText(requireContext(), "Save your profile first", Toast.LENGTH_SHORT).show(); return@setOnClickListener }
            if (b.etTitle.text.isNullOrBlank()) { b.etTitle.error = "Required"; return@setOnClickListener }
            vm.addProject(Project(
                profileId = profileId,
                title = b.etTitle.text.toString().trim(),
                role = b.etRole.text.toString().trim(),
                duration = b.etDuration.text.toString().trim(),
                description = b.etDescription.text.toString().trim(),
                tags = b.etTags.text.toString().trim(),
                fundingAgency = b.etFundingAgency.text.toString().trim(),
                fundingAmount = b.etFundingAmount.text.toString().trim(),
                outcomes = b.etOutcomes.text.toString().trim(),
                githubUrl = b.etGithub.text.toString().trim(),
                projectUrl = b.etUrl.text.toString().trim()
            ))
            findNavController().navigateUp()
        }
        b.btnCancel.setOnClickListener { findNavController().navigateUp() }
    }
    override fun onDestroyView() { super.onDestroyView(); _b = null }
}

// ── Add Publication ────────────────────────────────────────────────────────
class AddPublicationFragment : Fragment() {
    private var _b: FragmentAddPublicationBinding? = null
    private val b get() = _b!!
    private val vm: SmartCVViewModel by activityViewModels { SmartCVViewModelFactory(requireActivity().application) }

    override fun onCreateView(i: LayoutInflater, c: ViewGroup?, s: Bundle?) =
        FragmentAddPublicationBinding.inflate(i, c, false).also { _b = it }.root

    override fun onViewCreated(v: View, s: Bundle?) {
        val types = PublicationType.values().map { it.name.replace('_', ' ') }
        b.spinnerType.adapter = ArrayAdapter(requireContext(), android.R.layout.simple_spinner_item, types)
            .also { it.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item) }

        b.btnSave.setOnClickListener {
            val profileId = vm.activeProfileId.value.takeIf { it > 0 }
                ?: run { Toast.makeText(requireContext(), "Save your profile first", Toast.LENGTH_SHORT).show(); return@setOnClickListener }
            if (b.etTitle.text.isNullOrBlank()) { b.etTitle.error = "Required"; return@setOnClickListener }
            vm.addPublication(Publication(
                profileId = profileId,
                title = b.etTitle.text.toString().trim(),
                authors = b.etAuthors.text.toString().trim(),
                journal = b.etJournal.text.toString().trim(),
                year = b.etYear.text.toString().trim(),
                type = PublicationType.values()[b.spinnerType.selectedItemPosition],
                doi = b.etDoi.text.toString().trim(),
                url = b.etUrl.text.toString().trim(),
                impactFactor = b.etImpactFactor.text.toString().trim(),
                citations = b.etCitations.text.toString().trim(),
                abstract_text = b.etAbstract.text.toString().trim()
            ))
            findNavController().navigateUp()
        }
        b.btnCancel.setOnClickListener { findNavController().navigateUp() }
    }
    override fun onDestroyView() { super.onDestroyView(); _b = null }
}

// ── Add Award ──────────────────────────────────────────────────────────────
class AddAwardFragment : Fragment() {
    private var _b: FragmentAddAwardBinding? = null
    private val b get() = _b!!
    private val vm: SmartCVViewModel by activityViewModels { SmartCVViewModelFactory(requireActivity().application) }

    override fun onCreateView(i: LayoutInflater, c: ViewGroup?, s: Bundle?) =
        FragmentAddAwardBinding.inflate(i, c, false).also { _b = it }.root

    override fun onViewCreated(v: View, s: Bundle?) {
        b.btnSave.setOnClickListener {
            val profileId = vm.activeProfileId.value.takeIf { it > 0 }
                ?: run { Toast.makeText(requireContext(), "Save your profile first", Toast.LENGTH_SHORT).show(); return@setOnClickListener }
            if (b.etName.text.isNullOrBlank()) { b.etName.error = "Required"; return@setOnClickListener }
            vm.addAward(Award(
                profileId = profileId,
                name = b.etName.text.toString().trim(),
                awardingBody = b.etBody.text.toString().trim(),
                year = b.etYear.text.toString().trim(),
                description = b.etDescription.text.toString().trim(),
                category = b.etCategory.text.toString().trim()
            ))
            findNavController().navigateUp()
        }
        b.btnCancel.setOnClickListener { findNavController().navigateUp() }
    }
    override fun onDestroyView() { super.onDestroyView(); _b = null }
}

// ── Add Grant ──────────────────────────────────────────────────────────────
class AddGrantFragment : Fragment() {
    private var _b: FragmentAddGrantBinding? = null
    private val b get() = _b!!
    private val vm: SmartCVViewModel by activityViewModels { SmartCVViewModelFactory(requireActivity().application) }

    override fun onCreateView(i: LayoutInflater, c: ViewGroup?, s: Bundle?) =
        FragmentAddGrantBinding.inflate(i, c, false).also { _b = it }.root

    override fun onViewCreated(v: View, s: Bundle?) {
        b.btnSave.setOnClickListener {
            val profileId = vm.activeProfileId.value.takeIf { it > 0 }
                ?: run { Toast.makeText(requireContext(), "Save your profile first", Toast.LENGTH_SHORT).show(); return@setOnClickListener }
            if (b.etTitle.text.isNullOrBlank()) { b.etTitle.error = "Required"; return@setOnClickListener }
            vm.addGrant(Grant(
                profileId = profileId,
                title = b.etTitle.text.toString().trim(),
                agency = b.etAgency.text.toString().trim(),
                amount = b.etAmount.text.toString().trim(),
                period = b.etPeriod.text.toString().trim(),
                role = b.etRole.text.toString().trim(),
                status = b.etStatus.text.toString().trim()
            ))
            findNavController().navigateUp()
        }
        b.btnCancel.setOnClickListener { findNavController().navigateUp() }
    }
    override fun onDestroyView() { super.onDestroyView(); _b = null }
}

// ── Add Education ──────────────────────────────────────────────────────────
class AddEducationFragment : Fragment() {
    private var _b: FragmentAddEducationBinding? = null
    private val b get() = _b!!
    private val vm: SmartCVViewModel by activityViewModels { SmartCVViewModelFactory(requireActivity().application) }

    override fun onCreateView(i: LayoutInflater, c: ViewGroup?, s: Bundle?) =
        FragmentAddEducationBinding.inflate(i, c, false).also { _b = it }.root

    override fun onViewCreated(v: View, s: Bundle?) {
        b.btnSave.setOnClickListener {
            val profileId = vm.activeProfileId.value.takeIf { it > 0 }
                ?: run { Toast.makeText(requireContext(), "Save your profile first", Toast.LENGTH_SHORT).show(); return@setOnClickListener }
            if (b.etDegree.text.isNullOrBlank()) { b.etDegree.error = "Required"; return@setOnClickListener }
            vm.addEducation(Education(
                profileId = profileId,
                degree = b.etDegree.text.toString().trim(),
                institution = b.etInstitution.text.toString().trim(),
                year = b.etYear.text.toString().trim(),
                field = b.etField.text.toString().trim(),
                grade = b.etGrade.text.toString().trim()
            ))
            findNavController().navigateUp()
        }
        b.btnCancel.setOnClickListener { findNavController().navigateUp() }
    }
    override fun onDestroyView() { super.onDestroyView(); _b = null }
}

// ── Applications List ──────────────────────────────────────────────────────
class ApplicationsFragment : Fragment() {
    private var _b: FragmentApplicationsBinding? = null
    private val b get() = _b!!
    private val vm: SmartCVViewModel by activityViewModels { SmartCVViewModelFactory(requireActivity().application) }

    override fun onCreateView(i: LayoutInflater, c: ViewGroup?, s: Bundle?) =
        FragmentApplicationsBinding.inflate(i, c, false).also { _b = it }.root

    override fun onViewCreated(v: View, s: Bundle?) {
        vm.applications.observe(viewLifecycleOwner) { list ->
            b.tvCount.text = "${list.size} applications"
            b.tvEmpty.visibility = if (list.isEmpty()) View.VISIBLE else View.GONE
            // Adapter wired in production implementation
        }
    }
    override fun onDestroyView() { super.onDestroyView(); _b = null }
}
