package local.rastorguev;

import org.apache.log4j.Logger;

import java.util.Objects;

/**
 * Class <Name class>.
 *
 * @author Alexey Rastorguev (rastorguev00@gmail.com)
 * @version 0.1
 * @since 22.08.18
 */
public class Node {
    private static final Logger logger = Logger.getLogger(Node.class);
    private String name_product = "";
    private int pipeType = 0;
    private String pipeTypeStr = "";
    private  int diametr = 0;
    private  int price = 0;

    public Node(String name_product, int pipeType, int diametr, int price) {
        this.name_product = name_product;
        this.pipeType = pipeType;
        this.diametr = diametr;
        this.price = price;
    }

    public Node(String name_product, String pipeTypeStr, int diametr, int price) {
        this(name_product, 0, diametr, price);
        this.pipeTypeStr = pipeTypeStr;
    }

    public static Logger getLogger() {
        return logger;
    }

    public String getName_product() {
        return name_product;
    }

    public void setName_product(String name_product) {
        this.name_product = name_product;
    }

    public int getPipeType() {
        return pipeType;
    }

    public void setPipeType(int pipeType) {
        this.pipeType = pipeType;
    }

    public int getDiametr() {
        return diametr;
    }

    public void setDiametr(int diametr) {
        this.diametr = diametr;
    }

    public int getPrice() {
        return price;
    }

    public void setPrice(int price) {
        this.price = price;
    }

    public String getPipeTypeStr() {
        return pipeTypeStr;
    }

    public void setPipeTypeStr(String pipeTypeStr) {
        this.pipeTypeStr = pipeTypeStr;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Node node = (Node) o;
        return Objects.equals(name_product, node.name_product);
    }

    @Override
    public int hashCode() {
        return Objects.hash(name_product);
    }
}
