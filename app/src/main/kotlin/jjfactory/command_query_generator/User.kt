package jjfactory.command_query_generator

@GenerateCommand
class User(
    val id: Long,
    val name: String,
    var phone: String
) {
}