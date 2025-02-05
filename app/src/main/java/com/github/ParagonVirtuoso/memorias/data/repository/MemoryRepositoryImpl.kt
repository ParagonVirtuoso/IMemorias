package com.github.ParagonVirtuoso.memorias.data.repository

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.work.Data
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import com.github.ParagonVirtuoso.memorias.domain.model.Memory
import com.github.ParagonVirtuoso.memorias.domain.repository.AuthRepository
import com.github.ParagonVirtuoso.memorias.domain.repository.MemoryRepository
import com.github.ParagonVirtuoso.memorias.receiver.MemoryNotificationReceiver
import com.github.ParagonVirtuoso.memorias.worker.MemoryNotificationWorker
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.messaging.FirebaseMessaging
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.tasks.await
import java.util.Date
import java.util.UUID
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class MemoryRepositoryImpl @Inject constructor(
    private val firestore: FirebaseFirestore,
    private val authRepository: AuthRepository,
    private val fcm: FirebaseMessaging,
    @ApplicationContext private val context: Context
) : MemoryRepository {

    private suspend fun getCurrentUserId(): String {
        return authRepository.getCurrentUser().first()?.id ?: throw IllegalStateException("Usuário não autenticado")
    }

    override suspend fun createMemory(
        videoId: String,
        videoTitle: String,
        videoThumbnail: String,
        notificationTime: Date
    ): Result<Memory> = try {
        val userId = authRepository.getCurrentUser().first()?.id ?: throw IllegalStateException("Usuário não autenticado")

        val memory = Memory(
            id = UUID.randomUUID().toString(),
            userId = userId,
            videoId = videoId,
            videoTitle = videoTitle,
            videoThumbnail = videoThumbnail,
            notificationTime = notificationTime,
            createdAt = Date(),
            notified = false
        )

        val documentRef = firestore.collection(MEMORIES_COLLECTION).document(memory.id)
        documentRef.set(memory).await()
        scheduleNotification(memory)
        Result.success(memory)
    } catch (e: Exception) {
        Result.failure(e)
    }

    override suspend fun getMemories(): Result<List<Memory>> = try {
        val userId = getCurrentUserId()
        val memories = firestore.collection(MEMORIES_COLLECTION)
            .whereEqualTo("userId", userId)
            .orderBy("notificationTime")
            .get()
            .await()
            .toObjects(Memory::class.java)

        Result.success(memories)
    } catch (e: Exception) {
        Result.failure(e)
    }

    override suspend fun deleteMemory(memoryId: String): Result<Unit> = try {
        firestore.collection(MEMORIES_COLLECTION)
            .document(memoryId)
            .delete()
            .await()
        Result.success(Unit)
    } catch (e: Exception) {
        Result.failure(e)
    }

    override suspend fun updateMemory(memory: Memory): Result<Memory> = try {
        firestore.collection(MEMORIES_COLLECTION)
            .document(memory.id)
            .set(memory)
            .await()

        scheduleNotification(memory)
        Result.success(memory)
    } catch (e: Exception) {
        Result.failure(e)
    }

    override suspend fun scheduleMemoryNotification(memory: Memory): Result<Unit> = try {
        scheduleNotification(memory)
        Result.success(Unit)
    } catch (e: Exception) {
        Result.failure(e)
    }

    private fun scheduleNotification(memory: Memory) {
        val currentTimeMillis = System.currentTimeMillis()
        val notificationTimeMillis = memory.notificationTime.time
        val delay = notificationTimeMillis - currentTimeMillis
        
        if (delay <= 0) {
            return
        }

        val intent = Intent(context, MemoryNotificationReceiver::class.java).apply {
            putExtra(MemoryNotificationWorker.KEY_VIDEO_ID, memory.videoId)
            putExtra(MemoryNotificationWorker.KEY_VIDEO_TITLE, memory.videoTitle)
            putExtra(MemoryNotificationWorker.KEY_VIDEO_THUMBNAIL, memory.videoThumbnail)
            putExtra(MemoryNotificationWorker.KEY_NOTIFICATION_TIME, notificationTimeMillis)
        }

        val pendingIntent = PendingIntent.getBroadcast(
            context,
            memory.id.hashCode(),
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as android.app.AlarmManager

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            if (alarmManager.canScheduleExactAlarms()) {
                scheduleExactAlarm(alarmManager, notificationTimeMillis, pendingIntent)
            } else {
                scheduleInexactAlarm(alarmManager, notificationTimeMillis, pendingIntent)
            }
        } else {
            scheduleExactAlarm(alarmManager, notificationTimeMillis, pendingIntent)
        }
    }

    private fun scheduleExactAlarm(
        alarmManager: android.app.AlarmManager,
        triggerAtMillis: Long,
        pendingIntent: PendingIntent
    ) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            alarmManager.setExactAndAllowWhileIdle(
                android.app.AlarmManager.RTC_WAKEUP,
                triggerAtMillis,
                pendingIntent
            )
        } else {
            alarmManager.setExact(
                android.app.AlarmManager.RTC_WAKEUP,
                triggerAtMillis,
                pendingIntent
            )
        }
    }

    private fun scheduleInexactAlarm(
        alarmManager: android.app.AlarmManager,
        triggerAtMillis: Long,
        pendingIntent: PendingIntent
    ) {
        alarmManager.set(
            android.app.AlarmManager.RTC_WAKEUP,
            triggerAtMillis,
            pendingIntent
        )
    }

    override fun getMemories(userId: String): Flow<List<Memory>> = flow {
        val snapshot = firestore.collection(MEMORIES_COLLECTION)
            .whereEqualTo("userId", userId)
            .get()
            .await()

        val memories = snapshot.documents.mapNotNull { doc ->
            doc.toObject(Memory::class.java)
        }
        emit(memories)
    }

    override suspend fun markAsNotified(memoryId: String): Result<Unit> = try {
        firestore.collection(MEMORIES_COLLECTION)
            .document(memoryId)
            .update("notified", true)
            .await()
        Result.success(Unit)
    } catch (e: Exception) {
        Result.failure(e)
    }

    private fun generateMemoryId(): String {
        return firestore.collection(MEMORIES_COLLECTION).document().id
    }

    companion object {
        private const val MEMORIES_COLLECTION = "memories"
    }
}