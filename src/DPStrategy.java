import java.util.*;
import java.util.List;

/**
 * A dynamics programming algorithm to select vertices for robbing.
 * Currently failing chain26, random1000, sherwood_forest
 */
public class DPStrategy implements RobbingStrategy {
    Set<String> highAlertForts = new HashSet<>();
    Set<String> fortWithStars = new HashSet<>();
    Set<String> fortWithSharp = new HashSet<>();
    Set<String> fortToAttackNow = new HashSet<>();
    Set<String> fortToAttackLater = new HashSet<>();
    HashMap<Fort, Double> memoTable = new HashMap<>();
    List<Fort> allFortsSoFar = new ArrayList<>();
    List<Fort> parents = new ArrayList<>();
    List<String> vertices;

    @Override
    public List<String> chooseOrderToAttack(LabeledValueGraph graph) {
        //TODO: Implement a DP strategy to select the order of vertices to attack
        vertices = new ArrayList<>(graph.getAllVertexLabels());
        List<String> DPOrderingList = new ArrayList<>();
        cleanGraph(graph);
        DPOrderingList.addAll(fortWithSharp);

        //Make tree with cleaned graph
        makeParents(vertices, graph);

        //Create a memo table that store the best value of gold at each fort
        for (Fort fort: allFortsSoFar){
            memoTable.put(fort, -1.0);
        }
        //Initial values are the goldIfAttackNow of all the leaves. We
        //don't have to attack the leaves first all the time,
        //but the best value of the leaves are always to attack them first!
        handleLeaves(parents);

        //Post-order traversal through the tree to fill out the memo table
        for (Fort root: parents){
            //traverse post-order
            bestGoldStolenHelperForChildren(root);
        }
        //System.out.println("Memo Table:" + memoTable);

        for(Fort fort: memoTable.keySet()){
            if(!fort.ifAttack()){
                for (Fort child : fort.getChildren()){
                    if(child.bestAttackOrder() == 0){
                        child.updateAttackStatus(true);
                    } else {
                        child.updateAttackStatus(false);
                    }
                }
            }
        }
        for(Fort fort: memoTable.keySet()){
            if (fort.ifAttack()){
                DPOrderingList.add(fort.getLabel());
            }
            //System.out.println("Attack now for each?..." + fort.label + "..." +fort.attackNow);
        }

        for (Fort fort: memoTable.keySet()){
            if (!fort.ifAttack()){
                DPOrderingList.add(fort.getLabel());
            }
        }
        //DPOrderingList.addAll(fortToAttackNow);
        //DPOrderingList.addAll(fortToAttackLater);
        DPOrderingList.addAll(fortWithStars);
        return DPOrderingList;
    }

    /***
     * This function fill out the memo table for entry fort
     * @param fort
     * @return
     */
    private double bestGoldStolenHelper(Fort fort){
        if (!memoTable.get(fort).equals(-1.0)){
            return memoTable.get(fort);
        } else {
            double maxNow = fort.goldIfAttackNow();
            double maxLater = fort.goldIfAttackLater();
            double goldAttackNow = maxNow + laterForChildren(fort.getChildren());
            double goldAttackLater = maxLater + bestForChildren(fort.getChildren());
            boolean attackNow = goldAttackNow - goldAttackLater > 0;
            fort.updateGoldNow(goldAttackNow);
            fort.updateGoldLater(goldAttackLater);

            if(attackNow){
                fort.updateAttackNow();
                fort.updateBestAttackOrder(0);
                //fortToAttackNow.add(fort.getLabel());
                for (Fort child: fort.getChildren()){
                    child.updateAttackLater();
                }
            } else {
                fort.updateBestAttackOrder(1);
                fort.updateAttackLater();
                //fortToAttackLater.add(fort.getLabel());
            }
            memoTable.put(fort, Math.max(goldAttackNow, goldAttackLater));
            return memoTable.get(fort);
        }
    }

