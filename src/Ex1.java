
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class Ex1 {

    public static Bayesian_Net Load_XML_toGraph(String xml_path) {
        Bayesian_Net Bn = new Bayesian_Net();
        List<String> table;
        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        try {
            DocumentBuilder builder = factory.newDocumentBuilder();

            Document document = builder.parse(new File(xml_path));

            document.getDocumentElement().normalize();

            NodeList variableList = document.getElementsByTagName("VARIABLE");
            for (int i = 0; i < variableList.getLength(); i++) {
                Node variable = variableList.item(i);
                if (variable.getNodeType() == Node.ELEMENT_NODE) {
                    NodeList variableDetails = variable.getChildNodes();
                    String name = "";
                    List<String> outcomes = new LinkedList<>();
                    for (int j = 0; j < variableDetails.getLength(); j++) {
                        Node detail = variableDetails.item(j);
                        if (detail.getNodeType() == Node.ELEMENT_NODE) {
                            Element detailElement = (Element) detail;

                            if ((detailElement.getTagName()).equals("NAME"))
                                name = detailElement.getTextContent();
                            else if ((detailElement.getTagName()).equals("OUTCOME"))
                                outcomes.add(detailElement.getTextContent());
                        }
                    }
                    Bn_Node bnNode = new Bn_Node(name, outcomes);
                    if (!Bn.add_Node(bnNode)) {
                        Bn.get_Node(name).setOutcomes(outcomes);
                    }
                }
            }

            NodeList DefList = document.getElementsByTagName("DEFINITION");
            for (int i = 0; i < DefList.getLength(); i++) {
                Node def = DefList.item(i);
                if (def.getNodeType() == Node.ELEMENT_NODE) {
                    NodeList DefDetails = def.getChildNodes();
                    String name = "";
                    List<String> parents = new LinkedList<>();
                    for (int j = 0; j < DefDetails.getLength(); j++) {
                        Node detail = DefDetails.item(j);
                        if (detail.getNodeType() == Node.ELEMENT_NODE) {
                            Element detailElement = (Element) detail;

                            if ((detailElement.getTagName()).equals("FOR"))
                                name = detailElement.getTextContent();
                            else if ((detailElement.getTagName()).equals("GIVEN"))
                                Bn.Connect(detailElement.getTextContent(), name);
//                                parents.add(detailElement.getTextContent());
                            else if ((detailElement.getTagName()).equals("TABLE")) {
                                table = Stream.of(detailElement.getTextContent().split(" ", -1))
                                        .collect(Collectors.toList());
                                Bn.setCPT(name, table);

                            }
                        }
                    }

                }
            }
        } catch (ParserConfigurationException e) {
            e.printStackTrace();
        } catch (SAXException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return Bn;
    }

    private static String parse_Bayes_ball(String data, Bayesian_Net Bn) {
        String[] given = new String[0];
        int parse_inx1 = data.indexOf('-');
        int parse_inx2 = data.indexOf('|');
        String from = data.substring(0, parse_inx1);
        String to = data.substring(parse_inx1 + 1, parse_inx2);
        if (parse_inx2 + 1 != data.length()) {//there is a given data
            given = data.substring(parse_inx2 + 1, data.length()).split(",");
        }
        return Bn.Bayes_ball(from, to, given);
    }

    public static void start(String FileName) throws IOException {
        FileWriter outputFile = new FileWriter("output.txt");
        String ans = "";
        Bayesian_Net Bn = new Bayesian_Net();
        try {
            File inputFile = new File("src//"+ FileName);
            Scanner myReader = new Scanner(inputFile);
            int lineNumber = 0;
            while (myReader.hasNextLine()) {
                if (lineNumber == 0) {
                    Bn = Load_XML_toGraph("src//"+ myReader.nextLine());
                } else {
                    String data = myReader.nextLine();
                    if (data.charAt(0) == 'P') {//Variable elimination
                        ans = parse_Variable_elimination(data, Bn);
                        outputFile.write(ans+"\n");
                    } else {//Bayes-ball
                        ans = parse_Bayes_ball(data, Bn);
                        outputFile.write(ans+"\n");
                    }
                }
                lineNumber++;

            }
            outputFile.close();
            myReader.close();
        } catch (FileNotFoundException e) {
            System.out.println("An error occurred.");
            e.printStackTrace();
        }
    }

    private static String parse_Variable_elimination(String data, Bayesian_Net Bn) {
        int parse_inx1 = data.indexOf('(');
        int parse_inx2 = data.indexOf(')');
        String parentheses = data.substring(parse_inx1 + 1, parse_inx2);
        String Hidden = "";
        if (parse_inx2 != data.length() - 1) {//there is hidden variables
            Hidden = data.substring(parse_inx2 + 2, data.length());
        }
        parse_inx1 = parentheses.indexOf('|');
        String query, evidence;
        String[] evidenceList=new String[0];
        if(parse_inx1!=-1) {//the '|' is exist
            query = parentheses.substring(0, parse_inx1);
            if(parentheses.length()-1!=parse_inx1){//if the '|' is not the last char (there is evidence)
                evidence = parentheses.substring(parse_inx1 + 1, parentheses.length());
                evidenceList = evidence.split(",");
            }
        }
        else query=parentheses;

        if (Hidden.split("-")[0] == "") {
            return Bn.Variable_elimination(query, evidenceList, null);
        } else {
            return Bn.Variable_elimination(query, evidenceList, Hidden.split("-"));
        }

    }

    public static void main(String[] args) throws IOException {
        start("input.txt");
//        start("input2.txt");
    }
}