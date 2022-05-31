package uno;


 

import java.util.List;

public class Main {

    public static void main(String[] args) {

        UnoPlayer jup = new sk_UnoPlayer();
        
        List<Card> hand = new java.util.ArrayList<Card>();
        hand.add(new Card(UnoPlayer.Color.RED, UnoPlayer.Rank.NUMBER, 4));
        hand.add(new Card(UnoPlayer.Color.GREEN, UnoPlayer.Rank.NUMBER, 7));
        hand.add(new Card(UnoPlayer.Color.GREEN, UnoPlayer.Rank.REVERSE, -1));
        hand.add(new Card(UnoPlayer.Color.BLUE, UnoPlayer.Rank.NUMBER, 2));
        hand.add(new Card(UnoPlayer.Color.BLUE, UnoPlayer.Rank.SKIP, -1));
        hand.add(new Card(UnoPlayer.Color.NONE, UnoPlayer.Rank.WILD, -1));

        int cardPlayed = jup.play(hand, 
            new Card(UnoPlayer.Color.RED, UnoPlayer.Rank.NUMBER, 7),
            UnoPlayer.Color.RED, new GameState());

        /* Let's see whether the card played was legit. */
        if (cardPlayed == -1) {
            System.out.println("Player did not think any card could be played." +
                "\nThis is an error, since cards 0, 1, and 5 are legal plays.");
        }
        else {
            if (cardPlayed == 0 || cardPlayed == 1 || cardPlayed == 5) {
                System.out.println("Player played " + hand.get(cardPlayed));
            }
            else {
                System.out.println("Player tried to play " + hand.get(cardPlayed) + ", which is an error.");
            }
        }
    }
}
