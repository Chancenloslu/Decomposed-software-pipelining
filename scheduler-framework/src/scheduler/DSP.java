package scheduler;

import java.util.*;

public class DSP {
    private final Scheduler s;
    HashMap<Node, Integer> rn;
    HashMap<Node, Integer> cn;
    Integer ii;

    Integer depth;

    public DSP(Scheduler s) {
        this.s = s;
    }

    public Integer getIi() {
        return ii;
    }

    public Integer getDepth() {
        return depth;
    }

    /**
     *
     * @param sg    from which we construct rn
     * @param lddg  from which we construct cn
     */
    public void schedule(final Graph sg, Graph lddg) {
        rn = new HashMap<>();
        cn = new HashMap<>();
        /* get SCCs */
        Tarjans tarjans = new Tarjans();
        ArrayList<Set<Node>> sccs = tarjans.findSCCs(sg);

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

        Schedule sched = s.schedule(sg);

        // store the ll and rn of nodes
        ii = sched.length();
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

        // label all nodes with sa/cn using longest algorithm
        int columnLength = 0;
        for (Node n : lddg) {
            int pathLength = longestPath(vs, n);
            cn.put(n, pathLength);
            if (columnLength < pathLength) {
                columnLength = pathLength;
            }
        }
        depth = columnLength;

        // form the new body
        Set<Node>[][] loop = new Set[ii][columnLength];
        for (int i = 0; i < columnLength; i++) {
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
        printOut(loop);
    }

    /**
     * calculate the longest path between two nodes of a graph.
     * nodes have to be in same graph.
     *
     * @param vs   start node
     * @param node end node
     * @return length of path
     */
    private int longestPath(Node vs, Node node) {
        Deque<Node> dfsQueue = new ArrayDeque<>(vs.allSuccessors().keySet());
        Deque<Node> inPath = new ArrayDeque<>();
        inPath.add(vs);
        int pathLength = 1;
        int longestPath = 1;
        Node nextNode;
        while(true){
            // termination and multiple backtracking
            nextNode = dfsQueue.peekFirst();
            if (nextNode==null){ //termination
                break;
            } else { //for multiple backtrackings
                if (! inPath.peekLast().allSuccessors().containsKey(nextNode)){
                    Node lastNode = inPath.removeLast();
                    pathLength -= inPath.peekLast().getSuccWeight(lastNode);
                    continue;
                }
            }
            // pop next node from queue
            nextNode = dfsQueue.removeFirst();

            // add edge to this node to path and add node to inPath
            int edgeWeight = inPath.peekLast().getSuccWeight(nextNode);
            inPath.addLast(nextNode);
            pathLength += edgeWeight;

            // test to see if terminal node. if, then backtrack
            if (nextNode == node){
                if (pathLength > longestPath){
                    longestPath = pathLength;
                }
                inPath.removeLast();
                pathLength -= edgeWeight;
                continue;
            }

            // add successors of node to queue if they are not already in current path
            Set<Node> successors = nextNode.allSuccessors().keySet();
            boolean noSuccessor = true;
            for(Node succ : successors){
                if(!inPath.contains(succ)){
                    noSuccessor = false;
                    dfsQueue.addFirst(succ);
                }
            }

            // if no successors backtrack
            if (noSuccessor) {
                inPath.removeLast();
                pathLength -= edgeWeight;
                continue;
            }
        }
        return longestPath;
    }

    /**
     * print out the result of dsp to stdout.
     * @param loop the 2D array of set of nodes to print
     */
    private void printOut(Set<Node>[][] loop) {
        int noOfRows = loop.length;
        int noOfColumns = loop[0].length;

        // generate strings from nodes
        String[][] loopPrintOuts = new String[noOfRows][noOfColumns];
        int[] columnWidth = new int[noOfColumns];
        for (int i = 0; i < noOfRows; i++) {
            for (int j = 0; j < noOfColumns; j++) {
                Set<Node> nodes = loop[i][j];
                Iterator<Node> nodeIterator = nodes.iterator();
                StringBuilder cell = new StringBuilder();
                while (nodeIterator.hasNext()) {
                    cell.append(nodeIterator.next().toString());
                    if (nodeIterator.hasNext()){
                        cell.append(",");
                    }
                }
                loopPrintOuts[i][j] = cell.toString();
                if (loopPrintOuts[i][j].length() > columnWidth[j]){
                    columnWidth[j] = loopPrintOuts[i][j].length();
                }
            }
        }

        //build strings
        StringBuilder hline = new StringBuilder("-");
        StringBuilder header = new StringBuilder(" ");
        for (int i = 0; i < "row".length() + Integer.toString(noOfRows).length(); i++){
            hline.append("-");
            header.append(" ");
        }
        hline.append("-+");
        header.append(" |");
        for (int i = noOfColumns-1; i >= 0; i--){
            hline.append("-");
            for (int j = 0; j < columnWidth[i]; j++){
                hline.append("-");
            }
            hline.append("-+");
            header.append(" ");
            if (columnWidth[i] != 0) {
                header.append(String.format("%"+columnWidth[i]+"s", "c"+Integer.toString(i+1)));
            }
            header.append(" |");
        }

        // output
        System.out.println();
        System.out.println(header);
        System.out.println(hline);
        for (int i = 0; i < noOfRows; i++) {
            StringBuilder row = new StringBuilder(" ");
            row.append( String.format("row%-" + Integer.toString(noOfRows).length() + "d", i+1) );
            row.append(" |");
            for (int j = noOfColumns-1; j >= 0; j--) {
                row.append(" ");
                if (columnWidth[j] != 0) {
                    row.append(String.format("%" + columnWidth[j] + "s", loopPrintOuts[i][j]));
                }
                row.append(" |");
            }
            System.out.println(row);
        }
        System.out.println(hline);
    }
}
