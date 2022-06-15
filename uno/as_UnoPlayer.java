/*
 * BY AKASH SHAH
 */

package uno;
import java.util.List;

public class as_UnoPlayer implements UnoPlayer {
    // The game state stored for use during callColor
    GameState gameState;
    
    // Point values and coefficients given to cards in play method
    // The base number of points a number card gets
    private double baseNumberPoints = 1;
    // The coefficient of the number's value over 9
    private double numberValueCoefficient = 1;
    // The amount of extra points given to a card if it is the color we have the most of
    private double mostHeldColorPoints = 1;
    // The amount to multiply the ratio of card of a certain color to cards in the hand by
    private double colorRatioCoefficient = 4;
    // The ratio of opponents cards to our cards that indicates they are a threat
    private double significantLeadRatio = 0.5;
    // The amount of point given to a card that is not the color of the highest player's last wild
    private double playColorDislikedByHighestPlayerPoints = 1;
    // The amount of points given to offensive cards when the player next to us is becoming a threat
    private double reversePoints = 5;
    private double skipPoints = 5;
    private double drawTwoPoints = 6;
    private double wildDrawFourPoints = 10;

    // Point values given to colors in callColor method
    // The coefficient of the number of cards of a certain color over the total amount in the hand
    private double heldColorCoefficient = 3;
    // The amount of points subtracted from colors that players have called with wilds
    private double calledColorPoints = 1;

    /**
     * play - This method is called when it's your turn and you need to
     * choose what card to play.
     *
     * The hand parameter tells you what's in your hand. You can call
     * getColor(), getRank(), and getNumber() on each of the cards it
     * contains to see what it is. The color will be the color of the card,
     * or "Color.NONE" if the card is a wild card. The rank will be
     * "Rank.NUMBER" for all numbered cards, and another value (e.g.,
     * "Rank.SKIP," "Rank.REVERSE," etc.) for special cards. The value of
     * a card's "number" only has meaning if it is a number card. 
     * (Otherwise, it will be -1.)
     *
     * The upCard parameter works the same way, and tells you what the 
     * up card (in the middle of the table) is.
     *
     * The calledColor parameter only has meaning if the up card is a wild,
     * and tells you what color the player who played that wild card called.
     *
     * Finally, the state parameter is a GameState object on which you can 
     * invoke methods if you choose to access certain detailed information
     * about the game (like who is currently ahead, what colors each player
     * has recently called, etc.)
     *
     * You must return a value from this method indicating which card you
     * wish to play. If you return a number 0 or greater, that means you
     * want to play the card at that index. If you return -1, that means
     * that you cannot play any of your cards (none of them are legal plays)
     * in which case you will be forced to draw a card (this will happen
     * automatically for you.)
     */
    public int play(List<Card> hand, Card upCard, Color calledColor,
        GameState state) 
    {
        // Get color if wild was played
        Color upColor = upCard.getColor();
        if (upColor == Color.NONE)
        {
            upColor = calledColor;
        }

        // Get number of cards of each color
        int[] colors = countColors(hand);
        // The color with the highest amount of cards in the hand
        Color maxColor = Color.values()[max(colors)];

        // To determine which card to play, the code loops through each valid card in the hand and awards 
        // that card a certain number of points if it meets a certain criteria. Then, the card with the 
        // highest number of points is chosen and played.

        // Index of current hand to play
        // If no valid cards are found, index will remain unchanged
        int index = -1;
        // Max points
        // Starting at -1 ensures that there will definitely be a card selected
        double maxPoints = -1;
        // Loop through each card in the hand
        for (int i = 0; i < hand.size(); i++)
        {
            // Get card at current index
            Card card = hand.get(i);
            // If statement determining if the current card is valid
            // IF card is the same color as called
            if (card.canPlayOn(upCard, calledColor))
            {
                // Count points towards this card being good
                double points = 0;

                // Passive conditions ------------------------------------------------------------------------------------------------------------------
                // These points just prioritize getting rid of high cards and trying to play as optimally for ourselves without worrying about anyone else

                // Add points to number cards to prioritize saving offensive cards
                if (card.getRank() == Rank.NUMBER)
                    // Given points to start and then given more points for higher numbers to prioritize getting rid of high cards
                    points += baseNumberPoints + (numberValueCoefficient * (card.getNumber() / 9.0));

                // If this is not a wild card
                if (card.getColor() != Color.NONE)
                    // Add points proportionally to the amount of cards in the hand that this color takes up
                    points += colorRatioCoefficient * (colors[card.getColor().ordinal()] / (double) hand.size());
                
                // Add an extra bonus if this card is one of the most held color
                if (card.getColor() == maxColor)
                    points += mostHeldColorPoints;

                // Agressive conditions ----------------------------------------------------------------------------------------------------------------
                // These points are awarded when another player is close to winning and tries to hurt them
                // These conditions are usually activated when a player is close to winning or doing significantly better than us
                // (These points are awarded in addition to the points awarded previously for benefitting ourselves)

                // Get the number of cards in the other hands
                int[] cardsInHands = state.getNumCardsInHandsOfUpcomingPlayers();
                // Get the minimum number of cards (detect if someone is close to winning) and the corresponding player position
                int minIndex = min(cardsInHands);
                int minCards = cardsInHands[minIndex];

                // If a player is close to winning or they have a significant lead
                if ((minCards < 4 || leadRatio(hand, minCards) < significantLeadRatio)
                    // And this card is not a wild and this card's color is NOT one that the player with the lowest amount of cards last switched to
                    && (card.getColor() != Color.NONE && card.getColor() != state.getMostRecentColorCalledByUpcomingPlayers()[minIndex]))
                    // Give a point for playing a color that the winning player does not like
                    points += playColorDislikedByHighestPlayerPoints;

                // If the player next to us has a significant lead or is close to winning
                if ((leadRatio(hand, cardsInHands[0]) < significantLeadRatio) || cardsInHands[0] < 4)
                {
                    // Award points to offensive points
                    if (card.getRank() == Rank.REVERSE)
                        points += reversePoints;
                    else if (card.getRank() == Rank.SKIP)
                        points += skipPoints;
                    else if (card.getRank() == Rank.DRAW_TWO)
                        points += drawTwoPoints;
                    else if (card.getRank() == Rank.WILD_D4)
                        points += wildDrawFourPoints;
                }
                
                // If this point total is the new max, say this is the best card
                if (points > maxPoints)
                {
                    maxPoints = points;
                    index = i;
                }
            }
        }
        
        // Store GameState for use in calledColor
        gameState = state;
        
        return index;
    }

