import com.itextpdf.kernel.pdf.PdfDocument
import com.itextpdf.kernel.pdf.PdfWriter
import com.itextpdf.layout.Document
import com.itextpdf.layout.element.Cell
import com.itextpdf.layout.element.Paragraph
import com.itextpdf.layout.element.Table
import org.openqa.selenium.By
import org.openqa.selenium.ElementClickInterceptedException
import org.openqa.selenium.WebDriver
import org.openqa.selenium.chrome.ChromeDriver
import org.openqa.selenium.chrome.ChromeOptions
import org.openqa.selenium.support.ui.ExpectedConditions
import org.openqa.selenium.support.ui.WebDriverWait
import java.io.File
import java.time.Duration


data class Modulo(
    val nombre: String,
    val horasLectivas: Int,
    val faltasMaximas: Int
)


const val usuario = ""
const val contraseña = ""


fun main() {
    val faltasPorModulo = mutableMapOf<String, Int>()
    val rutaPDF = "faltas.pdf"


    val chromeOptions = ChromeOptions()

    //Extensión .crx selectorshub
    chromeOptions.addExtensions(File("selectorshub.crx"))

    val driver: WebDriver = ChromeDriver(chromeOptions)
    driver.manage().window().maximize()
    val wait = WebDriverWait(driver, Duration.ofSeconds(10))
    driver.get("https://familia.edu.gva.es/wf-front/myitaca/login_wf?idioma=C")


    val usernameInput = driver.findElement(By.id("usuario"))
    val passwordInput = driver.findElement(By.id("contrasenya"))
    val submitButton = driver.findElement(By.id("bt_envia"))

    usernameInput.sendKeys(usuario)
    passwordInput.sendKeys(contraseña)
    submitButton.click()


    driver.manage().timeouts().implicitlyWait(Duration.ofSeconds(2));

    val verTodas = driver.findElement(By.xpath("//p[3]//a[1]"))
    verTodas.click()
    val ulElement = driver.findElement(By.cssSelector("ul.imc-listado-detalle.imc-listado-faltas"))

    val liElements = ulElement.findElements(By.tagName("li"))
    var i = 1


    for (liElement in liElements) {


        try {

            val clicableXpath = driver.findElement(By.xpath("/html[1]/body[1]/div[3]/div[1]/ul[1]/li[$i]/a[1]/span[1]"))

            i++
            wait.until(ExpectedConditions.elementToBeClickable(clicableXpath))

            clicableXpath.click()
        } catch (e: ElementClickInterceptedException) {
            println("Interceptado")
        }
        detalles(driver, faltasPorModulo)
        val volver = driver.findElement(By.className("bt-volver-listado"))
        volver.click()
        println("$i" + " " + liElement.text) //

    }


    println("Contador de faltas por módulo:")
    faltasPorModulo.forEach { (modulo, contador) ->
        println("$modulo: $contador")
    }
    guardarEnPDF(faltasPorModulo,rutaPDF)

    driver.quit()
}

fun detalles(driver: WebDriver, faltasPorModulo: MutableMap<String, Int>) {



    var i = 1
    var moreRows = true
    while (moreRows) {
        try {

            val moduloElement =
                driver.findElement(By.xpath("/html/body/div[3]/div/div/ul/li[$i]/ul/li[1]/span[2]/strong"))
            val moduloNombre = moduloElement.text
            println("Nombre del módulo: $moduloNombre")



            val divContenedor = moduloElement.findElement(By.xpath("ancestor::div[1]"))



            val ulContenido = divContenedor.findElement(By.xpath(".//ul[1]"))



            val pElement = ulContenido.findElement(By.xpath("preceding-sibling::p[1]"))



            val textoParrafo = pElement.text
            if (textoParrafo.trim().equals("SIN JUSTIFICAR", ignoreCase = true)) {

                faltasPorModulo[moduloNombre] = faltasPorModulo.getOrDefault(moduloNombre, 0) + 1
                println("Falta SIN JUSTIFICAR en el módulo: $moduloNombre")
            }


        } catch (e: Exception) {
            moreRows = false // Sal del bucle
        }
        i++
    }
}

fun guardarEnPDF(faltasPorModulo: Map<String, Int>, rutaArchivo: String) {
    val writer = PdfWriter(rutaArchivo)
    val pdf = PdfDocument(writer)
    val document = Document(pdf)

    document.add(Paragraph("Contador de faltas por módulo:"))

    val modulos = getModulos()

    modulos.forEach { modulo ->
        val faltasActuales = faltasPorModulo.getOrDefault(modulo.nombre, 0)


        document.add(Paragraph(modulo.nombre))


        val table = Table(floatArrayOf(1f, 1f, 1f)).useAllAvailableWidth()


        table.addCell(Cell().add(Paragraph("Horas Lectivas")))
        table.addCell(Cell().add(Paragraph("Faltas Máximas")))
        table.addCell(Cell().add(Paragraph("Faltas Actuales")))


        table.addCell(Cell().add(Paragraph(modulo.horasLectivas.toString())))
        table.addCell(Cell().add(Paragraph(modulo.faltasMaximas.toString())))
        table.addCell(Cell().add(Paragraph(faltasActuales.toString())))


        document.add(table)
    }

    document.close()
}

fun getModulos():List<Modulo> {
    return listOf(
        Modulo("Acceso a datos", 120, 18),
        Modulo("Programación multimedia y dispositivos móviles", 100, 15),
        Modulo("Programación de servicios y procesos", 60, 9),
        Modulo("Desarrollo de interfaces", 120, 18),
        Modulo("Sistemas de gestión empresarial", 100, 15),
        Modulo("Inglés Técnico II-S / Horario reservado para la docencia en inglés", 40, 6),
        Modulo("Empresa e iniciativa emprendedora", 60, 9)
    )
}
