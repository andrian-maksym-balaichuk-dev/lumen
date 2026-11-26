package sk.mpage.lumen.core.network.model

public sealed interface RemoteQuestions {
    public data class Loaded(
        val questions: List<RemoteQuestion>,
    ) : RemoteQuestions

    public data object TokenNotFound : RemoteQuestions

    public data object TokenExhausted : RemoteQuestions
}
