package jjfactory.command_query_generator

import com.google.auto.service.AutoService
import com.squareup.kotlinpoet.*
import javax.annotation.processing.*
import javax.lang.model.element.ElementKind
import javax.lang.model.element.TypeElement
import javax.lang.model.element.VariableElement
import javax.tools.Diagnostic

@AutoService(Processor::class)
@SupportedAnnotationTypes("jjfactory.command_query_generator.GenerateInfo")
class InfoProcessor : AbstractProcessor() {

    override fun process(annotations: Set<TypeElement>, roundEnv: RoundEnvironment): Boolean {
        val annotatedElements = roundEnv.getElementsAnnotatedWith(GenerateInfo::class.java)

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
            val infoClassName = "${originalClassName}Info"

            val annotation = classElement.getAnnotation(GenerateInfo::class.java)
            val additionalClasses = annotation.additionalInnerClasses.toList()

            val defaultClasses = listOf("Detail", "List")

            val allClasses = defaultClasses + additionalClasses

            // 원본 클래스의 필드 정보 수집
            val fields = classElement.enclosedElements
                .filter { it.kind == ElementKind.FIELD }
                .map { it as VariableElement }

            val classBuilder = TypeSpec.classBuilder(infoClassName)

            for (initClass in allClasses) {
                val dataClassBuilder = TypeSpec.classBuilder(initClass)
                    .addModifiers(KModifier.DATA)

                val constructorBuilder = FunSpec.constructorBuilder()
                val properties = mutableListOf<PropertySpec>()

                fields.forEach { field ->
                    val propertyName = field.simpleName.toString()
                    val propertyType = field.asType().asTypeName()

                    constructorBuilder.addParameter(propertyName, propertyType)
                    properties += PropertySpec.builder(propertyName, propertyType)
                        .initializer(propertyName)
                        .addModifiers(KModifier.PRIVATE)
                        .build()
                }

                dataClassBuilder.primaryConstructor(constructorBuilder.build())
                dataClassBuilder.addProperties(properties)

                classBuilder.addType(dataClassBuilder.build())
            }

            val file = FileSpec.builder(packageName, infoClassName)
                .addType(classBuilder.build())
                .build()

            try {
                file.writeTo(processingEnv.filer)
            } catch (e: Exception) {
                processingEnv.messager.printMessage(
                    Diagnostic.Kind.ERROR,
                    "Error generating $infoClassName: ${e.message}"
                )
            }
        }

        return true
    }
}