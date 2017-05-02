# zip4agd
The zip4agd is built and tested using Java 8 and Maven to build the artifact and run the tests.
The compression/decompression is using internal JDK ZLIB compression library.

Prerequisites : 
-> JDK 8
-> Maven 3

External dependencies through maven:
1. log4j2 -> for logging purpose
2. junit4 -> for running unit tests

How to build application :
-> mvn clean package

How to run application :
-> build jar artifact then go to target folder, make sure zip4agd.jar is generated. 
Then run from cmd/shell: 
java -jar zip4agd.jar compress <input folder or file> <output folder or file> <max size in mb> <optional - number of threads>
java -jar zip4agd.jar decompress <input folder> <output folder>

Note : 
- [compress] if output folder is specified, the UUID random output zip file is generated
- [compress] if output file is specified, it will be used as output zip file name
- [compress] The <max size in mb> should be lesser than JVM heap memory. The formula for -Xmx : 32mb + <max size in mb> * <number of threads> * 2
- [compress] The <number of threads> can not be greater than number of files/directories to be compressed.
- [compress] When running in parallel, the number of compressed files generated tend to be higher than single thread operation

