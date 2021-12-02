import javax.sql.rowset.spi.SyncResolver;
import java.util.*;
import java.util.List;

public class Bayesian_Net {
    HashMap<String, Bn_Node> Nodes;
    HashMap<String, List<String>> MyParents; //each variable with his parents
    HashMap<String, List<String>> MyChildren; //each variable with his parents


    public Bayesian_Net() {
        Nodes = new HashMap<>();
        MyParents = new HashMap<>();
        MyChildren = new HashMap<>();
    }

    public Bn_Node get_Node(String name) {
        return Nodes.get(name);
    }

    public boolean add_Node(Bn_Node n) {
        if (Nodes.containsKey(n.getName())) {
            System.out.println("this node is already in the network");
            return false;
        } else {
            Nodes.put(n.getName(), n);
            MyParents.put(n.getName(), new LinkedList<String>());
            MyChildren.put(n.getName(), new LinkedList<String>());
            return true;
        }
    }

    public void remove_Node(Bn_Node n) {
        if (!Nodes.containsKey(n.getName())) {
            System.out.println("this node isn't in the network");
        } else {
            //disconnect his parents
            for (String parent : MyParents.get(n.getName())) {
                disconnect(get_Node(parent).getName(), n.getName());
            }
            //disconnect his children
            for (String child : MyChildren.get(n.getName())) {
                disconnect(n.getName(), get_Node(child).getName());
            }
            Nodes.remove(n.getName());
        }
    }

    public void Connect(String parent, String child) {
        if (MyParents.get(child).contains(parent) && MyChildren.get(parent).contains(child)) { //
            System.out.println("the connection already exist");
        } else {
            MyChildren.get(parent).add(child);
            MyParents.get(child).add(parent);
        }
    }

    public void disconnect(String parent, String child) {
        if (MyParents.get(child) == null | MyChildren.get(parent) == null
            |!MyParents.get(child).contains(parent) | !MyChildren.get(parent).contains(child)){
            System.out.println("connection doesn't exist");
        } else {
            MyParents.get(parent).remove(child);
            MyChildren.get(child).remove(parent);
        }
    }

    /**
     * @param name - the variable name
     * @param table - the list of the values
     */
    public void setCPT(String name, List<String> table) { //set the cpt for the variable
        Bn_Node curr = get_Node(name);
        List<String> myOutcomes = curr.getOutcomes();
        HashMap<String, Double> cpt=new HashMap<>();
        int amounts_per_out[][] =new int[2][MyParents.get(name).size()+1];
        amounts_per_out[0][MyParents.get(name).size()]=myOutcomes.size();
        amounts_per_out[1][MyParents.get(name).size()]=1;
        List<String> my_parents=MyParents.get(name);
        for (int i=(amounts_per_out[0].length)-2; i>=0; i--){
            String parent_in_i=my_parents.get(i);
            amounts_per_out[0][i]=get_Node(parent_in_i).getOutcomes().size();
        }

        int myAmount;
        for (int i=(amounts_per_out[0].length)-2; i>=0; i--){
            String curr_parent= MyParents.get(name).get(i);
            myAmount=amounts_per_out[0][i+1]*amounts_per_out[1][i+1];
            amounts_per_out[1][i]=myAmount;
        }

        int table_size =amounts_per_out[1][0]*amounts_per_out[0][0];
        List<String> key_list = new LinkedList<String>();

        //calculate the table size -  the table size is the
        // multiply of all the size of each variable(current and his parents)
        int i=0;
        while(i<table_size){
            for (String out: myOutcomes){
                key_list.add(out);
                i++;
            }
        }
        for (int p_inx=MyParents.get(name).size()-1; p_inx>=0; p_inx--){//from the last parent to first
            String curr_p=MyParents.get(name).get(p_inx);
            int p_index = MyParents.get(name).indexOf(curr_p);
            int amount_for_p=amounts_per_out[1][p_index];
            int index=0;
            while (index < table_size) {
                for (String o : get_Node(curr_p).getOutcomes()) {
                    for (int j = 0; j < amount_for_p; j++) {
                        String currCode = key_list.get(index);
                        key_list.set(index, o + "-" + currCode);
                        index++;
                    }
                }
            }
        }
        int j=0;
        for (String s: key_list){
            cpt.put(s, Double.parseDouble(table.get(j)));
            j++;
        }
        get_Node(name).setCPT(cpt);
    }

