# MonoSync

[![Codacy Badge](https://api.codacy.com/project/badge/Grade/8d52e0a50bdc4d9fa26eaa21eda5759e)](https://app.codacy.com/gh/yuriytkach/monosync?utm_source=github.com&utm_medium=referral&utm_content=yuriytkach/monosync&utm_campaign=Badge_Grade_Settings)

Application to download bank statements from Monobank UA using its [API](https://api.monobank.ua).

Before using the app login to https://api.monobank.ua and generate your token.

## Running the application in dev mode

You can run your application in dev mode that enables live coding using:
```
./mvnw quarkus:dev
```

## Packaging and running the application

The application can be packaged using `./mvnw package`.
It produces the `monosync-<version>-runner` file in the `/target` directory.
Be aware that it’s not an _über-jar_ as the dependencies are copied into the `target/lib` directory.

The application is now runnable using `java -jar target/monosync-<version>-runner.jar`.

## Creating a native executable

You can create a native executable using: `./mvnw package -Pnative`.

Or, if you don't have GraalVM installed, you can run the native executable build in a container using: 
`./mvnw package -Pnative -Dquarkus.native.container-build=true`.

You can then execute your native executable with: `./target/monosync-<version>-runner`

If you want to learn more about building native executables, please consult 
[Quarkus docs](https://quarkus.io/guides/building-native-image).
