package com.aaditx23.autodecompose

object TemplateProvider {

    fun navRootEntry(composableName: String, componentPackage: String): Pair<String, String> {
        val statement = "is Child.${composableName}Child -> ${composableName}(instance.component)\n"
        val importLine = "import $componentPackage.${composableName}\n\n"

        return Pair(statement, importLine)
    }
    fun rootComponentEntry(composableName: String, childFunPackage: String): Pair<String, String> {
        val statement = "                        Configuration.${composableName}Config -> ${composableName.unCapitalize()}Child(context, navigation)\n"
        val importLine = "import $childFunPackage.${composableName.unCapitalize()}Child\n\n"

        return Pair(statement, importLine)
    }



    fun childEntry(composable: String, componentPackage: String): Pair<String, String> {
        val entry = "data class ${composable}Child(val component: ${composable}Component): Child()"
        val importLine = "import $componentPackage.${composable}Component"
        return Pair(entry, importLine)
    }


    fun configurationEntry(composable: String): String = """
        @Serializable
            data object ${composable}Config: Configuration()
            
    """.trimIndent()

    fun childFunctionTemplate(
        composable: String,
        pkg: Packages
    ): String {
        val lowercase = composable.replaceFirstChar { it.lowercase() }
        return """
            package ${pkg.childFun}

            import com.arkivanov.decompose.ComponentContext
            import com.arkivanov.decompose.router.stack.StackNavigation
            import com.arkivanov.decompose.router.stack.pop
            import ${pkg.composable}.${composable}Component
            import ${pkg.config}.Child
            import ${pkg.config}.Configuration

            fun ${lowercase}Child(
                context: ComponentContext,
                navigation: StackNavigation<Configuration>
            ): Child.${composable}Child {
                return Child.${composable}Child(${composable}Component(context))
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
