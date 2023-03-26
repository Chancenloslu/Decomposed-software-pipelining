package scheduler;

import java.util.*;

public class Main {

	public static void main(String[] args) {

		RC rc = null;
//		String resourcesName = null;

		if (args.length>1){
			System.out.println("Reading resource constraints from "+args[1]+"\n");
			rc = new RC();
			rc.parse(args[1]);
//			resourcesName = args[1];
		}

		Scheduler s = new ListScheduler(rc);
		
		ProblemReader dr = new ProblemReader(true);

		if (args.length < 1) {
			System.err.printf("Usage: scheduler dotfile%n");
			System.exit(-1);
		}else {
			System.out.println("Scheduling "+args[0]);
			System.out.println();
		}

		Graph g = dr.parse(args[0]);
		Graph lddg = dr.parse(args[0]);

		System.out.printf("%s%n", g.diagnose());

//		Schedule sched = s.schedule(g);
//		System.out.printf("%nList Scheduler%n%s%n", sched.diagnose());
//		System.out.printf("cost = %s%n", sched.cost());

		System.out.printf("%nDSP%n");
		DSP dsp = new DSP(s);
		dsp.schedule(g, lddg);
		System.out.printf("ii = %d%n", dsp.getIi());
		System.out.printf("depth = %d%n", dsp.getDepth());


		/* exemplary validation of a schedule */

//		Node conflictingNode = sched.validateDependencies();
//		if (conflictingNode != null) {
//			System.out.println("Schedule validation failed. First conflicting node: " + conflictingNode.id);
//		} else {
//			System.out.println("Dependency validation successful.");
//		}
//
//		Node overusingNode = sched.validateResources();
//		if (overusingNode != null) {
//			System.out.println("Resource usage validation failed. First overuse by node " + overusingNode.id);
//		} else {
//			System.out.println("Resource usage validation successful.");
//		}

	}
}
