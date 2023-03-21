package scheduler;

import java.util.*;

public class DSP {
    private RC rc;

    public DSP(RC rc) {
        this.rc = rc;
    }

    /**
     *
     * @param sg
     * @param lddg
     */
    public void schedule(final Graph sg, Graph lddg) {
        /* get SCCs */
        Tarjans tarjans = new Tarjans();
        ArrayList<Set<Node>> sccs = tarjans.findSCCs(sg);

        //store the info of deleted Edge for reconstructing LDDG later
        HashMap<Node, Node> handledEdge = new HashMap<>();

        Set<Node> nodes = sg.getNodes(); //TODO:remove (used for debugging)

        //TODO add a data structure to save index shift (needed for LDDG)

        for (int i=0; i < sccs.size()-1; i++){
            // the SCC from which to remove edges
            Set<Node> sccCompare = sccs.get(i);
            // the other still SCCs which still have connections to other SCCs
            Set<Node> otherNodes = new HashSet<>();
            for ( int j = i+1; j < sccs.size(); j++){
                otherNodes.addAll( sccs.get(j) );
            }
            //remove the edge between sccs
            for(Node node : otherNodes){
                for ( Node preNode : node.allPredecessors().keySet() ){
                    if ( (node.getPredWeight(preNode) > 0)
                            || (sccCompare.contains(preNode)) ){
                        sg.unlinkEdge(preNode, node);
                        handledEdge.put(preNode, node);
                    }
                }
                for ( Node sucNode : node.allSuccessors().keySet() ){
                    if ( (node.getSuccWeight(sucNode) > 0)
                            || (sccCompare.contains(sucNode)) ){
                        sg.unlinkEdge(node, sucNode);
                        handledEdge.put(node, sucNode);
                    }
                }
            }
        }

        Scheduler s = new ListScheduler(rc);
        Schedule sched = s.schedule(sg);
        //System.out.printf("%nList Scheduler%n%s%n", sched.diagnose());
        //System.out.printf("cost = %s%n", sched.cost());

        //sched.draw("schedules/LS_" + problemName, problemName, resourcesName);

        // store the ll and rn of nodes
        int ll = sched.getSchedLength();
        HashMap<Node, Integer> rn = new HashMap<>();
        for (int i = 0; i < ll; i++) {
            Set<Node> set = sched.nodes(i);
            for (Node n: set) {
                rn.put(n, i + 1);
            }
        }

        // create a virtual source node
        //Node vs = new Node("vs", RT.OTHER);
        for (Node n: lddg) {
            for (Node succ: n.allSuccessors().keySet() ) {
                int shift = n.getSuccWeight(succ);
                double rn_from = rn.get(n);
                double rn_to = rn.get(succ);
                double d = n.getDelay();
                int tau = (int) (Math.ceil((d - rn_to + rn_from)/ll)) - shift;
                n.append(succ, tau);
                succ.prepend(n, tau);
            }
            /*vs.append(n, 0);
            n.prepend(vs, 0);*/
        }
        //lddg.add(vs);

        // label all nodes with sa(cn)
        HashMap<Node, Integer> cn = new HashMap<>();
        for (Node n : lddg) {
            cn.put(n, 1);
        }

        // TODO： calculate the cn (still have problem)
        /*for (Node n : lddg) {
            if(n.root()) {
                longestPath(n, cn);
            }
        }*/

        System.out.println(cn);
    }

    /**
     *
     * @param n root node from which to calculate the label
     * @param cn updated by every recursion
     */
    public void longestPath(Node n, HashMap<Node, Integer> cn) {
        if (n.allSuccessors().isEmpty())
            return ;
        else {
            for (Node succ: n.allSuccessors().keySet()) {
                int newLabel = cn.get(n) + succ.getPredWeight(n);
                if (newLabel > cn.get(succ))
                    cn.put(succ, newLabel);
                longestPath(succ, cn);
            }
        }
    }
}
