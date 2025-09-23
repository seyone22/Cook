# Recipe Importer - Kotlin JSON-LD Parser

[![GitHub](https://img.shields.io/badge/github-seyone22/recipe--importer-blue.svg)](https://github.com/seyone22/recipe-importer)
[![Kotlin](https://img.shields.io/badge/kotlin-1.9.0-orange.svg)](https://kotlinlang.org/)
[![Maven Central](https://img.shields.io/maven-central/v/com.seyone22/recipe-importer.svg)](https://search.maven.org/artifact/com.seyone22/recipe-importer)
[![License](https://img.shields.io/badge/license-MIT-green.svg)](LICENSE)

## Overview

`recipe-importer` is a lightweight Kotlin library for parsing [JSON-LD](https://json-ld.org/) recipe metadata from web pages. It extracts structured recipe data (ingredients, instructions, images, nutrition info, etc.) from any HTML containing `ld+json` script tags of type `Recipe`. Designed for both **Kotlin/JVM projects** and **Android apps**, it provides a clean, idiomatic API for integrating recipe data into your applications.

## Features

- Extract recipes from HTML `ld+json` scripts.
- Parse structured data including:
  - Title, description, author
  - Ingredients and instructions (supports nested steps)
  - Prep, cook, and total time
  - Yield/servings
  - Nutrition info (calories, fat, protein, sugar)
  - Images and videos
  - Ratings and rating counts
- Safe handling of JSON arrays, objects, and primitives.
- Fully Kotlin idiomatic API.
- Compatible with Android and JVM projects.
- Minimal dependencies: [Kotlinx Serialization](https://github.com/Kotlin/kotlinx.serialization) and [Jsoup](https://jsoup.org/).

## Installation

### Gradle (Kotlin DSL)
Add JitPack repository:

```kotlin
repositories {
    maven { url = uri("https://jitpack.io") }
}
````

Add dependency:

```kotlin
dependencies {
    implementation("com.github.seyone22:recipe-importer:1.0.2")
}
```

## Usage

### Extract JSON-LD from HTML

```kotlin
import recipeimporter.parser.JsonLdExtractor
import kotlinx.serialization.json.JsonObject

val html: String = "<html>...</html>"
val recipeJson: JsonObject? = JsonLdExtractor.extractRecipeJsonLd(html)
```

### Parse Recipe Object

```kotlin
import recipeimporter.parser.RecipeParser

recipeJson?.let {
    val recipe = RecipeParser.parseRecipe(it)
    println(recipe.title)
    println(recipe.ingredients)
}
```

### Recipe Data Model

The library provides the following models:

* `Recipe` - main data class
* `InstructionSection` - represents steps or sections of instructions
* `NutritionInfo` - nutrition details

## Example

```kotlin
val html = fetchHtmlFromUrl("https://example.com/recipe")
val jsonLd = JsonLdExtractor.extractRecipeJsonLd(html)

val recipe = jsonLd?.let { RecipeParser.parseRecipe(it) }
println("Recipe: ${recipe?.title}")
println("Ingredients: ${recipe?.ingredients?.joinToString()}")
```

## Contributing

Contributions are welcome!

* Fork the repository
* Create a feature branch
* Submit pull requests for review

Please ensure your code follows Kotlin idioms and includes unit tests.

## License

This project is licensed under the MIT License - see the [LICENSE](LICENSE) file for details.
