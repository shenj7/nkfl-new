import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;

/**
 * The Learning Strategy class is a single strategy to navigate around a NKFL.
 * 
 * The strategy is represented as an array of integers, and each integer
 * corresponds to a single step in a strategy. Ex: strategyArray [0, 0, 0, 1, 1,
 * 1, 0] will first take three steps of strategy zero, then three steps of
 * strategy 1, then another one step of strategy zero.
 * 
 * Strategies: 0 = random walk 1 = steepest climb
 * 
 * The Learning Strategy currently is capable of only random walking or steepest
 * climbing. This is to attempt to reproduce the results in Dr. Yoder's original
 * code. The code can easily be expanded to accommodate additional possible
 * strategies (such as doing nothing or non-steepest climbing)
 * 
 * @author Jacob Ashworth, Edward Kim, Lyra Lee
 *
 */
public class LearningStrategy implements Comparable<LearningStrategy>{
	//Data needed to function
	ArrayList<Step> strategy;
	ArrayList<Integer> lookedLocations = new ArrayList<Integer>();
	public double[] fitnessArray; //fitnesses at each step
	public FitnessLandscape landscape; // This LearningStrategy's NKFL
	
	public int[] phenotype; //
	public double phenotypeFitness; // the current fitness of the genotype
	
	public int[] genotype;
	public double genotypeFitness; // save this data so we don't have to recompute it every time we reset
	
	boolean strategyExecuted = false;
	/**
	 * Initializes a LearningStrategy with the specified strategyArray
	 * 
	 * @param landscape     the FitnessLandscape of the LearningStrategy
	 * @param strategyArray the array representing the strategy
	 */
	public LearningStrategy(FitnessLandscape landscape, ArrayList<Step> strategy, int[] genotype) {
		this.landscape = landscape;
		this.strategy = strategy;

		initializeArrays(genotype);
	}

	/**
	 * Initializes a LearningStrategy with a random strategy
	 * 
	 * @param landscape      the FitnessLandscape of the LearningStrategy
	 * @param strategyLength the desired length of the strategy
	 */
	public LearningStrategy(FitnessLandscape landscape, int strategyLength, int[] genotype) {
		this.landscape = landscape;
		
		
		strategy = new ArrayList<Step>();
		
		for(int i = 0; i < strategyLength; i++)
		{
			strategy.add(this.getRandomStep());
		}
		
		initializeArrays(genotype);
	}
	
	private void initializeArrays(int[] genotype)
	{
		this.phenotype = genotype;
		this.phenotypeFitness = landscape.fitness(genotype);

		this.genotype = NDArrayManager.copyArray1d(genotype);
		this.genotypeFitness = this.phenotypeFitness;
		
		//Set our original values
		fitnessArray = new double[strategy.size()];
		fitnessArray[0] = this.genotypeFitness;
	}
	
	//Not sure if this is the best implementation
	public Step getRandomStep() {
		int roll = SeededRandom.rnd.nextInt(2);
		if(roll == 0)
		{
			return new WalkStep();
		}
		else if(roll == 1)
		{
			return new LookStep();
		}
		else
		{
			System.err.println("Step not implemented");
			return null;
		}
	}

	/**
	 * Executes steps of the LearningStrategy
	 * 
	 * @param steps number of steps to execute
	 * @return the fitness once the steps are executed
	 */
	public double executeStrategy() {
		if(!strategyExecuted)
		{
			for(int i = 0; i < strategy.size(); i++)
			{
				Step current = strategy.get(i);
				
				this.phenotype = current.execute(landscape, phenotype, lookedLocations);
				this.phenotypeFitness = landscape.fitness(phenotype);
				
				fitnessArray[i] = this.phenotypeFitness;
			}
			strategyExecuted = true;
			return this.phenotypeFitness;
		}
		else
		{
			System.out.print("Double-executing strategy not permitted");
			return 0;
		}
	}

	/**
	 * Returns a child that has the exactly the same strategy as the parent
	 * @param index
	 * @return
	 */
	public LearningStrategy getDirectChild() {
		ArrayList<Step> childStrategy = new ArrayList<Step>();
		
		//Since our steps don't depend on the specific genotype(just the landscape), this is fine
		for(Step step : strategy)
		{
			childStrategy.add(step);
		}
		
		return new LearningStrategy(landscape, childStrategy, NDArrayManager.copyArray1d(genotype));
	}
	
	public int getStrategyLength() {
		return strategy.size();
	}
	
	public String getStepAtIndex(int i) {
		return strategy.get(i).getClass().getName(); //We should just pass the name of the step, not the step itself
	}
	
	public void mutate(double mutationPercentage) {
		for(int i = 0; i < strategy.size(); i++)
		{
			double roll = SeededRandom.rnd.nextDouble() * 100;
			if(roll < mutationPercentage)
			{
				this.mutateStep(i);
			}
		}
	}
	
	/**
	 * Randomly mutates step i of the strategy array
	 * @param i
	 */
	
	//Pretty sure this is a bad solution, but with classes we don't have another option...
	public void mutateStep(int i) {
		if(strategy.get(i).getClass().getName() == "LookStep")
		{
			strategy.set(i, new WalkStep());
		}
		else if(strategy.get(i).getClass().getName() == "WalkStep")
		{
			strategy.set(i, new LookStep());
		}
		else
		{
			System.err.println("Could not determine step to mutate");
		}
	}
	
	public double getFitnessAtStep(int step) {
		return fitnessArray[step];
	}

	/**
	 * Compares fitness for sorting
	 */
	@Override
	public int compareTo(LearningStrategy otherStrategy) {
		if(this.phenotypeFitness > otherStrategy.phenotypeFitness)
		{
			return 1;
		}
		else if(this.phenotypeFitness == otherStrategy.phenotypeFitness)
		{
			return 0;
		}
		else
		{
			return -1;
		}
	}
}