    /**
     * callColor - This method will be called when you have just played a
     * wild card, and is your way of specifying which color you want to 
     * change it to.
     *
     * You must return a valid Color value from this method. You must not
     * return the value Color.NONE under any circumstances.
     */
    public Color callColor(List<Card> hand)
    {
        // Number of points each color gets
        // Points are added or subtracted to each color based on fitness and then the one with the highest amount of points is chosen
        double[] colorPoints = new double[4];
        
        // Amount of each color in hand
        int[] colorCount = countColors(hand);
        // Loop through each color
        for (int i = 0; i < colorCount.length; i++)
            // Give each color a number of points proportional to the portional of the hand it takes up
            colorPoints[i] = heldColorCoefficient * (colorCount[i] / (double) hand.size());
        
        // Most recent colors
        // Subtract one point from a color if it was called by a player
        for (Color color : gameState.getMostRecentColorCalledByUpcomingPlayers())
        {
            if (color != null)
                colorPoints[color.ordinal()] -= calledColorPoints;
        }
            
        // Index of the color with the highest number of points
        int highestColor = max(colorPoints);
        // Return color with most points
        return Color.values()[highestColor];
    }

    /**
     * Returns the "lead ratio" between us and a player. This ratio is the ratio of their cards to our cards.
     * A ratio > 1 means that we are doing better, while a ratio < 1 means that they are doing better.
     * 
     * @param cards The number of cards the other player has.
     * @param hand Our current hand.
     */
    double leadRatio(List<Card> hand, int cards)
    {
        return cards / (double) hand.size();
    }
    
    /** 
     * Returns a 4 element array of how many cards of each color are in the hand.
     * Excludes wilds.
     */
    private int[] countColors(List<Card> hand)
    {
        // Array of number of each color to return
        int[] colors = new int[4];
        for (Card card : hand)
        {
            if (card.getColor() != Color.NONE)
                colors[card.getColor().ordinal()]++;
        }
        return colors;
    }

    /**
     * Returns the index of the maximum element of a double array
     */
    private int max(double[] arr)
    {
        int index = 0;
        for (int i = 1; i < arr.length; i++)
        {
            if (arr[i] > arr[index])
                index = i;
        }
        return index;
    }

    /**
     * Returns the index of the maximum element of an int array
     */
    private int max(int[] arr)
    {
        int index = 0;
        for (int i = 1; i < arr.length; i++)
        {
            if (arr[i] > arr[index])
                index = i;
        }
        return index;
    }

    /**
     * Returns the index of the minimum element of an int array
     */
    private int min(int[] arr)
    {
        int index = 0;
        for (int i = 1; i < arr.length; i++)
        {
            if (arr[i] < arr[index])
                index = i;
        }
        return index;
    }

    /**
     * Returns the index of the minimum element of an int array
     */
    private int min(double[] arr)
    {
        int index = 0;
        for (int i = 1; i < arr.length; i++)
        {
            if (arr[i] < arr[index])
                index = i;
        }
        return index;
    }
}