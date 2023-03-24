package scheduler;

import java.util.*;

public class DSP {
    private RC rc;
    HashMap<Node, Integer> rn;
    HashMap<Node, Integer> cn;
    public DSP(RC rc) {
        this.rc = rc;
    }

    /**
     *
     * @param sg
     * @param lddg
     */
    public void schedule(final Graph sg, Graph lddg) {
        rn = new HashMap<>();
        cn = new HashMap<>();
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
        int ii = sched.getSchedLength();
        for (int i = 0; i < ii; i++) {
            Set<Node> set = sched.nodes(i);
            for (Node n: set) {
                rn.put(n, i + 1);
            }
        }

        // create a virtual source node
        Node vs = new Node("vs", RT.OTHER);
        for (Node n: lddg) {
            for (Node succ: n.allSuccessors().keySet() ) {
                int shift = n.getSuccWeight(succ);
                double rn_from = rn.get(n);
                double rn_to = rn.get(succ);
                double d = n.getDelay();
                int tau = (int) (Math.ceil((d - rn_to + rn_from)/ii)) - shift;
                n.append(succ, tau);
                succ.prepend(n, tau);
            }
            vs.append(n, 0);
            n.prepend(vs, 0);
        }
        lddg.add(vs);

        //TODO: label all nodes with sa(cn) using longest algorithm
//        for (Node n : sched.nodes()) {
//            System.out.println(n.toString() + " : " + lddg.dijkstra(n));
//        }

        for (Node n : lddg) {
            cn.put(n, 1);
        }
        // new label should not be larger than 2 I think
        // still problem with longest path algorithm
        for (Node n: lddg) {
            for (Node pred: n.allPredecessors().keySet()) {
                int label = n.getPredWeight(pred) + cn.get(pred);
                if (label > cn.get(n)) {
                    cn.put(n, label);
                }
            }
        }

        // form the new body
        Set<Node>[][] loop = new Set[ii][2];
        for (int i = 0; i < 2; i++) {
            for (int j = 0; j < ii; j++) {
                loop[j][i] = new HashSet<>();
            }
        }
        for (Node n: lddg) {
            if(!n.equals(vs)) {
                int row = rn.get(n) - 1;
                int col = cn.get(n) - 1;
                loop[row][col].add(n);
            }
        }
        loopPrint(loop);

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

    private void loopPrint (Set<Node>[][] loop) {
        String leftAlignFormat = "| %-4s | %-30s | %-30s |%n";

        System.out.format("+------+--------------------------------+--------------------------------+%n");
        System.out.format("|      |            column1             |              column0           |%n");
        System.out.format("+------+--------------------------------+--------------------------------+%n");

        for (int i = 1; i <= loop.length; i++) {      //row index
            String out1 = "row" + i;
            String out2 = "";
            String out3 = "";
            for (Node n: loop[i-1][1])
                out2 += n.toString() + ",";
            for (Node n: loop[i-1][0])
                out3 += n.toString() + ",";
            System.out.format(leftAlignFormat, out1, out3, out2);
        }
        System.out.format("+------+--------------------------------+--------------------------------+%n");
    }
}
