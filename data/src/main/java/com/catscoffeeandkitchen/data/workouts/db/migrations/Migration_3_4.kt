package com.catscoffeeandkitchen.data.workouts.db.migrations

import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

val Migration_3_4 = object : Migration(3, 4) {
    override fun migrate(database: SupportSQLiteDatabase) {
        database.execSQL("""
            ALTER TABLE ExerciseGoal
            ADD COLUMN planId INTEGER DEFAULT NULL
            REFERENCES WorkoutPlanEntity(wpId)
            """.trimIndent())

        database.execSQL("""
            INSERT INTO ExercisePositionEntity 
            (planId, position, exerciseId, groupId)
            SELECT 
                positionInWorkout as position,
                workoutPlanId as planId,
                exerciseId,
                exerciseGroupId as groupId 
            FROM ExerciseGoal
        """.trimIndent())

        database.execSQL("""
            ALTER TABLE ExerciseGoal
            ADD COLUMN positionId 
            REFERENCES ExercisePositionEntity(epId)
        """.trimIndent())

        database.execSQL("""
            UPDATE ExerciseGoal
            SET positionInWorkout = IFNULL(
                
                SELECT epId
                FROM ExercisePositionEntity 
                WHERE ExercisePositionEntity.position = ExerciseGoal.positionInWorkout AND
                        ExercisePositionEntity.planId = ExerciseGoal.workoutPlanId
                , 1 
            );
        """.trimIndent())
    }
}