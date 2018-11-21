# redTCP

[![](https://jitpack.io/v/redParrot17/redTCP.svg)](https://jitpack.io/#redParrot17/redTCP)

Server/Client framework for communicating using a strong end-to-end encryption over a tcp connection.

### redTCP Dependency
use `master-fb08ef1bba-1` for the RELEASE until further notice
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

### Quick Start
#### Server Side
```java
TcpServer tcpServer = new TcpServer(7070, 0, 0, true);
tcpServer.addMessageListener(message -> {
     String text = message.getMessage();
     System.out.println(text);
});
/* wait till you want to shutdown the server */
tcpServer.close();
```
#### Client Side
```java
TcpClient tcpClient = new TcpClient("address", 7070, true);
tcpClient.sendText("HelloWorld!");
/* wait till you want to shutdown the client */
tcpClient.close();
```
> Change `address` to be the ip address of the computer hosting the server. If the client is hosted on the same computer as the server, change `address` to "localhost".

### License

redTCP is released under the [Apache 2.0 license](LICENSE).

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
