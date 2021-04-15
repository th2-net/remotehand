Using Google WebP library
=========================

## Annotation

On this page you can see how to use WebP library in Remotehand. 

## Prepare application
To build WebP library [clone](https://chromium.googlesource.com/webm/libwebp) ([github mirror of webm project](https://github.com/webmproject/libwebp) ) or download precompiled version form [https://developers.google.com/speed/webp/](https://developers.google.com/speed/webp/docs/precompiled#getting_cwebp_dwebp_and_the_webp_libraries).
Make the following commands to compile the library:
```
git clone webp-repository
cd libwebp
make -f makefile.unix
cd swig
mkdir -p java/com/exactprosystems/remotehand/screenwriter
swig -java -package com.exactprosystems.remotehand.screenwriter -outdir java/com/exactprosystems/remotehand/screenwriter -o libwebp_java_wrap.c libwebp.swig
gcc -shared -fPIC -fno-strict-aliasing -O2 -I/path/to/java/Contents/Home/include/ -I/path/to/java/Contents/Home/include/ostype -L../src  libwebp_java_wrap.c -lwebp -o libwebp_ostype.so
```
Copy the compiled library into `lib` folder and write the path to it in `config.ini` and use the following optional parameter  when you start application `-useWebpImageEncoder`.

## Configuration

You can change the following parameters to customize WebP library.

| Parameter  | Description |
| ------------- | ------------- |
| WebpLibPath  | Path to WebP library  |
| WebpQualityFactor  | Encoded image quality factor  |
| WebpLosslessCompression  | Use lossless compression  |