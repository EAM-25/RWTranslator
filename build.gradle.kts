// 项目根目录 build.gradle.kts
buildscript {
    repositories {
        // 阿里云
        maven (url= "https://maven.aliyun.com/repository/public")
        maven (url= "https://maven.aliyun.com/repository/google")
        maven (url= "https://maven.aliyun.com/repository/gradle-plugin")
        //华为云
        maven (url= "https://repo.huaweicloud.com/repository/maven/")
        mavenCentral()
        google()
        maven(url = "https://jitpack.io") // JitPack 仓库
    }
}

plugins {
    id("com.android.application") version "8.12.0" apply false
    id("com.android.library") version "8.12.0" apply false
    id("org.jetbrains.kotlin.android") version "2.2.0" apply false
}

apply(from = "config.gradle.kts")

tasks.register<Delete>("clean") {
    delete(getLayout().buildDirectory)
}