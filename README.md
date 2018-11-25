# RedTCP

[![](https://jitpack.io/v/redParrot17/redTCP.svg)](https://jitpack.io/#redParrot17/redTCP)
[![](https://img.shields.io/badge/License-Apache%202.0-lightgrey.svg)](https://github.com/redParrot17/LICENSE)

Server/Client framework for communicating using a strong end-to-end encryption over a tcp connection.

### Download
Replace `RELEASE` with the release you wish to use. The latest release is listed above.
#### Gradle
```gradle
allprojects {
    repositories {
        ...
        maven { url 'https://jitpack.io' }
    }
}
```
```gradle
dependencies {
        implementation 'com.github.redParrot17:redTCP:RELEASE'
}
```

#### Maven
```xml
<repository>
    <id>jitpack.io</id>
    <url>https://jitpack.io</url>
</repository>
```
```xml
<dependency>
    <groupId>com.github.redParrot17</groupId>
    <artifactId>redTCP</artifactId>
    <version>RELEASE</version>
</dependency>
```

### Documentation
*Coming soon*  
You can find example code within the [WIKI](https://github.com/redParrot17/redTCP/wiki).

### License

RedTCP is released under the [Apache 2.0 license](LICENSE).

```
Copyright 2018 redParrot17.

Licensed under the Apache License, Version 2.0 (the "License");
you may not use this file except in compliance with the License.
You may obtain a copy of the License at

    http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
limitations under the License.
```
