import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;

public class Bn_Node {
    private String Name;
    private List<String> Outcomes;
    private HashMap<String, Double> CPT;
    boolean given =false;

    public Bn_Node(String name, List<String> outcomes, HashMap<String, Double> cpt){
        Name = name;
        Outcomes = new LinkedList<>(outcomes);
        CPT = new HashMap<>(cpt);
    }

    public Bn_Node(String name, List<String> outcomes){
        Name = name;
        Outcomes = new LinkedList<>(outcomes);
        CPT = new HashMap<>();
    }
    public String getName(){
        return Name;
    }

    public void setOutcomes(List<String> outcomes) {
        Outcomes = outcomes;
    }
    public void setCPT(HashMap<String, Double> cpt){
        CPT = cpt;
    }

    public HashMap<String, Double> getCPT() {
        return CPT;
    }

    public List<String> getOutcomes(){
        return Outcomes;
    }

    public void setGiven(boolean given) {
        this.given = given;
    }

    public boolean getGiven() {
        return given;
    }
}

