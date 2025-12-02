import java.util.*;

/**
 * A greedy algorithm to select vertices for robbing.
 */
public class GreedyStrategy implements RobbingStrategy {
    Set<String> highAlertForts = new HashSet<>();
    Set<String> badForts = new HashSet<>();
    List<String> vertices;

    @Override
    public List<String> chooseOrderToAttack(LabeledValueGraph graph) {
        //TODO: Implement a greedy strategy to select the order of vertices to attack
        vertices = graph.getAllVertexLabels();
        System.out.println("Pre greedy vertex: " + vertices);
        List<String> greedyOrdering = new ArrayList<>();
        greedyAttack(graph, vertices, greedyOrdering);
        greedyOrdering.addAll(badForts);
        for(String fort : highAlertForts) {
            if(! greedyOrdering.contains(fort)){
                greedyOrdering.add(fort);
            }
        }
        //System.out.println("bad forts: " + badForts);
        System.out.println("Final order:       " + greedyOrdering);
        return greedyOrdering;
    }

    private void greedyAttack(LabeledValueGraph graph, List<String> vertices, List<String> greedyOrdering){
        //System.out.println("greedy");
        if(vertices.isEmpty()){
            return;
        }
        removeBadForts(graph);
        //System.out.println(vertices);
        //Find the fort to attack
        String maxFort = computeGoldMax(graph, vertices);

        //Update greedyOrdering and attack that fort
        if(! attackFort(graph, maxFort)){
            return;
        }
        greedyOrdering.add(maxFort);


        //remove the max vertices from the list to rerun GreedyAttack()
        vertices.remove(maxFort);

        //rerun GreedyAttack of graph g - fort
        greedyAttack(graph, vertices, greedyOrdering);
    }

    private void removeBadForts(LabeledValueGraph graph) {
        for (String fortName : vertices){
            if (fortName.contains("*")) { //*
                badForts.add(fortName);
            }
        }
        vertices.removeAll(badForts);
    }

    private String computeGoldMax(LabeledValueGraph graph, List<String> vertices){
        //List<Double> goldAtEachFort = new ArrayList<>();
        //System.out.println("comp max");
        double MaxGold = Double.MIN_VALUE;
        String maxFort = "";
        for (String fortName : vertices){
            double goldHere = graph.getValueAt(fortName);
            if (highAlertForts.contains(fortName) && !fortName.contains("*")) { //*
                goldHere = goldHere / 2.0;
            }
            if((goldHere > MaxGold) && ! highAlertForts.contains(fortName)){
                    MaxGold = goldHere;
                    maxFort = fortName;
            } else if (((goldHere == MaxGold) && ! highAlertForts.contains(fortName))){
                if(graph.getAdjacentVertices(fortName).size() < graph.getAdjacentVertices(maxFort).size()){
                    maxFort = fortName;
                }
            }
            //goldAtEachFort.add(goldHere);
        }
        return maxFort;
    }

    private boolean attackFort(LabeledValueGraph graph, String fortName){
        if(Objects.equals(fortName, "")){
            return false;
        }
        //System.out.println("attacking : " + fortName);
        if (fortName.contains("!")) { //!
            highAlertForts.add(fortName);
        }
        if (!fortName.contains("#")) { //#
            List<String> neighbors = graph.adj(fortName);
            highAlertForts.addAll(neighbors);
        }
        //System.out.println("attack end");
        return true;
    }

}