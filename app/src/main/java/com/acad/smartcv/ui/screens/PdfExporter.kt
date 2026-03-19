package com.acad.smartcv.ui.screens

import android.content.Context
import android.os.Environment
import com.acad.smartcv.data.model.*
import com.itextpdf.io.font.constants.StandardFonts
import com.itextpdf.io.image.ImageDataFactory
import com.itextpdf.kernel.colors.DeviceRgb
import com.itextpdf.kernel.font.PdfFontFactory
import com.itextpdf.kernel.pdf.PdfDocument
import com.itextpdf.kernel.pdf.PdfWriter
import com.itextpdf.layout.Document
import com.itextpdf.layout.borders.SolidBorder
import com.itextpdf.layout.element.Cell
import com.itextpdf.layout.element.Image
import com.itextpdf.layout.element.LineSeparator
import com.itextpdf.layout.element.Paragraph
import com.itextpdf.layout.element.Table
import com.itextpdf.layout.element.Text
import com.itextpdf.layout.properties.HorizontalAlignment
import com.itextpdf.layout.properties.TextAlignment
import com.itextpdf.layout.properties.UnitValue
import java.io.File

object PdfExporter {

    private val ACCENT   = DeviceRgb(24, 95, 165)
    private val LIGHT_BG = DeviceRgb(230, 241, 251)
    private val GRAY     = DeviceRgb(100, 100, 110)