    /**
     *
     * @param from
     * @param to
     * @param given
     * @rules: if this node is his parent and given=T -Allow to his brothers
     *         if this node is his parent and given=F  -Allow to his grandsons
     *         if this node is his child and given=T -Don't Allow everywhere
     *         if this node is his child and given=F -Allow everywhere
     * @return "yes" if from is independent in to, else - "no"
     */
    public String Bayes_ball(String from, String to, String[] given) {
        HashMap<String , Integer> came_from_index= new HashMap<>();//to save the last index we
        // didn't check yet in the came from
        HashMap<String, Boolean> visited =new HashMap<>();
        HashMap<String, List<String>> came_from =new HashMap<>();//node name, who we came from

        //init all
        for (Bn_Node n : Nodes.values()) {
            came_from_index.put(n.getName(), 1);
            visited.put(n.getName(), false);
            List<String> empty_list=new LinkedList<String>();
            empty_list.add("");
            came_from.put(n.getName(), empty_list);
            n.setGiven(false);
        }

        came_from_index.replace(from, 0);

        if(given.length!=0) {
            String[] given_copy=new String[given.length];
            for (int i=0; i<given.length; i++){
                given_copy[i]=given[i];
            }
            for (int i=0; i<given_copy.length; i++){
                int index=given_copy[i].indexOf('=');
                given_copy[i]=given_copy[i].substring(0, index);
                get_Node(given_copy[i]).setGiven(true);
            }
        }

        String curr= from;
        LinkedList<String> queue = new LinkedList<String>();
        visited.put(curr, true);
        queue.add(curr);
        while (queue.size() != 0) {
            curr = queue.poll();
            List<String> connections = new LinkedList<String>();
            String curr_came_from = came_from.get(curr).get(came_from_index.get(curr));//the last we came from
            if(MyChildren.get(curr).contains(curr_came_from) || curr_came_from.equals("")){//came to curr throw his child
                if(get_Node(curr).given==true){ //curr is "paint"
                    //can't go anywhere
                    continue;
                }
                else{//curr isn't "paint"
                    //can go everywhere
                    connections.addAll(MyChildren.get(curr));
                    connections.addAll(MyParents.get(curr));
                }
            }
            else if(MyParents.get(curr).contains(curr_came_from)){//came to curr throw his parent
                if(get_Node(curr).given==true){//curr is "paint"
                    //can go to parent
                    connections.addAll(MyParents.get(curr));
                }
                else{//curr isn't "paint"
                    //can to children
                    connections.addAll(MyChildren.get(curr));
                }
            }
            came_from_index.replace(curr, came_from_index.get(curr)+1);

            for (String n: connections){//go over curr's list
                if(n.equals(to)) return "no"; //we reached the "to" so they are Not independent
                if (!visited.get(n)) {
                    visited.put(n, true);
                    queue.add(n);
                    came_from.get(n).add(curr);
                }
                if(visited.get(n) && !came_from.get(n).contains(curr)) {//if we already have been in this node
                    // before, but we didn't came from curr yet, add him to the queue and update his came from list
                    came_from.get(n).add(curr);
                    queue.add(n);
                }

            }
        }
        if(visited.get(to)) return "no";
        else return "yes";
    }

