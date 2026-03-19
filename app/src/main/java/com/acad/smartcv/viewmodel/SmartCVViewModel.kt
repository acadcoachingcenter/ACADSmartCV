package com.acad.smartcv.viewmodel

import android.app.Application
import androidx.lifecycle.*
import com.acad.smartcv.data.model.*
import com.acad.smartcv.data.model.Application as CVApplication
import com.acad.smartcv.data.repository.SmartCVRepository
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

class SmartCVViewModel(application: Application) : AndroidViewModel(application) {

    private val repo = SmartCVRepository.getInstance(application)

    // ── Active profile ID ──────────────────────────────────────────────────
    private val _activeProfileId = MutableStateFlow<Long>(-1L)
    val activeProfileId: StateFlow<Long> = _activeProfileId

    fun setActiveProfile(id: Long) { _activeProfileId.value = id }

    // ── Profile list ───────────────────────────────────────────────────────
    val profiles = repo.allProfiles.asLiveData()

    // ── Full profile (reactive) ────────────────────────────────────────────
    val fullProfile: LiveData<FullProfile?> = _activeProfileId
        .flatMapLatest { id -> if(id < 0) flowOf(null) else repo.getFullProfile(id) }
        .asLiveData()

    // ── Section collections ────────────────────────────────────────────────
    val projects: LiveData<List<Project>> = _activeProfileId
        .flatMapLatest { if(it < 0) flowOf(emptyList()) else repo.projects(it) }.asLiveData()

    val publications: LiveData<List<Publication>> = _activeProfileId
        .flatMapLatest { if(it < 0) flowOf(emptyList()) else repo.publications(it) }.asLiveData()

    val awards: LiveData<List<Award>> = _activeProfileId
        .flatMapLatest { if(it < 0) flowOf(emptyList()) else repo.awards(it) }.asLiveData()

    val grants: LiveData<List<Grant>> = _activeProfileId
        .flatMapLatest { if(it < 0) flowOf(emptyList()) else repo.grants(it) }.asLiveData()

    val education: LiveData<List<Education>> = _activeProfileId
        .flatMapLatest { if(it < 0) flowOf(emptyList()) else repo.education(it) }.asLiveData()

    val achievements: LiveData<List<Achievement>> = _activeProfileId
        .flatMapLatest { if(it < 0) flowOf(emptyList()) else repo.achievements(it) }.asLiveData()

    // ── Applications ───────────────────────────────────────────────────────
    val applications: LiveData<List<CVApplication>> = _activeProfileId
        .flatMapLatest { if(it < 0) flowOf(emptyList()) else repo.applications(it) }.asLiveData()

    val submittedCount: LiveData<Int> = _activeProfileId
        .flatMapLatest { if(it < 0) flowOf(0) else repo.submittedCount(it) }.asLiveData()

    // ── Organisations ──────────────────────────────────────────────────────
    private val _orgQuery = MutableStateFlow("")
    val organisations = _orgQuery
        .debounce(250)
        .flatMapLatest { q -> if(q.isBlank()) repo.allOrgs else repo.searchOrgs(q) }
        .asLiveData()
    val bookmarkedOrgs = repo.bookmarkedOrgs.asLiveData()

    fun searchOrgs(q: String) { _orgQuery.value = q }
    fun toggleBookmark(id: Long, bookmarked: Boolean) = viewModelScope.launch {
        repo.toggleBookmark(id, bookmarked)
    }

    // ── CRUD operations ────────────────────────────────────────────────────
    fun saveProfile(p: Profile, onDone: (Long) -> Unit = {}) = viewModelScope.launch {
        val id = repo.saveProfile(p)
        _activeProfileId.value = id
        onDone(id)
    }

    fun addProject(p: Project) = viewModelScope.launch { repo.addProject(p) }
    fun deleteProject(p: Project) = viewModelScope.launch { repo.deleteProject(p) }

    fun addPublication(p: Publication) = viewModelScope.launch { repo.addPublication(p) }
    fun deletePublication(p: Publication) = viewModelScope.launch { repo.deletePublication(p) }

    fun addAward(a: Award) = viewModelScope.launch { repo.addAward(a) }
    fun deleteAward(a: Award) = viewModelScope.launch { repo.deleteAward(a) }

    fun addGrant(g: Grant) = viewModelScope.launch { repo.addGrant(g) }
    fun deleteGrant(g: Grant) = viewModelScope.launch { repo.deleteGrant(g) }

    fun addEducation(e: Education) = viewModelScope.launch { repo.addEducation(e) }
    fun deleteEducation(e: Education) = viewModelScope.launch { repo.deleteEducation(e) }

    fun addAchievement(a: Achievement) = viewModelScope.launch { repo.addAchievement(a) }
    fun deleteAchievement(a: Achievement) = viewModelScope.launch { repo.deleteAchievement(a) }

    fun submitApplication(app: CVApplication, onDone: () -> Unit = {}) = viewModelScope.launch {
        repo.submitApplication(app)
        onDone()
    }
    fun saveDraft(app: CVApplication) = viewModelScope.launch { repo.saveDraft(app) }
    fun deleteApplication(app: CVApplication) = viewModelScope.launch { repo.deleteApplication(app) }

    // ── Seed organisations ─────────────────────────────────────────────────
    fun seedOrganisationsIfEmpty(orgs: List<Organisation>) = viewModelScope.launch {
        repo.seedOrganisations(orgs)
    }
}

class SmartCVViewModelFactory(private val application: Application) :
    ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(SmartCVViewModel::class.java))
            @Suppress("UNCHECKED_CAST") return SmartCVViewModel(application) as T
        throw IllegalArgumentException("Unknown ViewModel")
    }
}