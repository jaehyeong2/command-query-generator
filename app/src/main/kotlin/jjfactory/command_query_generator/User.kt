package jjfactory.command_query_generator

@GenerateInfo
@GenerateCommand(additionalInnerClasses = ["Test"])
class User(
    val id: Long,
    val name: String,
    var phone: String
) {
}