    private HashMap<String, Boolean> BFS(String hidden) {
        HashMap<String, Boolean> visited =new HashMap<>();

        for (Bn_Node n : Nodes.values()) {
            visited.put(n.getName(), false);
        }

        String curr= hidden;
        LinkedList<String> queue = new LinkedList<String>();
        visited.put(curr, true);
        queue.add(curr);
        while (queue.size() != 0) {
            curr = queue.poll();
            for (String n: MyChildren.get(curr)){
                if (!visited.get(n)) {
                    visited.put(n, true);
                    queue.add(n);
                }
            }
        }
        return visited;
    }

    /**
     *
     * @param hidden
     * @param variables
     * @return true if there is a path from hidden to one of the variables, else false
     */
    public boolean is_an_ancestor(String hidden, List<String> variables){
        HashMap<String,Boolean> BFS_List= BFS(hidden);
        for (String v:variables) {
            if(BFS_List.get(v))
                return true;
        }
        return false;
    }

    //convert the Hashmap to arr for the calculations
    public String[][] Table_to_Array(String curr ,String name, HashMap<String, Double> t, HashMap<String, String> evidence_parent){
        String[][] convert=new String[0][];
        String[] variables = name.split(",");
        if (evidence_parent.size() != 0) {//create table without the evidence_parent
            int row_size = t.size();
            int col_size = variables.length - evidence_parent.size();
            for (String e : evidence_parent.keySet()) {
                row_size /= get_Node(e).getOutcomes().size();
            }
            convert = new String[row_size + 1][col_size + 1];
            int i = 0;

            HashMap<String, Integer> index = new HashMap<>();
            for (String p : MyParents.get(curr)) {
                if (evidence_parent.containsKey(p)) {
                    index.put(p, i);
                }
                i++;
            }

            int e=0;
            for (int j = 0; j < variables.length; j++) {
                if (!evidence_parent.containsKey(variables[j])) {
                    convert[0][e] = variables[j];
                    e++;
                }
            }
            convert[0][col_size]="";

            int line = 1;
            for (String s : t.keySet()) {
                String[] outcome = s.split("-");
                boolean flag = true;
                for (String p : evidence_parent.keySet()) {
                    if (!outcome[index.get(p)].equals(evidence_parent.get(p))) {
                        flag = false;
                        break;
                    }
                }
                if (flag) {
                    int k = 0;
                    for (int j = 0; j < outcome.length; j++) {
                        if (!index.values().contains(j)) {
                            convert[line][k] = outcome[j];
                            k++;
                        }
                    }
                    convert[line][convert[0].length - 1] = t.get(s).toString();
                    line++;
                }
            }
            return convert;
        }else{
            convert=  new String[t.size()+1][variables.length+1];
            for (int i=0; i<variables.length; i++){
                convert[0][i]=variables[i];
            }
            int line=1;
            for (String s: t.keySet()){
                String[] outcome=s.split("-");
                for (int j=0; j<outcome.length; j++){
                    convert[line][j]=outcome[j];
                }
                convert[line][convert[0].length-1]=t.get(s).toString();
                line++;
            }
            return convert;
        }
    }


