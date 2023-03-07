package scheduler;

import java.awt.*;
import java.util.*;

public class Tarjans {
    private Stack<Node> stack = new Stack<Node>();
    private HashMap<Node, Integer> dfn;
    private HashMap<Node, Integer> low;
    ArrayList<Node> nodes;
    private Integer index;

    public ArrayList<Set<Node>> findSCCs(Graph g) {
        nodes = new ArrayList<>(g.getNodes());
        dfn = new HashMap<>();
        low = new HashMap<>();
        index = 0;
        ArrayList<Set<Node>> sccs = new ArrayList<>();
        for (Node n: nodes) {
            tarjan(n, sccs);
        }
        return sccs;
    }

    public void tarjan(Node u, ArrayList<Set<Node>> sccs) {
        dfn.put(u, index);
        low.put(u, index);
        index++;
        stack.push(u);

        Iterator it = u.successors().iterator();
        while (it.hasNext()) {
            Node v = (Node) it.next();
            //System.out.println(v.id);
            if(!stack.contains(v)) {
                tarjan(v, sccs);
                low.replace(u, Math.min(low.get(u), low.get(v)));
            }
            else if(stack.contains(v)){
                low.replace(u, Math.min(low.get(u), dfn.get(v)));
            }
        }

        if (dfn.get(u)==low.get(u)) {
            Set<Node> scc = new HashSet<>();
            Node w;
            do {
                w = stack.pop();
                scc.add(w);
            }while (!w.equals(u));
            sccs.add(scc);
        }
    }
}
