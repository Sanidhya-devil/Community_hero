package com.example.data

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface IssueDao {
    @Query("SELECT * FROM issues ORDER BY createdAt DESC")
    fun getAllIssues(): Flow<List<Issue>>

    @Query("SELECT * FROM issues WHERE id = :id")
    suspend fun getIssueById(id: Int): Issue?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertIssue(issue: Issue): Long

    @Update
    suspend fun updateIssue(issue: Issue)

    @Delete
    suspend fun deleteIssue(issue: Issue)

    @Query("SELECT COUNT(*) FROM issues")
    fun getIssuesCount(): Flow<Int>
}