    private void set_Factors_For_Query_Var(HashMap<String, List<String[][]>> factors, String query_var,String[][] evidence){
        //collect all the evidence that are also the query's parents
        //so we can reduce the factor because they are given
        HashMap<String, String> evidence_parens=new HashMap<>();
        for (int e=0; e<evidence[0].length; e++) {
            String evidence_name = evidence[0][e];
            if (MyParents.get(query_var).contains(evidence_name)) {
                evidence_parens.put(evidence_name, evidence[1][e]);
            }
        }
        //create the name for the factor- all ths parents and finally the query var(separate with ",")
        String f_name="";
        if(MyParents.get(query_var)!=null && MyParents.get(query_var).size()!=0) {
            f_name = MyParents.get(query_var).toString();
            f_name=f_name.substring(1, f_name.length()-1);
            f_name= f_name.replaceAll(", ", ",");
            f_name+=",";
        }
        f_name+=query_var;

        //convert the factor from hashmap to array
        String[][] convert=Table_to_Array(query_var, f_name, get_Node(query_var).getCPT(),evidence_parens);
        f_name="";
        //take the new name of the factor after removing the evidence parents from the table
        for (int v=0; v<convert[0].length-1; v++){
            f_name+=convert[0][v]+",";
        }

        f_name=f_name.substring(0,f_name.length()-1);
        factors.put(f_name, new LinkedList<String[][]>());
        factors.get(f_name).add(convert);
    }

    
    public String Variable_elimination(String query, String[] evidenceList, String[] hiddenList) {
        int count_mul=0, count_plus=0;
        String[][] final_table = new String[0][];
        //parsing
        int parse_inx=query.indexOf('=');
        String query_var=query.substring(0, parse_inx);
        String query_outcome = query.substring(parse_inx+1);


        String[][] evidence= new String[2][evidenceList.length];//first line- names, second- outcome

        for(int i=0; i<evidenceList.length; i++){
            parse_inx=evidenceList[i].indexOf('=');
            evidence[0][i]=evidenceList[i].substring(0, parse_inx);
            evidence[1][i]=evidenceList[i].substring(parse_inx+1);
        }

        List<String> query_evidence_list=new LinkedList<String>();
        query_evidence_list.addAll(Arrays.asList(evidence[0]));
        query_evidence_list.add(query_var);
        String first_key_cpt=get_Node(query_var).getCPT().keySet().stream().findFirst().toString();
        String[] first_key = first_key_cpt.split("-");
        int CPT_col_size=first_key.length;
        //check if the value is already in the CPT
        if ((MyParents.get(query_var).containsAll(Arrays.asList(evidence[0].clone()))) && (CPT_col_size == evidence[0].length+1)) {
            String valFromCPT = get_value_from_cpt(query_var, query_outcome, evidence);
            if (valFromCPT != null)
                return valFromCPT;
        }

        HashMap<String, List<String[][]>> factors= new HashMap<>();
        //set the factors
        set_Factors_For_Query_Var(factors, query_var, evidence);

        List<String> hidden_to_ignore= set_Factors_For_Hidden(hiddenList,query_var,
                query_evidence_list, evidenceList, evidence, factors);
        set_Factors_For_Evidence(evidence, factors);


        String[] hidden_query = new String[hiddenList.length-hidden_to_ignore.size()+1];
        int hidden_i=0;
        for (int i=0; i<hiddenList.length; i++){
            if (!hidden_to_ignore.contains(hiddenList[i])) {
                hidden_query[hidden_i] = hiddenList[i];
                hidden_i++;
            }
        }
        hidden_query[hidden_query.length-1]=query_var;

        for (int i=0; i<hidden_query.length; i++) {
            //collecting the factors for thr current variable and delete them from the main factor list
            List<String> factors_to_remove = new LinkedList<String>();
            PriorityQueue<Factor> MyFactors = new PriorityQueue<Factor>();
            //collect the factors of hidden i
            for (String factor_name : factors.keySet()) {
                if (factor_name.contains((hidden_query[i] + ","))
                        || factor_name.contains("," + hidden_query[i] + ",")
                        || factor_name.contains("," + hidden_query[i])
                        || factor_name.equals(hidden_query[i])) {
                    for (String[][] t : factors.get(factor_name)) {
                        MyFactors.add(new Factor(factor_name, t));
                        factors_to_remove.add(factor_name);
                    }

                }
            }
            for (String f : factors_to_remove) {
                factors.remove(f);
            }

            //multiply tables
            while (MyFactors.size() >= 2) {
                Factor f1 = MyFactors.poll();
                Factor f2 = MyFactors.poll();

                HashMap<String, Integer[]> common = new HashMap<>();
                HashMap<String, Integer> only_f1 = new HashMap<>();
                HashMap<String, Integer> only_f2 = new HashMap<>();

                Factor new_factor =Building_the_new_factor(MyFactors, f1, f2, common, only_f1, only_f2);
                count_mul=MultiplyTables(new_factor, common, f1, f2, only_f1, only_f2, count_mul);

                MyFactors.add(new_factor);
            }

            //after there is only 1 factor for this hidden var-reduce the table(sum the lines)
            int col_size = MyFactors.peek().getName().split(",").length;
            if(col_size==1) {
                final_table = MyFactors.poll().getTable();
            }
            else {
                count_plus = sum_lines(col_size, MyFactors, count_plus, hidden_query[i], factors);

            }
        }

        //normalize
        count_plus=Normalize_table(final_table, count_plus);
        String ans="";
        for (int r=0; r<final_table.length; r++){
            if(final_table[r][0].equals(query_outcome)){
                ans= (final_table[r][1])+","+count_plus+","+count_mul;
                break;
            }
        }
        return ans;
    }

