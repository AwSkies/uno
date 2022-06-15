package unotraining;

import java.util.Arrays;
import java.util.Comparator;
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
            System.out.println("Usage: TrainValues [startingGeneration] [maxGenerations] [numPlayers] [gamesPerGen]");
            System.exit(1);
        }
        
        // Default values
        int startingGen = 0;
        int maxGenerations = 100000;
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

        double[] bestValues = new double[0];
        double[] baselineValues = new double[0];
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

        // Initialize best player as having either the previous generation's best values or having baseline
        as_UnoPlayer bestPlayer = new as_UnoPlayer("BestPlayer", startingGen, bestValues);
        
        // Initialize players array with baselines at all spots except for the first
        UnoPlayer[] players = new UnoPlayer[numPlayers];
        for (int i = 1; i < numPlayers; i++)
        {
            players[i] = new as_UnoPlayer("Baseline" + i, -1, baselineValues);
        }
        // The parents selected from the previous generation to breed for the next generation
        as_UnoPlayer[] parents = new as_UnoPlayer[playersPerGen / 10];
        // Populate parents array with the bestValues from already run simulations or baselines
        for (int i = 0; i < parents.length; i++) {
            parents[i] = new as_UnoPlayer("Parent " + i, startingGen, bestValues);
        }
        
        // For each generation
        for (int gen = startingGen + 1; gen < maxGenerations; gen++)
        {
            System.out.println("Breeding offspring from chosen parents...");
            // Initialize mutatedPlayers array
            as_UnoPlayer[] mutatedPlayers = new as_UnoPlayer[playersPerGen];
            // Populated current gen with offspring of the previous generation
            // All five parents will have two children with every parent (including themselves)
            for (int p1 = 0, i = 0; p1 < parents.length; p1++) {
                for (int p2 = 0; p2 < parents.length; p2++) {
                    for (int times = 0; times < 2; times++, i++) {
                        mutatedPlayers[i] = new as_UnoPlayer("Player" + i, gen, breed(parents[p1], parents[p2]));
                    }
                }
            }

            System.out.println("vvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvv");;
            System.out.println("Beginning generation " + gen + "...");
            
            for (int p = 0; p < playersPerGen; p++)
            {
                // Set player in first index of current player array
                players[0] = mutatedPlayers[p];

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
                mutatedPlayers[p].setPoints(s.getScore(0));
                mutatedPlayers[p].setWinRate(s.getWinRate(0));
                
                System.out.println("Finished player " + p + ". Fitness: " + mutatedPlayers[p].getFitness());
            }

            // Sort mutatedPlayers in ascending order by fitness
            Arrays.sort(mutatedPlayers, new Comparator<as_UnoPlayer>() {
                @Override
                public int compare(as_UnoPlayer player1, as_UnoPlayer player2) {
                    double f1 = player1.getFitness();
                    double f2 = player2.getFitness();
                    return (f1 < f2) ? -1 : ((f1 == f2) ? 0 : 1);
                }
            });

            as_UnoPlayer currentGenBestPlayer = mutatedPlayers[mutatedPlayers.length - 1];
            // Save best values if this generation's best player surpassed the previous
            if (currentGenBestPlayer.getFitness() > bestPlayer.getFitness())
            {
                bestPlayer = currentGenBestPlayer;
            }
            bestValues = currentGenBestPlayer.getValues();
            // Dump values for current generation
            currentGenBestPlayer.dumpValues();

            // Log generation results
            System.out.println("Finished generation " + gen + ".\nBest performer: " + currentGenBestPlayer);
            System.out.println("Current best generation: " + bestPlayer.getGeneration() +
                ", Fitness: " + bestPlayer.getFitness() +
                ", Rate: " + bestPlayer.getWinRate() +
                ", Points: " + bestPlayer.getPoints()
            );
            System.out.println("^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^");
            
            // Select parents for next generation
            System.out.println("Selecting parents for next generation...");

            // Reset parents array
            parents = new as_UnoPlayer[playersPerGen / 10];

            // Get weighted random selection
            int totalRanks = (int) ((playersPerGen / 2.0) * (playersPerGen - 1));
            // Repeat once for each parent to be selected
            for (int p = 0; p < parents.length; p++) {
                int i = 0;
                for (double r = Math.random() * totalRanks * 1.15; i < mutatedPlayers.length - 1 && r > 0; i++) {
                    r -= i;
                }
                parents[p] = mutatedPlayers[i];
                System.out.println("Chosen parent from rank " + (playersPerGen - i));
            }
        }
        System.out.println(maxGenerations + " generations surpassed. Best generation: " + bestPlayer.getGeneration());
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
     * Mutates the values passed randomly. Sometimes will swap a value with the
     * @param valuesToMutate The values to mutate
     */
    private static double[] breed(as_UnoPlayer parent1, as_UnoPlayer parent2)
    {
        // Dimension one is which parent, dimension two is which value
        double[][] parentValues = new double[][] {parent1.getValues(), parent2.getValues()};
        double[] values = new double[as_UnoPlayer.NUM_VALUES];
        for (int i = 0; i < values.length; i++)
        {
            // Randomize whether value is from first or second parent
            values[i] = parentValues[(int) (Math.random() * 2)][i];

            // Randomize elitism: 1/2 chance to remain, 1/2 chance to mutate
            if ((int) (Math.random() * 2) == 0) 
            {
                // Randomize addition or subtraction from current
                int sign = 1;
                if ((int) (Math.random() * 2) == 1)
                    sign = -1;
                // Mutates the values
                // Adds or subtracts two Math.random() calls to the value
                // The two Math.random() calls are so small changes occur most often but large changes can exist
                values[i] += sign * Math.random() * Math.random();
            }
        }
        return values;
    }
}
