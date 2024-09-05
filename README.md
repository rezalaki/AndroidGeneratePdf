# Android Pdf Generate
This repository is created to share a solution for the challenge: `How to created PDF file in android` by **iTextPdf** library. So you can clone and open it by **Android Studio**.

<br>
more info about **iTextPdf** avaiable on [https://itextpdf.com](https://itextpdf.com) and [https://itextpdf.com/products/itext-core](https://itextpdf.com/products/itext-core)

## About the source
This project contains these important files, you'd better have a look at:
+ **libs.versions.toml** and **build.gradle [app module]**: to add <u>iTextPdf dependency</u><br>
  current version is `com.itextpdf:itextpdf:5.5.13.1`
+ **Manifest**: to add READ, WRITE and MANAGE external storage permissions for different android api versions
+ **MainActivity**: to handle needed permissions to work with files, handling android api versions from 34 to 21 (at this moment)
+ **PdfGenerator**: this is the main class, which creates the PDF file
  <br>

#### Main points of  PdfGenerator class
This class contains followings:
+ <u>Fonts Object</u>: includes two fonts styles, one for titles and the other one for normal texts. In this Object we have customized Font-Family, Size, Color and Style[bold, italic, ...]
+ <u>**Elements**</u>: this inner class contains custom elements are used.
    - <u>TextTitle</u>: creates paragraph for Titles which is bigger, bold and in green color
    - <u>TextBody</u>: creates paragraph for normal usage
    - <u>EmptyLine</u>: creates a simple table with no border, in order to make some vertical spacing
    - **<u>TableRow</u>**: this is the most important element. It creates a table having only one row and spilit it based on given arguments. Texts and images are append to each cell of this row/element.
    - <u>CustomTextCell</u>: this element is a cell which accepts only Text in format of a Paragraph class or TextTitle or TextBody. Some attributes are customizable, such as borderWidth, borderColor, align, backgroundColor and etc
    - <u>CustomImageCell</u>:  this element is a cell which accepts only image. Image is given from Resources Drawables. imageFromDrawables() helper function will prepare image and pass it to our cell.

#### How to add elements to the document/page?
First, we create an Element (may itself includes some other elements),
and finally we add it to the document variable. So you are going to see

<pre>.also {
   ...
   ...
   document.add(it)
}</pre>

inside the parent element, a lot!