    private String[] getAllHidden(String query_var,List<String> evidence) {
        String[] hidden=new String[Nodes.size()-evidence.size()-1];
        int i=0;
        for(String node_name: Nodes.keySet()){
            if (!node_name.equals(query_var) && !evidence.contains(node_name)){
                hidden[i]=node_name;
                i++;
            }
        }
        return hidden;
    }

    private Factor Building_the_new_factor(Queue<Factor> MyFactors, Factor f1, Factor f2, HashMap<String,
            Integer[]> common, HashMap<String, Integer> only_f1, HashMap<String, Integer> only_f2) {

        if (f1.getName().contains(",")) {
            for (String var : f1.getName().split(",")) {
                only_f1.put(var, 0);
            }
        } else {
            only_f1.put(f1.getName(), 0);
        }

        if (f2.getName().contains(",")) {
            for (String var : f2.getName().split(",")) {
                only_f2.put(var, 0);
            }
        } else {
            only_f2.put(f2.getName(), 0);
        }

        for (String v : only_f1.keySet()) {
            if (only_f2.keySet().contains(v)) {
                common.put(v, new Integer[2]);
            }
        }
        int new_factor_size = 1;
        for (String v : common.keySet()) {
            only_f1.remove(v);
            only_f2.remove(v);
            new_factor_size *= get_Node(v).getOutcomes().size();
        }
        for (String v : only_f1.keySet()) {
            new_factor_size *= get_Node(v).getOutcomes().size();
        }
        for (String v : only_f2.keySet()) {
            new_factor_size *= get_Node(v).getOutcomes().size();
        }

        String new_factor_name = "";

        if (only_f1.size() != 0)
            new_factor_name += only_f1.keySet().toString().replace("[", "").replace("]", "")+",";

        new_factor_name +=common.keySet().toString().replace("[", "").replace("]", "");

        if (only_f2.size() != 0)
            new_factor_name += "," + only_f2.keySet().toString().replace("[", "").replace("]", "");

        new_factor_name = new_factor_name.replace(" ", "");
        String[][] arr = new String[new_factor_size + 1][common.size() + only_f1.size() + only_f2.size() + 1];
        arr[0] = (new_factor_name + ", ").split(",");
        Factor new_factor = new Factor(new_factor_name, arr);

        for (int index = 0; index < f1.getTable()[0].length - 1; index++) {
            if (common.containsKey(f1.getTable()[0][index])) {
                common.get(f1.getTable()[0][index])[0] = index;
            } else {
                only_f1.replace(f1.getTable()[0][index], index);
            }
        }
        for (int index = 0; index < f2.getTable()[0].length - 1; index++) {
            if (common.containsKey(f2.getTable()[0][index])) {
                common.get(f2.getTable()[0][index])[1] = index;
            } else {
                only_f2.replace(f2.getTable()[0][index], index);
            }
        }
        return new_factor;
    }

