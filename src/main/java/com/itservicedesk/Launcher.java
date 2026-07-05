package com.itservicedesk;

/**
 * Plain entry point (does NOT extend javafx.application.Application) used
 * as the jar's Main-Class.
 *
 * Why this exists: if the Main-Class in the jar manifest is a class that
 * directly extends Application, the java launcher refuses to run it from
 * a plain "java -jar" with the error "JavaFX runtime components are
 * missing" — even when all the JavaFX jars are bundled inside. Pointing
 * Main-Class at this neutral wrapper class instead avoids that check.
 */
public class Launcher {
    public static void main(String[] args) {
        ITServiceDeskApp.main(args);
    }
}
