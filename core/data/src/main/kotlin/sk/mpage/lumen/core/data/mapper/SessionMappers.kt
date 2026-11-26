package sk.mpage.lumen.core.data.mapper

import sk.mpage.lumen.core.database.entity.QuestionAttemptEntity
import sk.mpage.lumen.core.database.entity.QuizSessionEntity
import sk.mpage.lumen.core.model.FinishedGame
import sk.mpage.lumen.core.model.QuestionAttempt

internal fun FinishedGame.toSessionEntity(): QuizSessionEntity =
    QuizSessionEntity(
        gameUuid = gameUuid,
        mode = config.mode,
        categoryId = config.categoryId,
        categoryName = config.categoryName,
        difficulty = config.difficulty,
        questionType = config.type,
        startedAt = startedAt,
        finishedAt = finishedAt,
        localDate = localDate,
        durationSeconds = durationSeconds,
        answerTimeMs = answerTimeMs,
        totalQuestions = totalQuestions,
        correctCount = correctCount,
        wrongCount = wrongCount,
        timeoutCount = timeoutCount,
        scorePercent = scorePercent,
        avgAnswerMs = averageAnswerMs,
        endReason = endReason,
    )

internal fun QuestionAttempt.toEntity(): QuestionAttemptEntity =
    QuestionAttemptEntity(
        position = position,
        questionText = question.text,
        categoryName = question.categoryName,
        difficulty = question.difficulty,
        type = question.type,
        options = question.options,
        correctAnswer = question.correctAnswer,
        selectedAnswer = selectedAnswer,
        isCorrect = isCorrect,
        timeSpentMs = timeSpentMs,
        answeredAt = answeredAt,
    )
