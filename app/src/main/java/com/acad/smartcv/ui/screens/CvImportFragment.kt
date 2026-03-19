package com.acad.smartcv.ui.screens

import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import com.acad.smartcv.BuildConfig
import com.acad.smartcv.R
import com.acad.smartcv.data.model.*
import com.acad.smartcv.viewmodel.SmartCVViewModel
import com.acad.smartcv.viewmodel.SmartCVViewModelFactory
import com.google.android.material.button.MaterialButton
import com.google.android.material.textfield.TextInputEditText
import com.tom_roush.pdfbox.android.PDFBoxResourceLoader
import com.tom_roush.pdfbox.pdmodel.PDDocument
import com.tom_roush.pdfbox.text.PDFTextStripper
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONArray
import org.json.JSONObject
import java.io.InputStream
import java.util.concurrent.TimeUnit

class CvImportFragment : Fragment() {

    private val apiKey = BuildConfig.ANTHROPIC_API_KEY

    private val vm: SmartCVViewModel by activityViewModels {
        SmartCVViewModelFactory(requireActivity().application)
    }

    private lateinit var etName:      TextInputEditText
    private lateinit var etEmail:     TextInputEditText
    private lateinit var etPhone:     TextInputEditText
    private lateinit var etTitle:     TextInputEditText
    private lateinit var etOrg:       TextInputEditText
    private lateinit var etSkills:    TextInputEditText
    private lateinit var etBio:       TextInputEditText
    private lateinit var btnPickFile: MaterialButton
    private lateinit var btnSave:     MaterialButton

    private val httpClient = OkHttpClient.Builder()
        .connectTimeout(30, TimeUnit.SECONDS)
        .readTimeout(60, TimeUnit.SECONDS)
        .build()

    private val filePicker = registerForActivityResult(
        ActivityResultContracts.GetContent()
    ) { uri -> uri?.let { parseFile(it) } }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? = inflater.inflate(R.layout.fragment_cv_import, container, false)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        PDFBoxResourceLoader.init(requireContext().applicationContext)

        etName      = view.findViewById(R.id.etImportName)
        etEmail     = view.findViewById(R.id.etImportEmail)
        etPhone     = view.findViewById(R.id.etImportPhone)
        etTitle     = view.findViewById(R.id.etImportTitle)
        etOrg       = view.findViewById(R.id.etImportOrg)
        etSkills    = view.findViewById(R.id.etImportSkills)
        etBio       = view.findViewById(R.id.etImportBio)
        btnPickFile = view.findViewById(R.id.btnPickCvFile)
        btnSave     = view.findViewById(R.id.btnSaveImported)

