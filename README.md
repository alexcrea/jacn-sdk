This Neuro SDK api implementation currently is not tested 
# Java alexcrea's Neuro SDK
This Neuro SDK implementation is a, bad, unofficial, 
implementation of the NeuroSDK API via [websocket](https://youtu.be/1TRlupOj0i0) (TODO correct link to the api doc)

## Using the SDK
You need to install the SDK, then follow the [Usage Guide](./USAGE.md)

## Installing the SDK:

### via maven central
It is planned to publish to maven central when tested. 
use the publishing to maven local instruction

If you wish to publish to maven central yourself: 
change the group id to one that you own
and run the `gradlew publishToCentralPortal` with your credentials in your environment variable

this can be done, if you are using intellij, by editing the run configuration 
& adding to the Environment variable field
`SONATYPE_USERNAME=YourSonatypeUsername;SONATYPE_PASSWORD=YourSonatypePassword`
with your credentials instead of placeholder

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
    implementation("xyz.alexcrea.jacn:jacn_sdk:0.0.1") // check latest version

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
    <version>0.0.1</version>
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

## Need help or suggestion ?
You can either create an issue/pr/discussion here or join [my discord](https://discord.gg/swBrMf327a)

