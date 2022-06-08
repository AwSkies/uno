package uno;
import java.util.List;
import java.util.ArrayList;

public class as_UnoPlayer implements UnoPlayer {
    GameState gameState;

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
        
        // Loop through hand and decide which card to play
        // Index of current hand to play
        // If no valid cards are found, index will remain unchanged
        int index = -1;
        for (int i = 0; i < hand.size(); i++)
        {
            Card card = hand.get(i);
            // Card is the same color as called
            if ((card.getColor() == upColor) ||
                // Card is not a number and the same rank as upCard
                (card.getRank() != Rank.NUMBER && card.getRank() == upCard.getRank()) ||
                // Card is a number and the same number as upCard
                (card.getRank() == Rank.NUMBER && card.getNumber() == upCard.getNumber()) ||
                // Card is a wild
                (card.getColor() == Color.NONE))
            {
                index = i;
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
        int mostCardColor = 0;
        for (int i = 1; i < colorCount.length; i++)
        {
            // If the amount of cards of this color are greater than the previous highest
            if (colorCount[i] > colorCount[mostCardColor])
                mostCardColor = i;
        }
        // Add one point to the color with the most cards
        colorPoints[mostCardColor]++;
        
        
        
        return Color.NONE;
    }
    
    /** 
     * Returns a 5 element array of how many cards of each color are in the hand.
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
}