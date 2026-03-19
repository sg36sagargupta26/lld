plugins {
	java
	id("org.springframework.boot") version "4.1.0-M2"
	id("io.spring.dependency-management") version "1.1.7"
}

group = "com.lld"
version = "0.0.1-SNAPSHOT"

java {
	toolchain {
		languageVersion = JavaLanguageVersion.of(25)
	}
}

repositories {
	mavenCentral()
}

dependencies {
	implementation("org.springframework.boot:spring-boot-starter-webmvc")
    implementation("org.springframework.boot:spring-boot-starter-jdbc:4.1.0-M2")
    implementation("org.springframework.boot:spring-boot-starter-data-jpa:4.1.0-M2")
    implementation("org.springframework.boot:spring-boot-starter-restclient:4.1.0-M2")
    implementation("org.postgresql:postgresql:42.7.9")
	testImplementation("org.springframework.boot:spring-boot-starter-webmvc-test")
	testRuntimeOnly("org.junit.platform:junit-platform-launcher")
}

tasks.withType<Test> {
	useJUnitPlatform()
}
