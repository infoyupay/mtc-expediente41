package com.infoyupay.mtcexpediente41.javafx.treetable;

import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;

import java.nio.file.Path;
import java.util.Objects;
import java.util.UUID;

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

    public TreeTableVehicle() {
        this._oid = UUID.randomUUID();
    }

    public static TreeTableVehicle groupBranch(String group, Path brochure){
        var r = new TreeTableVehicle();
        r.setGroup(group);
        r.setBrochure(brochure);
        return r;
    }

    public static TreeTableVehicle vinBranch(String vin){
        var r = new TreeTableVehicle();
        r.setVin(vin);
        return r;
    }

    public static TreeTableVehicle documentLeaf(String document, Path pdf){
        var r = new TreeTableVehicle();
        r.setDocument(document);
        r.setPdf(pdf);
        return r;
    }

    /**
     * FX Accessor - getter.
     *
     * @return value of {@link #groupProperty()}.get();
     */
    public final String getGroup() {
        return group.get();
    }

    /**
     * FX Accessor - setter.
     *
     * @param group value to assign into {@link #groupProperty()}.
     */
    public final void setGroup(String group) {
        this.group.set(group);
    }

    /**
     * TODO: write documentation.
     *
     * @return JavaFX Property.
     */
    public final StringProperty groupProperty() {
        return group;
    }

    /**
     * FX Accessor - getter.
     *
     * @return value of {@link #brochureProperty()}.get();
     */
    public final Path getBrochure() {
        return brochure.get();
    }

    /**
     * FX Accessor - setter.
     *
     * @param brochure value to assign into {@link #brochureProperty()}.
     */
    public final void setBrochure(Path brochure) {
        this.brochure.set(brochure);
    }

    /**
     * TODO: write documentation.
     *
     * @return javaFX Property.
     */
    public final ObjectProperty<Path> brochureProperty() {
        return brochure;
    }

    /**
     * FX Accessor - getter.
     *
     * @return value of {@link #vinProperty()}.get();
     */
    public final String getVin() {
        return vin.get();
    }

    /**
     * FX Accessor - setter.
     *
     * @param vin value to assign into {@link #vinProperty()}.
     */
    public final void setVin(String vin) {
        this.vin.set(vin);
    }

    /**
     * TODO: write documentation.
     *
     * @return JavaFX Property.
     */
    public final StringProperty vinProperty() {
        return vin;
    }

    /**
     * FX Accessor - getter.
     *
     * @return value of {@link #documentProperty()}.get();
     */
    public final String getDocument() {
        return document.get();
    }

    /**
     * FX Accessor - setter.
     *
     * @param document value to assign into {@link #documentProperty()}.
     */
    public final void setDocument(String document) {
        this.document.set(document);
    }

    /**
     * TODO: write documentation.
     *
     * @return JavaFX Property.
     */
    public final StringProperty documentProperty() {
        return document;
    }

    /**
     * FX Accessor - getter.
     *
     * @return value of {@link #pdfProperty()}.get();
     */
    public final Path getPdf() {
        return pdf.get();
    }

    /**
     * FX Accessor - setter.
     *
     * @param pdf value to assign into {@link #pdfProperty()}.
     */
    public final void setPdf(Path pdf) {
        this.pdf.set(pdf);
    }

    /**
     * TODO: write documentation.
     *
     * @return javaFX Property.
     */
    public final ObjectProperty<Path> pdfProperty() {
        return pdf;
    }

    @Override
    public boolean equals(Object o) {
        return this == o || o instanceof TreeTableVehicle that &&
                Objects.equals(_oid, that._oid)
                && Objects.equals(getGroup(), that.getGroup())
                && Objects.equals(getBrochure(), that.getBrochure())
                && Objects.equals(getVin(), that.getVin())
                && Objects.equals(getDocument(), that.getDocument())
                && Objects.equals(getPdf(), that.getPdf());
    }

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
