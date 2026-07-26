package com.infoyupay.mtcexpediente41.javafx.treetable;

import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;

import java.nio.file.Path;
import java.util.Objects;
import java.util.UUID;

/**
 * Represents one row in the workspace {@code TreeTableView} hierarchy.
 * <br/>
 * Each instance describes exactly one visual level: a group branch, a vehicle
 * branch, or a document leaf. Properties that do not apply to that level remain
 * unset so the same row type can back every table column.
 *
 * @author David Vidal - InfoYupay SACS
 * @version 1.0
 */
public class TreeTableVehicle {
    private final UUID _oid;
    private final StringProperty group =
            new SimpleStringProperty(this, "group");
    private final ObjectProperty<Path> brochure =
            new SimpleObjectProperty<>(this, "brochure");
    private final StringProperty vin =
            new SimpleStringProperty(this, "vin");
    private final StringProperty document =
            new SimpleStringProperty(this, "document");
    private final ObjectProperty<Path> pdf =
            new SimpleObjectProperty<>(this, "pdf");

    /**
     * Creates an empty tree-table row with a stable instance identity.
     */
    public TreeTableVehicle() {
        this._oid = UUID.randomUUID();
    }

    /**
     * Creates a group branch row.
     *
     * @param group group label displayed by the tree table
     * @param brochure path of the brochure shared by the group
     * @return group branch row
     */
    public static TreeTableVehicle groupBranch(String group, Path brochure) {
        var r = new TreeTableVehicle();
        r.setGroup(group);
        r.setBrochure(brochure);
        return r;
    }

    /**
     * Creates a vehicle branch row.
     *
     * @param vin complete vehicle identification number
     * @return vehicle branch row
     */
    public static TreeTableVehicle vinBranch(String vin) {
        var r = new TreeTableVehicle();
        r.setVin(vin);
        return r;
    }

    /**
     * Creates a vehicle-document leaf row.
     *
     * @param document document number displayed by the tree table
     * @param pdf source PDF path
     * @return document leaf row
     */
    public static TreeTableVehicle documentLeaf(String document, Path pdf) {
        var r = new TreeTableVehicle();
        r.setDocument(document);
        r.setPdf(pdf);
        return r;
    }

    /**
     * Returns the group label.
     *
     * @return current group label
     */
    public final String getGroup() {
        return group.get();
    }

    /**
     * Sets the group label.
     *
     * @param group group label to assign
     */
    public final void setGroup(String group) {
        this.group.set(group);
    }

    /**
     * Returns the observable group property.
     *
     * @return group property
     */
    public final StringProperty groupProperty() {
        return group;
    }

    /**
     * Returns the group brochure path.
     *
     * @return current brochure path
     */
    public final Path getBrochure() {
        return brochure.get();
    }

    /**
     * Sets the group brochure path.
     *
     * @param brochure brochure path to assign
     */
    public final void setBrochure(Path brochure) {
        this.brochure.set(brochure);
    }

    /**
     * Returns the observable brochure property.
     *
     * @return brochure property
     */
    public final ObjectProperty<Path> brochureProperty() {
        return brochure;
    }

    /**
     * Returns the complete vehicle identification number.
     *
     * @return current vehicle identification number
     */
    public final String getVin() {
        return vin.get();
    }

    /**
     * Sets the complete vehicle identification number.
     *
     * @param vin vehicle identification number to assign
     */
    public final void setVin(String vin) {
        this.vin.set(vin);
    }

    /**
     * Returns the observable vehicle identification number property.
     *
     * @return vehicle identification number property
     */
    public final StringProperty vinProperty() {
        return vin;
    }

    /**
     * Returns the vehicle document number.
     *
     * @return current document number
     */
    public final String getDocument() {
        return document.get();
    }

    /**
     * Sets the vehicle document number.
     *
     * @param document document number to assign
     */
    public final void setDocument(String document) {
        this.document.set(document);
    }

    /**
     * Returns the observable document number property.
     *
     * @return document number property
     */
    public final StringProperty documentProperty() {
        return document;
    }

    /**
     * Returns the vehicle-specific PDF path.
     *
     * @return current PDF path
     */
    public final Path getPdf() {
        return pdf.get();
    }

    /**
     * Sets the vehicle-specific PDF path.
     *
     * @param pdf PDF path to assign
     */
    public final void setPdf(Path pdf) {
        this.pdf.set(pdf);
    }

    /**
     * Returns the observable vehicle-specific PDF property.
     *
     * @return PDF path property
     */
    public final ObjectProperty<Path> pdfProperty() {
        return pdf;
    }

    /**
     * Compares this row with another object using its generated identity and
     * current property values.
     *
     * @param other object to compare with this row
     * @return {@code true} if both objects represent the same row
     */
    @Override
    public boolean equals(Object other) {
        return this == other || other instanceof TreeTableVehicle that
                && Objects.equals(_oid, that._oid)
                && Objects.equals(getGroup(), that.getGroup())
                && Objects.equals(getBrochure(), that.getBrochure())
                && Objects.equals(getVin(), that.getVin())
                && Objects.equals(getDocument(), that.getDocument())
                && Objects.equals(getPdf(), that.getPdf());
    }

    /**
     * Returns a hash code based on the generated identity and current property
     * values.
     *
     * @return hash code for this row
     */
    @Override
    public int hashCode() {
        return Objects.hash(_oid,
                getGroup(),
                getBrochure(),
                getVin(),
                getDocument(),
                getPdf());
    }
}
