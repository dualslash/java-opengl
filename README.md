# Wind3D: An open-source Java 3D Engine

Fully built on open-source, complete 3D rendering engine built in Java using native OpenGL, OpenAL and OpenCL APIs. 

Included is a demo rendition of the opening scene from The Wind Waker.

![Rendering Screenshot](https://i.imgur.com/1HRdqou.png)

## Features

Support for 

|Library|Description|
|-------|-----------|
|[OpenGL](https://www.opengl.org/)|A cross-language, cross-platform application programming interface for rendering 2D and 3D vector graphics|
|[OpenAL](https://www.openal.org/)|Cross-platform audio application programming interface. It is designed for efficient rendering of multichannel three-dimensional positional audio.|
|[OpenCL](https://www.khronos.org/opencl/)|Cross-platform low-level API for heterogeneous parallel processing.|

## Usage

Compatible with Java 8 for the following platforms:
* Windows (x86 only, 64 bit support coming) 
* Linux (64 bit) 
* OSX (64 bit).

**Compiling**

Win - `javac -cp .;jar/* *.java`

Unix - `javac -cp .:jar/* *.java`

**Running**

Win - `java -cp .;jar/* -Djava.library.path=native/win32 CGApp`

Unix - `java -cp .:jar/* -Djava.library.path=native/linux CGApp`


