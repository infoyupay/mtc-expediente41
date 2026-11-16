# Expediente41

Expediente41 is a lightweight desktop utility developed for **VERICAR Perú**.
It prepares the PDF document packages used in the **DSTT-041** procedure before
the Peruvian Ministry of Transport and Communications (MTC).

The application reads the vehicle identifiers from a spreadsheet, correlates
them with deterministically named PDF files, validates the complete batch, and
generates both the individual vehicle expedientes and the lot-wide consolidated
documents.

## Preparing a workspace

Create or choose a directory to use as the workspace. The application must have
read and write access to that directory.

Place the following input files in it:

- Exactly one `.xlsx` or `.ods` workbook.
- All source `.pdf` documents required by the batch.

The simplest arrangement is to put every input file directly in the same
directory. Subdirectories are also scanned recursively, except for the
`Expedientes` output directory created by the application.

Each worksheet in the workbook represents one vehicle. Its complete VIN must be
stored in cell **C13**. Expediente41 uses the final four characters of the VIN
to match it with its PDF documents; those four characters must be ASCII letters
or digits.

Example:

```text
LZZ5EL3D5RA124821
             └── 4821
```

Do not place two supported workbooks in the workspace. Expediente41 requires
exactly one workbook and will reject an empty or ambiguous selection.

## PDF naming convention

Every source PDF name must begin with:

```text
group.document
```

Both values are positive integers separated by a dot. Leading zeroes are
supported in either value, so `01.01` and `01.1` are equivalent to `1.1`:

Document positions are ordered numerically within each group. Position `1`
remains the shared catalog; positions `2` and `3` remain mandatory but can
now contain either shared or individual documents. Positions `4` and above
are optional.

A single PDF without a VIN marker is shared by every vehicle in its group.
Repeated positions must contain one PDF per vehicle, ending with a space and
the final four ASCII alphanumeric characters of the VIN. A PDF carrying a
marker stays individual even when it is the only PDF at that position, which
supports groups with a single vehicle. Position `1` is always shared, so a
catalog description may end with a year.

```text
1.1 Catálogo grupo A.pdf
1.2 0025.pdf
1.2 A1B2.pdf
1.3 Anexo común grupo A.pdf
2.1 Catálogo grupo B.pdf
2.2 3451.pdf
2.3 Anexo común grupo B.pdf
```

Descriptions are free text. Leading zeroes in the prefix and case-insensitive
PDF extensions are supported. Shared documents must not be repeated or mixed
with individual documents at the same position. Each group must contain
exactly one catalog and at least one vehicle-specific document. Vehicle document
numbers must not be duplicated for the same marker and group. A shared document
at position `2` or `3` satisfies that mandatory position for every group vehicle.

## Using the application

1. Open Expediente41.
2. Select the prepared workspace directory, or drag the directory onto the
   workspace table.
3. Review the detected groups, VINs, and documents.
4. Correct any reported inconsistency and reload the workspace.
5. Select **Generar** once the analysis is valid.

The application generates files only when the workbook and PDF inventory are
internally consistent. Among other checks, it detects missing or duplicate
brochures, missing mandatory positions, unknown or ambiguous VIN markers,
vehicles assigned to multiple groups, duplicate document numbers, and workbook
vehicles without PDFs.

## Generated output

Expediente41 creates the following structure inside the selected workspace:

```text
Expedientes/
├── Individuales/
└── Consolidados/
```

### Individual expedientes

One file is created per VIN:

```text
Expedientes/Individuales/<VIN> verYYYY-MM-dd_HH-mm.pdf
```

Its contents are merged in ascending document-number order, combining every
shared document with the documents for that vehicle only.

For the example above, vehicle `0025` receives `1.1`, `1.2 0025`, and `1.3`.
Vehicle `A1B2` receives `1.1`, `1.2 A1B2`, and `1.3`.

### Group expedientes

One complete PDF is created per group:

```text
Expedientes/Consolidados/Expediente Grupo <group> verYYYY-MM-dd_HH-mm.pdf
```

Each contains the shared documents once, plus all individual documents from
that group. Sources are ordered numerically by document position, and then
by vehicle marker within each position. For example, group `1` contains
`1.1`, `1.2 0025`, `1.2 A1B2`, and `1.3`.

All files produced by one generation share the same timestamp. Existing output
files are never overwritten. Running generation twice within the same minute
therefore reports a version collision; wait for the next minute or move the
previous output before trying again.

## Design goals

- Local execution with no database dependency.
- Deterministic input validation and output ordering.
- Minimal user interaction.
- Reduced upload and vehicle-assignment errors.
- Reproducible, timestamped output.

## Technology stack

- Java 26
- JavaFX 26
- JDK XML streaming and ZIP APIs
- Apache PDFBox
- Gradle

## Project name

The project is named **Expediente41** because it automates the document
preparation stage for the **DSTT-041** administrative procedure. It does not
submit documents to the MTC platform; it produces the individual and
consolidated PDF files that the operator will upload.
