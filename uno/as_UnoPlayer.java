package uno;
import java.util.List;
import java.util.ArrayList;

public class as_UnoPlayer implements UnoPlayer {

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
        
        // Get valid cards to play
        ArrayList<Card> validCards = new ArrayList<Card>();
        for (Card card : hand)
        {
            // Card is the same color as called
            if ((card.getColor() == upColor) ||
                // Card is not a number and the same rank as upCard
                (card.getRank() != Rank.NUMBER && card.getRank() == upCard.getRank()) ||
                // Card is a number and the same number as upCard
                (card.getRank() == Rank.NUMBER && card.getNumber() == upCard.getNumber()) ||
                // Card is a wild
                (card.getColor() == Color.NONE))
                validCards.add(card);
        }
        
        // If no valid cards are found return -1
        if (validCards.size() == 0)
            return -1;
        
        return hand.indexOf(validCards.get(0));
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
        return Color.GREEN;
    }
 
}