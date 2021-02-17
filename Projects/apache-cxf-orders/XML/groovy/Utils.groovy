import javax.net.ssl.HttpsURLConnection
import java.net.HttpURLConnection
import java.text.SimpleDateFormat
import org.apache.log4j.*
import groovy.util.logging.*

class Utils {

    static def leesData(Object context, String orderID) {
        def log = Logger.getLogger('groovy.log')
        def groovyUtils = new com.eviware.soapui.support.GroovyUtils(context)
        String dataPath = groovyUtils.projectPath + "/data"

        log.info "[leesData] - orderID: $orderID"
        String bestandsNaam = dataPath + "/" + orderID + ".xml"
        String data = leesBestand(bestandsNaam)
        if (data == null) {
            data = ophalenGegevens(context, orderID)
            if (data != null)
                schrijfBestand(context, orderID, data)
        }

        return data
    }

    static def leesBestand(String bestandsNaam) {
        File file = new File(bestandsNaam)
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

    static def schrijfBestand(Object context, String objectID, String data) {
        def log = Logger.getLogger('groovy.log')
        def groovyUtils = new com.eviware.soapui.support.GroovyUtils(context)
        String dataPath = groovyUtils.projectPath + "/data"
        String fileName = dataPath + "/" + objectID + ".xml"

        try{
            FileOutputStream fos = new FileOutputStream(fileName)
            PrintStream out = new PrintStream(fos)
            out.print(data)
            out.close()
            fos.close()
        } catch (IOException e) {
            String message = e.getMessage()
            log.info "[schrijfBestand] - error writing data to file: $message"
        }
    }


    def static ophalenGegevens(Object context, String orderID) {
        def log = Logger.getLogger('groovy.log')
        Date datum = new Date();
        String huidigeDatum = new SimpleDateFormat("yyyy-MM-dd").format(datum);
        String template =
        "<soapenv:Envelope xmlns:soapenv=\"http://schemas.xmlsoap.org/soap/envelope/\" xmlns:ord=\"http://www.pluralsight.com/service/Order\">\n" +
                "   <soapenv:Header>\n" +
                "      <macAddress xmlns=\"http://ws.mkyong.com/\" \n" +
                "            xmlns:SOAP-ENV=\"http://schemas.xmlsoap.org/soap/envelope/\" \n" +
                "            SOAP-ENV:actor=\"http://schemas.xmlsoap.org/soap/actor/next\">90-4C-E5-44-B9-8F</macAddress>\n" +
                "      <authRole xmlns=\"http://ws.mkyong.com/\" \n" +
                "            xmlns:SOAP-ENV=\"http://schemas.xmlsoap.org/soap/envelope/\" \n" +
                "            SOAP-ENV:actor=\"http://www.w3.org/2003/05/soap-envelope/role/next\">system</authRole>\n" +
                "   </soapenv:Header>\n" +
                "   <soapenv:Body>\n" +
                "      <ord:orderInquiry>\n" +
                "         <ord:uniqueOrderId>${orderID}</ord:uniqueOrderId>\n" +
                "         <ord:orderQuantity>2</ord:orderQuantity>\n" +
                "         <ord:accountId>3</ord:accountId>\n" +
                "         <ord:ean13>4</ord:ean13>\n" +
                "      </ord:orderInquiry>\n" +
                "   </soapenv:Body>\n" +
                "</soapenv:Envelope>";
        String message = template.replace("${orderID}", orderID)

        URL url = new URL("http://localhost:8080/apache-cxf-jax-ws-demo/Orders")
//        HttpsURLConnection con = (HttpsURLConnection) url.openConnection()
        HttpURLConnection con = (HttpURLConnection) url.openConnection();
        con.setRequestMethod("POST")
        int length = message.length()
        con.setRequestProperty("Content-Length", String.valueOf(length))
        con.setRequestProperty("Content-Type", "text/xml; charset=utf-8")
        con.setRequestProperty("SOAPAction", "http://www.pluralsight.com/service/Orders/ProcessOrderPlacement");

        con.setDoOutput(true)
        con.setConnectTimeout(5000)
        con.setReadTimeout(5000)

        OutputStream os = con.getOutputStream()
        byte[] input = message.getBytes("utf-8")
        os.write(input)
        os.close()

        int status = con.getResponseCode()
        log.info "[ophalenGegevens] - response Code : $status"
        if (status != 200 && status != 201)
            return null

        InputStreamReader isr = new InputStreamReader(con.getInputStream())
        BufferedReader inr = new BufferedReader(isr)
        String content = ""
        String inputLine
        while ((inputLine = inr.readLine()) != null)
            content = content + inputLine
        inr.close()
        inr.close()

        con.disconnect()

        int indexReturn = content.indexOf("<orderInquiryResponse")
        if (indexReturn < 0)
            return null
        String returnValue = content.substring(content.indexOf("<orderInquiryResponse"),
                content.indexOf("</orderInquiryResponse>") + 23)
        log.info "[ophalenGegevens] - returnValue: $returnValue"

        return returnValue
    }

}


