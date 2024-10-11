plugins {
	kotlin("jvm")
	id("kotlin-kapt")
}

dependencies {
	implementation(project(":annotations"))
	kapt("com.google.auto.service:auto-service:1.0.1")
	implementation("com.squareup:kotlinpoet:1.12.0")
	implementation("com.google.auto.service:auto-service:1.0.1")
}

kapt {
	correctErrorTypes = true
}
