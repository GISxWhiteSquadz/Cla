package com.example.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface RepairDao {
    // Repair History
    @Query("SELECT * FROM repair_history ORDER BY timestamp DESC")
    fun getAllHistory(): Flow<List<RepairHistoryEntity>>

    @Query("SELECT * FROM repair_history WHERE id = :id LIMIT 1")
    suspend fun getHistoryById(id: Long): RepairHistoryEntity?

    @Query("SELECT * FROM repair_history WHERE fingerprintKey = :fingerprintKey ORDER BY timestamp DESC")
    fun getHistoryByFingerprint(fingerprintKey: String): Flow<List<RepairHistoryEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertHistory(item: RepairHistoryEntity): Long

    @Query("DELETE FROM repair_history")
    suspend fun clearHistory()

    @Query("DELETE FROM repair_history WHERE id = :id")
    suspend fun deleteHistoryById(id: Long)

    // Repair Projects
    @Query("SELECT * FROM repair_projects ORDER BY updatedTimestamp DESC")
    fun getAllProjects(): Flow<List<RepairProjectEntity>>

    @Query("SELECT * FROM repair_projects WHERE id = :id LIMIT 1")
    suspend fun getProjectById(id: Long): RepairProjectEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertProject(project: RepairProjectEntity): Long

    @Update
    suspend fun updateProject(project: RepairProjectEntity)

    @Query("DELETE FROM repair_projects WHERE id = :id")
    suspend fun deleteProjectById(id: Long)

    @Query("SELECT COUNT(*) FROM repair_projects")
    suspend fun getProjectCount(): Int

    // Knowledge Base Chunks (Vector RAG)
    @Query("SELECT * FROM knowledge_chunks")
    fun getAllChunksFlow(): Flow<List<KnowledgeChunkEntity>>

    @Query("SELECT * FROM knowledge_chunks")
    suspend fun getAllChunks(): List<KnowledgeChunkEntity>

    @Query("SELECT * FROM knowledge_chunks WHERE deviceFingerprintKey = :fingerprintKey")
    suspend fun getChunksByFingerprint(fingerprintKey: String): List<KnowledgeChunkEntity>

    @Query("SELECT * FROM knowledge_chunks WHERE category = :category")
    suspend fun getChunksByCategory(category: String): List<KnowledgeChunkEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertChunk(chunk: KnowledgeChunkEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAllChunks(chunks: List<KnowledgeChunkEntity>)

    @Query("SELECT COUNT(*) FROM knowledge_chunks")
    suspend fun getChunksCount(): Int

    @Query("DELETE FROM knowledge_chunks WHERE source != 'Herstellerhandbuch'")
    suspend fun clearUserChunks()

    // Saved Manuals
    @Query("SELECT * FROM saved_manuals ORDER BY savedTimestamp DESC")
    fun getAllManuals(): Flow<List<SavedManualEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertManual(manual: SavedManualEntity): Long

    @Query("DELETE FROM saved_manuals WHERE id = :id")
    suspend fun deleteManualById(id: Long)

    // Device Profiles
    @Query("SELECT * FROM device_profiles ORDER BY manufacturer ASC, modelName ASC")
    fun getAllDeviceProfiles(): Flow<List<DeviceProfileEntity>>

    @Query("SELECT * FROM device_profiles ORDER BY manufacturer ASC, modelName ASC")
    suspend fun getAllDeviceProfilesSync(): List<DeviceProfileEntity>

    @Query("SELECT * FROM device_profiles WHERE category = :category ORDER BY manufacturer ASC, modelName ASC")
    fun getDeviceProfilesByCategory(category: String): Flow<List<DeviceProfileEntity>>

    @Query("SELECT * FROM device_profiles WHERE category = :category AND subcategory = :subcategory ORDER BY manufacturer ASC, modelName ASC")
    fun getDeviceProfilesByCategoryAndSubcategory(category: String, subcategory: String): Flow<List<DeviceProfileEntity>>

    @Query("SELECT * FROM device_profiles WHERE category = :category AND manufacturer = :manufacturer ORDER BY modelName ASC")
    fun getDeviceProfilesByCategoryAndManufacturer(category: String, manufacturer: String): Flow<List<DeviceProfileEntity>>

    @Query("SELECT * FROM device_profiles WHERE fingerprintKey = :fingerprintKey LIMIT 1")
    suspend fun getDeviceProfileByFingerprint(fingerprintKey: String): DeviceProfileEntity?

    @Query("SELECT * FROM device_profiles WHERE fingerprintKey = :fingerprintKey LIMIT 1")
    fun getDeviceProfileByFingerprintFlow(fingerprintKey: String): Flow<DeviceProfileEntity?>

    @Query("SELECT * FROM device_profiles WHERE modelName LIKE '%' || :query || '%' OR manufacturer LIKE '%' || :query || '%' OR modelNumber LIKE '%' || :query || '%'")
    fun searchDeviceProfiles(query: String): Flow<List<DeviceProfileEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertDeviceProfile(profile: DeviceProfileEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAllDeviceProfiles(profiles: List<DeviceProfileEntity>)

    @Query("SELECT COUNT(*) FROM device_profiles")
    suspend fun getDeviceProfilesCount(): Int

    @Query("DELETE FROM device_profiles WHERE id = :id")
    suspend fun deleteDeviceProfileById(id: Long)

    @Query("DELETE FROM device_profiles WHERE fingerprintKey = :fingerprintKey")
    suspend fun deleteDeviceProfileByFingerprint(fingerprintKey: String)

    // Device Components
    @Query("SELECT * FROM device_components WHERE deviceFingerprintKey = :fingerprintKey ORDER BY componentCategory ASC, componentName ASC")
    fun getComponentsForDevice(fingerprintKey: String): Flow<List<DeviceComponentEntity>>

    @Query("SELECT * FROM device_components WHERE deviceFingerprintKey = :fingerprintKey ORDER BY componentCategory ASC, componentName ASC")
    suspend fun getComponentsForDeviceSync(fingerprintKey: String): List<DeviceComponentEntity>

    @Query("SELECT * FROM device_components WHERE id = :id LIMIT 1")
    suspend fun getComponentById(id: Long): DeviceComponentEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertComponent(component: DeviceComponentEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAllComponents(components: List<DeviceComponentEntity>)

    @Query("DELETE FROM device_components WHERE deviceFingerprintKey = :fingerprintKey")
    suspend fun deleteComponentsForDevice(fingerprintKey: String)

    // Office Documents & Invoices
    @Query("SELECT * FROM office_documents ORDER BY updatedTimestamp DESC")
    fun getAllOfficeDocuments(): Flow<List<OfficeDocumentEntity>>

    @Query("SELECT * FROM office_documents ORDER BY updatedTimestamp DESC")
    suspend fun getAllOfficeDocumentsSync(): List<OfficeDocumentEntity>

    @Query("SELECT * FROM office_documents WHERE categoryFolder = :folder ORDER BY updatedTimestamp DESC")
    fun getOfficeDocumentsByFolder(folder: String): Flow<List<OfficeDocumentEntity>>

    @Query("SELECT * FROM office_documents WHERE docType = :docType ORDER BY updatedTimestamp DESC")
    fun getOfficeDocumentsByType(docType: String): Flow<List<OfficeDocumentEntity>>

    @Query("SELECT * FROM office_documents WHERE id = :id LIMIT 1")
    suspend fun getOfficeDocumentById(id: Long): OfficeDocumentEntity?

    @Query("SELECT * FROM office_documents WHERE title LIKE '%' || :query || '%' OR customerName LIKE '%' || :query || '%' OR documentNumber LIKE '%' || :query || '%' OR content LIKE '%' || :query || '%'")
    fun searchOfficeDocuments(query: String): Flow<List<OfficeDocumentEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOfficeDocument(document: OfficeDocumentEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAllOfficeDocuments(documents: List<OfficeDocumentEntity>)

    @Update
    suspend fun updateOfficeDocument(document: OfficeDocumentEntity)

    @Query("DELETE FROM office_documents WHERE id = :id")
    suspend fun deleteOfficeDocumentById(id: Long)

    @Query("SELECT COUNT(*) FROM office_documents")
    suspend fun getOfficeDocumentsCount(): Int

    // Part Comparisons & Budget Planning
    @Query("SELECT * FROM part_comparisons ORDER BY createdTimestamp DESC")
    fun getAllPartComparisons(): Flow<List<PartComparisonEntity>>

    @Query("SELECT * FROM part_comparisons WHERE id = :id LIMIT 1")
    suspend fun getPartComparisonById(id: Long): PartComparisonEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPartComparison(comparison: PartComparisonEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAllPartComparisons(comparisons: List<PartComparisonEntity>)

    @Update
    suspend fun updatePartComparison(comparison: PartComparisonEntity)

    @Query("DELETE FROM part_comparisons WHERE id = :id")
    suspend fun deletePartComparisonById(id: Long)

    // Repair Skill Packs (Offline)
    @Query("SELECT * FROM repair_skill_packs ORDER BY isInstalled DESC, title ASC")
    fun getAllSkillPacks(): Flow<List<RepairSkillPackEntity>>

    @Query("SELECT * FROM repair_skill_packs WHERE isInstalled = 1 ORDER BY title ASC")
    fun getInstalledSkillPacks(): Flow<List<RepairSkillPackEntity>>

    @Query("SELECT * FROM repair_skill_packs WHERE packId = :packId LIMIT 1")
    suspend fun getSkillPackById(packId: String): RepairSkillPackEntity?

    @Query("SELECT * FROM repair_skill_packs WHERE isInstalled = 1")
    suspend fun getInstalledSkillPacksSync(): List<RepairSkillPackEntity>

    @Query("SELECT COUNT(*) FROM repair_skill_packs WHERE isInstalled = 1")
    suspend fun getInstalledPacksCount(): Int

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSkillPack(pack: RepairSkillPackEntity)

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertInitialSkillPacks(packs: List<RepairSkillPackEntity>)

    @Update
    suspend fun updateSkillPack(pack: RepairSkillPackEntity)

    @Query("UPDATE repair_skill_packs SET isInstalled = :installed, installTimestamp = :timestamp WHERE packId = :packId")
    suspend fun updateSkillPackInstallState(packId: String, installed: Boolean, timestamp: Long?)

    @Query("DELETE FROM repair_skill_packs WHERE packId = :packId")
    suspend fun deleteSkillPack(packId: String)
}

