========================
BUILD OUTPUT DESCRIPTION
========================

When you build an Java application project that has a main class, the IDE
automatically copies all of the JAR
files on the projects classpath to your projects dist/lib folder. The IDE
also adds each of the JAR files to the Class-Path element in the application
JAR files manifest file (MANIFEST.MF).

To run the project from the command line, go to the dist folder and
type the following:

java -jar "ImprimirEtiquetasV71.jar" 

To distribute this project, zip up the dist folder (including the lib folder)
and distribute the ZIP file.

Notes:

* If two JAR files on the project classpath have the same name, only the first
JAR file is copied to the lib folder.
* Only JAR files are copied to the lib folder.
If the classpath contains other types of files or folders, these files (folders)
are not copied.
* If a library on the projects classpath also has a Class-Path element
specified in the manifest,the content of the Class-Path element has to be on
the projects runtime path.
* To set a main class in a standard Java project, right-click the project node
in the Projects window and choose Properties. Then click Run and enter the
class name in the Main Class field. Alternatively, you can manually type the
class name in the manifest Main-Class element.
/********************************/
1- El fichero generado para la impresión de las etiquetas reside en c:\tmp (si no existe se ha de crear).
2- La impresión se lanza a una impresora mapeada al puerto LPT1, si no existe se ha de mapear
3- Crear directorio C:\DASCHER para alojar los ficheros que se generan para dascher
4- Crear directorio C:\XPO para alojar los fichero de integración de XPO
5- Crear directorio C:\MRW para alojar los ficheros de integración de MRW
/*******************************/