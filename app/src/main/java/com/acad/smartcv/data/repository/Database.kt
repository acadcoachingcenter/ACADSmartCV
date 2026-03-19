package com.acad.smartcv.data.repository

import android.content.Context
import androidx.room.*
import com.acad.smartcv.data.model.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

// ── Room Database ──────────────────────────────────────────────────────────
@Database(
    entities = [Profile::class, Project::class, Publication::class,
                Award::class, Grant::class, Education::class,
                Achievement::class, Organisation::class, Application::class],
    version = 1,
    exportSchema = true
)
@TypeConverters(Converters::class)
abstract class SmartCVDatabase : RoomDatabase() {
    abstract fun profileDao(): ProfileDao
    abstract fun projectDao(): ProjectDao
    abstract fun publicationDao(): PublicationDao
    abstract fun awardDao(): AwardDao
    abstract fun grantDao(): GrantDao
    abstract fun educationDao(): EducationDao
    abstract fun achievementDao(): AchievementDao
    abstract fun organisationDao(): OrganisationDao
    abstract fun applicationDao(): ApplicationDao

    companion object {
        @Volatile private var INSTANCE: SmartCVDatabase? = null

        fun getInstance(context: Context): SmartCVDatabase =
            INSTANCE ?: synchronized(this) {
                INSTANCE ?: Room.databaseBuilder(
                    context.applicationContext,
                    SmartCVDatabase::class.java,
                    "smartcv_db"
                ).fallbackToDestructiveMigration()
                 .build().also { INSTANCE = it }
            }
    }
}

// ── Type Converters ────────────────────────────────────────────────────────
class Converters {
    @TypeConverter fun pubTypeToString(v: PublicationType) = v.name
    @TypeConverter fun stringToPubType(v: String) = PublicationType.valueOf(v)
    @TypeConverter fun achCatToString(v: AchievementCategory) = v.name
    @TypeConverter fun stringToAchCat(v: String) = AchievementCategory.valueOf(v)
    @TypeConverter fun appStatusToString(v: ApplicationStatus) = v.name
    @TypeConverter fun stringToAppStatus(v: String) = ApplicationStatus.valueOf(v)
}

// ── Main Repository ────────────────────────────────────────────────────────
class SmartCVRepository(private val db: SmartCVDatabase) {

    // Profile
    val allProfiles = db.profileDao().getAll()
    fun getFullProfile(id: Long) = db.profileDao().getFullProfile(id)
    suspend fun saveProfile(p: Profile) = withContext(Dispatchers.IO) { db.profileDao().insert(p) }
    suspend fun updateProfile(p: Profile) = withContext(Dispatchers.IO) { db.profileDao().update(p) }

    // Projects
    fun projects(profileId: Long) = db.projectDao().getByProfile(profileId)
    suspend fun addProject(p: Project) = withContext(Dispatchers.IO) { db.projectDao().insert(p) }
    suspend fun deleteProject(p: Project) = withContext(Dispatchers.IO) { db.projectDao().delete(p) }

    // Publications
    fun publications(profileId: Long) = db.publicationDao().getByProfile(profileId)
    suspend fun addPublication(p: Publication) = withContext(Dispatchers.IO) { db.publicationDao().insert(p) }
    suspend fun deletePublication(p: Publication) = withContext(Dispatchers.IO) { db.publicationDao().delete(p) }
    suspend fun pubCount(profileId: Long) = withContext(Dispatchers.IO) { db.publicationDao().countForProfile(profileId) }

    // Awards
    fun awards(profileId: Long) = db.awardDao().getByProfile(profileId)
    suspend fun addAward(a: Award) = withContext(Dispatchers.IO) { db.awardDao().insert(a) }
    suspend fun deleteAward(a: Award) = withContext(Dispatchers.IO) { db.awardDao().delete(a) }

    // Grants
    fun grants(profileId: Long) = db.grantDao().getByProfile(profileId)
    suspend fun addGrant(g: Grant) = withContext(Dispatchers.IO) { db.grantDao().insert(g) }
    suspend fun deleteGrant(g: Grant) = withContext(Dispatchers.IO) { db.grantDao().delete(g) }

    // Education
    fun education(profileId: Long) = db.educationDao().getByProfile(profileId)
    suspend fun addEducation(e: Education) = withContext(Dispatchers.IO) { db.educationDao().insert(e) }
    suspend fun deleteEducation(e: Education) = withContext(Dispatchers.IO) { db.educationDao().delete(e) }

    // Achievements
    fun achievements(profileId: Long) = db.achievementDao().getByProfile(profileId)
    suspend fun addAchievement(a: Achievement) = withContext(Dispatchers.IO) { db.achievementDao().insert(a) }
    suspend fun deleteAchievement(a: Achievement) = withContext(Dispatchers.IO) { db.achievementDao().delete(a) }

    // Organisations
    val allOrgs = db.organisationDao().getAll()
    val bookmarkedOrgs = db.organisationDao().getBookmarked()
    fun searchOrgs(q: String) = db.organisationDao().search(q)
    suspend fun toggleBookmark(id: Long, bookmarked: Boolean) =
        withContext(Dispatchers.IO) { db.organisationDao().setBookmark(id, bookmarked) }
    suspend fun seedOrganisations(orgs: List<Organisation>) =
        withContext(Dispatchers.IO) { db.organisationDao().insertAll(orgs) }

    // Applications
    fun applications(profileId: Long) = db.applicationDao().getByProfile(profileId)
    fun submittedCount(profileId: Long) = db.applicationDao().countSubmitted(profileId)
    suspend fun submitApplication(app: Application) = withContext(Dispatchers.IO) {
        db.applicationDao().insert(app.copy(
            status = ApplicationStatus.SUBMITTED,
            submittedAt = System.currentTimeMillis()
        ))
    }
    suspend fun saveDraft(app: Application) = withContext(Dispatchers.IO) { db.applicationDao().insert(app) }
    suspend fun deleteApplication(app: Application) = withContext(Dispatchers.IO) { db.applicationDao().delete(app) }

    companion object {
        @Volatile private var INSTANCE: SmartCVRepository? = null
        fun getInstance(context: Context): SmartCVRepository =
            INSTANCE ?: synchronized(this) {
                INSTANCE ?: SmartCVRepository(SmartCVDatabase.getInstance(context)).also { INSTANCE = it }
            }
    }
}
