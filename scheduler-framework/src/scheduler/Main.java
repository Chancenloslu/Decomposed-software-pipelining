package scheduler;

import java.io.*;
import java.util.*;

public class Main {

	private static final String writeOutFile = "evaluation.csv";

	public static void main(String[] args) {

		if (args.length < 1) {
			System.err.printf("Usage: scheduler dotfile%n");
			System.exit(-1);
		}

		RC rc = null;
		String resourcePath = null;
		if (args.length > 1){
			resourcePath = args[1];
			System.out.println("Reading resource constraints from "+  resourcePath + "\n");
			rc = new RC();
			rc.parse(resourcePath);
		}
		String resourceName = resourcePath.substring(resourcePath.lastIndexOf("/")+1);

		Scheduler s = new ListScheduler(rc);
		
		ProblemReader dr = new ProblemReader(true);

		String problemGraph = "";
		problemGraph = args[0];
		String problemName = problemGraph.substring(problemGraph.lastIndexOf("/")+1);
		System.out.println("Scheduling " + problemGraph);
		System.out.println();

		Graph g = dr.parse(problemGraph);
		Graph lddg = dr.parse(problemGraph);

		System.out.printf("%s%n", g.diagnose());

//		Schedule sched = s.schedule(g);
//		System.out.printf("%nList Scheduler%n%s%n", sched.diagnose());
//		System.out.printf("cost = %s%n", sched.cost());

		System.out.printf("%nDSP%n");
		DSP dsp = new DSP(s);
		dsp.schedule(g, lddg);
		System.out.printf("ii = %d%n", dsp.getIi());
		System.out.printf("depth = %d%n", dsp.getDepth());

		BufferedWriter w;
		File out = new File(writeOutFile);
		try {
			w = new BufferedWriter(new FileWriter(out, true));
			if (!out.exists() || out.length() == 0 ) {
				w.write(resourceName + ",,");
				w.newLine();
				w.write("problemGraph,ii,depth");
				w.newLine();
				w.flush();
			}
			w.append(problemName).append(",").append(dsp.getIi().toString()).append(",").append(dsp.getDepth().toString());
			w.newLine();
			w.close();
		} catch (IOException e) {
			System.out.println("File: " + writeOutFile + " is not existing.");
			throw new RuntimeException(e);
		}

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
