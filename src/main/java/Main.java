/*
Класс Main - содержит  два конвертора: из формата CSV и XML в формат JSON, а так же парсер JSON файлов в Java классы.
 */

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import com.opencsv.CSVReader;
import com.opencsv.bean.ColumnPositionMappingStrategy;
import com.opencsv.bean.CsvToBean;
import com.opencsv.bean.CsvToBeanBuilder;
import org.json.simple.JSONArray;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;
import org.w3c.dom.*;
import org.xml.sax.SAXException;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import java.io.*;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;

public class Main {
    public static void main(String[] args) throws ParseException {
        String[] columnMapping = {"id", "firstName", "lastName", "country", "age"};

        String fileName = "data.csv";

        List<Employee> list = parseCSV(fileName, columnMapping);

        String json = listToJson(list);

        writeString(json, "dataCSV.json");

        try {
            List<Employee> list2 = parseXML("data.xml");
            String json2 = listToJson(list2);
            writeString(json2, "dataXML.json");
        } catch (ParserConfigurationException e) {
            throw new RuntimeException(e);
        } catch (IOException e) {
            throw new RuntimeException(e);
        } catch (SAXException e) {
            throw new RuntimeException(e);
        }

        String json3 = readString("dataCSV.json");

        jsonToList(json3);
    }

    public static List<Employee> parseCSV(String fileName, String[] columnMapping) {
        try (CSVReader csvReader = new CSVReader(new FileReader(fileName))) {
            ColumnPositionMappingStrategy<Employee> strategy =
                    new ColumnPositionMappingStrategy<>();
            strategy.setType(Employee.class);
            strategy.setColumnMapping(columnMapping);
            CsvToBean<Employee> csv = new CsvToBeanBuilder<Employee>(csvReader)
                    .withMappingStrategy(strategy)
                    .build();
            List<Employee> staff = csv.parse();
            staff.forEach(System.out::println);
            return staff;
        } catch (IOException e) {
            e.printStackTrace();
        }

        return null;
    }

    public static String listToJson(List<Employee> list) {
        Type listType = new TypeToken<List<Employee>>() {
        }.getType();

        String json = new Gson().toJson(list, listType);

        return json;
    }

    public static void writeString(String json, String fileName) {
        try (FileWriter file = new
                FileWriter(fileName)) {
            file.write(json);
            file.flush();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static List<Employee> parseXML(String fileName) throws ParserConfigurationException, IOException, SAXException {
        Staff staff = new Staff();

        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        DocumentBuilder builder = factory.newDocumentBuilder();
        Document doc = builder.parse(new File(fileName));
        Node root = doc.getDocumentElement();
        System.out.println("Корневой элемент: " + root.getNodeName());

        Node staffNode = doc.getFirstChild();

        List<Employee> employeeList = new ArrayList<>();

        NodeList staffChilds = staffNode.getChildNodes();

        Employee employee;
        for (int i = 0; i < staffChilds.getLength(); i++) {
            if (staffChilds.item(i).getNodeType() != Node.ELEMENT_NODE |
                    !staffChilds.item(i).getNodeName().equals("employee")) {
                continue;
            }

            Long id = Long.valueOf(0);
            String firstName = "";
            String lastName = "";
            String country = "";
            int age = 0;

            NodeList employeeChilds = staffChilds.item(i).getChildNodes();
            for (int j = 0; j < employeeChilds.getLength(); j++) {
                if (employeeChilds.item(j).getNodeType() != Node.ELEMENT_NODE) {
                    continue;
                }

                switch (employeeChilds.item(j).getNodeName()) {
                    case "id": {
                        id = Long.valueOf(employeeChilds.item(j).getTextContent());
                        break;
                    }
                    case "firstName": {
                        firstName = employeeChilds.item(j).getTextContent();
                        break;
                    }
                    case "lastName": {
                        lastName = employeeChilds.item(j).getTextContent();
                        break;
                    }
                    case "country": {
                        country = employeeChilds.item(j).getTextContent();
                        break;
                    }
                    case "age": {
                        age = Integer.valueOf(employeeChilds.item(j).getTextContent());
                        break;
                    }
                }
            }
            employee = new Employee(id, firstName, lastName, country, age);
            employeeList.add(employee);
        }
        staff.setEmployee(employeeList);

        System.out.println(staff.toString());

        return employeeList;

    }

    public static String readString(String fileName) {
        String line;
        StringBuilder stringBuilder = new StringBuilder();
        try (BufferedReader br = new BufferedReader(new FileReader(fileName))) {

            while ((line = br.readLine()) != null) {
                stringBuilder.append(line);
            }

        } catch (IOException ex) {
            System.out.println(ex.getMessage());
        }
        return stringBuilder.toString().replaceAll("\\s+", "");
    }

    public static void jsonToList(String json) throws ParseException {
        JSONParser jsonParser = new JSONParser();
        JSONArray array = (JSONArray) jsonParser.parse(json);
        GsonBuilder builder = new GsonBuilder();
        Gson gson = builder.create();

        List<Employee> employees = new ArrayList<>();

        for (Object jsonObj : array) {

            Employee employee = gson.fromJson(jsonObj.toString(), Employee.class);
            employees.add(employee);
        }

        employees.forEach((value) -> System.out.println("Employee" + value.toString()));
    }


}
