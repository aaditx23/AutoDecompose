package com.aaditx23.autodecompose

object TemplateProvider {

    fun childEntry(composable: String): String = """
        data class ${composable}Child(val component: ${composable}Component): Child()
    """.trimIndent()

    fun configurationEntry(composable: String): String = """
        @Serializable
        data object ${composable}Config: Configuration()
    """.trimIndent()

    fun childFunctionTemplate(
        composable: String,
        componentPackage: String,
        configPackage: String,
        childPackage: String
    ): String {
        val lowercase = composable.replaceFirstChar { it.lowercase() }
        return """
            package $childPackage

            import com.arkivanov.decompose.ComponentContext
            import com.arkivanov.decompose.router.stack.StackNavigation
            import com.arkivanov.decompose.router.stack.pop
            import $componentPackage.${composable}Component
            import $childPackage.Child
            import $configPackage.Configuration

            fun ${lowercase}Child(
                context: ComponentContext,
                navigation: StackNavigation<Configuration>
            ): Child.${composable}Child {
                return Child.${composable}Child(${composable}Component(context, navigation))
            }
        """.trimIndent()
    }

    fun composableTemplate(composable: String, packageName: String): String = """
        package $packageName

        import androidx.compose.runtime.Composable

        @Composable
        fun $composable(component: ${composable}Component) {
        }
    """.trimIndent()

    fun componentTemplate(composable: String, packageName: String): String = """
        package $packageName

        import com.arkivanov.decompose.ComponentContext

        class ${composable}Component(
            componentContext: ComponentContext,
        ) {

            fun onEvent(event: ${composable}Event) {
                when (event) {
                    
                }
            }
        }
    """.trimIndent()

    fun eventTemplate(composable: String, packageName: String): String = """
        package $packageName

        interface ${composable}Event {
        }
    """.trimIndent()
}
