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

- Document `1` is the manufacturer brochure shared by the entire group.
- Document `2` is the vehicle request.
- Document `3` is the technical specification sheet.
- Documents `4` and above are optional additional documents.

The brochure does not carry a VIN marker. Every vehicle-specific document
(`2` and above) must end with a space followed by the final four alphanumeric
characters of the VIN.

Recommended format:

```text
<group>.<document> <description> <last-four-VIN-characters>.pdf
```

Example for group `3` and a vehicle whose VIN ends in `4821`:

```text
03.01 Manufacturer Brochure.pdf
03.02 Special Characteristics 4821.pdf
03.03 Technical Sheet 4821.pdf
03.04 Transfer Order 4821.pdf
03.05 Photographic Panel 4821.pdf
```

Descriptions are free text. The significant parts are the numeric
`group.document` prefix and, for vehicle-specific documents, the
four-character alphanumeric marker immediately before `.pdf`. File suffix
matching is case-insensitive.

Each group must contain exactly one brochure. Every vehicle must contain
documents `2` and `3`; document numbers must not be duplicated for the same
vehicle and group.

## Using the application

1. Open Expediente41.
2. Select the prepared workspace directory, or drag the directory onto the
   workspace table.
3. Review the detected groups, VINs, and documents.
4. Correct any reported inconsistency and reload the workspace.
5. Select **Generar** once the analysis is valid.

The application generates files only when the workbook and PDF inventory are
internally consistent. Among other checks, it detects missing or duplicate
brochures, missing mandatory documents, unknown or ambiguous VIN markers,
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

Its contents are merged in this order:

1. The group brochure (`n.1`).
2. The vehicle request (`n.2`).
3. The technical specification sheet (`n.3`).
4. Any additional documents (`n.4+`) in ascending document-number order.

Example:

```text
Expedientes/Individuales/LZZ5EL3D5RA124821 ver2026-07-27_14-35.pdf
```

### Consolidated documents

Exactly two lot-wide files are created:

```text
Expedientes/Consolidados/Solicitudes consolidadas verYYYY-MM-dd_HH-mm.pdf
Expedientes/Consolidados/Fichas técnicas consolidadas verYYYY-MM-dd_HH-mm.pdf
```

The first contains every `n.2` request and the second every `n.3` technical
sheet, ordered by document group and vehicle marker. Their names intentionally
do not retain any individual group identity.

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
