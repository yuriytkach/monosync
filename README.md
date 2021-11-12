# MonoSync

[![CircleCI](https://circleci.com/gh/yuriytkach/monosync/tree/master.svg?style=shield)](https://circleci.com/gh/yuriytkach/monosync/tree/master) [![Codacy Badge](https://app.codacy.com/project/badge/Grade/c36ff84d04734c02878d1bc25165b089)](https://www.codacy.com/gh/yuriytkach/monosync/dashboard?utm_source=github.com&amp;utm_medium=referral&amp;utm_content=yuriytkach/monosync&amp;utm_campaign=Badge_Grade) [![Codacy Badge](https://app.codacy.com/project/badge/Coverage/c36ff84d04734c02878d1bc25165b089)](https://www.codacy.com/gh/yuriytkach/monosync/dashboard?utm_source=github.com&utm_medium=referral&utm_content=yuriytkach/monosync&utm_campaign=Badge_Coverage)

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
