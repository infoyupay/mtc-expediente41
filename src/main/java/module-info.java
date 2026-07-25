/**
 * Monolythic application module.
 *
 * @author David Vidal - InfoYupay SACS
 */
module mtc.expediente41s {
    requires javafx.controls;
    requires javafx.fxml;


    opens com.infoyupay.mtcexpediente41 to javafx.fxml;
    exports com.infoyupay.mtcexpediente41;
}