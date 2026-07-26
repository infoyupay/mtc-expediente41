# Expediente41

Expediente41 is a lightweight desktop utility developed for **VERICAR Perú** to automate the preparation of document packages for the **DSTT-041** procedure submitted to the Peruvian Ministry of Transport and Communications (MTC).

The application eliminates a repetitive and error-prone manual process by generating a single PDF package for each vehicle, ready to be uploaded to the MTC platform.

---

## Motivation

When processing a batch of vehicle approval requests, the MTC platform requires several documents to be uploaded individually.

For each vehicle, the operator typically has to upload:

* Manufacturer brochure
* Special vehicle characteristics
* Technical specification sheet
* Transfer order (when applicable)
* Photographic panel (when applicable)

Because the platform discards the original file names after upload, operators must manually open the preview of every uploaded document to verify that it actually belongs to the intended VIN.

This repetitive verification significantly increases processing time and creates opportunities for human error.

Expediente41 removes this problem by producing a **single consolidated PDF** for every vehicle.

---

## How It Works

The application performs the following steps:

1. Reads an Excel workbook containing the processing batch.
2. Extracts the VIN from cell **C13** of every worksheet.
3. Uses the last four VIN characters as a document identifier.
4. Scans a local directory containing all PDF files.
5. Determines the vehicle group from the document naming convention.
6. Locates the common manufacturer brochure for that group.
7. Concatenates all applicable documents into a single PDF.
8. Produces a text mapping between VIN and group number.

---

## Document Naming Convention

The application relies on deterministic file names.

```
n.1*.pdf              Manufacturer brochure (shared by the entire group)
n.2*xxxx.pdf          Special vehicle characteristics
n.3*xxxx.pdf          Technical specification sheet
n.4*xxxx.pdf          Transfer order (optional)
n.5*xxxx.pdf          Photographic panel (optional)
```

Where:

* **n** is the vehicle group assigned by the operator.
* **xxxx** are the last four characters of the VIN.

Example:

```
3.1 Manufacturer Brochure.pdf
3.2 Special Characteristics 4821.pdf
3.3 Technical Sheet 4821.pdf
3.4 Transfer Order 4821.pdf
3.5 Photographic Panel 4821.pdf
```

For the VIN ending in **4821**, Expediente41 generates a single document containing:

1. Manufacturer brochure
2. Special vehicle characteristics
3. Technical specification sheet
4. Transfer order (if present)
5. Photographic panel (if present)

---

## Validation

Before generating any output, the application validates the document set.

Checks include:

* VIN exists in every worksheet.
* Mandatory documents are present.
* All documents belonging to the same VIN reference the same group number.
* The corresponding group brochure exists.
* No conflicting document groups exist for a single VIN.
* Documents that do not match any VIN are reported.

Generation proceeds only after the document set is internally consistent.

---

## Output

For every vehicle, the application generates:

* One consolidated PDF ready for upload to the MTC platform.

It also generates a VIN-to-group mapping, exported both as plain text and copied to the system clipboard.

Example:

```
LZZ5EL3D5RA123456    3
LZZ5EL3D5RA123789    3
LZZ5EL3D5RA987654    7
```

---

## Design Goals

* Zero database dependencies.
* Local execution.
* Deterministic processing.
* Minimal user interaction.
* Fast processing of large batches.
* Reduced upload errors.
* Fully reproducible output.

---

## Technology Stack

* Java
* JavaFX
* Apache POI
* Apache PDFBox
* Gradle

---

## Project Name

The project is named **Expediente41** because it automates the preparation of document packages for the **DSTT-041** administrative procedure before the Peruvian Ministry of Transport and Communications (MTC).

The goal is not to automate the submission itself, but to streamline the document preparation stage, reducing processing time and minimizing the risk of attaching incorrect files.
