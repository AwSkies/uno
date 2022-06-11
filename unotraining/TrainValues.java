package unotraining;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
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
     * Controls how many messages fly by the screen while narrating an Uno
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
            System.out.println("Usage: TrainValues [startingGeneration] [numGenerations] [numPlayers] [gamesPerGen] [permutationsPerGen]");
            System.exit(1);
        }
        
        // Default values
        int startingGen = 0;
        int numGenerations = 10000;
        int numPlayers = 4;
        int gamesPerGen = 50000;
        int permutationsPerGen = 4;
        
        // Set parameter based on arguments
        if (args.length > 0)
            startingGen = Integer.parseInt(args[0]);

        if (args.length > 1)
            numGenerations = Integer.parseInt(args[1]);

        if (args.length > 2)
            numPlayers = Integer.parseInt(args[2]);

        if (args.length > 3)
            gamesPerGen = Integer.parseInt(args[3]);

        if (args.length > 4)
            permutationsPerGen = Integer.parseInt(args[4]);

        // Initialize players from starting param list
        as_UnoPlayer[] players = new as_UnoPlayer[numPlayers];
        double[] bestValues = new double[0];
        // Read the values from the starting generation with error handling
        try
        {
            bestValues = readValues(startingGen);
        }
        catch (Exception e)
        {
            e.printStackTrace();
            System.out.println("There probably isn't the correct file for the generation you chose");
            System.exit(1);
        }

        // For each generation
        for (int gen = startingGen; gen < numGenerations; gen++)
        {
            System.out.println("Beginning generation " + gen + "...");
            as_UnoPlayer[] newPlayers = new as_UnoPlayer[players.length];
            for (int i = 0; i < newPlayers.length; i++)
            {
                // Initialize players for this gen based on mutations and stuff from the previous gen
                for (int j = 0; j < newPlayers.length; j++)
                {
                    newPlayers[j] = new as_UnoPlayer("Player" + j, startingGen, mutateValues(bestValues));
                }
            }
            players = newPlayers;

            System.out.println("Running games...");

            int[] points = new int[players.length];
            for (int p = 0; p < permutationsPerGen; p++)
            {
                // Randomize player order
                ArrayList<as_UnoPlayer> randomOrderedPlayersList = (ArrayList<as_UnoPlayer>) Arrays.asList(players);
                Collections.shuffle(randomOrderedPlayersList);
                as_UnoPlayer[] randomOrderedPlayers = (as_UnoPlayer[]) randomOrderedPlayersList.toArray();

                // Create and run games
                Scoreboard s = new Scoreboard(randomOrderedPlayers);
                for (int i=0; i < gamesPerGen; i++)
                {
                    Game g = new Game(s);
                    if(!g.play()) 
                    {
                        System.out.println("Illegal play. Aborting.");
                        return;
                    }
                }

                // Add points
                for (int i = 0; i < randomOrderedPlayers.length; i++) {
                    for (int j = 0; j < players.length; j++) {
                        if (randomOrderedPlayers[i].name.equals(players[j].name)) {
                            points[j] += s.getScore(i);
                        }
                    }
                }
            }

            // Calculate winner
            int winner = 0;
            for (int i = 1; i < points.length; i++) {
                if (points[i] > points[winner])
                    winner = i;
            }

            bestValues = players[winner].getValues();
            players[winner].dumpValues(gen, points[winner]);

            System.out.println("Finished generation " + gen + ".\nBest performer: " + players[winner]);
        }
    }

    /**
     * Reads values from a file with the name "gen[generation].txt"
     * @param generation The generation to take the information from
     */
    private static double[] readValues(int generation) throws Exception {
        BufferedReader br = new BufferedReader(new FileReader("gen" + generation + ".txt"));
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