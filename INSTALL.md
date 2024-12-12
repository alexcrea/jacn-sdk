## Installing the SDK:

### via maven central
for gradle, make sure you have `mavenCentral()` in your project gradle repository

Proceed to follow the gradle or maven instruction

### via maven local
If you want to fork this and test without publishing to maven central,
you need build it to publish it to maven local to integrate it into your app.

This can be done after cloning to your machine via the command
`gradlew publishToMavenLocal`

then, for gradle, make sure you have `mavenLocal()` in your project gradle repository

Proceed to follow the gradle or maven instruction

---

### Using Gradle
add jacn to your dependency
(please check latest version):
```kotlin
dependencies {
    implementation("xyz.alexcrea.jacn:jacn_sdk:1.0.0") // check latest version

    implementation("org.jetbrains:annotations:24.0.1") // recommended but optional. from maven central
    ... // rest of your dependency
}
```
You can check latest jetbrain annotation
[maven central](https://central.sonatype.com/artifact/org.jetbrains/annotations)

### Using Maven
add jacn to your dependency
(please check latest version):
```xml
<dependency>
    <groupId>xyz.alexcrea.jacn</groupId>
    <artifactId>jacn_sdk</artifactId>
    <version>1.0.0</version>
</dependency>
```
It is also recommended you add jetbrain's annotation from
[maven central](https://central.sonatype.com/artifact/org.jetbrains/annotations)
```xml
<dependency>
    <groupId>org.jetbrains</groupId>
    <artifactId>annotations</artifactId>
    <version>24.0.1</version>
</dependency>
```

---