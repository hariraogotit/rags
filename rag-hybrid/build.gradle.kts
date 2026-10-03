plugins {
    kotlin("jvm") version "2.2.0"
    kotlin("plugin.spring") version "2.2.0"
    id("org.springframework.boot") version "4.1.1"
    id("io.spring.dependency-management") version "1.1.6"
}
repositories { mavenCentral() }
dependencies {
    implementation(project(":rag-common"))
    implementation("org.apache.lucene:lucene-core:9.11.1")
    implementation("org.apache.lucene:lucene-analysis-common:9.11.1")
    implementation("org.apache.lucene:lucene-queryparser:9.11.1")
    implementation("org.springframework.boot:spring-boot-starter-web")
}
