package com.acad.smartcv.ui.screens

import android.os.Bundle
import android.view.*
import androidx.core.widget.addTextChangedListener
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.acad.smartcv.R
import com.acad.smartcv.databinding.FragmentApplyBinding
import com.acad.smartcv.data.model.Organisation
import com.acad.smartcv.ui.components.OrgAdapter
import com.acad.smartcv.viewmodel.SmartCVViewModel
import com.acad.smartcv.viewmodel.SmartCVViewModelFactory

class ApplyFragment : Fragment() {
    private var _binding: FragmentApplyBinding? = null
    private val binding get() = _binding!!
    private val vm: SmartCVViewModel by activityViewModels {
        SmartCVViewModelFactory(requireActivity().application)
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, s: Bundle?): View {
        _binding = FragmentApplyBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val adapter = OrgAdapter(
            onApply = { org -> navigateToOrgDetail(org) },
            onBookmark = { org, bookmarked -> vm.toggleBookmark(org.id, bookmarked) }
        )

        binding.rvOrganisations.layoutManager = LinearLayoutManager(requireContext())
        binding.rvOrganisations.adapter = adapter

        vm.organisations.observe(viewLifecycleOwner) { list ->
            adapter.submitList(list)
            binding.tvOrgCount.text = "${list.size} organisations"
            binding.tvEmpty.visibility = if (list.isEmpty()) View.VISIBLE else View.GONE
        }

        binding.etSearch.addTextChangedListener { vm.searchOrgs(it.toString()) }

        // Filter chips
        binding.chipAll.setOnClickListener { vm.searchOrgs("") }
        binding.chipUniversity.setOnClickListener { vm.searchOrgs("university") }
        binding.chipFunding.setOnClickListener { vm.searchOrgs("fund") }
        binding.chipMedical.setOnClickListener { vm.searchOrgs("medical") }
        binding.chipInternational.setOnClickListener { vm.searchOrgs("international") }
    }

    private fun navigateToOrgDetail(org: Organisation) {
        val action = ApplyFragmentDirections.actionApplyToOrgDetail(org.id)
        findNavController().navigate(action)
    }

    override fun onDestroyView() { super.onDestroyView(); _binding = null }
}
