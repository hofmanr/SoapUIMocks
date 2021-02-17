import java.io.Serializable
import java.io.File
import java.io.FileOutputStream
import java.io.ObjectOutputStream
import java.io.FileNotFoundException
import java.io.IOException

import org.apache.log4j.*
import groovy.util.logging.*

class OrderInquiry implements Serializable {
    private static final long serialVersionUID = 1L

    Long accountId
    List<Order> orders = new ArrayList<>()

    class Order implements Serializable {
        private static final long serialVersionUID = 1L

        String status
        List<Item> items = new ArrayList<>()

        Order(String status, List<Item> items) {
            this.status = status
            this.items = items
        }
    }

    class Item implements Serializable {
        private static final long serialVersionUID = 1L

        Long lineNumber
        Date shippingDate
        Long ean13
        String title
        BigDecimal price
        Integer quantity

        Item(Long lineNumber, Date shippingDate, Long ean13, String title, BigDecimal price, Integer quantity) {
            this.lineNumber = lineNumber
            this.shippingDate = shippingDate
            this.ean13 = ean13
            this.title = title
            this.price = price
            this.quantity = quantity
        }
    }

    OrderInquiry() {
    }

    OrderInquiry(Long accountId) {
        this.accountId = accountId
    }

    def createItem(Long lineNumber, Date shippingDate, Long ean13, String title, BigDecimal price, Integer quantity) {
        return new Item(lineNumber, shippingDate, ean13, title, price, quantity)
    }

    def addOrder(String status, List<Item> items) {
        orders.add(new Order(status, items))
    }


    static def saveOrders(String path, LinkedHashMap<Long, Object> orders) {
        def log = Logger.getLogger('groovy.log')
            //log.setLevel(Level.INFO)
        try {
            String filePath = path.replace("\\","/")
            String fileName = filePath + "/orders.data"
            log.info "[saveOrders] - filePath: $filePath"
            FileOutputStream f = new FileOutputStream(new File(fileName))
            ObjectOutputStream os = new ObjectOutputStream(f)
            // Write objects to file
            Iterator it = orders.entrySet().iterator()
            while (it.hasNext()) {
                Map.Entry<Long, Object> entry = it.next()
                Object orderInquiry = entry.getValue()
                log.info "[saveOrders] - orders for account: $orderInquiry.accountId"
                os.writeObject(orderInquiry)
            }
            os.close()
            f.close()
        } catch (FileNotFoundException e) {
            log.warn "Can not open file"
        } catch (IOException e) {
            log.warn "Error initializing stream"
        } catch (ClassNotFoundException e) {
            log.warn "Class not found"
        } catch (Exception e) {
            log.warn e.getMessage()
        }
    }



    static Map<Long, OrderInquiry> loadOrders(String path) {
        String filePath = path.replace("\\","/")
//evaluate(new File(filePath + "/OrderInquiry.groovy"))

        def log = Logger.getLogger('groovy.log')
        Map<Long, OrderInquiry> orders = new LinkedHashMap<Long, OrderInquiry>()

        try {
            String fileName = filePath + "/orders.data"
            log.info "[loadOrders] - filePath: $filePath"

            FileInputStream f = new FileInputStream(new File(fileName))
            if (f == null)
                return orders

            ObjectInputStream is = new ObjectInputStream(f) {
                @Override
                protected Class resolveClass(ObjectStreamClass desc) throws IOException, ClassNotFoundException {
                    String name = desc.getName()
                    //log.info "[loadOrders] - resolveClass $name"
                    if ("OrderInquiry\$Order".equalsIgnoreCase(name))
                        return OrderInquiry.Order.class
                    if (name.contains("Item"))
                         return OrderInquiry.Item.class
                    if (name.contains("OrderInquiry"))
                        return OrderInquiry.class
                    // Default:
                    return Class.forName(name);
                }
            }

            // Read objects from file
            OrderInquiry orderInquiry = null
            while (true) {
                def object = is.readObject()
                if (object == null)
                    break;
                if (object instanceof OrderInquiry) {
                    orderInquiry = (OrderInquiry)object
                    orders.put(orderInquiry.accountId, orderInquiry)
                    log.info "[loadOrders] - orders for account: $orderInquiry.accountId"
                }
            }
            is.close()
            f.close()
        } catch (FileNotFoundException e) {
            // ignore
        } catch (IOException e) {
            String message = e.getMessage()
            if (message != null)
                log.warn "IO Exception $message"
        } catch (ClassNotFoundException e) {
            String message = e.getMessage()
            log.warn "Class not found: $message"
        } catch (Exception e) {
            log.warn e.getMessage()
        }

        return orders
    }
   

}
