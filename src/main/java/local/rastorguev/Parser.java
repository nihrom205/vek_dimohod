package local.rastorguev;

import org.apache.log4j.Logger;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

/**
 * Class <Name class>.
 *
 * @author Alexey Rastorguev (rastorguev00@gmail.com)
 * @version 0.1
 * @since 17.08.18
 */
public class Parser {
    private static final Logger logger = Logger.getLogger(Parser.class);
    private int numberPage = 0;
    private Map<Integer, String> typeDimohod = new HashMap<>();
    private Map<Integer, String> diametr = new HashMap<>();
    private Document doc = null;
    private StoreSQL storeSQL = null;
    private String url = "https://vek-pechi.ru/catalog/dymohody/dymohody_iz_nerzhaveyushey_stali";
    private List<Node> storeAllPipe = new LinkedList<>();

    public static void main(String[] args) {
        Parser parser = new Parser();
        parser.start();
        logger.info("Program completed.");
    }

    public void start() {
        try {
            storeSQL = new StoreSQL();
            storeAllPipe = storeSQL.allPipe();
            doc = Jsoup.connect(url).get();
            numberPage = Integer.valueOf(doc.select("div.catalog-p").get(1).text().split(" ")[5]) / 50 + 1;
            numberPage = 1;
            logger.info("Initialization...");
            init(doc);
            logger.info("Initialization completed.");
            logger.info("Parsing page...");
            StringBuilder sbFiltr = new StringBuilder();
            for (Map.Entry<Integer, String> entryType : typeDimohod.entrySet()) {
                for (Map.Entry<Integer, String> entryDiametr : diametr.entrySet()) {
                    sbFiltr.append(url).
                            append("/action.index?Filters%5B3842%5D%5Bvalue%5D=").
                            append(entryDiametr.getKey()).
                            append("&Filters%5B4038%5D%5Bvalue%5D=").
                            append(entryType.getKey());
                    parse(sbFiltr.toString(), Integer.valueOf(entryDiametr.getValue()), entryType.getKey());
                    sbFiltr.delete(0, sbFiltr.length());
                }
            }
            logger.info("Parsing completed.");
            logger.info("Create Excel file...");
            storeAllPipe = storeSQL.allPipe();
            createFileExcel(storeAllPipe);
            logger.info("Create Excel file completed.");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * Метод инициализации переменных typeDimohod, diametr.
     * @param doc Начальная страница.
     */
    private void init(Document doc) {
        for (int i = 0; i <= 1; i++) {
            Elements viborSort = doc.getElementsByClass("filter-catalog-block").get(i).select("option");
            for (Element el : viborSort) {
                if (el.select("option").val().equals("0")) {
                    continue;
                } else {
                    if (i == 0) {
                        diametr.put(Integer.valueOf(el.select("option").val()), el.select("option").text().split(" ")[0]);

                    } else {
                        typeDimohod.put(Integer.valueOf(el.select("option").val()), el.select("option").text());
                    }
                }
            }
        }
    }

    /**
     * Метод ищет на странице и записи наименований и цен (парсер).
     * @param strUrl адрес страницы
     * @param diametr диаметр
     * @param type тип трубы
     */
    private void parse(String strUrl, Integer diametr, Integer type) {
        try {
            doc = Jsoup.connect(strUrl).get();
            for (int i = 1; i <= numberPage;) {
                Elements pages = doc.select("div.viewed-block");
                for (Element page : pages) {
                    String  name = page.select("div.viewed-block-text").select("a[href]").text();
                    Integer price = Integer.valueOf(page.select("div.viewed-cena").text().replace(" ", ""));
                    Node newNode = new Node(name, type, diametr, price);
                    if (storeAllPipe.contains(newNode)) {
                        Node curentNode = storeAllPipe.get(storeAllPipe.indexOf(newNode));
                        if (curentNode.getPrice() != newNode.getPrice()) {
                            logger.info("entry found: price to BD: " + curentNode.getPrice() + ",  price to site: " + newNode.getPrice());
                            storeSQL.update(newNode);
                            logger.info("entry replaced");
                        }
                    } else {
                        storeSQL.addBD(newNode);
                    }


                }
                i++;
                doc = Jsoup.connect(url + "/requestParams/ProductGroup_page/" + i).get();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * Метод создания excel файла.
     * @param list список результатов
     */
    private void createFileExcel(List<Node> list) {
        File file = new File("vek.xls");
        HSSFWorkbook excel = new HSSFWorkbook();
        Sheet sheet = excel.createSheet("Лист 1");

        Row row = sheet.createRow(0);
        row.createCell(0).setCellValue("п/п");
        row.createCell(1).setCellValue("Наименование");
        row.createCell(2).setCellValue("Тип трубы");
        row.createCell(3).setCellValue("Диаметр");
        row.createCell(4).setCellValue("Цена");

        for (int i = 0; i < list.size(); i++) {
            Node currentNode = list.get(i);
            Row rowCell = sheet.createRow(i + 1);
            rowCell.createCell(0).setCellValue( i + 1);
            rowCell.createCell(1).setCellValue(currentNode.getName_product());
            rowCell.createCell(2).setCellValue(currentNode.getPipeTypeStr());
            rowCell.createCell(3).setCellValue(currentNode.getDiametr());
            rowCell.createCell(4).setCellValue(currentNode.getPrice());
        }
        sheet.autoSizeColumn(1);
        sheet.autoSizeColumn(2);
        try {
            excel.write(new FileOutputStream(file));
            excel.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}