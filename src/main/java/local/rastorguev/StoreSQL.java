package local.rastorguev;

import org.apache.log4j.Logger;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.sql.*;
import java.util.*;

/**
 * Class <Name class>.
 *
 * @author Alexey Rastorguev (rastorguev00@gmail.com)
 * @version 0.1
 * @since 20.08.18
 */
public class StoreSQL {
    private static final Logger logger = Logger.getLogger(StoreSQL.class);
    private Connection conn = null;

    public StoreSQL() {
        Properties pr = new Properties();
        try (FileInputStream fs = new FileInputStream("config.properties")) {
            pr.load(fs);
        } catch (IOException e) {
            e.printStackTrace();
        }

        String bdName = pr.getProperty("bd.name");
        File fNameBd = new File(bdName);
        if (!fNameBd.exists()) {
            try {
                fNameBd.createNewFile();
                logger.info("file BD created");
            } catch (IOException e) {
                e.printStackTrace();
            }
        } else {
            logger.info("file BD found");
        }

        StringBuilder url = new StringBuilder();
        url.append(pr.getProperty("jdbc.driver")).
                append(":").
                append(bdName);

        try {
            conn = DriverManager.getConnection("jdbc:sqlite:vek.sqlite");
//            conn.setAutoCommit(false);
            logger.info("Connected");

        } catch (Exception e) {
            e.printStackTrace();
        }
        try(Statement st = conn.createStatement()) {
            st.execute("PRAGMA foreign_keys = on");

            st.execute("create TABLE IF NOT EXISTS type_pipe(id integer primary key not null , name_type text)");
            if (executeTypePipe().size() <= 0) {
                st.execute("insert into type_pipe(id, name_type) values (4040, 'Дымоход - трубы без изоляции')");
                st.execute("insert into type_pipe(id, name_type) values (4039, 'Дымоход - сэндвич (двойные трубы с изоляцией)')");
            }

            st.execute("CREATE TABLE IF NOT EXISTS products (\n" +
                    "  id integer primary key AUTOINCREMENT not null,\n" +
                    "  name_product character (200),\n" +
                    "  pipeType integer references type_pipe(id),\n" +
                    "  diametr integer, price integer,\n" +
                    "  foreign key (pipeType) references type_pipe(id)\n" +
                    ")");
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    /**
     * Метод добавляет заптись в БД
     * @param node запись
     */
    public void addBD(Node node) {
        try(PreparedStatement pst = conn.prepareStatement("insert into products(name_product, pipeType, diametr, price) values(?, ?, ?, ?);")) {
            pst.setString(1, node.getName_product());
            pst.setInt(2, node.getPipeType());
            pst.setInt(3, node.getDiametr());
            pst.setInt(4, node.getPrice());
            pst.execute();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * Метод возвращает список типов трубы.
     * @return сптсок
     */
    public List executeTypePipe() {
        List<Integer> listPipe = new LinkedList<>();
        try(Statement st = conn.createStatement()) {
            ResultSet rs = st.executeQuery("select * from type_pipe");
            while (rs.next()) {
                listPipe.add(rs.getInt("id"));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return listPipe;
    }

    /**
     * Метод возвращает из БД для создания excel файла.
     * @return список
     */
    public List<Node> allPipe() {
        List<Node> listPipeExcel = new LinkedList<>();
        try(Statement st = conn.createStatement()) {
            ResultSet rs = st.executeQuery("select p.name_product, tp.name_type, p.diametr, p.price from products p inner join type_pipe tp on p.pipeType = tp.id;");
            while (rs.next()) {
                String name = rs.getString("name_product");
                String typeDiametr = rs.getString("name_type");
                int diamet = rs.getInt("diametr");
                int price = rs.getInt("price");
                listPipeExcel.add(new Node(name, typeDiametr, diamet, price));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return listPipeExcel;
    }

    /**
     * Метод обновляет цену в одном поле.
     */
    public void update(Node node) {
        try(PreparedStatement ps = conn.prepareStatement("update products set price = ? where name_product = ?;")) {
            ps.setInt(1, node.getPrice());
            ps.setString(2, node.getName_product());
            ps.execute();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}
