package jjfactory.command_query_generator

import com.google.auto.service.AutoService
import com.squareup.kotlinpoet.*
import javax.annotation.processing.*
import javax.lang.model.element.ElementKind
import javax.lang.model.element.TypeElement
import javax.lang.model.element.VariableElement
import javax.tools.Diagnostic

@AutoService(Processor::class)
@SupportedAnnotationTypes("jjfactory.command_query_generator.GenerateCommand")
class CommandProcessor : AbstractProcessor() {

    override fun process(annotations: Set<TypeElement>, roundEnv: RoundEnvironment): Boolean {
        val annotatedElements = roundEnv.getElementsAnnotatedWith(GenerateCommand::class.java)

        for (element in annotatedElements) {
            if (element.kind != ElementKind.CLASS) {
                processingEnv.messager.printMessage(
                    Diagnostic.Kind.ERROR,
                    "@GenerateCommand can only be applied to classes.",
                    element
                )
                continue
            }

            val classElement = element as TypeElement
            val packageName = processingEnv.elementUtils.getPackageOf(classElement).qualifiedName.toString()
            val originalClassName = classElement.simpleName.toString()
            val commandClassName = "${originalClassName}Command"

            val annotation = classElement.getAnnotation(GenerateCommand::class.java)
            val additionalCommands = annotation.additionalInnerClasses.toList()

            val defaultCommands = listOf("Create", "Modify")

            val allCommands = defaultCommands + additionalCommands

            val fields = classElement.enclosedElements
                .filter { it.kind == ElementKind.FIELD }
                .map { it as VariableElement }

            val classBuilder = TypeSpec.classBuilder(commandClassName)

            for (command in allCommands) {
                val dataClassBuilder = TypeSpec.classBuilder(command)
                    .addModifiers(KModifier.DATA)

                val constructorBuilder = FunSpec.constructorBuilder()
                val properties = mutableListOf<PropertySpec>()

                fields.forEach { field ->
                    val propertyName = field.simpleName.toString()
                    var propertyType = field.asType().asTypeName()

                    if (propertyType.toString() == "java.lang.String") {
                        propertyType = ClassName("kotlin", "String")
                    }

                    constructorBuilder.addParameter(propertyName, propertyType)
                    properties += PropertySpec.builder(propertyName, propertyType)
                        .initializer(propertyName)
                        .addModifiers(KModifier.PRIVATE)
                        .build()
                }

                if (command.equals("Create", ignoreCase = true)) {
                    val toEntityFun = FunSpec.builder("toEntity")
                        .returns(ClassName(packageName, originalClassName))

                    val constructorParams = fields.joinToString(", ") { field ->
                        val propertyName = field.simpleName.toString()
                        "$propertyName = $propertyName"
                    }

                    toEntityFun.addStatement(
                        "return %T($constructorParams)",
                        ClassName(packageName, originalClassName)
                    )

                    dataClassBuilder.addFunction(toEntityFun.build())
                }

                dataClassBuilder.primaryConstructor(constructorBuilder.build())
                dataClassBuilder.addProperties(properties)

                classBuilder.addType(dataClassBuilder.build())
            }

            val file = FileSpec.builder(packageName, commandClassName)
                .addType(classBuilder.build())
                .build()

            try {
                file.writeTo(processingEnv.filer)
            } catch (e: Exception) {
                processingEnv.messager.printMessage(
                    Diagnostic.Kind.ERROR,
                    "Error generating $commandClassName: ${e.message}"
                )
            }
        }

        return true
    }
}
