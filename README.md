# Android Pdf Generate
This repository is created to share a solution for the challenge `How to created PDF file in android programming`.
<br>
I solve it by **iTextPdf** library.
<br>You can clone and open this repository by **Android Studio**.
<br>
<br>
more info about **iTextPdf** is available on
+ https://github.com/itext/itextpdf
+ https://itextpdf.com
+ https://itextpdf.com/products/itext-core

<br>

## Output Screenshots
This repository output
<br>
<img src="https://github.com/rezalaki/AndroidGeneratePdf/blob/master/arts/two.jpg?raw=true" alt="screenshot-two" height="650"/>
<br>
My personal project output
<br>
<img src="https://github.com/rezalaki/AndroidGeneratePdf/blob/master/arts/one.jpg?raw=true" alt="screenshot-one" height="650"/>

<br>

## About the source
This project contains these important files, you'd better have a look at:
+ **libs.versions.toml** and **build.gradle [app module]**: to add <ins>iTextPdf dependency</ins><br>
  current version is `com.itextpdf:itextpdf:5.5.13.1`
+ **Manifest**: to add READ, WRITE and MANAGE external storage permissions for different android api versions
+ **MainActivity**: to handle needed permissions to work with files, handling android api versions from 34 to 21 (at this moment)
+ **PdfGenerator**: this is the main class, which creates the PDF file

<br>

#### Main points of  PdfGenerator class
This class contains followings:
+ <ins>Fonts Object</ins>: includes two fonts styles, one for titles and the other one for normal texts. In this Object we have customized Font-Family, Size, Color and Style(bold, italic, ...)
+ <ins>**Elements**</ins>: this inner class contains custom elements are used.
    - <ins>TextTitle</ins>: creates paragraph for Titles which is bigger, bold and in green color
    - <ins>TextBody</ins>: creates paragraph for normal usage
    - <ins>EmptyLine</ins>: creates a simple table with no border, in order to make some vertical spacing
    - **<ins>TableRow</ins>**: this is the most important element. It creates a table having only one row and spilit it based on given arguments. Texts and images are append to each cell of this row/element.
    - <ins>CustomTextCell</ins>: this element is a cell which accepts only Text in format of a Paragraph class or TextTitle or TextBody. Some attributes are customizable, such as borderWidth, borderColor, align, backgroundColor and etc
    - <ins>CustomImageCell</ins>:  this element is a cell which accepts only image. Image is given from Resources Drawables. imageFromDrawables() helper function will prepare image and pass it to our cell.

<br>

#### How to add elements to the document/page?
First, we create an Element(perhaps itself includes some other elements),
and finally we add it to the document variable. So you are going to see

<pre>.also {
   ...
   ...
   document.add(it)
}</pre>

inside the parent element, a lot!
