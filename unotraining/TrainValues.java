package unotraining;

import java.util.Scanner;
import java.io.BufferedReader;
import java.io.FileReader;

/**
 * <p>An entire terminal-based simulation of a multi-game Uno match.
 * Command-line switches can control certain aspects of the game. Output is
 * provided to the screen about game flow and final scores.</p>
 * @since 1.0
 */
public class TrainValues {

    /** 
     * Controls how many messages fly by the screen while narFitness an Uno
     * match in text.
     */
    static boolean PRINT_VERBOSE = false;

    /**
     * <p>The name of a file (relative to working directory) containing
     * comma-separated lines, each of which contains a player name
     * (unrestricted text) and the <i>prefix</i> of the (package-less)
     * class name (implementer of UnoPlayer) that player will use as a
     * playing strategy.</p>
     *
     * For example, if the file contained these lines:
     * <pre>
     * Fred,fsmith
     * Jane,jdoe
     * Billy,bbob
     * Thelma,tlou
     * </pre>
     * then the code would pit Fred (whose classname was
     * "uno.fsmith_UnoPlayer") against Jane (whose classname was
     * "uno.jdoe_Unoplayer") against, Billy,... etc.
     */

    /** 
     * Run an Uno simulation of some number of games pitting some set of
     * opponents against each other. The mandatory command-line argument
     * (numberOfGames) should contain an integer specifying how many games
     * to play in the match. The optional second command-line argument
     * should be either the word "verbose" or "quiet" and controls the
     * magnitude of output.
     */
    public static void main(String args[]) {
        if (args.length == 1 && args[0].equals("-h")) {
            System.out.println("Usage: TrainValues [startingGeneration] [maxGenerations] [numPlayers] [gamesPerGen] [playersPerGen]");
            System.exit(1);
        }
        
        // Default values
        int startingGen = 0;
        int maxGenerations = 1000;
        int numPlayers = 4;
        int gamesPerGen = 10000;
        int playersPerGen = 50;
        
        // Set parameter based on arguments
        if (args.length > 0)
            startingGen = Integer.parseInt(args[0]);

        if (args.length > 1)
            maxGenerations = Integer.parseInt(args[1]);

        if (args.length > 2)
            numPlayers = Integer.parseInt(args[2]);

        if (args.length > 3)
            gamesPerGen = Integer.parseInt(args[3]);

        if (args.length > 4)
            playersPerGen = Integer.parseInt(args[4]);

        double[] bestValues = new double[0];
        double[] baselineValues = new double[0];
        double bestFitnessRate = 0;
        int bestFitnessPoints = 0;
        double bestFitness = 0;
        int bestGen = 0;
        // Read the values from the starting generation with error handling
        try
        {
            baselineValues = readValues("baseline");
            if (startingGen != 0)
            {
                bestValues = readValues(startingGen);
            }
            else
            {
                bestValues = baselineValues;
            }
        }
        catch (Exception e)
        {
            e.printStackTrace();
            System.out.println("There probably isn't the correct file for the generation you chose");
            System.exit(1);
        }

        // Initialize players array
        UnoPlayer[] players = new UnoPlayer[numPlayers];
        for (int i = 1; i < numPlayers; i++)
        {
            players[i] = new random_UnoPlayer();
        }

        // For each generation
        for (int gen = startingGen + 1; gen < maxGenerations; gen++)
        {
            System.out.println("Beginning generation " + gen + "...");

            // Store mutated players
            as_UnoPlayer[] mutations = new as_UnoPlayer[playersPerGen];
            double currentGenFitnessRate = 0;
            int currentGenFitnessPoints = 0;
            double currentGenBestFitness = 0;
            int bestPlayer = 0;
            for (int p = 0; p < playersPerGen; p++)
            {
                // Make new player based on mutations from previous generation
                as_UnoPlayer currentPlayer = new as_UnoPlayer("Player" + p, gen, mutateValues(bestValues));
                players[0] = currentPlayer;
                mutations[p] = currentPlayer;

                // Create and run games
                Scoreboard s = new Scoreboard(players);
                for (int i=0; i < gamesPerGen; i++)
                {
                    Game g = new Game(s);
                    if(!g.play()) 
                    {
                        System.out.println("Illegal play. Aborting.");
                        return;
                    }
                }
                System.out.println("Finished player " + p + ". Win rate: " + s.getWinRate(0));

                double fitness = fitness(s.getWinRate(0), s.getScore(0));
                if (fitness > currentGenBestFitness) {
                    currentGenFitnessPoints = s.getScore(0);
                    currentGenFitnessRate = s.getWinRate(0);
                    currentGenBestFitness = fitness;
                    bestPlayer = p;
                }
            }

            // Save best values if this performed better than the previous best generation
            if (currentGenFitnessRate > bestFitness)
            {
                bestFitnessPoints = currentGenFitnessPoints;
                bestFitnessRate = currentGenFitnessRate;
                bestFitness = currentGenBestFitness;
                bestGen = gen;
            }
            bestValues = mutations[bestPlayer].getValues();
            // Dump values for current generation
            mutations[bestPlayer].dumpValues(currentGenFitnessPoints, currentGenFitnessRate);

            System.out.println("Finished generation " + gen + ".\nBest performer: " + mutations[bestPlayer]);
            System.out.println("Rate: " + currentGenFitnessRate);
            System.out.println("Points: " + currentGenFitnessPoints);
            System.out.println("Fitness: " + currentGenBestFitness);
            System.out.println("Current best generation: " + bestGen + ", Rate: " + bestFitnessRate + ", Points: " + bestFitnessPoints);
        }
        System.out.println(maxGenerations + " generations surpassed. Best generation: " + bestGen);
    }

    private static double[] readValues(String name) throws Exception
    {
        BufferedReader br = new BufferedReader(new FileReader("values/" + name + ".csv"));
        try (Scanner line = new Scanner(br.readLine()).useDelimiter(",")) {
            double[] values  = new double[as_UnoPlayer.NUM_VALUES];
            for (int i = 0; i < values.length; i++)
            {
                values[i] = Double.parseDouble(line.next());
            }
            // Close streams
            br.close();

            return values;
        }
    }

    /**
     * Reads values from a file with the name "gen[generation].csv"
     * @param generation The generation to take the information from
     */
    private static double[] readValues(int generation) throws Exception {
        return readValues("gen" + generation);
    }

    /**
     * Returns the fitness (points times win rate) of a player
     */
    private static double fitness(double winRate, int points)
    {
        return winRate * points;
    }

    /**
     * Mutates the values passed randomly. Sometimes will swap a value with the
     * @param valuesToMutate The values to mutate
     */
    private static double[] mutateValues(double[] valuesToMutate)
    {
        double[] values = new double[valuesToMutate.length];
        for (int i = 0; i < valuesToMutate.length; i++)
        {
            int sign = 1;
            if ((int) (Math.random() * 2) == 1)
                sign = -1;
            // Mutates the values
            // Adds or subtracts two Math.random() calls to the value
            // The two Math.random() calls are so small changes occur most often but large changes can exist
            values[i] = valuesToMutate[i] + (sign * Math.random() * Math.random());
        }
        return values;
    }
}
