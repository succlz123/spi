## Service Provider Interface

A simple Android library that implements Service Provider Interface function.

## Usage

~~~
    repositories {
        google()
        jcenter()
        maven { url 'https://jitpack.io' }
    }
~~~

~~~
    dependencies {
        classpath 'com.android.tools.build:gradle:4.0.0'
        classpath 'com.github.succlz123.spi:plugin:0.0.1'
    }
~~~

~~~
    apply plugin: 'org.succlz123.spi'
~~~

~~~
    kapt 'com.github.succlz123.spi:processor:0.0.1'
    implementation 'com.github.succlz123.spi:lib:0.0.1'
~~~

## APT

### Debug

gradle.properties
~~~
org.gradle.jvmargs=-agentlib:jdwp=transport=dt_socket,server=y,suspend=n,address=5005
~~~

~~~
Click Annotation Debug Button
~~~

~~~
Rebuild Project
~~~

## Plugin

### Debug

~~~
./gradlew --no-daemon clean :app:assemble -Dorg.gradle.debug=true
~~~

### Upload

> upload to local repo

~~~
./gradlew :spi-plug:uploadArchives
~~~

> upload to gradle repo

~~~
https://plugins.gradle.org/docs/submit
~~~

~~~
./gradlew publishPlugins
~~~