    fun export(context: Context, profile: FullProfile, application: Application? = null): File {
        val dir = context.getExternalFilesDir(Environment.DIRECTORY_DOCUMENTS) ?: context.filesDir
        val file = File(dir, "ACAD_SmartCV_${profile.profile.lastName}_${System.currentTimeMillis()}.pdf")

        val writer = PdfWriter(file)
        val pdf    = PdfDocument(writer)
        val doc    = Document(pdf)
        doc.setMargins(36f, 45f, 36f, 45f)

        val bold    = PdfFontFactory.createFont(StandardFonts.HELVETICA_BOLD)
        val regular = PdfFontFactory.createFont(StandardFonts.HELVETICA)
        val italic  = PdfFontFactory.createFont(StandardFonts.HELVETICA_OBLIQUE)

        val p = profile.profile

        // ── Header ─────────────────────────────────────────────────────────
        doc.add(Paragraph("ACAD SmartCV")
            .setFont(bold).setFontSize(9f).setFontColor(ACCENT)
            .setTextAlignment(TextAlignment.RIGHT))

        // Load photo using correct field name
        val photoBitmap = p.profilePhotoPath.takeIf { it.isNotBlank() }?.let {
            ProfilePhotoHelper.loadPhoto(it)
        }

        if (photoBitmap != null) {
            val headerTable = Table(UnitValue.createPercentArray(floatArrayOf(75f, 25f)))
                .setWidth(UnitValue.createPercentValue(100f))

            val leftCell = Cell().setBorder(com.itextpdf.layout.borders.Border.NO_BORDER)
            leftCell.add(Paragraph("${p.firstName} ${p.lastName}")
                .setFont(bold).setFontSize(22f).setFontColor(ACCENT))
            if (p.title.isNotBlank())
                leftCell.add(Paragraph(p.title + if (p.organization.isNotBlank()) " · ${p.organization}" else "")
                    .setFont(regular).setFontSize(12f).setFontColor(GRAY))
            val contact = listOfNotNull(
                p.location.ifBlank { null }, p.email.ifBlank { null }, p.phone.ifBlank { null }
            ).joinToString("  |  ")
            if (contact.isNotBlank())
                leftCell.add(Paragraph(contact).setFont(italic).setFontSize(9f).setFontColor(GRAY))
            if (p.skills.isNotBlank())
                leftCell.add(Paragraph(p.skills.split(",").joinToString("  ·  ") { it.trim() })
                    .setFont(regular).setFontSize(9f))
            headerTable.addCell(leftCell)

            val photoBytes = ProfilePhotoHelper.toByteArray(photoBitmap)
            val img = Image(ImageDataFactory.create(photoBytes))
                .setWidth(70f).setHeight(70f)
                .setHorizontalAlignment(HorizontalAlignment.RIGHT)
            val rightCell = Cell().setBorder(com.itextpdf.layout.borders.Border.NO_BORDER)
                .setTextAlignment(TextAlignment.RIGHT)
            rightCell.add(img)
            headerTable.addCell(rightCell)
            doc.add(headerTable)

        } else {
            doc.add(Paragraph("${p.firstName} ${p.lastName}")
                .setFont(bold).setFontSize(22f).setFontColor(ACCENT))
            if (p.title.isNotBlank())
                doc.add(Paragraph(p.title + if (p.organization.isNotBlank()) " · ${p.organization}" else "")
                    .setFont(regular).setFontSize(12f).setFontColor(GRAY))
            val contact = listOfNotNull(
                p.location.ifBlank { null }, p.email.ifBlank { null }, p.phone.ifBlank { null }
            ).joinToString("  |  ")
            if (contact.isNotBlank())
                doc.add(Paragraph(contact).setFont(italic).setFontSize(9f).setFontColor(GRAY))
            if (p.skills.isNotBlank())
                doc.add(Paragraph(p.skills.split(",").joinToString("  ·  ") { it.trim() })
                    .setFont(regular).setFontSize(9f))
        }

        doc.add(LineSeparator(com.itextpdf.kernel.pdf.canvas.draw.SolidLine(1f)).setStrokeColor(ACCENT))
        doc.add(Paragraph("\n"))

        if (p.bio.isNotBlank()) {
            doc.add(sectionHeader("Professional Summary", bold))
            doc.add(Paragraph(p.bio).setFont(regular).setFontSize(10f).setMultipliedLeading(1.4f))
            spacer(doc)
        }

        if (profile.education.isNotEmpty()) {
            doc.add(sectionHeader("Education", bold))
            profile.education.forEach { e ->
                doc.add(Paragraph(e.degree).setFont(bold).setFontSize(10f))
                doc.add(Paragraph("${e.institution}${if (e.year.isNotBlank()) "  ·  ${e.year}" else ""}").setFont(regular).setFontSize(9f).setFontColor(GRAY))
            }
            spacer(doc)
        }

        if (profile.publications.isNotEmpty()) {
            doc.add(sectionHeader("Publications (${profile.publications.size})", bold))
            profile.publications.forEachIndexed { i, pub ->
                val entry = Paragraph()
                entry.add(Text("${i + 1}. ").setFont(bold).setFontSize(9f).setFontColor(ACCENT))
                entry.add(Text(pub.title + ". ").setFont(bold).setFontSize(9f))
                if (pub.authors.isNotBlank()) entry.add(Text("${pub.authors}. ").setFont(italic).setFontSize(9f))
                if (pub.journal.isNotBlank()) entry.add(Text(pub.journal).setFont(italic).setFontSize(9f).setFontColor(ACCENT))
                if (pub.year.isNotBlank()) entry.add(Text(", ${pub.year}.").setFont(regular).setFontSize(9f))
                if (pub.doi.isNotBlank()) entry.add(Text(" DOI: ${pub.doi}").setFont(regular).setFontSize(8f).setFontColor(GRAY))
                doc.add(entry.setMultipliedLeading(1.3f))
            }
            spacer(doc)
        }

        if (profile.projects.isNotEmpty()) {
            doc.add(sectionHeader("Projects", bold))
            profile.projects.forEach { pr ->
                doc.add(Paragraph(pr.title).setFont(bold).setFontSize(10f))
                val sub = listOfNotNull(pr.role.ifBlank { null }, pr.duration.ifBlank { null }).joinToString("  ·  ")
                if (sub.isNotBlank()) doc.add(Paragraph(sub).setFont(italic).setFontSize(9f).setFontColor(GRAY))
                if (pr.description.isNotBlank()) doc.add(Paragraph(pr.description).setFont(regular).setFontSize(9f).setMultipliedLeading(1.3f))
                if (pr.tags.isNotBlank()) doc.add(Paragraph("Tags: ${pr.tags}").setFont(italic).setFontSize(8f).setFontColor(GRAY))
                doc.add(Paragraph(""))
            }
            spacer(doc)
        }

        if (profile.grants.isNotEmpty()) {
            doc.add(sectionHeader("Grants & Funding", bold))
            profile.grants.forEach { g ->
                val line = Paragraph()
                line.add(Text(g.title).setFont(bold).setFontSize(10f))
                if (g.amount.isNotBlank()) line.add(Text("  –  ${g.amount}").setFont(regular).setFontSize(9f).setFontColor(ACCENT))
                doc.add(line)
                val sub = listOfNotNull(g.agency.ifBlank { null }, g.period.ifBlank { null }, g.role.ifBlank { null }).joinToString("  ·  ")
                if (sub.isNotBlank()) doc.add(Paragraph(sub).setFont(regular).setFontSize(9f).setFontColor(GRAY))
            }
            spacer(doc)
        }

        if (profile.awards.isNotEmpty()) {
            doc.add(sectionHeader("Awards & Honours", bold))
            profile.awards.forEach { a ->
                doc.add(Paragraph("${a.name}${if (a.year.isNotBlank()) " (${a.year})" else ""}").setFont(bold).setFontSize(10f))
                if (a.awardingBody.isNotBlank()) doc.add(Paragraph(a.awardingBody).setFont(regular).setFontSize(9f).setFontColor(GRAY))
            }
            spacer(doc)
        }

        if (profile.achievements.isNotEmpty()) {
            doc.add(sectionHeader("Other Achievements", bold))
            profile.achievements.forEach { a ->
                doc.add(Paragraph("${a.title}  [${a.category.name.replace('_', ' ')}]").setFont(bold).setFontSize(10f))
                if (a.description.isNotBlank()) doc.add(Paragraph(a.description).setFont(regular).setFontSize(9f))
            }
        }

        doc.add(Paragraph("\n"))
        doc.add(LineSeparator(com.itextpdf.kernel.pdf.canvas.draw.SolidLine(0.5f)).setStrokeColor(GRAY))
        doc.add(Paragraph("Generated by ACAD SmartCV · acadapp.in")
            .setFont(italic).setFontSize(8f).setFontColor(GRAY)
            .setTextAlignment(TextAlignment.CENTER))

        doc.close()
        return file
    }

    private fun sectionHeader(title: String, bold: com.itextpdf.kernel.font.PdfFont): Paragraph =
        Paragraph(title.uppercase())
            .setFont(bold).setFontSize(9f).setFontColor(ACCENT)
            .setCharacterSpacing(1.2f)
            .setBorderBottom(SolidBorder(LIGHT_BG, 2f))
            .setMarginBottom(6f)

    private fun spacer(doc: Document) { doc.add(Paragraph("\n")) }
}
