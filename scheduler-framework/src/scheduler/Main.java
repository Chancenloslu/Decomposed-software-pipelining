package scheduler;

import java.io.*;
import java.util.*;

public class Main {

	// used for writing out results to file for evaluation
//	private static final String writeOutFile = "evaluation.csv";

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
//		String resourceName = resourcePath.substring(resourcePath.lastIndexOf("/")+1); // used for evaluation

		Scheduler s = new ListScheduler(rc);
		
		ProblemReader dr = new ProblemReader(true);

		String problemGraph = "";
		problemGraph = args[0];
//		String problemName = problemGraph.substring(problemGraph.lastIndexOf("/")+1); // used for evaluation
		System.out.println("Scheduling " + problemGraph);
		System.out.println();

		Graph g = dr.parse(problemGraph);
		Graph lddg = dr.parse(problemGraph);

		System.out.printf("%s%n", g.diagnose());

		System.out.printf("%nDSP%n");
		DSP dsp = new DSP(s);
		dsp.schedule(g, lddg);
		System.out.printf("ii = %d%n", dsp.getIi());
		System.out.printf("depth = %d%n", dsp.getDepth());

		// write out results of schedule to file for evaluation
		/*
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
		*/
	}
}
