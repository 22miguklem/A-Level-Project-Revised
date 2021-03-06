import java.util.HashMap;
import java.util.Map;

public class PieceSquareTable {
    Board b = new Board();
    //<editor-fold desc="Piece-Square Tables">
    // Pawns should be encouraged to advance
    int[] pawnScores = {
            0, 0, 0, 0, 0, 0, 0, 0,
            50, 50, 50, 50, 50, 50, 50, 50, 0, 0, 0, 0, 0, 0, 0, 0,
            10, 10, 20, 30, 30, 20, 10, 10, 0, 0, 0, 0, 0, 0, 0, 0,
            5, 5, 10, 25, 25, 10, 5, 5, 0, 0, 0, 0, 0, 0, 0, 0,
            0, 0, 0, 20, 20, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0,
            5, -5, -10, 0, 0, -10, -5, 5, 0, 0, 0, 0, 0, 0, 0, 0,
            5, 10, 10, -20, -20, 10, 10, 5, 0, 0, 0, 0, 0, 0, 0, 0,
            0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0,
    };
    // Knights should be encouraged to the middle
    int[] knightScores = {
            -50, -40, -30, -30, -30, -30, -40, -50, 0, 0, 0, 0, 0, 0, 0, 0,
            -40, -20, 0, 0, 0, 0, -20, -40, 0, 0, 0, 0, 0, 0, 0, 0,
            -30, 0, 10, 15, 15, 10, 0, -30, 0, 0, 0, 0, 0, 0, 0, 0,
            -30, 5, 15, 20, 20, 15, 5, -30, 0, 0, 0, 0, 0, 0, 0, 0,
            -30, 0, 15, 20, 20, 15, 0, -30, 0, 0, 0, 0, 0, 0, 0, 0,
            -30, 5, 10, 15, 15, 10, 5, -30, 0, 0, 0, 0, 0, 0, 0, 0,
            -40, -20, 0, 5, 5, 0, -20, -40, 0, 0, 0, 0, 0, 0, 0, 0,
            -50, -40, -30, -30, -30, -30, -40, -50, 0, 0, 0, 0, 0, 0, 0, 0,
    };
    // Bishops should avoid corners and borders
    int[] bishopScores = {
            -20, -10, -10, -10, -10, -10, -10, -20, 0, 0, 0, 0, 0, 0, 0, 0,
            -10, 0, 0, 0, 0, 0, 0, -10, 0, 0, 0, 0, 0, 0, 0, 0,
            -10, 0, 5, 10, 10, 5, 0, -10, 0, 0, 0, 0, 0, 0, 0, 0,
            -10, 5, 5, 10, 10, 5, 5, -10, 0, 0, 0, 0, 0, 0, 0, 0,
            -10, 0, 10, 10, 10, 10, 0, -10, 0, 0, 0, 0, 0, 0, 0, 0,
            -10, 10, 10, 10, 10, 10, 10, -10, 0, 0, 0, 0, 0, 0, 0, 0,
            -10, 5, 0, 0, 0, 0, 5, -10, 0, 0, 0, 0, 0, 0, 0, 0,
            -20, -10, -10, -10, -10, -10, -10, -20, 0, 0, 0, 0, 0, 0, 0, 0,
    };
    // Rooks should be centralised
    int[] rookScores = {
            0, 0, 0, 0, 0, 0, 0, 0,
            5, 10, 10, 10, 10, 10, 10, 5, 0, 0, 0, 0, 0, 0, 0, 0,
            -5, 0, 0, 0, 0, 0, 0, -5, 0, 0, 0, 0, 0, 0, 0, 0,
            -5, 0, 0, 0, 0, 0, 0, -5, 0, 0, 0, 0, 0, 0, 0, 0,
            -5, 0, 0, 0, 0, 0, 0, -5, 0, 0, 0, 0, 0, 0, 0, 0,
            -5, 0, 0, 0, 0, 0, 0, -5, 0, 0, 0, 0, 0, 0, 0, 0,
            -5, 0, 0, 0, 0, 0, 0, -5, 0, 0, 0, 0, 0, 0, 0, 0,
            0, 0, 0, 5, 5, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0,
    };
    int[] queenScores = {
            -20, -10, -10, -5, -5, -10, -10, -20, 0, 0, 0, 0, 0, 0, 0, 0,
            -10, 0, 0, 0, 0, 0, 0, -10, 0, 0, 0, 0, 0, 0, 0, 0,
            -10, 0, 5, 5, 5, 5, 0, -10, 0, 0, 0, 0, 0, 0, 0, 0,
            -5, 0, 5, 5, 5, 5, 0, -5, 0, 0, 0, 0, 0, 0, 0, 0,
            0, 0, 5, 5, 5, 5, 0, -5, 0, 0, 0, 0, 0, 0, 0, 0,
            -10, 5, 5, 5, 5, 5, 0, -10, 0, 0, 0, 0, 0, 0, 0, 0,
            -10, 0, 5, 0, 0, 0, 0, -10, 0, 0, 0, 0, 0, 0, 0, 0,
            -20, -10, -10, -5, -5, -10, -10, -20, 0, 0, 0, 0, 0, 0, 0, 0,
    };

    private final Map<Integer, int[]> piecePositionScores = new HashMap<>() {{
        put(1, pawnScores);
        put(2, knightScores);
        put(3, bishopScores);
        put(4, rookScores);
        put(5, queenScores);
    }};
    //</editor-fold>

    public int read(int piece, boolean isWhite, int square) {
        if (!isWhite) {
            int file = b.getFile(square);
            int rank = b.getRank(square);
            rank = 7 - rank;
            square = b.getSquare(rank, file);
        }
        return piecePositionScores.get(piece)[square];
    }
}
