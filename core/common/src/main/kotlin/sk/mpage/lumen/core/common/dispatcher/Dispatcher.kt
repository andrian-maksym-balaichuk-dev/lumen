package sk.mpage.lumen.core.common.dispatcher

import javax.inject.Qualifier

@Qualifier
@Retention(AnnotationRetention.BINARY)
public annotation class Dispatcher(
    val dispatcher: LumenDispatcher,
)

public enum class LumenDispatcher {
    Default,
    IO,
}
