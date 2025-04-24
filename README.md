# 🧩 AutoDecompose

An IntelliJ plugin that automates navigation setup for [Decompose](https://github.com/arkivanov/Decompose) in Kotlin Multiplatform projects.

Inspired by the clean and scalable architecture shown in [Philip Lackner's YouTube video](https://www.youtube.com/watch?v=g4XSWQ7QT8g), this plugin helps eliminate boilerplate when wiring up new screens.

---

## ✨ Features

- 🔍 UI to select your existing Decompose files:
    - `Child.kt`
    - `Configuration.kt`
    - Navigation Root
    - Root Component
    - Output directories for generated code
- 🧠 Automatically generates:
    - `YourScreen.kt` (Composable)
    - `YourScreenComponent.kt`
    - `YourScreenEvent.kt`
- ✍️ Inserts navigation logic into:
    - `Children` `when` block
    - `createChild` factory method
- ✅ Remembers paths across projects (stored in `.decompose_plugin_paths.properties`)
- 🖼️ Visual progress UI with step status tracking

---

## 🚀 Getting Started

### 1. Build the Plugin

```bash
./gradlew buildPlugin
