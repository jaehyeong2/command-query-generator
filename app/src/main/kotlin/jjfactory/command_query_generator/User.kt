package jjfactory.command_query_generator

@GenerateInfo
@GenerateCommand
class User(
    val id: Long,
    val name: String,
    var phone: String
) {
}