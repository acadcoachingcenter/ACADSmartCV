package com.acad.smartcv.ui.screens

import android.os.Bundle
import android.view.*
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import com.acad.smartcv.R
import com.acad.smartcv.databinding.FragmentHomeBinding
import com.acad.smartcv.viewmodel.SmartCVViewModel
import com.acad.smartcv.viewmodel.SmartCVViewModelFactory

class HomeFragment : Fragment() {

    private var _binding: FragmentHomeBinding? = null
    private val binding get() = _binding!!
    private val vm: SmartCVViewModel by activityViewModels {
        SmartCVViewModelFactory(requireActivity().application)
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentHomeBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Observe profile completeness
        vm.fullProfile.observe(viewLifecycleOwner) { fp ->
            if (fp == null) {
                binding.tvWelcome.text = "Welcome to ACAD SmartCV"
                binding.tvSubtitle.text = "Build your academic profile to get started"
                binding.cardStats.visibility = View.GONE
                binding.btnGetStarted.visibility = View.VISIBLE
            } else {
                val p = fp.profile
                binding.tvWelcome.text = "Hello, ${p.firstName}!"
                binding.tvSubtitle.text = p.title.ifBlank { "ACAD SmartCV" }
                binding.cardStats.visibility = View.VISIBLE
                binding.btnGetStarted.visibility = View.GONE

                // Stats
                binding.tvPublications.text = fp.publications.size.toString()
                binding.tvProjects.text = fp.projects.size.toString()
                binding.tvAwards.text = fp.awards.size.toString()
                binding.tvGrants.text = fp.grants.size.toString()

                // Profile completeness
                var score = 0
                if (p.firstName.isNotBlank()) score += 10
                if (p.bio.isNotBlank()) score += 10
                if (p.skills.isNotBlank()) score += 10
                if (fp.education.isNotEmpty()) score += 15
                if (fp.publications.isNotEmpty()) score += 20
                if (fp.projects.isNotEmpty()) score += 15
                if (fp.awards.isNotEmpty()) score += 10
                if (fp.grants.isNotEmpty()) score += 10
                binding.progressCompletion.progress = score
                binding.tvCompletionPct.text = "$score%"
            }
        }

        vm.submittedCount.observe(viewLifecycleOwner) { count ->
            binding.tvAppsSubmitted.text = count.toString()
        }

        binding.btnGetStarted.setOnClickListener {
            findNavController().navigate(R.id.action_home_to_profile)
        }

        binding.cardBuildProfile.setOnClickListener {
            findNavController().navigate(R.id.action_home_to_profile)
        }

        binding.cardApply.setOnClickListener {
            findNavController().navigate(R.id.action_home_to_apply)
        }
    }

    override fun onDestroyView() { super.onDestroyView(); _binding = null }
}
