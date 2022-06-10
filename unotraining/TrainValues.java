package unotraining;

import java.util.ArrayList;
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
    static boolean PRINT_VERBOSE = true;

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
        PRINT_VERBOSE = false;
        int numGames = 50000;
        if (args.length != 1) {
            System.out.println("Usage: TrainValues startingGeneration.");
            System.exit(1);
        }

        // Starting generation
        int startingGen = Integer.valueOf(args[0]);

        // Initialize players from starting param list
        as_UnoPlayer[] players = new as_UnoPlayer[4];
        // Read file and parse values

        // For each generation
        for (int gen = startingGen; gen < 10000; gen++)
        {
            try
            {
                for (int i = 0; i < 4; i++)
                {
                    as_UnoPlayer[] newPlayers = new as_UnoPlayer[4];
                    // Initialize players for this gen based on mutations and stuff from the previous gen
                    players = newPlayers;
                }
                Scoreboard s = new Scoreboard(players);
                for (int i=0; i<numGames; i++)
                {
                    Game g = new Game(s);
                    if(!g.play()) 
                    {
                        System.out.println("Illegal play. Aborting.");
                        return;
                    }
                }
                System.out.println(s);
            }
            catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    private static double[] readValues(int generation) throws Exception {
        BufferedReader br = new BufferedReader(new FileReader("gen" + generation + ".txt"));
        Scanner line = new Scanner(br.readLine()).useDelimiter(",");

        double[] values  = new double[18];
        for (int i = 0; i < values.length; i++)
        {
            values[i] = Double.parseDouble(line.next());
        }
        return values;
    }

}