    /**
     *
     * @param new_factor
     * @param common
     * @param f1
     * @param f2
     * @param only_f1
     * @param only_f2
     * @param count_mul
     * @return the number of multiply actions that has been done until now
     */
    private int MultiplyTables(Factor new_factor, HashMap<String, Integer[]> common, Factor f1, Factor f2,
                HashMap<String, Integer> only_f1, HashMap<String, Integer> only_f2, int count_mul) {
        int new_factor_index = 1; //the index of the last line in the new table
        for (int r = 1; r < f1.getTable().length; r++) {
            //save all the outcomes of this curr line for each variable
            HashMap<String, String> curr_row = new HashMap<>();
            for (String s : common.keySet()) {
                curr_row.put(s, f1.getTable()[r][common.get(s)[0]]);
            }
            for (int row = 1; row < f2.getTable().length; row++) {
                boolean isEqual = true;
                //check if the curr row in f2 is the same like the line row in f1 (the equality
                //of the common variables in the tables)
                for (String s : common.keySet()) {
                    if (!f2.getTable()[row][common.get(s)[1]].equals(curr_row.get(s))) {
                        isEqual = false;
                        break;
                    }
                }
                if (isEqual) {//if the lines are equal than take the curr outcomes of the values in this line
                    for (int c = 0; c < new_factor.getTable()[0].length - 1; c++) {
                        String var = new_factor.getTable()[0][c];
                        String val = "";
                        if (common.containsKey(var)) {
                            val = curr_row.get(var);
                        } else if (only_f1.keySet().contains(var)) {
                            val = f1.getTable()[r][only_f1.get(var)];
                        } else if (only_f2.keySet().contains(var)) {
                            val = f2.getTable()[row][only_f2.get(var)];
                        }
                        new_factor.getTable()[new_factor_index][c] = val;
                    }
                    double mul = Double.parseDouble(f1.getTable()[r][f1.getTable()[0].length - 1])//calculate the multiply
                            * Double.parseDouble(f2.getTable()[row][f2.getTable()[0].length - 1]);
                    count_mul++;
                    new_factor.getTable()[new_factor_index][new_factor.getTable()[0].length - 1] = String.format("%.10f", mul);
                    new_factor_index++;
                }
            }

        }
        return count_mul;
    }

    private void set_Factors_For_Evidence(String[][] evidence, HashMap<String , List<String[][]>> factors) {
        for (int i=0; i<evidence[0].length; i++) {
            String evidence_name = evidence[0][i];
            //collect all the evidence that are also the query's parents
            //so we can reduce the factor because they are given
            HashMap<String, String> evidence_parens=new HashMap<>();
            for (int e=0; e<evidence[0].length; e++) {
                String evidence_name2 = evidence[0][e];
                if(MyParents.get(evidence_name).contains(evidence_name2)){
                    evidence_parens.put(evidence_name2, evidence[1][e]);
                }
            }
            //if the evidence has no parent- he can be removed (its multiply of a constant)
            if(MyParents.get(evidence_name).size()==0) continue;
            //create the name for the factor- all ths parents and finally the curr evidence(separate with ",")
            String factor_name = "";
            if (MyParents.get(evidence_name) != null) {
                factor_name = MyParents.get(evidence_name).toString();
                factor_name = factor_name.substring(1, factor_name.length() - 1);
                factor_name = factor_name.replaceAll(", ", ",");
            }

            HashMap<String, Double> f = new HashMap<>();
            for (String key : get_Node(evidence_name).getCPT().keySet()) {
                int end = 0;
                if (MyParents.get(evidence_name).size() != 0) {//doesn't have a parents
                    end = key.lastIndexOf('-');
                    if (key.substring(end+1).equals(evidence[1][i])) {
                        f.put(key.substring(0, end), get_Node(evidence_name).getCPT().get(key));
                    }
                }
            }
            //convert the factor from hashmap to array
            String[][] convert=Table_to_Array(evidence_name, factor_name, f, evidence_parens);


            //take the new name of the factor after removing the evidence parents from the table
            factor_name="";
            for (int v=0; v<convert[0].length-1; v++){
                factor_name+=convert[0][v]+",";
            }
            factor_name=factor_name.substring(0,factor_name.length()-1);

            if(factors.get(factor_name)==null) {
                factors.put(factor_name, new LinkedList<String[][]>());
            }
            factors.get(factor_name).add(convert);

        }
    }

