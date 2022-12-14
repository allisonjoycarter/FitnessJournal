package com.catscoffeeandkitchen.data.workouts.repository

import androidx.compose.ui.geometry.Offset
import com.catscoffeeandkitchen.data.workouts.db.FitnessJournalDb
import com.catscoffeeandkitchen.data.workouts.models.GroupExerciseXRef
import com.catscoffeeandkitchen.domain.interfaces.ExerciseRepository
import com.catscoffeeandkitchen.domain.models.*
import com.catscoffeeandkitchen.data.workouts.models.ExerciseGroupEntity
import com.catscoffeeandkitchen.data.workouts.util.*
import java.time.DayOfWeek
import java.time.OffsetDateTime
import java.time.ZoneId
import javax.inject.Inject


class ExerciseRepositoryImpl @Inject constructor(
    private val database: FitnessJournalDb
): ExerciseRepository {
    override suspend fun createGroup(groupName: String, exerciseNames: List<String>): ExerciseGroup {
        val exercises = database.exerciseDao().getExercisesByName(exerciseNames)
        val groupId = database.exerciseGroupDao().insert(
            ExerciseGroupEntity(
                gId = 0L,
                name = groupName,
            )
        )

        database.exerciseGroupDao().insertAllRefs(exercises.map { ex ->
            GroupExerciseXRef(
                egxrId = 0L,
                groupId = groupId,
                exerciseId = ex.eId
            )
        })

        return ExerciseGroup(
            id = groupId,
            name = groupName,
            exercises = exercises.map { it.toExercise() }
        )
    }

    override suspend fun updateGroup(group: ExerciseGroup) {
        database.exerciseGroupDao().update(group.toEntity())
    }

    override suspend fun updateGroupExercises(
        group: ExerciseGroup,
        exerciseNames: List<String>
    ): ExerciseGroup {
        database.exerciseGroupDao().removeGroupXRefs(group.id)

        val exercises = database.exerciseDao().getExercisesByName(exerciseNames)
        database.exerciseGroupDao().insertAllRefs(exercises.map { ex ->
            GroupExerciseXRef(
                egxrId = 0L,
                groupId = group.id,
                exerciseId = ex.eId
            )
        })

        return group.copy(exercises = exercises.map { exercise ->
            val stats = database.exerciseDao().getExerciseWithStatsByName(
                exercise.name,
                startOfWeek = OffsetDateTime.now().inUTC().with(DayOfWeek.MONDAY).withHour(0)
                    .toUTCEpochMilli(),
                currentTime = OffsetDateTime.now().toUTCEpochMilli()
            )
            exercise.toExercise(
                stats = stats?.toStats()
            )
        })
    }

    override suspend fun removeGroup(group: ExerciseGroup) {
        database.exerciseGroupDao().delete(group.toEntity())
    }

    override suspend fun getGroups(): List<ExerciseGroup> {
        return database.exerciseGroupDao().getGroups().map { entry ->
            entry.group.toExerciseGroup(
                exercises = entry.exercises.map { exercise ->
                    val stats = database.exerciseDao().getExerciseWithStatsByName(
                        exercise.name,
                        startOfWeek = OffsetDateTime.now().inUTC().with(DayOfWeek.MONDAY).withHour(0).toUTCEpochMilli(),
                        currentTime = OffsetDateTime.now().toUTCEpochMilli()
                    )
                    exercise.toExercise(
                        stats = stats?.toStats()
                    )
                }
            )
        }
    }
}