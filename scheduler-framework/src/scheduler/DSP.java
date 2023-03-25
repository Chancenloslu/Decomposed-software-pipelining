package scheduler;

import java.util.*;

public class DSP {
    private RC rc;
    HashMap<Node, Integer> rn;
    HashMap<Node, Integer> cn;
    Integer ii;
    public DSP(RC rc) {
        this.rc = rc;
    }

    public Integer getIi() {
        return ii;
    }

    /**
     *
     * @param sg    from which we construct rn
     * @param lddg  fromn which we construct cn
     */
    public void schedule(final Graph sg, Graph lddg) {
        rn = new HashMap<>();
        cn = new HashMap<>();
        /* get SCCs */
        Tarjans tarjans = new Tarjans();
        ArrayList<Set<Node>> sccs = tarjans.findSCCs(sg);

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
                    }
                }
                for ( Node sucNode : node.allSuccessors().keySet() ){
                    if ( (node.getSuccWeight(sucNode) > 0)
                            || (sccCompare.contains(sucNode)) ){
                        sg.unlinkEdge(node, sucNode);
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
        ii = sched.getSchedLength();
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

        // label all nodes with sa(cn) using longest algorithm
        int cl = 0;// coloumn length
        for (Node n: lddg) {
            HashMap<Node, Integer> handledNode = new HashMap<>();
            handledNode.put(vs, 1);
            longestPath(vs, n, handledNode);
            int label = handledNode.get(n);
            if (cl < label) {
                cl = label;
            }
            cn.put(n, label);
        }

        // form the new body
        Set<Node>[][] loop = new Set[ii][cl];
        for (int i = 0; i < cl; i++) {
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
     * @param src   source node
     * @param dest  destination node
     * @param handledNodes  handled nodes for taking notes during
     */
    public void longestPath(Node src, Node dest, HashMap<Node, Integer> handledNodes) {
        int res = 0;
        int newlabel = 0;
        if (src.allSuccessors().isEmpty())
            return;
        else {
            for (Node succ: src.allSuccessors().keySet()) {
                newlabel = succ.getPredWeight(src) + handledNodes.get(src);
                if (!handledNodes.containsKey(succ)) {
                    handledNodes.put(succ, newlabel);
                } else {
                    // not start from already handled nodes again
                    if (newlabel > handledNodes.get(succ)) {
                        handledNodes.put(succ, newlabel);
                    }
                    continue;
                }
                if (succ.equals(dest))
                    return;
                longestPath(succ, dest, handledNodes);
            }
        }
    }

    private void loopPrint (Set<Node>[][] loop) {
        int row = loop.length;
        int col = loop[0].length;
        String leftAlignFormat = "| %-4s ";
        for (int i=0; i<col; i++)
            leftAlignFormat += "| %-30s ";
        leftAlignFormat += "|%n";

//        System.out.format("+------+--------------------------------+--------------------------------+%n");
//        System.out.format("|      |            column1             |              column0           |%n");
//        System.out.format("+------+--------------------------------+--------------------------------+%n");
        System.out.format("+-----+----------------------------------------------------------------+%n");
        for (int i = 1; i <= row; i++) {      //row index
            String out1 = " row" + i;
            String out2 = "";
            String out3 = "";
            for (int j=1; j<col; j++) {
                for (Node n : loop[i - 1][j - 1])
                    out2 += n.toString() + ",";
                out2 += "\t| ";
            }
            System.out.println(out1 + " | " + out2);
        }
//        System.out.format("+------+--------------------------------+--------------------------------+%n");
    }
}
