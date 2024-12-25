import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    alias(libs.plugins.android.application) apply false
    alias(libs.plugins.android.library) apply false
    alias(libs.plugins.jetbrainsKotlinAndroid) apply false
    alias(libs.plugins.ksp) apply false
    alias(libs.plugins.kotlin.serialization) apply false
    alias(libs.plugins.room) apply false
    alias(libs.plugins.compose.compiler) apply false
}

allprojects {
    tasks.withType(KotlinCompile::class.java).all {
        compilerOptions {
            freeCompilerArgs.addAll(
                listOf(
                    "-Xopt-in=kotlin.ExperimentalUnsignedTypes",
                    "-Xopt-in=kotlinx.coroutines.ExperimentalCoroutinesApi",
                    "-Xopt-in=kotlinx.coroutines.InternalCoroutinesApi",
                    "-Xopt-in=androidx.compose.animation.ExperimentalAnimationApi",
                    "-Xopt-in=androidx.compose.ExperimentalComposeApi",
                    "-Xopt-in=androidx.compose.material.ExperimentalMaterialApi",
                    "-Xopt-in=androidx.compose.runtime.ExperimentalComposeApi",
                    "-Xopt-in=androidx.compose.ui.ExperimentalComposeUiApi",
                    "-Xopt-in=androidx.compose.foundation.ExperimentalFoundationApi",
                    "-Xopt-in=coil.annotation.ExperimentalCoilApi",
                    "-Xopt-in=kotlinx.serialization.ExperimentalSerializationApi",
                    "-Xopt-in=com.google.accompanist.pager.ExperimentalPagerApi",
                    "-Xopt-in=androidx.compose.material3.ExperimentalMaterial3Api",
                    "-Xopt-in=androidx.lifecycle.compose.ExperimentalLifecycleComposeApi",
                    "-Xopt-in=androidx.constraintlayout.compose.ExperimentalMotionApi"
                )
            )
        }
    }
}
