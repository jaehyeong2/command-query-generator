package jjfactory.command_query_generator

import com.google.auto.service.AutoService
import com.squareup.kotlinpoet.*
import java.util.concurrent.Flow
import javax.annotation.processing.AbstractProcessor
import javax.annotation.processing.RoundEnvironment
import javax.annotation.processing.SupportedAnnotationTypes
import javax.annotation.processing.SupportedSourceVersion
import javax.lang.model.SourceVersion
import javax.lang.model.element.ElementKind
import javax.lang.model.element.TypeElement
import javax.lang.model.element.VariableElement
import javax.tools.Diagnostic

@AutoService(Flow.Processor::class)
@SupportedAnnotationTypes("jjfactory.command_query_generator.GenerateCommand")
@SupportedSourceVersion(SourceVersion.RELEASE_8)
class CommandProcessor: AbstractProcessor() {

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

            // 원본 클래스의 필드 정보 수집
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
                    val propertyType = field.asType().asTypeName()

                    constructorBuilder.addParameter(propertyName, propertyType)
                    properties += PropertySpec.builder(propertyName, propertyType)
                        .initializer(propertyName)
                        .build()
                }
//
//                // 특정 커맨드에 따라 추가 메서드 생성 (예: Create 커맨드에 toEntity 메서드 추가)
//                if (command.equals("Create", ignoreCase = true)) {
//                    val toEntityFun = FunSpec.builder("toEntity")
//                        .returns(ClassName(packageName, originalClassName))
//                        .addParameter("userId", Long::class)
//                        .addStatement(
//                            "return %T(" +
//                                    "userId = userId, " +
//                                    "type = type, " +
//                                    "title = title, " +
//                                    "content = content, " +
//                                    "accessLevel = accessLevel" +
//                                    ")",
//                            ClassName(packageName, originalClassName)
//                        )
//                        .build()
//                    dataClassBuilder.addFunction(toEntityFun)
//                }

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