        btnPickFile.setOnClickListener { filePicker.launch("*/*") }
        btnSave.setOnClickListener    { saveToProfile() }
    }

    // ── File handling ──────────────────────────────────────────────────────

    private fun parseFile(uri: Uri) {
        Toast.makeText(requireContext(), "Extracting CV text...", Toast.LENGTH_SHORT).show()
        CoroutineScope(Dispatchers.IO).launch {
            var text = ""
            try {
                text = extractText(uri)
                if (text.isBlank()) {
                    showToast("Could not extract text from file.")
                    return@launch
                }
                showToast("Sending to Gemini AI for parsing...")
                val json = callGeminiApi(text)
                withContext(Dispatchers.Main) {
                    populateAllSections(json)
                    Toast.makeText(
                        requireContext(),
                        "CV parsed! Review and save.",
                        Toast.LENGTH_LONG
                    ).show()
                }
            } catch (e: Exception) {
                if (e.message?.contains("429") == true) {
                    showToast("Rate limit hit. Retrying in 30 seconds...")
                    kotlinx.coroutines.delay(30000)
                    try {
                        val json = callGeminiApi(text)
                        withContext(Dispatchers.Main) {
                            populateAllSections(json)
                            showToast("CV parsed! Review and save.")
                        }
                    } catch (e2: Exception) {
                        showToast("Error after retry: ${e2.message}")
                    }
                } else {
                    showToast("Error: ${e.message}")
                }
            }
        }
    }

    private fun extractText(uri: Uri): String {
        val mimeType = requireContext().contentResolver.getType(uri) ?: ""
        val stream: InputStream =
            requireContext().contentResolver.openInputStream(uri) ?: return ""
        return if (mimeType.contains("pdf") || uri.toString().endsWith(".pdf", ignoreCase = true)) {
            val doc  = PDDocument.load(stream)
            val text = PDFTextStripper().getText(doc)
            doc.close()
            text
        } else {
            stream.bufferedReader().readText()
        }
    }

    // ── Gemini API call ────────────────────────────────────────────────────

    private fun callGeminiApi(cvText: String): JSONObject {
        val prompt = """
            You are a CV parser. Extract all information from the following CV text and return it
            as a single JSON object with exactly these keys. Return ONLY the JSON, no other text,
            no markdown, no backticks.

            {
              "profile": {
                "firstName": "", "lastName": "", "email": "", "phone": "",
                "title": "", "organization": "", "location": "", "bio": "",
                "skills": "",
                "linkedIn": "", "website": "", "orcidId": ""
              },
              "education": [
                { "degree": "", "institution": "", "year": "", "field": "", "grade": "" }
              ],
              "projects": [
                { "title": "", "role": "", "duration": "", "description": "",
                  "tags": "", "fundingAgency": "", "fundingAmount": "",
                  "outcomes": "", "githubUrl": "", "projectUrl": "" }
              ],
              "publications": [
                { "title": "", "authors": "", "journal": "", "year": "",
                  "type": "JOURNAL", "doi": "", "url": "",
                  "impactFactor": "", "citations": "", "abstract_text": "" }
              ],
              "awards": [
                { "name": "", "awardingBody": "", "year": "", "description": "", "category": "" }
              ],
              "grants": [
                { "title": "", "agency": "", "amount": "", "period": "",
                  "role": "", "status": "Completed" }
              ],
              "achievements": [
                { "title": "", "category": "OTHER", "description": "", "year": "" }
              ]
            }

            Rules:
            - skills must be a comma-separated string
            - publication type must be one of: JOURNAL, CONFERENCE, BOOK_CHAPTER, BOOK, PREPRINT, PATENT, THESIS, REPORT
            - achievement category must be one of: INVITED_TALK, PATENT, MEDIA_FEATURE, LEADERSHIP, MENTORSHIP, OPEN_SOURCE, EDITORIAL_BOARD, PEER_REVIEW, CONSULTING, OTHER
            - If a field has no data, leave it as empty string
            - Return empty arrays [] if no items found for a section

            CV TEXT:
            $cvText
        """.trimIndent()

        // Build Gemini request body
        val requestBody = JSONObject().apply {
            put("contents", JSONArray().apply {
                put(JSONObject().apply {
                    put("parts", JSONArray().apply {
                        put(JSONObject().apply {
                            put("text", prompt)
                        })
                    })
                })
            })
            put("generationConfig", JSONObject().apply {
                put("temperature", 0.1)
                put("maxOutputTokens", 4096)
            })
        }.toString().toRequestBody("application/json".toMediaType())

        val request = Request.Builder()
	    .url("https://generativelanguage.googleapis.com/v1beta/models/gemini-2.0-flash:generateContent?key=$apiKey")
            .addHeader("content-type", "application/json")
            .post(requestBody)
            .build()

        val response = httpClient.newCall(request).execute()
        val body     = response.body?.string()
            ?: throw Exception("Empty response from Gemini API")

        if (!response.isSuccessful)
            throw Exception("API error ${response.code}: $body")

        // Extract text from Gemini response envelope
        val rawText = JSONObject(body)
            .getJSONArray("candidates")
            .getJSONObject(0)
            .getJSONObject("content")
            .getJSONArray("parts")
            .getJSONObject(0)
            .getString("text")
            .trim()
            .removePrefix("```json")
            .removePrefix("```")
            .removeSuffix("```")
            .trim()

        return JSONObject(rawText)
    }

    // ── Populate UI + save all sections ───────────────────────────────────

    private fun populateAllSections(json: JSONObject) {
        val profileId = vm.activeProfileId.value.takeIf { it > 0 } ?: 0L

        // Profile fields → fill the visible edit texts
        val p         = json.optJSONObject("profile") ?: JSONObject()
        val firstName = p.optString("firstName")
        val lastName  = p.optString("lastName")
        etName.setText("$firstName $lastName".trim())
        etEmail.setText(p.optString("email"))
        etPhone.setText(p.optString("phone"))
        etTitle.setText(p.optString("title"))
        etOrg.setText(p.optString("organization"))
        etSkills.setText(p.optString("skills"))
        etBio.setText(p.optString("bio"))

        // Education
        json.optJSONArray("education")?.let { arr ->
            repeat(arr.length()) { i ->
                val e = arr.getJSONObject(i)
                if (e.optString("degree").isBlank()) return@repeat
                vm.addEducation(Education(
                    profileId   = profileId,
                    degree      = e.optString("degree"),
                    institution = e.optString("institution"),
                    year        = e.optString("year"),
                    field       = e.optString("field"),
                    grade       = e.optString("grade")
                ))
            }
        }

        // Projects
        json.optJSONArray("projects")?.let { arr ->
            repeat(arr.length()) { i ->
                val pr = arr.getJSONObject(i)
                if (pr.optString("title").isBlank()) return@repeat
                vm.addProject(Project(
                    profileId     = profileId,
                    title         = pr.optString("title"),
                    role          = pr.optString("role"),
                    duration      = pr.optString("duration"),
                    description   = pr.optString("description"),
                    tags          = pr.optString("tags"),
                    fundingAgency = pr.optString("fundingAgency"),
                    fundingAmount = pr.optString("fundingAmount"),
                    outcomes      = pr.optString("outcomes"),
                    githubUrl     = pr.optString("githubUrl"),
                    projectUrl    = pr.optString("projectUrl")
                ))
            }
        }

        // Publications
        json.optJSONArray("publications")?.let { arr ->
            repeat(arr.length()) { i ->
                val pub = arr.getJSONObject(i)
                if (pub.optString("title").isBlank()) return@repeat
                val pubType = runCatching {
                    PublicationType.valueOf(pub.optString("type", "JOURNAL"))
                }.getOrDefault(PublicationType.JOURNAL)
                vm.addPublication(Publication(
                    profileId     = profileId,
                    title         = pub.optString("title"),
                    authors       = pub.optString("authors"),
                    journal       = pub.optString("journal"),
                    year          = pub.optString("year"),
                    type          = pubType,
                    doi           = pub.optString("doi"),
                    url           = pub.optString("url"),
                    impactFactor  = pub.optString("impactFactor"),
                    citations     = pub.optString("citations"),
                    abstract_text = pub.optString("abstract_text")
                ))
            }
        }

        // Awards
        json.optJSONArray("awards")?.let { arr ->
            repeat(arr.length()) { i ->
                val a = arr.getJSONObject(i)
                if (a.optString("name").isBlank()) return@repeat
                vm.addAward(Award(
                    profileId    = profileId,
                    name         = a.optString("name"),
                    awardingBody = a.optString("awardingBody"),
                    year         = a.optString("year"),
                    description  = a.optString("description"),
                    category     = a.optString("category")
                ))
            }
        }

        // Grants
        json.optJSONArray("grants")?.let { arr ->
            repeat(arr.length()) { i ->
                val g = arr.getJSONObject(i)
                if (g.optString("title").isBlank()) return@repeat
                vm.addGrant(Grant(
                    profileId = profileId,
                    title     = g.optString("title"),
                    agency    = g.optString("agency"),
                    amount    = g.optString("amount"),
                    period    = g.optString("period"),
                    role      = g.optString("role"),
                    status    = g.optString("status").ifBlank { "Completed" }
                ))
            }
        }

        // Achievements
        json.optJSONArray("achievements")?.let { arr ->
            repeat(arr.length()) { i ->
                val ac = arr.getJSONObject(i)
                if (ac.optString("title").isBlank()) return@repeat
                val acCat = runCatching {
                    AchievementCategory.valueOf(ac.optString("category", "OTHER"))
                }.getOrDefault(AchievementCategory.OTHER)
                vm.addAchievement(Achievement(
                    profileId   = profileId,
                    title       = ac.optString("title"),
                    category    = acCat,
                    description = ac.optString("description"),
                    year        = ac.optString("year")
                ))
            }
        }
    }

    // ── Save profile from edit texts ───────────────────────────────────────

    private fun saveToProfile() {
        val fullName  = etName.text.toString().trim()
        val parts     = fullName.split(" ")
        val firstName = parts.firstOrNull() ?: ""
        val lastName  = parts.drop(1).joinToString(" ")
        val profileId = vm.activeProfileId.value.takeIf { it > 0 } ?: 0L

        vm.saveProfile(
            Profile(
                id           = profileId,
                firstName    = firstName,
                lastName     = lastName,
                email        = etEmail.text.toString().trim(),
                phone        = etPhone.text.toString().trim(),
                title        = etTitle.text.toString().trim(),
                organization = etOrg.text.toString().trim(),
                skills       = etSkills.text.toString().trim(),
                bio          = etBio.text.toString().trim(),
                updatedAt    = System.currentTimeMillis()
            )
        ) {
            Toast.makeText(requireContext(), "Profile saved!", Toast.LENGTH_SHORT).show()
            findNavController().navigateUp()
        }
    }

    // ── Helpers ────────────────────────────────────────────────────────────

    private suspend fun showToast(msg: String) = withContext(Dispatchers.Main) {
        Toast.makeText(requireContext(), msg, Toast.LENGTH_LONG).show()
    }
}