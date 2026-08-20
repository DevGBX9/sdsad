package com.ooredoost.app.data

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface SessionDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(session: SessionEntity)

    @Query("SELECT * FROM sessions ORDER BY startTime DESC")
    fun getAllSessions(): Flow<List<SessionEntity>>

    @Query("SELECT * FROM sessions ORDER BY startTime DESC LIMIT :limit")
    fun getRecentSessions(limit: Int): Flow<List<SessionEntity>>

    @Query("SELECT * FROM sessions WHERE startTime >= :since ORDER BY startTime DESC")
    fun getSessionsSince(since: Long): Flow<List<SessionEntity>>

    @Query("SELECT COALESCE(SUM(dataBytes), 0) FROM sessions")
    fun getTotalDataBytes(): Flow<Long>

    @Query("SELECT COALESCE(SUM(dataBytes), 0) FROM sessions WHERE startTime >= :since")
    fun getDataBytesSince(since: Long): Flow<Long>

    @Query("SELECT COUNT(*) FROM sessions")
    fun getSessionCount(): Flow<Int>

    @Query("SELECT COALESCE(SUM(cycleCount), 0) FROM sessions")
    fun getTotalCycles(): Flow<Int>

    @Query("SELECT COALESCE(SUM(burstCount), 0) FROM sessions")
    fun getTotalBursts(): Flow<Int>

    @Query("DELETE FROM sessions")
    suspend fun clearAll()

    @Query("DELETE FROM sessions WHERE id = :sessionId")
    suspend fun deleteSession(sessionId: Long)
}
