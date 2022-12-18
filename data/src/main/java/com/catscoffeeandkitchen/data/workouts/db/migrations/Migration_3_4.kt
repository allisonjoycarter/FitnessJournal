package com.catscoffeeandkitchen.data.workouts.db.migrations

import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

val Migration_3_4 = object : Migration(3, 4) {
    override fun migrate(database: SupportSQLiteDatabase) {
        // Create a temporary table with the PRIMARY KEY constraint corrected
        // (what we want it to be in the newest version)
        database.execSQL("""
            CREATE TABLE IF NOT EXISTS ExerciseGoal_tmp (
                egId INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                exerciseId INTEGER,
                exerciseGroupId INTEGER,
                workoutPlanId INTEGER NOT NULL,
                positionId INTEGER NOT NULL,
                positionInWorkout INTEGER NOT NULL,
                sets INTEGER NOT NULL,
                reps INTEGER NOT NULL,
                repRangeMax INTEGER NOT NULL,
                repRangeMin INTEGER NOT NULL,
                weightInPounds REAL NOT NULL,
                weightInKilograms REAL NOT NULL,
                repsInReserve INTEGER NOT NULL,
                perceivedExertion INTEGER NOT NULL,
                note TEXT NOT NULL,
                modifier TEXT,
                type TEXT NOT NULL,
                FOREIGN KEY(exerciseId) 
                    REFERENCES ExerciseEntity(eId) ON UPDATE NO ACTION ON DELETE CASCADE , 
                FOREIGN KEY(positionId) 
                    REFERENCES ExercisePositionEntity(epId) ON UPDATE NO ACTION ON DELETE CASCADE , 
                FOREIGN KEY(exerciseGroupId)
                    REFERENCES ExerciseGroupEntity(gId) ON UPDATE NO ACTION ON DELETE CASCADE ,
                FOREIGN KEY(workoutPlanId)
                    REFERENCES WorkoutPlanEntity(wpId) ON UPDATE NO ACTION ON DELETE CASCADE 
            )
        """.trimIndent())

        // Create ExercisePositionEntity's for each goal in the original table
        database.execSQL("""
            INSERT INTO ExercisePositionEntity 
            (planId, position, exerciseId, groupId)
            SELECT 
                workoutPlanId AS planId,
                positionInWorkout AS position,
                exerciseId,
                exerciseGroupId AS groupId 
            FROM ExerciseGoal
        """.trimIndent())

        // copy over all the data into the temporary table
        database.execSQL("""
            INSERT INTO ExerciseGoal_tmp
            (
                exerciseId,
                exerciseGroupId,
                workoutPlanId,
                positionId,
                positionInWorkout,
                sets, reps, repRangeMax, repRangeMin,
                weightInPounds, weightInKilograms,
                repsInReserve, perceivedExertion,
                note, modifier, type
            )
            SELECT
                exerciseId,
                exerciseGroupId,
                workoutPlanId,
                (
                    SELECT epId
                    FROM ExercisePositionEntity 
                    WHERE ExercisePositionEntity.position = ExerciseGoal.positionInWorkout AND
                            ExercisePositionEntity.planId = ExerciseGoal.workoutPlanId
                ) AS positionId,
                positionInWorkout,
                sets, reps, repRangeMax, repRangeMin,
                weightInPounds, weightInKilograms,
                repsInReserve, perceivedExertion,
                note, modifier, type
            FROM ExerciseGoal
        """.trimIndent())

        // delete the original table and rename the new table to the old table name
        database.execSQL("DROP TABLE ExerciseGoal")
        database.execSQL("ALTER TABLE ExerciseGoal_tmp RENAME TO ExerciseGoal")
    }
}