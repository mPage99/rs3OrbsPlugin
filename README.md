# RS3 XP Orbs

A local RuneLite plugin prototype that shows a skill progress orb and fading XP drops.

## Build

Use JDK 21 (recommended) with the included Gradle 8.10 wrapper. Gradle 8.10 supports running on Java versions through 23; Java 25 is not supported by this wrapper. See the [Gradle Java compatibility matrix](https://docs.gradle.org/current/userguide/compatibility.html).

Build on macOS or Linux:

```sh
./gradlew assemble
```

Build on Windows PowerShell:

```powershell
.\gradlew.bat assemble
```

To launch the RuneLite development client with the plugin loaded:

```sh
./gradlew run
```

On Windows PowerShell, use `.\gradlew.bat run`.

Log in through RuneLite's Jagex Accounts plugin in the development client.
