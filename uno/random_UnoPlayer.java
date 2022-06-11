package uno;

import java.util.ArrayList;
import java.util.List;

public class random_UnoPlayer implements UnoPlayer {
    @Override
    public int play(List<Card> hand, Card upCard, Color calledColor, GameState state) {
        // Get color if wild was played
        Color upColor = upCard.getColor();
        if (upColor == Color.NONE)
        {
            upColor = calledColor;
        }

        ArrayList<Card> validCards = new ArrayList<Card>();
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
                validCards.add(card);
            }
        }

        if (validCards.size() == 0)
            return -1;
        else
            return hand.indexOf(validCards.get((int) (Math.random() * validCards.size())));
    }

    @Override
    public Color callColor(List<Card> hand) {
        return Color.values()[(int) (Math.random() * 4)];
    }
}
