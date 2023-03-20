package scheduler;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.Set;

public class DSP {

    public void schedule(final Graph sg) {
        /* get SCCs */
        Tarjans tarjans = new Tarjans();
        ArrayList<Set<Node>> sccs = tarjans.findSCCs(sg);
        Iterator<Set<Node>> it = sccs.iterator();
        /* get nodes to be handled */
        Set<Node> nodeToHandle = sg.getNodes();
        while (it.hasNext()) {
            Set<Node> set = it.next();
            if(set.size() > 1) {
                for (Node n: set) {
                    nodeToHandle.remove(n);
                }
            }
        }

        for (Node n: nodeToHandle) {
            for (Node pred: n.allPredecessors().keySet()) {
                n.remove(pred);
                pred.remove(n);
            }
            //n.remove()
        }


    }
}
