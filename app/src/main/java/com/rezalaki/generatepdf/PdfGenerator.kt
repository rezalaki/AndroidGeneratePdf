package com.rezalaki.generatepdf

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.os.Environment
import com.itextpdf.text.BaseColor
import com.itextpdf.text.Document
import com.itextpdf.text.Element
import com.itextpdf.text.Font
import com.itextpdf.text.FontFactory
import com.itextpdf.text.Image
import com.itextpdf.text.PageSize
import com.itextpdf.text.Paragraph
import com.itextpdf.text.pdf.BaseFont
import com.itextpdf.text.pdf.PdfPCell
import com.itextpdf.text.pdf.PdfPTable
import com.itextpdf.text.pdf.PdfWriter
import com.itextpdf.text.pdf.languages.ArabicLigaturizer
import com.itextpdf.text.pdf.languages.LanguageProcessor
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.FileOutputStream


class PdfGenerator(
    private val context: Context,
    private val onlyPersianLanguage: Boolean,
    private val onSuccess: (pdfPath: String) -> Unit,
    private val onFailure: (errorMessage: String) -> Unit
) {

    companion object {
        const val DIRECTORY_NAME = "reports"
        val DOWNLOADS_PATH: String = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS).path

        val COLOR_TEXT_TITLE = BaseColor(57, 110, 96)
        val COLOR_TEXT_BODY = BaseColor(48, 44, 44)
        val COLOR_BACKGROUND_TABLE_HEADER = BaseColor(228, 255, 247)
        val COLOR_BACKGROUND_TABLE_ROW_GRAY = BaseColor(236, 236, 236)
        val COLOR_BORDER = BaseColor(207, 66, 35)
    }

    // more info: https://www.javadoc.io/doc/com.itextpdf/itextpdf/5.1.1/com/itextpdf/text/pdf/ArabicLigaturizer.html
    private val persianOrArabicLanguage: LanguageProcessor by lazy { ArabicLigaturizer() }

    private object Fonts {
        val Title: Font = FontFactory.getFont(
            "assets/fonts/vazir_medium.ttf", BaseFont.IDENTITY_H, 16F, Font.BOLD, COLOR_TEXT_TITLE
        )
        val Body: Font = FontFactory.getFont(
            "assets/fonts/vazir_medium.ttf", BaseFont.IDENTITY_H, 12F, Font.NORMAL, COLOR_TEXT_BODY
        )
    }

    inner class Elements {

        fun TextTitle(text: String) =
            if (onlyPersianLanguage) Paragraph(persianOrArabicLanguage.process(text), Fonts.Title)
            else Paragraph(text, Fonts.Title)

        fun TextBody(text: String) =
            if (onlyPersianLanguage) Paragraph(persianOrArabicLanguage.process(text), Fonts.Body)
            else Paragraph(text, Fonts.Body)

        fun EmptyLine(height: Float = 30F) = TableRow(1, floatArrayOf(1F), height = height).also {
            it.addCell(
                CustomTextCell(
                    elements.TextBody(""),
                    height = height,
                    borderWidth = 1F,
                    borderBaseColor = BaseColor.WHITE,
                    align = null,
                    bgColor = null
                )
            )
        }

        fun TableRow(columnsCount: Int, cellsWidth: FloatArray, height: Float = 80F): PdfPTable {
            if (cellsWidth.size != columnsCount) throw Exception("in tableRow() function, cellsWidth array size is not equal to columnsCount")
            return PdfPTable(columnsCount).apply {
                widthPercentage = 100F
                setWidths(cellsWidth)
                defaultCell.apply {
                    verticalAlignment = Element.ALIGN_CENTER
                    horizontalAlignment = Element.ALIGN_CENTER
                    paddingTop = 4F
                    paddingBottom = 8F
                    fixedHeight = height
                }
            }
        }

        fun CustomTextCell(
            paragraph: Paragraph,
            borderWidth: Float,
            align: Int?,
            bgColor: BaseColor?,
            borderBaseColor: BaseColor?,
            height: Float?
        ) = PdfPCell(paragraph).apply {
            horizontalAlignment = align ?: Element.ALIGN_CENTER
            if (bgColor != null) backgroundColor = bgColor
            if (borderBaseColor != null) borderColor = borderBaseColor
            borderWidthTop = borderWidth
            borderWidthBottom = borderWidth
            borderWidthLeft = borderWidth
            borderWidthRight = borderWidth
            if (height != null) fixedHeight = height
            verticalAlignment = Element.ALIGN_MIDDLE
            isUseAscender = true
            paddingLeft = 2f
            paddingRight = 2f
            paddingTop = 4f
            paddingBottom = 6f
        }

        private fun imageFromDrawables(drawableRes: Int): Image {
            val bitmap = BitmapFactory.decodeResource(context.resources, drawableRes)
            val byteOutStream = ByteArrayOutputStream()
            bitmap.compress(Bitmap.CompressFormat.JPEG, 80, byteOutStream)
            val byteArray = byteOutStream.toByteArray()
            return Image.getInstance(byteArray).apply {
                scaleToFit(1F, 1F)
            }
        }

        fun CustomImageCell(
            imageDrawable: Int,
            align: Int = Element.ALIGN_CENTER,
            height: Float
        ) = PdfPCell(imageFromDrawables(imageDrawable), true).apply {
            fixedHeight = height
            borderWidthTop = 0F
            borderWidthBottom = 0F
            borderWidthLeft = 0F
            borderWidthRight = 0F
            horizontalAlignment = align
        }

    }

    private lateinit var pdfWriter: PdfWriter
    private lateinit var elements: Elements


    private fun createFile(): File? {
        return try {
            // root/Download/reports
            val targetDir = File(DOWNLOADS_PATH + File.separator + DIRECTORY_NAME)

            var makeDirSuccess = true
            if (targetDir.exists().not()) {
                makeDirSuccess = targetDir.mkdir()
            }

            val randomNumber = (1_000..9_999).random()
            val fileName = "Report-${randomNumber}.pdf"

            // if (makeDirSuccess) root/Download/reports/Report-5555.pdf else  root/Download/Report-5555.pdf
            val pdfFile = if (makeDirSuccess) File(targetDir, fileName)
                          else File(DOWNLOADS_PATH + File.separator, fileName)
            pdfFile.createNewFile()
            pdfFile

        } catch (ex: Exception) {
            null
        }
    }

    private fun createDocument() = Document().apply {
        pageSize = PageSize.A4
        setMargins(20F, 20F, 16F, 16F)
    }

    fun generate(personList: List<Person>) {
        val file = createFile()
        if (file == null) {
            onFailure("error in making file in device!")
            return
        }

        val document = createDocument()

        val fileOutputStream = FileOutputStream(file)
        pdfWriter = PdfWriter.getInstance(document, fileOutputStream).apply {
            setFullCompression()
        }

        try {
            document.open()

            elements = Elements()

            elements.TableRow(columnsCount = 1, cellsWidth =  floatArrayOf(1F)).also {
                it.addCell(
                    elements.CustomTextCell(
                        paragraph = elements.TextTitle("I have been generated by iTextPdf"),
                        borderWidth = 0F,
                        align = null,
                        bgColor = null,
                        borderBaseColor = null,
                        height = null
                    )
                )
                document.add(it)
            }

            elements.EmptyLine().also { document.add(it) }

            elements.TableRow(columnsCount= 1, cellsWidth =  floatArrayOf(1F), height = 30F).also {
                it.addCell(
                    elements.CustomTextCell(
                        Paragraph("There is a testing table having 3 cells | Normal Paragraph"),
                        borderWidth = 0F,
                        align = Element.ALIGN_LEFT,
                        bgColor = BaseColor.WHITE,
                        borderBaseColor = BaseColor.WHITE,
                        height = 30F
                    )
                )
                document.add(it)
            }

            elements.TableRow(columnsCount=3 ,cellsWidth = floatArrayOf(1F, 1F, 1F), height = 45F).also {
                it.addCell(
                    elements.CustomTextCell(
                        elements.TextBody("توسعه دهنده: رضا لکی"),
                        borderWidth = 0.3F,
                        align = Element.ALIGN_RIGHT,
                        bgColor = null,
                        borderBaseColor = null,
                        height = null
                    )
                )
                it.addCell(
                    Paragraph("I am a normal text inside default cell")
                )
                it.addCell(
                    elements.TextTitle("developer: Reza Laki")
                )

                document.add(it)
            }

            elements.EmptyLine().also { document.add(it) }

            elements.TableRow(columnsCount = 1, cellsWidth =  floatArrayOf(1F), height = 30F).also {
                it.addCell(
                    elements.CustomTextCell(
                        elements.TextTitle("There is some data to show | Title element"),
                        borderWidth = 0F,
                        align = Element.ALIGN_LEFT,
                        bgColor = null,
                        borderBaseColor = BaseColor.WHITE,
                        height = 30F
                    )
                )
                document.add(it)
            }

            elements.EmptyLine(5F).also { document.add(it) }

            // table header
            elements.TableRow(columnsCount = 4, cellsWidth = floatArrayOf(1F, 1F, 3F, 1F)).also {
                it.addCell(
                    elements.CustomTextCell(
                        paragraph = elements.TextTitle("#"),
                        borderWidth = 0F,
                        borderBaseColor = COLOR_BORDER,
                        align = Element.ALIGN_CENTER,
                        bgColor = COLOR_BACKGROUND_TABLE_HEADER,
                        height = null
                    )
                )
                it.addCell(
                    elements.CustomTextCell(
                        paragraph = elements.TextTitle("id"),
                        borderWidth = 0F,
                        borderBaseColor = COLOR_BORDER,
                        align = Element.ALIGN_CENTER,
                        bgColor = COLOR_BACKGROUND_TABLE_HEADER,
                        height = null
                    )
                )
                it.addCell(
                    elements.CustomTextCell(
                        paragraph = elements.TextTitle("fullName"),
                        borderWidth = 0F,
                        borderBaseColor = COLOR_BORDER,
                        align = Element.ALIGN_CENTER,
                        bgColor = COLOR_BACKGROUND_TABLE_HEADER,
                        height = null
                    )
                )
                it.addCell(
                    elements.CustomTextCell(
                        paragraph = elements.TextTitle("age"),
                        borderWidth = 0F,
                        borderBaseColor = COLOR_BORDER,
                        align = Element.ALIGN_CENTER,
                        bgColor = COLOR_BACKGROUND_TABLE_HEADER,
                        height = null
                    )
                )

                document.add(it)
            }

            // table body
            personList.onEachIndexed { index, person ->
                elements.TableRow(columnsCount = 4, cellsWidth = floatArrayOf(1F, 1F, 3F, 1F)).also {
                    val rowIndex = index + 1
                    val cellBgColor = if(index % 2 == 0) COLOR_BACKGROUND_TABLE_ROW_GRAY else BaseColor.WHITE

                    it.addCell(
                        elements.CustomTextCell(
                            paragraph = elements.TextBody(rowIndex.toString()),
                            borderWidth = 0F,
                            borderBaseColor = COLOR_BORDER,
                            align = Element.ALIGN_CENTER,
                            bgColor = cellBgColor,
                            height = null
                        )
                    )
                    it.addCell(
                        elements.CustomTextCell(
                            paragraph = elements.TextBody(person.id.toString()),
                            borderWidth = 0F,
                            borderBaseColor = COLOR_BORDER,
                            align = Element.ALIGN_CENTER,
                            bgColor = cellBgColor,
                            height = null
                        )
                    )
                    it.addCell(
                        elements.CustomTextCell(
                            paragraph = elements.TextBody(person.fullName),
                            borderWidth = 0F,
                            borderBaseColor = COLOR_BORDER,
                            align = Element.ALIGN_CENTER,
                            bgColor = cellBgColor,
                            height = null
                        )
                    )
                    it.addCell(
                        elements.CustomTextCell(
                            paragraph = elements.TextBody(person.age.toString()),
                            borderWidth = 0F,
                            borderBaseColor = COLOR_BORDER,
                            align = Element.ALIGN_CENTER,
                            bgColor = cellBgColor,
                            height = null
                        )
                    )

                    document.add(it)
                }
            }

            elements.EmptyLine(height = 30F).also { document.add(it) }

            elements.TableRow(columnsCount =1, cellsWidth = floatArrayOf(1F), height = 30F).also {
                it.addCell(
                    elements.CustomTextCell(
                        elements.TextTitle("There is also an image on the right side"),
                        borderWidth = 1F,
                        align = Element.ALIGN_LEFT,
                        bgColor = null,
                        borderBaseColor = BaseColor.WHITE,
                        height = 30F
                    )
                )
                document.add(it)
            }

            elements.EmptyLine(height = 10F).also { document.add(it) }

            // Qrcode image
            elements.TableRow(columnsCount = 1, cellsWidth = floatArrayOf(1F)).also {
                it.addCell(
                    elements.CustomImageCell(
                        imageDrawable = R.drawable.qrcode,
                        align = Element.ALIGN_RIGHT,
                        height = 150F
                    )
                )
                document.add(it)
            }

            // text below of Qrcode image
            elements.TableRow(columnsCount = 1, cellsWidth =  floatArrayOf(1F), height = 30F).also {
                it.addCell(
                    elements.CustomTextCell(
                        Paragraph("plz scan to find out my LinkedIn profile :)"),
                        borderWidth = 0F,
                        align = Element.ALIGN_RIGHT,
                        bgColor = null,
                        borderBaseColor = null,
                        height = 30F
                    )
                )
                document.add(it)
            }


            onSuccess(file.absolutePath)

        } catch (ex: Exception) {
            onFailure(ex.message.toString())

        } finally {
            if (document.isOpen) document.close()
            pdfWriter.close()
        }

    }

}
