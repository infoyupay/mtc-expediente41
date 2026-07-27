/**
 * Defines the monolithic Expediente41 desktop application module.
 * <br/>
 * The module assembles the JavaFX presentation layer, spreadsheet vehicle
 * identification, source PDF analysis and validation, and PDFBox-based
 * generation of individual and consolidated DSTT-041 expediente documents.
 * Only the application entry-point package is exported; JavaFX-specific
 * packages are opened exclusively to the modules that load their FXML and
 * reflective presentation types.
 *
 * @author David Vidal - InfoYupay SACS
 * @version 1.0
 */
module mtc.expediente41s {
    /*=============*
     * Code tools. *
     *=============*/
    requires org.jetbrains.annotations;

    /*==========*
     * Logging. *
     *==========*/
    requires org.slf4j;
    requires ch.qos.logback.core;
    requires org.apache.commons.logging;

    /*==============*
     * JDK modules. *
     *==============*/
    requires java.xml;

    /*===================*
     * PDF manipulation. *
     *===================*/
    requires org.apache.pdfbox;
    requires org.apache.pdfbox.io;

    /*=================*
     * JavaFX modules. *
     *=================*/
    requires javafx.controls;
    requires javafx.fxml;

    /*=============================*
     * Open and export directives. *
     *=============================*/
    opens com.infoyupay.mtcexpediente41 to javafx.fxml;
    //Exports entry point.
    exports com.infoyupay.mtcexpediente41;
    opens com.infoyupay.mtcexpediente41.javafx.fxml to javafx.fxml, javafx.graphics;
    opens com.infoyupay.mtcexpediente41.javafx.treetable to javafx.fxml, javafx.graphics;
}
