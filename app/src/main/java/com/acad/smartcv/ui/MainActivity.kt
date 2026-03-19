package com.acad.smartcv.ui

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.setupWithNavController
import com.acad.smartcv.R
import com.acad.smartcv.data.model.*
import com.acad.smartcv.databinding.ActivityMainBinding
import com.acad.smartcv.viewmodel.SmartCVViewModel
import com.acad.smartcv.viewmodel.SmartCVViewModelFactory

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    lateinit var viewModel: SmartCVViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        viewModel = ViewModelProvider(this, SmartCVViewModelFactory(application))
            .get(SmartCVViewModel::class.java)

        val navHost = supportFragmentManager
            .findFragmentById(R.id.nav_host_fragment) as NavHostFragment
        val navController = navHost.navController

        binding.bottomNav.setupWithNavController(navController)

        // Seed sample organisations on first run
        seedOrgsIfNeeded()
    }

    private fun seedOrgsIfNeeded() {
        val prefs = getSharedPreferences("acad_prefs", MODE_PRIVATE)
        if (prefs.getBoolean("orgs_seeded", false)) return

        viewModel.seedOrganisationsIfEmpty(listOf(
            Organisation(name="IIT Madras", type="Research University", field="Engineering & Sciences", location="Chennai, India", description="Premier engineering institute with active PhD, postdoc, and visiting faculty programs across all STEM domains.", website="https://www.iitm.ac.in"),
            Organisation(name="AIIMS New Delhi", type="Medical Institution", field="Medical & Health Sciences", location="New Delhi, India", description="Premier medical college seeking researchers in clinical, biomedical, and health sciences.", website="https://www.aiims.edu"),
            Organisation(name="DST – SERB", type="Funding Agency", field="Grants & Fellowships", location="New Delhi, India", description="Science & Engineering Research Board. Accepting NPDF, CRG, and SRG fellowship applications for early and mid-career researchers.", website="https://serb.gov.in"),
            Organisation(name="TIFR Mumbai", type="Research Institute", field="Fundamental Sciences", location="Mumbai, India", description="Tata Institute of Fundamental Research. Postdoctoral and faculty openings in physics, chemistry, biology, and mathematics.", website="https://www.tifr.res.in"),
            Organisation(name="Wellcome Trust / DBT India", type="Funding Body", field="Biomedical Research", location="India & UK", description="Indo-UK biomedical research funding — Intermediate and Senior Fellowships open.", website="https://www.wellcometrust.in"),
            Organisation(name="CSIR – CDRI", type="National Laboratory", field="Drug Discovery", location="Lucknow, India", description="Central Drug Research Institute. Research associate and scientist positions in medicinal chemistry, pharmacology, bioinformatics.", website="https://www.cdri.res.in"),
            Organisation(name="Infosys Foundation", type="Corporate / CSR", field="Education & Technology", location="Bengaluru, India", description="Education innovation grants and Humanities & Social Sciences award.", website="https://www.infosys.com/infosys-foundation"),
            Organisation(name="University of Edinburgh", type="International University", field="Global Academia", location="Edinburgh, UK", description="School of Medicine and Engineering actively seeking international research collaborators and visiting scholars.", website="https://www.ed.ac.uk"),
            Organisation(name="IISc Bengaluru", type="Research University", field="Science & Engineering", location="Bengaluru, India", description="Indian Institute of Science — top-ranked research university with openings for research associates and faculty.", website="https://www.iisc.ac.in"),
            Organisation(name="ICMR", type="Funding Agency", field="Medical Research", location="New Delhi, India", description="Indian Council of Medical Research — fellowships, project grants, and extramural research funding for health sciences.", website="https://www.icmr.gov.in"),
            Organisation(name="DRDO", type="Government R&D", field="Defence & Technology", location="Pan-India", description="Defence Research and Development Organisation. Scientist positions and collaborative project openings.", website="https://www.drdo.gov.in"),
            Organisation(name="Anna University", type="State University", field="Engineering & Technology", location="Chennai, India", description="Leading state university in Tamil Nadu. Faculty and research positions in all engineering and applied science departments.", website="https://www.annauniv.edu")
        ))
        prefs.edit().putBoolean("orgs_seeded", true).apply()
    }
}
