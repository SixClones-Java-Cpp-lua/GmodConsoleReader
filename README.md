# GmodConsoleReader

This a small program that use [pseudo terminal](https://en.wikipedia.org/wiki/Pseudoterminal) in order to read the full
output of a garry's mod server.

## Content

- Only one [java file](src/Main.java) that can start and read a garry's mod server
- A [cpp file](src/cpp/main.cpp) not used anymore, that used to do the job of the pty4J lib

## Compile

Simply run the gradle 'jar' task !

You can also find a compiled jar in the release.

## Usage

Put the compiled jar in the directory where the garry's mod server's executable is. Then
run `java -jar ./gmodconsolereader.jar <usual-cmd-for-a-gmod-server>`.

For example, I
have `java -jar ./gmodconsolereader.jar ./srcds_run -game garrysmod +maxplayers 15 +map rp_rockford_karnaka +host_workshop_collection 1382039356 -norestart`

That it, you should have the garry's mod server starting and see the output in the console.

*Note : The program wasn't test on a Windows system, but I think that you only need to change `./srcds_run`
for `./srcds.exe`*

## Dependencies

- Pty4J (https://mvnrepository.com/artifact/org.jetbrains.pty4j/pty4j)

