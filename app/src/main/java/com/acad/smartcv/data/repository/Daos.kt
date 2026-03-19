package com.acad.smartcv.data.repository

import androidx.lifecycle.LiveData
import androidx.room.*
import com.acad.smartcv.data.model.*
import kotlinx.coroutines.flow.Flow

// ── Profile DAO ────────────────────────────────────────────────────────────
@Dao
interface ProfileDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(profile: Profile): Long

    @Update
    suspend fun update(profile: Profile)

    @Delete
    suspend fun delete(profile: Profile)

    @Query("SELECT * FROM profiles ORDER BY updatedAt DESC")
    fun getAll(): Flow<List<Profile>>

    @Query("SELECT * FROM profiles WHERE id = :id")
    suspend fun getById(id: Long): Profile?

    @Transaction
    @Query("SELECT * FROM profiles WHERE id = :id")
    fun getFullProfile(id: Long): Flow<FullProfile?>
}

// ── Project DAO ────────────────────────────────────────────────────────────
@Dao
interface ProjectDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(project: Project): Long

    @Update
    suspend fun update(project: Project)

    @Delete
    suspend fun delete(project: Project)

    @Query("SELECT * FROM projects WHERE profileId = :profileId ORDER BY id DESC")
    fun getByProfile(profileId: Long): Flow<List<Project>>

    @Query("DELETE FROM projects WHERE profileId = :profileId")
    suspend fun deleteAllForProfile(profileId: Long)
}

// ── Publication DAO ────────────────────────────────────────────────────────
@Dao
interface PublicationDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(pub: Publication): Long

    @Update
    suspend fun update(pub: Publication)

    @Delete
    suspend fun delete(pub: Publication)

    @Query("SELECT * FROM publications WHERE profileId = :profileId ORDER BY year DESC, id DESC")
    fun getByProfile(profileId: Long): Flow<List<Publication>>

    @Query("SELECT COUNT(*) FROM publications WHERE profileId = :profileId")
    suspend fun countForProfile(profileId: Long): Int
}

// ── Award DAO ──────────────────────────────────────────────────────────────
@Dao
interface AwardDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(award: Award): Long

    @Update
    suspend fun update(award: Award)

    @Delete
    suspend fun delete(award: Award)

    @Query("SELECT * FROM awards WHERE profileId = :profileId ORDER BY year DESC")
    fun getByProfile(profileId: Long): Flow<List<Award>>
}

// ── Grant DAO ──────────────────────────────────────────────────────────────
@Dao
interface GrantDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(grant: Grant): Long

    @Update
    suspend fun update(grant: Grant)

    @Delete
    suspend fun delete(grant: Grant)

    @Query("SELECT * FROM grants WHERE profileId = :profileId ORDER BY id DESC")
    fun getByProfile(profileId: Long): Flow<List<Grant>>
}

// ── Education DAO ──────────────────────────────────────────────────────────
@Dao
interface EducationDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(edu: Education): Long

    @Update
    suspend fun update(edu: Education)

    @Delete
    suspend fun delete(edu: Education)

    @Query("SELECT * FROM education WHERE profileId = :profileId ORDER BY year DESC")
    fun getByProfile(profileId: Long): Flow<List<Education>>
}

// ── Achievement DAO ────────────────────────────────────────────────────────
@Dao
interface AchievementDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(a: Achievement): Long

    @Update
    suspend fun update(a: Achievement)

    @Delete
    suspend fun delete(a: Achievement)

    @Query("SELECT * FROM achievements WHERE profileId = :profileId ORDER BY year DESC")
    fun getByProfile(profileId: Long): Flow<List<Achievement>>
}

// ── Organisation DAO ───────────────────────────────────────────────────────
@Dao
interface OrganisationDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(org: Organisation): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(orgs: List<Organisation>)

    @Query("SELECT * FROM organisations ORDER BY name ASC")
    fun getAll(): Flow<List<Organisation>>

    @Query("SELECT * FROM organisations WHERE isBookmarked = 1")
    fun getBookmarked(): Flow<List<Organisation>>

    @Query("""SELECT * FROM organisations WHERE
        LOWER(name) LIKE '%' || LOWER(:q) || '%' OR
        LOWER(field) LIKE '%' || LOWER(:q) || '%' OR
        LOWER(type) LIKE '%' || LOWER(:q) || '%'""")
    fun search(q: String): Flow<List<Organisation>>

    @Query("UPDATE organisations SET isBookmarked = :bookmarked WHERE id = :id")
    suspend fun setBookmark(id: Long, bookmarked: Boolean)
}

// ── Application DAO ────────────────────────────────────────────────────────
@Dao
interface ApplicationDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(app: Application): Long

    @Update
    suspend fun update(app: Application)

    @Delete
    suspend fun delete(app: Application)

    @Query("SELECT * FROM applications WHERE profileId = :profileId ORDER BY createdAt DESC")
    fun getByProfile(profileId: Long): Flow<List<Application>>

    @Query("SELECT * FROM applications WHERE status = :status ORDER BY createdAt DESC")
    fun getByStatus(status: ApplicationStatus): Flow<List<Application>>

    @Query("SELECT COUNT(*) FROM applications WHERE profileId = :profileId AND status = 'SUBMITTED'")
    fun countSubmitted(profileId: Long): Flow<Int>
}
