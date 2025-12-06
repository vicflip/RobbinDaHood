import java.util.*;

/**
 * A greedy algorithm to select vertices for robbing.
 */
public class GreedyStrategy implements RobbingStrategy {
    Set<String> highAlertForts = new HashSet<>();
    Set<String> fortWithStars = new HashSet<>();
    Set<String> fortWithSharp = new HashSet<>();
    List<String> vertices;
    List<String> forHighAlertList = new ArrayList<>();

    @Override
    public List<String> chooseOrderToAttack(LabeledValueGraph graph) {
        //TODO: Implement a greedy strategy to select the order of vertices to attack
        vertices = graph.getAllVertexLabels();
        List<String> greedyOrdering = new ArrayList<>();
        cleanGraph(graph);
        greedyOrdering.addAll(fortWithSharp);
        greedyAttack(graph, vertices, greedyOrdering, false);
        for(String fort : highAlertForts) {
            if(!greedyOrdering.contains(fort) && !fortWithStars.contains(fort)){
                greedyOrdering.add(fort);
            }
        }
        greedyOrdering.addAll(fortWithStars);
        return greedyOrdering;
    }

    private void greedyAttack(LabeledValueGraph graph, List<String> vertices, List<String> greedyOrdering, boolean forHighAlert){
        if(vertices.isEmpty()){
            return;
        }
        String maxFort = computeGoldMax(graph, vertices, forHighAlert);

        if(!attackFort(graph, maxFort)){
            return;
        }
        greedyOrdering.add(maxFort);

        //remove the max vertices from the list to rerun GreedyAttack()
        vertices.remove(maxFort);

        //rerun GreedyAttack of graph g - fort
        greedyAttack(graph, vertices, greedyOrdering, forHighAlert);
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

    private String computeGoldMax(LabeledValueGraph graph, List<String> vertices, boolean forHighAlert){
        double MaxGold = Double.MIN_VALUE;
        String maxFort = "";
        for (String fortName : vertices){
            double goldHere = graph.getValueAt(fortName);
            if (highAlertForts.contains(fortName) && !fortName.contains("*")) { //*
                goldHere = goldHere / 2.0;
            }
            /***
             * Now this is the problem: After a while,
             * I will have a graph all high alert. This code will not
             * handle them!
             */
            if (forHighAlert){
                if((goldHere > MaxGold)){
                    MaxGold = goldHere;
                    maxFort = fortName;
                } else if ((goldHere == MaxGold)){
                    if(graph.getAdjacentVertices(fortName).size() < graph.getAdjacentVertices(maxFort).size()){
                        maxFort = fortName;
                    }
                }
            } else {
                if((goldHere > MaxGold) && !highAlertForts.contains(fortName)){
                    MaxGold = goldHere;
                    maxFort = fortName;
                } else if ((goldHere == MaxGold) && !highAlertForts.contains(fortName)){
                    if(graph.getAdjacentVertices(fortName).size() < graph.getAdjacentVertices(maxFort).size()){
                        maxFort = fortName;
                    }
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
        if (fortName.contains("!")) { //!
            highAlertForts.add(fortName);
        }
        if (!fortName.contains("#")) { //#
            List<String> neighbors = graph.adj(fortName);
            highAlertForts.addAll(neighbors);
        }
        return true;
    }

}