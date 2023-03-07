package scheduler;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Set;
import java.util.Stack;

public class LDDG extends Graph{

    ArrayList<Node> plainnodes = new ArrayList<>();
    HashMap<Node, ArrayList<Edge>> edges;

    HashMap<Node, Integer> rn;
    HashMap<Node, Integer> cn;

    public LDDG() {
        super();
    }

    public LDDG(Graph g){
        for (Node n: g.getNodes()) {
            plainnodes.add(n);
        }
    }

    public void schedule() {
        Tarjans tarjans = new Tarjans();
        ArrayList<Set<Node>> sccs = tarjans.findSCCs(this);
        int i = 0;
    }

    /**
     * an inner class Edge
     */
    private class Edge {
        private Node start;
        private Node end;
        private int[] weight = new int[2];

        public Edge(Node start, Node end, int idxShift, int latency) {
            this.start = start;
            this.end = end;
            this.weight[0] = idxShift;
            this.weight[1] = latency;
        }

        public Node getStart() {
            return start;
        }

        public Node getEnd() {
            return end;
        }

        public int getIndexShift() {
            return weight[0];
        }

        public int getLatency() {
            return weight[1];
        }
    }
}
