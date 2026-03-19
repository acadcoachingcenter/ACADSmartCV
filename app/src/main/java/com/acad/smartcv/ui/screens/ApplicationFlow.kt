package com.acad.smartcv.ui.screens

import android.os.Bundle
import android.view.*
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import com.acad.smartcv.data.model.Application
import com.acad.smartcv.data.model.ApplicationStatus
import com.acad.smartcv.databinding.FragmentApplyFormBinding
import com.acad.smartcv.databinding.FragmentPreviewBinding
import com.acad.smartcv.databinding.FragmentOrgDetailBinding
import com.acad.smartcv.viewmodel.SmartCVViewModel
import com.acad.smartcv.viewmodel.SmartCVViewModelFactory
import com.google.android.material.chip.Chip

// ── Apply Form ─────────────────────────────────────────────────────────────
class ApplyFormFragment : Fragment() {
    private var _b: FragmentApplyFormBinding? = null
    private val b get() = _b!!
    private val vm: SmartCVViewModel by activityViewModels { SmartCVViewModelFactory(requireActivity().application) }
    private val args: ApplyFormFragmentArgs by navArgs()
    private val sectionKeys = listOf("projects","publications","grants","awards","education","achievements")
    private val selectedSections = sectionKeys.toMutableSet()

    override fun onCreateView(i: LayoutInflater, c: ViewGroup?, s: Bundle?) =
        FragmentApplyFormBinding.inflate(i, c, false).also { _b = it }.root

    override fun onViewCreated(v: View, s: Bundle?) {
        // Bind org info
        vm.organisations.observe(viewLifecycleOwner) { orgs ->
            orgs.find { it.id == args.orgId }?.let { org ->
                b.tvOrgName.text = org.name
                b.tvOrgType.text = org.type
            }
        }

        // Section toggle chips
        sectionKeys.forEach { key ->
            val chip = Chip(requireContext()).apply {
                text = key.replaceFirstChar { it.uppercase() }
                isCheckable = true
                isChecked = true
                setOnCheckedChangeListener { _, checked ->
                    if (checked) selectedSections.add(key) else selectedSections.remove(key)
                }
            }
            b.chipGroupSections.addView(chip)
        }

        // Save draft
        b.btnSaveDraft.setOnClickListener {
            val app = buildApplication(ApplicationStatus.DRAFT)
            vm.saveDraft(app)
            Toast.makeText(requireContext(), "Draft saved", Toast.LENGTH_SHORT).show()
        }

        // Preview
        b.btnPreview.setOnClickListener {
            val action = ApplyFormFragmentDirections.actionApplyFormToPreview(
                orgId = args.orgId,
                position = b.etPosition.text.toString().trim(),
                coverLetter = b.etCoverLetter.text.toString().trim(),
                sections = selectedSections.joinToString(",")
            )
            findNavController().navigate(action)
        }
    }

    private fun buildApplication(status: ApplicationStatus) = Application(
        profileId = vm.activeProfileId.value.takeIf { it > 0 } ?: 0L,
        organisationId = args.orgId,
        position = b.etPosition.text.toString().trim(),
        coverLetter = b.etCoverLetter.text.toString().trim(),
        sectionsIncluded = selectedSections.joinToString(","),
        status = status
    )

    override fun onDestroyView() { super.onDestroyView(); _b = null }
}

// ── Preview & Submit ───────────────────────────────────────────────────────
class PreviewFragment : Fragment() {
    private var _b: FragmentPreviewBinding? = null
    private val b get() = _b!!
    private val vm: SmartCVViewModel by activityViewModels { SmartCVViewModelFactory(requireActivity().application) }
    private val args: PreviewFragmentArgs by navArgs()

    override fun onCreateView(i: LayoutInflater, c: ViewGroup?, s: Bundle?) =
        FragmentPreviewBinding.inflate(i, c, false).also { _b = it }.root

