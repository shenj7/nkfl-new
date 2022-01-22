import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class ExperimentRunner {
	
	
	public static void main(String[] args) {
		//Strategy Parameters
		int strategyLength = 50;
		int sensitivity = 1;
		int maxSensitivity = 14; //Set this equal to sensitivity for a single sensitiviry run

		//Landscape Parameters
		int n = 15;
		int k = 6;
		
		//Evolution Parameters
		String selectionType = "mutation"; 
		int numGenerations = 100;
		int popsPerGeneration = 100;
		int childrenPercentage = 50; //always set to 100 for ranked
		double mutationPercentage = 2;
		
		//Seed parameters
		long seed = 7; //Set Seed
//		long seed = SeededRandom.rnd.nextInt(); //Random seed
		SeededRandom.rnd.setSeed(seed);

		//Data Reporting Parameters
		int incrementCSVoutput = 10;
		String experimentName = "Experiment_" + seed + "_" + selectionType;
		PrintWriter csvWriter;
		File csvFile = new File(experimentName);

		
		//Num Simulation Parameters
		int simulations = 1;
		int starts = 1;
		int runs = 10;
		
		if(selectionType.contains("ranked"))
		{
			childrenPercentage = 100;
		}
		
//		//Setup CSV writer
//		try {
//            csvFile.createNewFile();
//        } catch (IOException e) {
//            System.err.println("CSV file not created");
//        }
		
		try {
			csvWriter = new PrintWriter(csvFile + ".csv");
		} catch (FileNotFoundException e) {
			System.err.println("could not create csv writer");
			e.printStackTrace();
			return;
		}
		
		HashMap<String, ArrayList<Step>> strats = new HashMap<String, ArrayList<Step>>();
		
		ArrayList<Step> pureWalk = new ArrayList<Step>();
		for(int i = 0; i < strategyLength; i++)
		{
			pureWalk.add(new WalkStep());
		}
		strats.put("PureWalk", pureWalk);
		
		ArrayList<Step> alternateLookWalk = new ArrayList<Step>();
		for(int i = 0; i < strategyLength/2; i++)
		{
			alternateLookWalk.add(new LookStep());
			alternateLookWalk.add(new WalkStep());
		}
		strats.put("AlternateLookWalk", alternateLookWalk);
		
		ArrayList<Step> SHC = new ArrayList<Step>();
		int looksperwalk = (int) Math.ceil((n * 1.0/sensitivity));
		
		for(int i = 0; i < strategyLength/(looksperwalk + 1); i++)
		{
			for(int j = 0; j < looksperwalk; j++)
			{
				SHC.add(new LookStep());
			}
			SHC.add(new WalkStep());
		}
		strats.put("Steep Hill Climb", SHC);
		
		
		//Run Simulation
		for(int sense = sensitivity; sense <= maxSensitivity; sense++)
		{
			LookStep.DEFAULT_NUM_CHECKS = sense;
			for(int simulation = 0; simulation < simulations; simulation++)
			{
				FitnessLandscape landscape = new FitnessLandscape(n, k, SeededRandom.rnd.nextInt());
				for(int start = 0; start < starts; start++)
				{
					int[] startingLocation = NDArrayManager.array1dRandInt(n, 2);
					//Setup comparison strategies
					Map<String, LearningStrategy> comparisonStrategies = new HashMap<String, LearningStrategy>();
					
					for(String name : strats.keySet())
					{
						LearningStrategy comparison = new LearningStrategy(landscape, strats.get(name), startingLocation);
						comparisonStrategies.put(name, comparison);
					}
					
					
					for(int run = 0; run < runs; run++)
					{
						String simNum = "" + simulation + "." + start + "." + run;
						
						EvolutionSimulation sim = new EvolutionSimulation(
								landscape,
								popsPerGeneration,
								numGenerations,
								mutationPercentage,
								strategyLength,
								childrenPercentage,
								startingLocation,
								selectionType
								);
						sim.setStringNum(simNum);
						
						sim.runSimulation();
						System.out.println(simNum + " complete");
						
						sim.writeExperimentToCSV(csvWriter, comparisonStrategies, incrementCSVoutput);
						
					}
				}
			}
		}
		
		System.out.println("Data successfully written to " + experimentName + ".csv");
		
		//cleanup
		csvWriter.flush();
        csvWriter.close();
	}
}
