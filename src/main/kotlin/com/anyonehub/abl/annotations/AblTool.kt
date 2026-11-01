package com.anyonehub.abl.annotations

/**
 * Marks a class or component as an A.B.L. Toolchain entity.
 */
@Target(AnnotationTarget.CLASS, AnnotationTarget.PROPERTY, AnnotationTarget.FIELD)
@Retention(AnnotationRetention.RUNTIME)
annotation class AblTool(
    val name: String = "",
    val category: String = "toolchain"
)

