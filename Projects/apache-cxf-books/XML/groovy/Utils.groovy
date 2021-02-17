import javax.net.ssl.HttpsURLConnection
import java.net.HttpURLConnection
import java.text.SimpleDateFormat
import org.apache.log4j.*
import groovy.util.logging.*

class Utils {

    def static findByEan13(Object context, long ean13) {
        def log = Logger.getLogger('groovy.log')
        def groovyUtils = new com.eviware.soapui.support.GroovyUtils(context)
        String dataPath = groovyUtils.projectPath + "/data"

        log.info "[findByEan13] - ean13: $ean13"
        String fileName = dataPath + "/EAN13_" + ean13 + ".xml"
        String data = readFromFile(fileName)
        if (data == null) {
            data = getDataByEan13(context, ean13)
            if (data != null)
                writeToFile(context, fileName, data)
        }

        return data
    }

    def static findByTitle(Object context, String title) {
        def log = Logger.getLogger('groovy.log')
        def groovyUtils = new com.eviware.soapui.support.GroovyUtils(context)
        String dataPath = groovyUtils.projectPath + "/data"

        log.info "[findByTitle] - title: $title"
        String fileName = dataPath + "/TITLE_" + title.toUpperCase() + ".xml"
        String data = readFromFile(fileName)
        if (data == null) {
            data = getDataByTitle(context, title)
            if (data != null)
                writeToFile(context, fileName, data)
        }

        return data
    }

    def static findByAuthor(Object context, String author) {
        def log = Logger.getLogger('groovy.log')
        def groovyUtils = new com.eviware.soapui.support.GroovyUtils(context)
        String dataPath = groovyUtils.projectPath + "/data"

        log.info "[findByAuthor] - author: $author"
        String fileName = dataPath + "/AUTHOR_" + author.toUpperCase() + ".xml"
        String data = readFromFile(fileName)
        if (data == null) {
            data = getDataByAuthor(context, author)
            if (data != null)
                writeToFile(context, fileName, data)
        }

        return data
    }

    def static readFromFile(String fileName) {
        File file = new File(fileName)
        InputStream fis = null
        try {
            fis = new FileInputStream(file)
            byte[] b  = new byte[file.length()]
            int len = b.length
            int total = 0

            while (total < len) {
                int result = fis.read(b, total, len - total)
                if (result == -1) {
                    break
                }
                total += result
            }
            return new String( b , "UTF-8" )
        } catch (IOException e) {
            return null
        } finally {
            if (fis != null)
                fis.close()
        }
    }

    def static writeToFile(Object context, String fileName, String data) {
        def log = Logger.getLogger('groovy.log')

        try{
            FileOutputStream fos = new FileOutputStream(fileName)
            PrintStream out = new PrintStream(fos)
            out.print(data)
            out.close()
            fos.close()
        } catch (IOException e) {
            String message = e.getMessage()
            log.info "[writeToFile] - error writing data to file: $message"
        }
    }

    def static getDataByEan13(Object context, long ean13) {
         String template = "<soapenv:Envelope xmlns:soapenv=\"http://schemas.xmlsoap.org/soap/envelope/\" xmlns:book=\"http://www.pluralsight.com/service/Book\">\n" +
                "   <soapenv:Header/>\n" +
                "   <soapenv:Body>\n" +
                "      <book:query>\n" +
                "         <book:ean13>${ean13}</book:ean13>\n" +
                "      </book:query>\n" +
                "   </soapenv:Body>\n" +
                "</soapenv:Envelope>"
        String message = template.replace("${ean13}", String.valueOf(ean13))
        return getData(context, message)
    }

    def static getDataByTitle(Object context, String title) {
         String template = "<soapenv:Envelope xmlns:soapenv=\"http://schemas.xmlsoap.org/soap/envelope/\" xmlns:book=\"http://www.pluralsight.com/service/Book\">\n" +
                "   <soapenv:Header/>\n" +
                "   <soapenv:Body>\n" +
                "      <book:query>\n" +
                "         <book:title>${title}</book:title>\n" +
                "      </book:query>\n" +
                "   </soapenv:Body>\n" +
                "</soapenv:Envelope>"
        String message = template.replace("${title}", title)
        return getData(context, message)
    }

    def static getDataByAuthor(Object context, String author) {
         String template = "<soapenv:Envelope xmlns:soapenv=\"http://schemas.xmlsoap.org/soap/envelope/\" xmlns:book=\"http://www.pluralsight.com/service/Book\">\n" +
                "   <soapenv:Header/>\n" +
                "   <soapenv:Body>\n" +
                "      <book:query>\n" +
                "         <book:author>${author}</book:author>\n" +
                "      </book:query>\n" +
                "   </soapenv:Body>\n" +
                "</soapenv:Envelope>"
        String message = template.replace("${author}", author)
        return getData(context, message)
    }

    def static getData(Object context, String message) {
        def log = Logger.getLogger('groovy.log')
        URL url = new URL("http://localhost:8080/apache-cxf-jax-ws-demo/Books")
//        HttpsURLConnection con = (HttpsURLConnection) url.openConnection()
        HttpURLConnection con = (HttpURLConnection) url.openConnection();
        con.setRequestMethod("POST")
        int length = message.length()
        con.setRequestProperty("Content-Length", String.valueOf(length))
        con.setRequestProperty("Content-Type", "text/xml; charset=utf-8")
        con.setRequestProperty("SOAPAction", "http://www.pluralsight.com/service/Books/FindBooks");

        con.setDoOutput(true)
        con.setConnectTimeout(5000)
        con.setReadTimeout(5000)

        OutputStream os = con.getOutputStream()
        byte[] input = message.getBytes("utf-8")
        os.write(input)
        os.close()

        int status = con.getResponseCode()
        log.info "[getData] - response Code : $status"
        if (status != 200 && status != 201) {
            con.disconnect()
            return null
        }

        InputStreamReader isr = new InputStreamReader(con.getInputStream())
        BufferedReader inr = new BufferedReader(isr)
        String content = ""
        String inputLine
        while ((inputLine = inr.readLine()) != null)
            content = content + inputLine
        inr.close()
        inr.close()

        con.disconnect()
        int indexStart = content.indexOf("<findBookResult")
        int indexEnd = content.indexOf("</findBookResult>")
         if (indexStart < 0)
            return null
        String returnValue
        if (indexEnd < 0)
            returnValue = "<findBookResult xmlns=\"http://www.pluralsight.com/service/Book\"/>"
        else
            returnValue = content.substring(indexStart, indexEnd + 17)
        log.info "[getData] - returnValue: $returnValue"

        return returnValue
    }

}