    private List<String> set_Factors_For_Hidden(String[] hiddenList, String query_var, List<String> query_evidence_list,
        String[] evidenceList, String[][] evidence, HashMap<String, List<String[][]>> factors) {
        List<String> hidden_to_ignore=new LinkedList<String>();
        for (String s:hiddenList){
            //check if this hidden is Unnecessary
            if(!is_an_ancestor(s, query_evidence_list) ||Bayes_ball(s,query_var,evidenceList).equals("yes")) {
                hidden_to_ignore.add(s);
                continue;
            }

            //create the name for the factor- all ths parents and finally the curr hidden(separate with ",")
            String factor_name="";
            if(MyParents.get(s)!=null && MyParents.get(s).size()!=0) {
                factor_name = MyParents.get(s).toString();
                factor_name=factor_name.substring(1, factor_name.length()-1);
                factor_name= factor_name.replaceAll(", ", ",");
                factor_name+=",";
            }
            factor_name+=s;
            //collect all the evidence that are also the query's parents
            //so we can reduce the factor because they are given
            HashMap<String, String > evidence_parens=new HashMap<>();
            for (int i=0; i<evidence[0].length; i++) {
                String evidence_name = evidence[0][i];
                if(MyParents.get(s).contains(evidence_name)){
                    evidence_parens.put(evidence[0][i], evidence[1][i]);
                }
            }
            //convert the Hashmap to arr for the calculations
            String[][] convert=Table_to_Array(s, factor_name, get_Node(s).getCPT(), evidence_parens);

            //take the new name of the factor after removing the evidence parents from the table
            factor_name="";
            for (int v=0; v<convert[0].length-1; v++){
                factor_name+=convert[0][v]+",";
            }
            factor_name=factor_name.substring(0,factor_name.length()-1);
            factors.put(factor_name, new LinkedList<String[][]>());
            factors.get(factor_name).add(convert);
        }
        return hidden_to_ignore;
    }

    /**
     *
     * @param query_var
     * @param query_outcome
     * @param evidence
     * @return the propability value of thr query variable given the evidence
     */
    private String get_value_from_cpt(String query_var, String query_outcome, String[][] evidence) {
        if(evidence[0].length==0){//just get the probability of the query alone
            return get_Node(query_var).getCPT().get(query_outcome).toString()+",0,0";
        }

        String key="";
        for(String o: evidence[1]){
            key+=o+"-";
        }
        key+=query_outcome;

        return get_Node(query_var).getCPT().get(key).toString()+",0,0";
    }