    private double laterForChildren(List<Fort> children){
        double goldChildrenLater = 0.0;
        for (Fort child: children){
            goldChildrenLater += child.goldIfAttackLater();
        }
        return goldChildrenLater;
    }

    private double bestForChildren(List<Fort> children){
        double goldChildrenBest = 0;
        for (Fort child: children){
            goldChildrenBest += bestGoldStolenHelper(child);
        }
        return goldChildrenBest;
    }


    public class Fort {
        String label;
        List<Fort> children = new ArrayList<>();
        double startValue;
        double goldNow;
        double goldLater;
        boolean attackNow;
        /***
         * bestAttackOrder is 0 if we attack now, otherwise 0
         */
        int bestAttackOrder;

        Fort(String label, LabeledValueGraph graph){
            this.label = label;
            this.startValue = computeOGGoldValue(graph, label);
            this.goldNow = computeOGGoldValue(graph, label);
            this.goldLater = goldNow/2;
            this.attackNow = false;

        }
        private String getLabel(){
            return label;
        }
        private double goldIfAttackNow(){
            return this.goldNow;
        }

        private double goldIfAttackLater(){
            return this.goldLater;
        }

        private void updateGoldNow(double goldNow){
            this.goldNow = goldNow;
        }

        private void updateGoldLater(double goldLater){
            this.goldLater = goldLater;
        }
        private boolean ifAttack(){
            return attackNow;
        }
        private void updateAttackStatus(boolean order){
            this.attackNow = order;
        }

        private void updateBestAttackOrder(int order){
            this.bestAttackOrder = order;
        }

        private int bestAttackOrder(){
            return this.bestAttackOrder;
        }

        @Override
        public String toString(){
            return String.format("Fort %s (%.1f)", label, startValue);
        }

        private List<Fort> getChildren(){
            return children;
        }
        private void updateAttackNow(){
            this.attackNow = true;
        }

        private void updateAttackLater(){
            this.attackNow = false;
        }

    }

    public void makeParents(List<String> vertices, LabeledValueGraph graph){
        while(!vertices.isEmpty()){
            String label = vertices.getFirst();
            vertices.remove(label);
            Fort fort = new Fort(label, graph);
            parents.add(fort);
            allFortsSoFar.add(fort);
            makeTree(fort, graph);
        }
    }

    public void makeTree(Fort fort, LabeledValueGraph graph){
        for(String vertex : graph.getAdjacentVertices(fort.label)){
            if (vertices.contains(vertex)){
                graph.removeEdge(fort.label, vertex);
                vertices.remove(vertex);
                Fort child = new Fort(vertex, graph);
                fort.children.add(child);
                allFortsSoFar.add(child);
                makeTree(child, graph);
            }
        }
    }

    private void cleanGraph(LabeledValueGraph graph) {
        /***
         * This function remove all the fort with * and add all fort
         * with ! to high alert list. This is "data preprocessing" step
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

    private double computeOGGoldValue(LabeledValueGraph graph, String fortName){
        //OG stands for original
        double goldHere = graph.getValueAt(fortName);
        if (highAlertForts.contains(fortName) && !fortName.contains("*")) { //*
            goldHere = goldHere / 2.0;
        }
        return goldHere;
    }

    private void handleLeaves(List<Fort> parents){
        for (Fort root: parents){
            handleLeaf(root);
        }
    }
    private void handleLeaf(Fort fort){
        if (fort.getChildren().isEmpty()){
            memoTable.put(fort, fort.goldIfAttackNow());
            fort.updateAttackNow();
        }
        for (Fort child : fort.getChildren()){
            handleLeaf(child);
        }
    }

    private void bestGoldStolenHelperForChildren(Fort fort){
        if(fort.getChildren().isEmpty()){
            bestGoldStolenHelper(fort);
            return;
        }
        for (Fort child: fort.getChildren()){
            bestGoldStolenHelperForChildren(child);
        }
        bestGoldStolenHelper(fort);
    }


}
