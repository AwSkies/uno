package uno;
import java.util.List;

public class as_UnoPlayer implements UnoPlayer {
    GameState gameState;
    
    // The base number of points a number card gets
    double baseNumberPoints;
    // The coefficient of the number's value over 9
    double numberValueCoefficient;
    // 

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
            if ((card.getColor() == upColor) ||
                // or card is not a number and the same rank as upCard
                (card.getRank() != Rank.NUMBER && card.getRank() == upCard.getRank()) ||
                // or card is a number and the same number as upCard
                (card.getRank() == Rank.NUMBER && card.getNumber() == upCard.getNumber()) ||
                // or card is a wild
                (card.getColor() == Color.NONE))
            {
                // Count points towards this card being good
                double points = 0;

                // Passive conditions ------------------------------------------------------------------------------------------------------------------
                // These points just prioritize getting rid of high cards and trying to play as optimally for ourselves without worrying about anyone else

                // Add points to number cards to prioritize saving offensive cards
                if (card.getRank() == Rank.NUMBER)
                    // Given one point to start and then given more points for higher numbers to prioritize getting rid of high cards
                    points += 1 + (card.getNumber() / 9.0);

                // If color is not the same color as the current card and not a wild and the hand has more cards of this color than the current color
                if (card.getColor() != upColor && card.getColor() != Color.NONE && colors[card.getColor().ordinal()] > colors[upColor.ordinal()])
                    // If this card would be changing the color to the color we have most of, give 2 points
                    if (card.getColor() == maxColor)
                        points += 2;
                    // else, the color is just one we have more of, give 1 point
                    else
                        points++;

                // Agressive conditions ----------------------------------------------------------------------------------------------------------------
                // These points are awarded when another player is close to winning and tries to hurt them
                // These conditions are usually activated when a player is close to winning or doing significantly better than us
                // (These points are awarded in addition to the points awarded previously for benefitting ourselves)

                // Get the number of cards in the other hands
                int[] cardsInHands = state.getNumCardsInHandsOfUpcomingPlayers();
                // Get the minimum number of cards (detect if someone is close to winning) and the corresponding player position
                int minIndex = min(cardsInHands);
                int minCards = cardsInHands[minIndex];

                // The ratio of the least number of cards a player has to our hand (to be adjusted)
                final double SIGNIFICANT_LEAD_RATIO = 0.5;

                // If a player is close to winning or they have a significant lead
                if ((minCards < 4 || leadRatio(hand, minCards) < SIGNIFICANT_LEAD_RATIO)
                    // And this card is not a wild and this card's color is NOT one that the player with the lowest amount of cards last switched to
                    && (card.getColor() != Color.NONE && card.getColor() != state.getMostRecentColorCalledByUpcomingPlayers()[minIndex]))
                    // Give a point for playing a color that the winning player does not like
                    points++;

                // If the player next to us has a significant lead or is close to winning
                if ((leadRatio(hand, cardsInHands[0]) < SIGNIFICANT_LEAD_RATIO) || cardsInHands[0] < 4)
                {
                    // Skips or reverses get five points to prioritize hurting the next player
                    if (card.getRank() == Rank.SKIP || card.getRank() == Rank.REVERSE)
                        points += 5;
                    // +2s are better than skips or reverses so get more points
                    else if (card.getRank() == Rank.DRAW_TWO)
                        points += 6;
                    // +4s are better than skips, reverses, and +2s so get more points
                    else if (card.getRank() == Rank.WILD_D4)
                        points += 10;
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
        // Points are added or subtracted to each color based on fitness and then the one 
        // with the highest amount of points is chosen
        int[] colorPoints = new int[4];
        
        // Amount of each color in hand
        int[] colorCount = countColors(hand);
        // Find highest amount of cards
        int mostCardColor = max(colorCount);
        // Add one point to the color with the most cards
        colorPoints[mostCardColor]++;
        
        // Most recent colors
        // Subtract one point from a color if it was called by a player
        for (Color color : gameState.getMostRecentColorCalledByUpcomingPlayers())
        {
            if (color != null)
                colorPoints[color.ordinal()]--;
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
     * Returns the index of the maximum element of an array
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
     * Returns the index of the minimum element of an array
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
}