    /**
     *
     * @param col_size
     * @param MyFactors
     * @param count_plus
     * @param hidden_i
     * @param factors
     * @return the number of times the the plus action has been used until now
     */
    private int sum_lines(int col_size, Queue<Factor> MyFactors,
         int count_plus, String hidden_i, HashMap<String, List<String[][]>> factors) {
        //get the factor we need to sum his lines
        Factor f=MyFactors.peek();
        int row_size = (f.getTable().length - 1) / (get_Node(hidden_i).getOutcomes().size());
            String[][] table_after_sum = new String[row_size + 1][col_size];
            String name_after_sum = "";
            int len =f.getName().split(",").length;
            for (String s : f.getName().split(",")) {
                if (!s.equals(hidden_i)) {
                    name_after_sum += s + ",";
                }
            }
            if (name_after_sum.charAt(name_after_sum.length() - 1) == ',')
                name_after_sum = name_after_sum.substring(0, name_after_sum.length() - 1);
            Factor factor_after_sum = new Factor(name_after_sum, table_after_sum);
            table_after_sum[0] = (factor_after_sum.getName() + ", ").split(",");
            //get the col index for each variable in the factor
            HashMap<String, Integer> var_index = new HashMap<>();
            for (int index = 0; index < f.getTable()[0].length - 1; index++) {
                    var_index.put(f.getTable()[0][index], index);
            }
            int count=0, times_input=0, r=1;
            //go over all the lines in the factor (line number r) and check if there is a line after this
            // line with the same outcomes like this line (except the hidden outcome)
            while (times_input<table_after_sum.length-1){
                count=0;
                double sum = Double.parseDouble(f.getTable()[r][f.getTable()[0].length - 1]);
                HashMap<String, String> curr_row = new HashMap<>();
                for (String curr : var_index.keySet()) {
                    String outcome = f.getTable()[r][var_index.get(curr)];
                    curr_row.put(curr, outcome);
                }
                for (int row = r+1; row < f.getTable().length; row++) {
                    boolean isEqual = true;
                    for (String curr : var_index.keySet()) {
                        //if there is a variable that his outcome doesn't fit the curr line- isEqual=false(the line doesn't fit)
                        if (!curr.equals(hidden_i) && !f.getTable()[row][var_index.get(curr)].equals(curr_row.get(curr))){
                            isEqual = false;
                            break;
                        }
                    }
                    if (isEqual) {//the line number row is same like r
                        count++;
                        sum += Double.parseDouble(f.getTable()[row][f.getTable()[0].length - 1]);
                    }
                }
                if(count==(get_Node(hidden_i).getOutcomes().size()-1)) {//if we fount x(the hidden outcomes size) fit
                    // line then we can take this sum to the new table and raise the number of plus actions
                    times_input++;
                    count_plus+=count;
                    for (int c = 0; c < table_after_sum[0].length - 1; c++) {
                        table_after_sum[times_input][c] = curr_row.get(table_after_sum[0][c]);
                    }
                    table_after_sum[times_input][table_after_sum[0].length - 1] = String.format("%.10f", sum);
                }
                r++;
            }
            MyFactors.poll();
            MyFactors.add(new Factor(name_after_sum, table_after_sum));

            if (factors.get(factor_after_sum.getName()) == null) {
                factors.put(factor_after_sum.getName(), new LinkedList<String[][]>());
            }
            factors.get(factor_after_sum.getName()).add(table_after_sum);

       return count_plus;
    }

    /**
     * normalize the last table
     * @param final_table
     * @param count_plus
     * @return the number of plus action that has been done until now
     */
    private int Normalize_table(String[][] final_table, int count_plus) {
        //calculate the sum of all the values in the table
        double sum=Double.parseDouble(final_table[1][1]);
        for (int r=2; r<final_table.length; r++){
            sum+=Double.parseDouble(final_table[r][1]);
            count_plus++;
        }
        //div all the values in the table in th sum
        for (int r=1; r<final_table.length; r++){
            double norm_value= Double.parseDouble(final_table[r][1])/sum;
            final_table[r][1]=String.format("%.5f", norm_value);
        }
        return count_plus;
    }

    @Override
    public String toString() {
        String s="";
        for (String n: Nodes.keySet()){
            s+="------------\n"+n+":\nParents:\n";
            s+=MyParents.get(n).toString();
            s+="\nChildren:\n";
            s+=MyChildren.get(n).toString()+"\n";
            s+="=======CPT========\n";
            String me=n;
            String parents="";
            for (String CptLable: MyParents.get(n)){
                parents+="-"+CptLable;
            }
            int index = parents.indexOf('-');
            me=parents.substring(index+1, parents.length())+"-"+me;
            s+=me+"\n-----------\n";
            for (String s1: get_Node(n).getCPT().keySet()){
               s+=s1+"  "+get_Node(n).getCPT().get(s1)+"\n";
            }
            s+="==================\n";
        }
        return s;
    }

}
