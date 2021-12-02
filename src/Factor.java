import java.util.HashMap;

public class Factor implements Comparable<Factor> {
    private String name;
    private String[][] Table;

    public Factor(String name, String[][] table){
        this.name=name;
        this.Table=table;
    }

    public String getName() {
        return name;
    }

    public String[][] getTable() {
        return Table;
    }

    @Override
    public int compareTo(Factor o) {
        if(this.getTable().length > o.getTable().length){
            return 1;
        }
        else if(this.getTable().length < o.getTable().length) {
            return -1;
        }
        else{
            int sum_of_ASCII_1=0, sum_of_ASCII_2=0;
            for(int i=0; i<this.getName().length(); i++){
                if(this.getName().charAt(i)!=','){
                    sum_of_ASCII_1+=(int)this.getName().charAt(i);
                }
            }
            for(int i=0; i<o.getName().length(); i++){
                if(o.getName().charAt(i)!=','){
                    sum_of_ASCII_2+=(int)o.getName().charAt(i);
                }
            }
            if(sum_of_ASCII_1>=sum_of_ASCII_2) return 1;
            else return -1;
        }
    }

}
