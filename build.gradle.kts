import com.vanniktech.maven.publish.SonatypeHost

plugins {
	kotlin("jvm") version "1.9.25" apply false
	id("com.vanniktech.maven.publish") version "0.28.0" // 대체 플러그인
	id("signing") // GPG 서명을 위한 플러그인 추가
}



allprojects {
	group = "jjfactory"
	version = "1.0.0"

	repositories {
		mavenCentral()
	}
}


tasks.withType<Javadoc> {
	options.encoding = "UTF-8"
}

signing {
	sign(publishing.publications)
}


mavenPublishing {
	signAllPublications() // Gpg 서명을 위한 설정
	publishToMavenCentral(SonatypeHost.CENTRAL_PORTAL) // 포탈로 등록 할거기 때문에 타입 추가

	coordinates("io.github.jaehyeong2", "command-query-generator", "1.0.0") // 네임 스페이스, 라이브러리 이름, 버전 순서로 작성

    // POM 설정
    pom {
        name.set("command-query-generator")
        description.set("command and query dto generator for springboot")
        url.set("https://github.com/jaehyeong2/command-query-generator")

        // 라이선스 정보
        licenses {
            license {
                name.set("Apache License")
                url.set("https://github.com/dami325/excel-utils/blob/master/LICENSE")
            }
        }

        // 개발자 정보
        developers {
            developer {
                id.set("jaehyeong2")
                name.set("jaehyeong lee")
                email.set("wogud1514@naver.com")
            }
        }

        scm {
            connection.set("scm:git:github.com/dami325/excel-utils.git")
            developerConnection.set("scm:git:ssh://github.com:dami325/excel-utils.git")
            url.set("https://github.com/dami325/excel-utils/tree/master")
        }
    }
}


