package com.acad.smartcv.data.model

import androidx.room.*

// ── Candidate Profile ──────────────────────────────────────────────────────
@Entity(tableName = "profiles")
data class Profile(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val firstName: String = "",
    val lastName: String = "",
    val email: String = "",
    val phone: String = "",
    val title: String = "",
    val organization: String = "",
    val location: String = "",
    val bio: String = "",
    val skills: String = "",          // comma-separated
    val profilePhotoPath: String = "",
    val linkedIn: String = "",
    val website: String = "",
    val orcidId: String = "",
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis()
)

// ── Project ────────────────────────────────────────────────────────────────
@Entity(tableName = "projects", foreignKeys = [
    ForeignKey(entity = Profile::class, parentColumns = ["id"],
               childColumns = ["profileId"], onDelete = ForeignKey.CASCADE)
])
data class Project(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val profileId: Long,
    val title: String,
    val role: String = "",
    val duration: String = "",
    val description: String = "",
    val tags: String = "",            // comma-separated
    val fundingAgency: String = "",
    val fundingAmount: String = "",
    val outcomes: String = "",
    val githubUrl: String = "",
    val projectUrl: String = ""
)

// ── Publication ────────────────────────────────────────────────────────────
@Entity(tableName = "publications", foreignKeys = [
    ForeignKey(entity = Profile::class, parentColumns = ["id"],
               childColumns = ["profileId"], onDelete = ForeignKey.CASCADE)
])
data class Publication(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val profileId: Long,
    val title: String,
    val authors: String = "",
    val journal: String = "",
    val year: String = "",
    val type: PublicationType = PublicationType.JOURNAL,
    val doi: String = "",
    val url: String = "",
    val impactFactor: String = "",
    val citations: String = "",
    val abstract_text: String = ""
)

enum class PublicationType {
    JOURNAL, CONFERENCE, BOOK_CHAPTER, BOOK, PREPRINT, PATENT, THESIS, REPORT
}

// ── Award ──────────────────────────────────────────────────────────────────
@Entity(tableName = "awards", foreignKeys = [
    ForeignKey(entity = Profile::class, parentColumns = ["id"],
               childColumns = ["profileId"], onDelete = ForeignKey.CASCADE)
])
data class Award(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val profileId: Long,
    val name: String,
    val awardingBody: String = "",
    val year: String = "",
    val description: String = "",
    val category: String = ""
)

// ── Grant / Funding ────────────────────────────────────────────────────────
@Entity(tableName = "grants", foreignKeys = [
    ForeignKey(entity = Profile::class, parentColumns = ["id"],
               childColumns = ["profileId"], onDelete = ForeignKey.CASCADE)
])
data class Grant(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val profileId: Long,
    val title: String,
    val agency: String = "",
    val amount: String = "",
    val period: String = "",
    val role: String = "",           // PI / Co-PI / Investigator
    val status: String = "Completed"
)

// ── Education ──────────────────────────────────────────────────────────────
@Entity(tableName = "education", foreignKeys = [
    ForeignKey(entity = Profile::class, parentColumns = ["id"],
               childColumns = ["profileId"], onDelete = ForeignKey.CASCADE)
])
data class Education(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val profileId: Long,
    val degree: String,
    val institution: String = "",
    val year: String = "",
    val field: String = "",
    val grade: String = ""
)

// ── Other Achievement ──────────────────────────────────────────────────────
@Entity(tableName = "achievements", foreignKeys = [
    ForeignKey(entity = Profile::class, parentColumns = ["id"],
               childColumns = ["profileId"], onDelete = ForeignKey.CASCADE)
])
data class Achievement(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val profileId: Long,
    val title: String,
    val category: AchievementCategory = AchievementCategory.OTHER,
    val description: String = "",
    val year: String = ""
)

enum class AchievementCategory {
    INVITED_TALK, PATENT, MEDIA_FEATURE, LEADERSHIP, MENTORSHIP,
    OPEN_SOURCE, EDITORIAL_BOARD, PEER_REVIEW, CONSULTING, OTHER
}

// ── Organisation ───────────────────────────────────────────────────────────
@Entity(tableName = "organisations")
data class Organisation(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val name: String,
    val type: String,
    val field: String,
    val location: String = "",
    val description: String = "",
    val website: String = "",
    val logoUrl: String = "",
    val isBookmarked: Boolean = false
)

// ── Application ────────────────────────────────────────────────────────────
@Entity(tableName = "applications", foreignKeys = [
    ForeignKey(entity = Profile::class, parentColumns = ["id"],
               childColumns = ["profileId"], onDelete = ForeignKey.CASCADE),
    ForeignKey(entity = Organisation::class, parentColumns = ["id"],
               childColumns = ["organisationId"], onDelete = ForeignKey.CASCADE)
])
data class Application(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val profileId: Long,
    val organisationId: Long,
    val position: String = "",
    val coverLetter: String = "",
    val sectionsIncluded: String = "",   // JSON array of section keys
    val status: ApplicationStatus = ApplicationStatus.DRAFT,
    val submittedAt: Long? = null,
    val createdAt: Long = System.currentTimeMillis()
)

enum class ApplicationStatus {
    DRAFT, SUBMITTED, UNDER_REVIEW, SHORTLISTED, REJECTED, ACCEPTED
}

// ── Full Profile (Room relation) ───────────────────────────────────────────
data class FullProfile(
    @Embedded val profile: Profile,
    @Relation(parentColumn = "id", entityColumn = "profileId") val projects: List<Project>,
    @Relation(parentColumn = "id", entityColumn = "profileId") val publications: List<Publication>,
    @Relation(parentColumn = "id", entityColumn = "profileId") val awards: List<Award>,
    @Relation(parentColumn = "id", entityColumn = "profileId") val grants: List<Grant>,
    @Relation(parentColumn = "id", entityColumn = "profileId") val education: List<Education>,
    @Relation(parentColumn = "id", entityColumn = "profileId") val achievements: List<Achievement>
)
