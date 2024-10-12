package jjfactory.command_query_generator


@Target(AnnotationTarget.CLASS)
@Retention(AnnotationRetention.SOURCE)
annotation class GenerateCommand(
    val additionalInnerClasses: Array<String> = []
)