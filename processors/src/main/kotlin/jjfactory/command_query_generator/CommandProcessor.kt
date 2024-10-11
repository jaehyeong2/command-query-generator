package jjfactory.command_query_generator

import com.squareup.kotlinpoet.*
import javax.annotation.processing.AbstractProcessor
import javax.annotation.processing.RoundEnvironment
import javax.lang.model.element.ElementKind
import javax.lang.model.element.TypeElement
import javax.lang.model.element.VariableElement
import javax.tools.Diagnostic

class CommandProcessor: AbstractProcessor() {
    @OptIn(DelicateKotlinPoetApi::class)
    override fun process(annotations: Set<TypeElement>, roundEnv: RoundEnvironment): Boolean {
        val annotatedElements = roundEnv.getElementsAnnotatedWith(GenerateCommand::class.java)

        for (element in annotatedElements) {
            if (element.kind != ElementKind.CLASS) {
                processingEnv.messager.printMessage(Diagnostic.Kind.ERROR, "@GenerateCommand can only be applied to classes.")
                return true
            }

            val classElement = element as TypeElement
            val packageName = processingEnv.elementUtils.getPackageOf(classElement).qualifiedName.toString()
            val originalClassName = classElement.simpleName.toString()
            val commandClassName = "${originalClassName}Command"

            val classBuilder = TypeSpec.classBuilder(commandClassName)
                .addKdoc("자동 생성된 Command 클래스입니다.")

            val constructorBuilder = FunSpec.constructorBuilder()

            for (enclosed in classElement.enclosedElements) {
                if (enclosed.kind == ElementKind.FIELD) {
                    val variableElement = enclosed as VariableElement
                    val propertyName = variableElement.simpleName.toString()
                    val propertyType = variableElement.asType().asTypeName()

                    classBuilder.addProperty(
                        PropertySpec.builder(propertyName, propertyType)
                            .initializer(propertyName)
                            .build()
                    )

                    constructorBuilder.addParameter(propertyName, propertyType)
                }
            }

            classBuilder.primaryConstructor(constructorBuilder.build())

            val file = FileSpec.builder(packageName, commandClassName)
                .addType(classBuilder.build())
                .build()

            try {
                file.writeTo(processingEnv.filer)
            } catch (e: Exception) {
                processingEnv.messager.printMessage(Diagnostic.Kind.ERROR, "Error generating $commandClassName: ${e.message}")
            }
        }

        return true
    }
}