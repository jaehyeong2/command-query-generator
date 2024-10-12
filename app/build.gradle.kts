// generate-command/app/build.gradle.kts

plugins {
	kotlin("jvm")
	kotlin("kapt")
}

dependencies {
	implementation(project(":annotations"))
	kapt(project(":processors"))
}

kapt {
	correctErrorTypes = true
}
