package sk.mpage.lumen.core.database.di

import android.content.Context
import androidx.room.Room
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import sk.mpage.lumen.core.database.LumenDatabase
import sk.mpage.lumen.core.database.dao.CategoryDao
import sk.mpage.lumen.core.database.dao.QuizSessionDao
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
internal object DatabaseModule {
    @Provides
    @Singleton
    fun database(
        @ApplicationContext context: Context,
    ): LumenDatabase =
        Room
            .databaseBuilder(context, LumenDatabase::class.java, "lumen.db")
            .build()

    @Provides
    fun quizSessionDao(database: LumenDatabase): QuizSessionDao = database.quizSessionDao()

    @Provides
    fun categoryDao(database: LumenDatabase): CategoryDao = database.categoryDao()
}
