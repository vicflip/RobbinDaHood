import java.util.*;
import java.util.List;

/**
 * A dynamics programming algorithm to select vertices for robbing.
 */
public class DPStrategy implements RobbingStrategy {
    Set<String> highAlertForts = new HashSet<>();
    Set<String> fortWithStars = new HashSet<>();
    Set<String> fortWithSharp = new HashSet<>();
    List<Fort> parents = new ArrayList<>();
    List<String> vertices;
    List<String> greedyOrdering;

    public class Fort {
        String label;
        List<Fort> children = new ArrayList<>();
        double startValue;
        double maxNow;
        double maxLater;
        Order nowOrder;
        Order laterOrder;

        Fort(String label, LabeledValueGraph graph){
            this.label = label;
            this.maxNow = getMaxNow(label, graph);
            this.maxLater = getMaxLater(label, graph);
            this.startValue = graph.getValueAt(label);
        }

        class Order {
            List<String> nowNodes;
            List<String> laterNodes;

            Order (List<String> now, List<String> later){
                nowNodes = new ArrayList<>(now);
                laterNodes = new ArrayList<>(later);
            }
        }

        public void mergeBothOrders(Fort fort1, Fort fort2){
            mergeNowOrder();
            mergeLaterOrder();
        }

        public void mergeNowOrder(){

        }

        public void mergeLaterOrder(){

        }

        double getMaxLater(String label, LabeledValueGraph graph){
            double val = graph.getValueAt(label);
            if(label.contains("#")){
                this.maxLater = val;
            }
            if(label.contains("*")){
                this.maxLater = val;
            }
            if(label.contains("!")){
                this.maxLater = val/2;
            }
            return 2.0;
        }

        double getMaxNow(String label, LabeledValueGraph graph){
            double val = graph.getValueAt(label);
            if(label.contains("#")){
                this.maxLater = val;
            }
            if(label.contains("*")){
                this.maxLater = val;
            }
            if(label.contains("!")){
                this.maxLater = val/2;
            }
            return 2.0;
        }
        @Override
        public String toString(){
            return String.format("Fort %s (%.1f)", label, startValue);
        }

    }

    @Override
    public List<String> chooseOrderToAttack(LabeledValueGraph graph) {
        //TODO: Implement a DP strategy to select the order of vertices to attack
        vertices = new ArrayList<>(graph.getAllVertexLabels());
        cleanGraph(graph);
        greedyOrdering = new ArrayList<>();
        vertices = new ArrayList<>(graph.getAllVertexLabels());
        System.out.println("Vertices before " + vertices);
        makeParents(vertices, graph);
        System.out.println("Vertices after " + vertices);
        System.out.println("Parents after " + parents);
        printTrees(parents);

        return greedyOrdering;
    }

    public void makeParents(List<String> vertices, LabeledValueGraph graph){
        while(!vertices.isEmpty()){
            String label = vertices.getFirst();
            vertices.remove(label);
            Fort fort = new Fort(label, graph);
            parents.add(fort);
            makeTree(fort, graph);
        }
    }

    public void makeTree(Fort fort, LabeledValueGraph graph){
        for(String vertex : graph.getAdjacentVertices(fort.label)){
            graph.removeEdge(fort.label, vertex);
            vertices.remove(vertex);
            Fort child = new Fort(vertex, graph);
            fort.children.add(child);
            makeTree(child, graph);
        }
    }

    private void cleanGraph(LabeledValueGraph graph) {
        /***
         * This function remove all the fort with * and add all fort
         * with ! to high alert list. This is "data preprocessing"
         */
        for (String fortName : vertices){
            if(fortName.contains("#") && !fortName.contains("!") && !fortName.contains("*")){
                fortWithSharp.add(fortName);
            }
            if (fortName.contains("*")) { //*
                fortWithStars.add(fortName);
            }
            if (fortName.contains("!")){
                highAlertForts.add(fortName);
            }
        }
        vertices.removeAll(fortWithSharp);
        vertices.removeAll(fortWithStars);
    }

    public void printTrees(List <Fort> parents){
        for(Fort fort : parents){
            System.out.print("New tree root is ");
            printTree(fort);
        }
    }

    public void printTree(Fort fort){
        if(fort.children.isEmpty()){
            System.out.println(fort + " has no children!");
        } else {
            System.out.println(fort + " has children " + fort.children);
            for(Fort child : fort.children){
                printTree(child);
            }
        }
    }

}
