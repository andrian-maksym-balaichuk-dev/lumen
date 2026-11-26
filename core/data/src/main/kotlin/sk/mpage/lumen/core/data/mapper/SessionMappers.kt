package sk.mpage.lumen.core.data.mapper

import sk.mpage.lumen.core.database.entity.QuestionAttemptEntity
import sk.mpage.lumen.core.database.entity.QuizSessionEntity
import sk.mpage.lumen.core.database.relation.SessionWithAttempts
import sk.mpage.lumen.core.model.FinishedGame
import sk.mpage.lumen.core.model.Question
import sk.mpage.lumen.core.model.QuestionAttempt
import sk.mpage.lumen.core.model.QuizConfig
import sk.mpage.lumen.core.model.QuizSession
import sk.mpage.lumen.core.model.QuizSessionDetail

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

internal fun QuizSessionEntity.toSession(): QuizSession =
    QuizSession(
        id = id,
        config = QuizConfig(mode, categoryId, categoryName, difficulty, questionType),
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
        averageAnswerMs = avgAnswerMs,
        endReason = endReason,
    )

internal fun SessionWithAttempts.toDetail(): QuizSessionDetail =
    QuizSessionDetail(
        session = session.toSession(),
        attempts = attempts.sortedBy { it.position }.map { it.toAttempt(session.id) },
    )

private fun QuestionAttemptEntity.toAttempt(sessionId: Long): QuestionAttempt =
    QuestionAttempt(
        position = position,
        question =
            Question(
                id = "$sessionId-$position",
                text = questionText,
                options = options,
                correctAnswer = correctAnswer,
                categoryName = categoryName,
                difficulty = difficulty,
                type = type,
            ),
        selectedAnswer = selectedAnswer,
        isCorrect = isCorrect,
        timeSpentMs = timeSpentMs,
        answeredAt = answeredAt,
    )