    override fun onViewCreated(v: View, s: Bundle?) {
        val sections = args.sections.split(",").filter { it.isNotBlank() }

        // Show profile preview
        vm.fullProfile.observe(viewLifecycleOwner) { fp ->
            fp ?: return@observe
            val p = fp.profile
            b.tvPreviewName.text = "${p.firstName} ${p.lastName}"
            b.tvPreviewTitle.text = p.title
            b.tvPreviewOrg.text = p.organization
            b.tvPreviewEmail.text = p.email
            b.tvPreviewBio.text = p.bio
            b.tvPreviewSkills.text = p.skills

            // Dynamically show/hide sections
            b.sectionProjects.visibility = if ("projects" in sections && fp.projects.isNotEmpty()) View.VISIBLE else View.GONE
            b.tvProjectCount.text = "${fp.projects.size} projects"
            b.sectionPublications.visibility = if ("publications" in sections && fp.publications.isNotEmpty()) View.VISIBLE else View.GONE
            b.tvPubCount.text = "${fp.publications.size} publications"
            b.sectionGrants.visibility = if ("grants" in sections && fp.grants.isNotEmpty()) View.VISIBLE else View.GONE
            b.sectionAwards.visibility = if ("awards" in sections && fp.awards.isNotEmpty()) View.VISIBLE else View.GONE
            b.sectionEducation.visibility = if ("education" in sections && fp.education.isNotEmpty()) View.VISIBLE else View.GONE
        }

        vm.organisations.observe(viewLifecycleOwner) { orgs ->
            orgs.find { it.id == args.orgId }?.let { org ->
                b.tvApplyingTo.text = "Applying to: ${org.name}"
            }
        }

        b.tvCoverLetter.text = args.coverLetter.ifBlank { "(No cover letter)" }
        b.tvPosition.text = args.position.ifBlank { "(No position specified)" }

        b.btnSubmit.setOnClickListener {
            val app = Application(
                profileId = vm.activeProfileId.value.takeIf { it > 0 } ?: 0L,
                organisationId = args.orgId,
                position = args.position,
                coverLetter = args.coverLetter,
                sectionsIncluded = args.sections,
                status = ApplicationStatus.SUBMITTED,
                submittedAt = System.currentTimeMillis()
            )
            vm.submitApplication(app) {
                Toast.makeText(requireContext(), "Application submitted!", Toast.LENGTH_LONG).show()
                findNavController().navigate(com.acad.smartcv.R.id.applicationsFragment)
            }
        }
    }

    override fun onDestroyView() { super.onDestroyView(); _b = null }
}

// ── Org Detail ─────────────────────────────────────────────────────────────
class OrgDetailFragment : Fragment() {
    private var _b: FragmentOrgDetailBinding? = null
    private val b get() = _b!!
    private val vm: SmartCVViewModel by activityViewModels { SmartCVViewModelFactory(requireActivity().application) }
    private val args: OrgDetailFragmentArgs by navArgs()

    override fun onCreateView(i: LayoutInflater, c: ViewGroup?, s: Bundle?) =
        FragmentOrgDetailBinding.inflate(i, c, false).also { _b = it }.root

    override fun onViewCreated(v: View, s: Bundle?) {
        vm.organisations.observe(viewLifecycleOwner) { orgs ->
            orgs.find { it.id == args.orgId }?.let { org ->
                b.tvName.text = org.name
                b.tvType.text = org.type
                b.tvField.text = org.field
                b.tvLocation.text = org.location
                b.tvDescription.text = org.description
                if (org.website.isNotBlank()) {
                    b.btnWebsite.visibility = View.VISIBLE
                    b.btnWebsite.setOnClickListener {
                        val intent = android.content.Intent(android.content.Intent.ACTION_VIEW,
                            android.net.Uri.parse(org.website))
                        startActivity(intent)
                    }
                }
                b.btnApply.setOnClickListener {
                    val action = OrgDetailFragmentDirections.actionOrgDetailToApplyForm(org.id)
                    findNavController().navigate(action)
                }
            }
        }
    }

    override fun onDestroyView() { super.onDestroyView(); _b = null }
}
