/**
 * Monolythic application module.
 *
 * @author David Vidal - InfoYupay SACS
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
