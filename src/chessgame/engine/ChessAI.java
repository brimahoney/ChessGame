package chessgame.engine;

import chessgame.model.BoardSquare;
import chessgame.model.TeamColor;
import javafx.concurrent.Task;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;

/**
 * Chess AI using minimax with alpha-beta pruning.
 *
 * Works entirely on int[8][8] board snapshots — no shared state with the
 * JavaFX model — so it is safe to run on a background thread.
 *
 * Board encoding: board[file][rank], file 0=a…7=h, rank 0=1st…7=8th.
 *   0        = empty
 *   +1..+6   = white PAWN / KNIGHT / BISHOP / ROOK / QUEEN / KING
 *   -1..-6   = black PAWN / KNIGHT / BISHOP / ROOK / QUEEN / KING
 *
 * Piece ordinals mirror chessgame.model.Piece: PAWN=1, KNIGHT=2, BISHOP=3,
 * ROOK=4, QUEEN=5, KING=6.
 */
public class ChessAI
{
    public enum Difficulty { EASY, HARD, EXPERT }

    static final int EMPTY = 0, PAWN = 1, KNIGHT = 2, BISHOP = 3,
                     ROOK  = 4, QUEEN = 5, KING   = 6;

    // Material values (centipawns)
    private static final int[] VALUE = { 0, 100, 320, 330, 500, 900, 20000 };

    // Piece-square tables [row][file] where row 0 = rank 8, row 7 = rank 1
    // (flip for black: use [rank] instead of [7-rank])
    private static final int[][] PST_PAWN = {
        {  0,  0,  0,  0,  0,  0,  0,  0 },
        { 50, 50, 50, 50, 50, 50, 50, 50 },
        { 10, 10, 20, 30, 30, 20, 10, 10 },
        {  5,  5, 10, 25, 25, 10,  5,  5 },
        {  0,  0,  0, 20, 20,  0,  0,  0 },
        {  5, -5,-10,  0,  0,-10, -5,  5 },
        {  5, 10, 10,-20,-20, 10, 10,  5 },
        {  0,  0,  0,  0,  0,  0,  0,  0 }
    };
    private static final int[][] PST_KNIGHT = {
        {-50,-40,-30,-30,-30,-30,-40,-50 },
        {-40,-20,  0,  0,  0,  0,-20,-40 },
        {-30,  0, 10, 15, 15, 10,  0,-30 },
        {-30,  5, 15, 20, 20, 15,  5,-30 },
        {-30,  0, 15, 20, 20, 15,  0,-30 },
        {-30,  5, 10, 15, 15, 10,  5,-30 },
        {-40,-20,  0,  5,  5,  0,-20,-40 },
        {-50,-40,-30,-30,-30,-30,-40,-50 }
    };
    private static final int[][] PST_BISHOP = {
        {-20,-10,-10,-10,-10,-10,-10,-20 },
        {-10,  0,  0,  0,  0,  0,  0,-10 },
        {-10,  0,  5, 10, 10,  5,  0,-10 },
        {-10,  5,  5, 10, 10,  5,  5,-10 },
        {-10,  0, 10, 10, 10, 10,  0,-10 },
        {-10, 10, 10, 10, 10, 10, 10,-10 },
        {-10,  5,  0,  0,  0,  0,  5,-10 },
        {-20,-10,-10,-10,-10,-10,-10,-20 }
    };
    private static final int[][] PST_ROOK = {
        {  0,  0,  0,  0,  0,  0,  0,  0 },
        {  5, 10, 10, 10, 10, 10, 10,  5 },
        { -5,  0,  0,  0,  0,  0,  0, -5 },
        { -5,  0,  0,  0,  0,  0,  0, -5 },
        { -5,  0,  0,  0,  0,  0,  0, -5 },
        { -5,  0,  0,  0,  0,  0,  0, -5 },
        { -5,  0,  0,  0,  0,  0,  0, -5 },
        {  0,  0,  0,  5,  5,  0,  0,  0 }
    };
    private static final int[][] PST_QUEEN = {
        {-20,-10,-10, -5, -5,-10,-10,-20 },
        {-10,  0,  0,  0,  0,  0,  0,-10 },
        {-10,  0,  5,  5,  5,  5,  0,-10 },
        { -5,  0,  5,  5,  5,  5,  0, -5 },
        {  0,  0,  5,  5,  5,  5,  0, -5 },
        {-10,  5,  5,  5,  5,  5,  0,-10 },
        {-10,  0,  5,  0,  0,  0,  0,-10 },
        {-20,-10,-10, -5, -5,-10,-10,-20 }
    };
    private static final int[][] PST_KING = {
        {-30,-40,-40,-50,-50,-40,-40,-30 },
        {-30,-40,-40,-50,-50,-40,-40,-30 },
        {-30,-40,-40,-50,-50,-40,-40,-30 },
        {-30,-40,-40,-50,-50,-40,-40,-30 },
        {-20,-30,-30,-40,-40,-30,-30,-20 },
        {-10,-20,-20,-20,-20,-20,-20,-10 },
        { 20, 20,  0,  0,  0,  0, 20, 20 },
        { 20, 30, 10,  0,  0, 10, 30, 20 }
    };

    private static final Random RNG = new Random();

    // -------------------------------------------------------------------------
    // Public API
    // -------------------------------------------------------------------------

    /**
     * Creates a JavaFX Task that searches for the best move on a background thread.
     * The task returns int[5] = {fromFile, fromRank, toFile, toRank, promotionPiece}
     * where promotionPiece is QUEEN (5) for promotions, 0 otherwise.
     * Returns null if there are no legal moves.
     */
    public static Task<int[]> createTask(
            int[][] board, boolean[][] hasMoved, int epFile, int epRank,
            boolean aiIsWhite, Difficulty difficulty)
    {
        int[][] boardCopy = copyBoard(board);
        boolean[][] movedCopy = copyMoved(hasMoved);

        return new Task<>()
        {
            @Override
            protected int[] call()
            {
                return search(boardCopy, movedCopy, epFile, epRank, aiIsWhite, difficulty);
            }
        };
    }

    /**
     * Builds an int[8][8] snapshot from the model squares.
     * Must be called on the JavaFX thread before the task is started.
     */
    public static int[][] snapshot(BoardSquare[][] model)
    {
        int[][] b = new int[8][8];
        for (int f = 0; f < 8; f++)
            for (int r = 0; r < 8; r++)
                if (model[f][r].isOccupied())
                {
                    var p = model[f][r].getCurrentPiece();
                    int type = p.getType().ordinal() + 1;
                    b[f][r] = p.getColor() == TeamColor.WHITE ? type : -type;
                }
        return b;
    }

    /**
     * Builds a hasMoved[8][8] snapshot — true where a piece has already moved.
     * Must be called on the JavaFX thread before the task is started.
     */
    public static boolean[][] movedSnapshot(BoardSquare[][] model)
    {
        boolean[][] m = new boolean[8][8];
        for (int f = 0; f < 8; f++)
            for (int r = 0; r < 8; r++)
                if (model[f][r].isOccupied())
                    m[f][r] = !model[f][r].getCurrentPiece().isFirstMove();
        return m;
    }

    // -------------------------------------------------------------------------
    // Search
    // -------------------------------------------------------------------------

    private static int[] search(int[][] board, boolean[][] hasMoved,
                                 int epFile, int epRank,
                                 boolean aiIsWhite, Difficulty difficulty)
    {
        List<int[]> moves = legalMoves(board, hasMoved, epFile, epRank, aiIsWhite);
        if (moves.isEmpty()) return null;

        if (difficulty == Difficulty.EASY)
            return moves.get(RNG.nextInt(moves.size()));

        int depth = difficulty == Difficulty.HARD ? 3 : 5;

        Collections.shuffle(moves, RNG); // variety at equal scores

        int best = Integer.MIN_VALUE;
        int[] bestMove = moves.get(0);

        for (int[] mv : moves)
        {
            int[][] nb  = applyMove(board, mv);
            boolean[][] nm  = applyMoved(hasMoved, mv);
            int[] nep   = newEP(board, mv);
            int score   = minimax(nb, nm, nep[0], nep[1],
                                  depth - 1, Integer.MIN_VALUE, Integer.MAX_VALUE,
                                  !aiIsWhite, aiIsWhite);
            if (score > best)
            {
                best     = score;
                bestMove = mv;
            }
        }
        return bestMove;
    }

    private static int minimax(int[][] board, boolean[][] hasMoved,
                                int epFile, int epRank,
                                int depth, int alpha, int beta,
                                boolean whiteToMove, boolean aiIsWhite)
    {
        if (depth == 0) return evaluate(board, aiIsWhite);

        List<int[]> moves = legalMoves(board, hasMoved, epFile, epRank, whiteToMove);

        if (moves.isEmpty())
        {
            if (isInCheck(board, whiteToMove))
                // The side to move is checkmated — good for AI if opponent is mated
                return (whiteToMove == aiIsWhite) ? -50000 - depth : 50000 + depth;
            return 0; // stalemate
        }

        if (whiteToMove == aiIsWhite) // AI's turn — maximise
        {
            int max = Integer.MIN_VALUE;
            for (int[] mv : moves)
            {
                int[][] nb = applyMove(board, mv);
                boolean[][] nm = applyMoved(hasMoved, mv);
                int[] nep = newEP(board, mv);
                int score = minimax(nb, nm, nep[0], nep[1],
                                    depth - 1, alpha, beta, !whiteToMove, aiIsWhite);
                max   = Math.max(max, score);
                alpha = Math.max(alpha, score);
                if (beta <= alpha) break;
            }
            return max;
        }
        else // opponent's turn — minimise
        {
            int min = Integer.MAX_VALUE;
            for (int[] mv : moves)
            {
                int[][] nb = applyMove(board, mv);
                boolean[][] nm = applyMoved(hasMoved, mv);
                int[] nep = newEP(board, mv);
                int score = minimax(nb, nm, nep[0], nep[1],
                                    depth - 1, alpha, beta, !whiteToMove, aiIsWhite);
                min  = Math.min(min, score);
                beta = Math.min(beta, score);
                if (beta <= alpha) break;
            }
            return min;
        }
    }

    // -------------------------------------------------------------------------
    // Move generation
    // -------------------------------------------------------------------------

    static List<int[]> legalMoves(int[][] board, boolean[][] hasMoved,
                                   int epFile, int epRank, boolean white)
    {
        List<int[]> legal = new ArrayList<>();
        for (int[] mv : pseudoMoves(board, hasMoved, epFile, epRank, white))
            if (!isInCheck(applyMove(board, mv), white))
                legal.add(mv);
        return legal;
    }

    private static List<int[]> pseudoMoves(int[][] board, boolean[][] hasMoved,
                                            int epFile, int epRank, boolean white)
    {
        List<int[]> moves = new ArrayList<>();
        for (int f = 0; f < 8; f++)
            for (int r = 0; r < 8; r++)
            {
                int p = board[f][r];
                if (p == EMPTY || (p > 0) != white) continue;
                switch (Math.abs(p))
                {
                    case PAWN   -> pawnMoves(board, epFile, epRank, f, r, white, moves);
                    case KNIGHT -> knightMoves(board, f, r, white, moves);
                    case BISHOP -> slideMoves(board, f, r, white, moves,
                                       new int[][]{{1,1},{1,-1},{-1,1},{-1,-1}});
                    case ROOK   -> slideMoves(board, f, r, white, moves,
                                       new int[][]{{1,0},{-1,0},{0,1},{0,-1}});
                    case QUEEN  -> slideMoves(board, f, r, white, moves,
                                       new int[][]{{1,0},{-1,0},{0,1},{0,-1},
                                                   {1,1},{1,-1},{-1,1},{-1,-1}});
                    case KING   -> {
                        kingMoves(board, f, r, white, moves);
                        castlingMoves(board, hasMoved, f, r, white, moves);
                    }
                }
            }
        return moves;
    }

    private static void pawnMoves(int[][] board, int epFile, int epRank,
                                   int f, int r, boolean white, List<int[]> moves)
    {
        int dir    = white ? 1 : -1;
        int start  = white ? 1 : 6;
        int promoR = white ? 7 : 0;

        int nr = r + dir;
        if (inBounds(f, nr) && board[f][nr] == EMPTY)
        {
            addPawn(f, r, f, nr, promoR, moves);
            if (r == start && board[f][r + 2 * dir] == EMPTY)
                moves.add(new int[]{f, r, f, r + 2 * dir, 0});
        }
        for (int df : new int[]{-1, 1})
        {
            int nf = f + df;
            if (!inBounds(nf, nr)) continue;
            if (board[nf][nr] != EMPTY && (board[nf][nr] > 0) != white)
                addPawn(f, r, nf, nr, promoR, moves);
            if (nf == epFile && nr == epRank)
                moves.add(new int[]{f, r, nf, nr, 0});
        }
    }

    private static void addPawn(int f, int r, int nf, int nr, int promoR, List<int[]> moves)
    {
        moves.add(new int[]{f, r, nf, nr, nr == promoR ? QUEEN : 0});
    }

    private static void knightMoves(int[][] board, int f, int r, boolean white, List<int[]> moves)
    {
        for (int[] d : new int[][]{{2,1},{2,-1},{-2,1},{-2,-1},{1,2},{1,-2},{-1,2},{-1,-2}})
        {
            int nf = f + d[0], nr = r + d[1];
            if (inBounds(nf, nr) && (board[nf][nr] == EMPTY || (board[nf][nr] > 0) != white))
                moves.add(new int[]{f, r, nf, nr, 0});
        }
    }

    private static void slideMoves(int[][] board, int f, int r, boolean white,
                                    List<int[]> moves, int[][] dirs)
    {
        for (int[] d : dirs)
        {
            int nf = f + d[0], nr = r + d[1];
            while (inBounds(nf, nr))
            {
                if (board[nf][nr] == EMPTY)
                    moves.add(new int[]{f, r, nf, nr, 0});
                else
                {
                    if ((board[nf][nr] > 0) != white)
                        moves.add(new int[]{f, r, nf, nr, 0});
                    break;
                }
                nf += d[0]; nr += d[1];
            }
        }
    }

    private static void kingMoves(int[][] board, int f, int r, boolean white, List<int[]> moves)
    {
        for (int df = -1; df <= 1; df++)
            for (int dr = -1; dr <= 1; dr++)
            {
                if (df == 0 && dr == 0) continue;
                int nf = f + df, nr = r + dr;
                if (inBounds(nf, nr) && (board[nf][nr] == EMPTY || (board[nf][nr] > 0) != white))
                    moves.add(new int[]{f, r, nf, nr, 0});
            }
    }

    private static void castlingMoves(int[][] board, boolean[][] hasMoved,
                                       int f, int r, boolean white, List<int[]> moves)
    {
        if (hasMoved[f][r]) return;
        int back = white ? 0 : 7;
        if (r != back || f != 4) return;

        int rookVal = white ? ROOK : -ROOK;
        // Kingside
        if (!hasMoved[7][back] && board[7][back] == rookVal
                && board[5][back] == EMPTY && board[6][back] == EMPTY)
            moves.add(new int[]{4, back, 6, back, 0});
        // Queenside
        if (!hasMoved[0][back] && board[0][back] == rookVal
                && board[1][back] == EMPTY && board[2][back] == EMPTY && board[3][back] == EMPTY)
            moves.add(new int[]{4, back, 2, back, 0});
    }

    // -------------------------------------------------------------------------
    // Move application
    // -------------------------------------------------------------------------

    static int[][] applyMove(int[][] board, int[] mv)
    {
        int[][] nb   = copyBoard(board);
        int ff = mv[0], fr = mv[1], tf = mv[2], tr = mv[3], promo = mv[4];
        int piece    = nb[ff][fr];
        int type     = Math.abs(piece);
        boolean white = piece > 0;

        nb[ff][fr] = EMPTY;

        // En passant: captured pawn sits on same rank as moving pawn, target file
        if (type == PAWN && ff != tf && nb[tf][tr] == EMPTY)
            nb[tf][fr] = EMPTY;

        // Castling: reposition the rook
        if (type == KING && Math.abs(tf - ff) == 2)
        {
            boolean kingside = tf == 6;
            nb[kingside ? 5 : 3][tr] = nb[kingside ? 7 : 0][tr];
            nb[kingside ? 7 : 0][tr] = EMPTY;
        }

        nb[tf][tr] = (promo != 0) ? (white ? promo : -promo) : piece;
        return nb;
    }

    private static boolean[][] applyMoved(boolean[][] hasMoved, int[] mv)
    {
        boolean[][] nm = copyMoved(hasMoved);
        nm[mv[0]][mv[1]] = true;
        nm[mv[2]][mv[3]] = true;
        return nm;
    }

    private static int[] newEP(int[][] board, int[] mv)
    {
        int ff = mv[0], fr = mv[1], tf = mv[2], tr = mv[3];
        if (Math.abs(board[ff][fr]) == PAWN && Math.abs(tr - fr) == 2)
            return new int[]{tf, (fr + tr) / 2};
        return new int[]{-1, -1};
    }

    // -------------------------------------------------------------------------
    // Check detection
    // -------------------------------------------------------------------------

    static boolean isInCheck(int[][] board, boolean white)
    {
        int kf = -1, kr = -1;
        outer:
        for (int f = 0; f < 8; f++)
            for (int r = 0; r < 8; r++)
                if (board[f][r] == (white ? KING : -KING)) { kf = f; kr = r; break outer; }
        if (kf < 0) return true;
        return isAttacked(board, kf, kr, !white);
    }

    private static boolean isAttacked(int[][] board, int kf, int kr, boolean byWhite)
    {
        // Knights
        for (int[] d : new int[][]{{2,1},{2,-1},{-2,1},{-2,-1},{1,2},{1,-2},{-1,2},{-1,-2}})
        {
            int f = kf + d[0], r = kr + d[1];
            if (inBounds(f, r) && board[f][r] == (byWhite ? KNIGHT : -KNIGHT)) return true;
        }
        // Diagonals (bishop / queen)
        for (int[] d : new int[][]{{1,1},{1,-1},{-1,1},{-1,-1}})
        {
            int f = kf + d[0], r = kr + d[1];
            while (inBounds(f, r))
            {
                int p = board[f][r];
                if (p != EMPTY)
                {
                    if ((p > 0) == byWhite && (Math.abs(p) == BISHOP || Math.abs(p) == QUEEN))
                        return true;
                    break;
                }
                f += d[0]; r += d[1];
            }
        }
        // Orthogonals (rook / queen)
        for (int[] d : new int[][]{{1,0},{-1,0},{0,1},{0,-1}})
        {
            int f = kf + d[0], r = kr + d[1];
            while (inBounds(f, r))
            {
                int p = board[f][r];
                if (p != EMPTY)
                {
                    if ((p > 0) == byWhite && (Math.abs(p) == ROOK || Math.abs(p) == QUEEN))
                        return true;
                    break;
                }
                f += d[0]; r += d[1];
            }
        }
        // Pawns
        int pawnDir = byWhite ? 1 : -1;
        for (int df : new int[]{-1, 1})
        {
            int f = kf + df, r = kr - pawnDir;
            if (inBounds(f, r) && board[f][r] == (byWhite ? PAWN : -PAWN)) return true;
        }
        // Enemy king
        for (int df = -1; df <= 1; df++)
            for (int dr = -1; dr <= 1; dr++)
            {
                if (df == 0 && dr == 0) continue;
                int f = kf + df, r = kr + dr;
                if (inBounds(f, r) && board[f][r] == (byWhite ? KING : -KING)) return true;
            }
        return false;
    }

    // -------------------------------------------------------------------------
    // Evaluation
    // -------------------------------------------------------------------------

    /** Returns a score > 0 when the AI is ahead. */
    private static int evaluate(int[][] board, boolean aiIsWhite)
    {
        int score = 0;
        for (int f = 0; f < 8; f++)
            for (int r = 0; r < 8; r++)
            {
                int p = board[f][r];
                if (p == EMPTY) continue;
                boolean white = p > 0;
                int type = Math.abs(p);
                int val  = VALUE[type] + pst(type, f, r, white);
                score   += white ? val : -val;
            }
        return aiIsWhite ? score : -score;
    }

    private static int pst(int type, int f, int r, boolean white)
    {
        int row = white ? (7 - r) : r; // row 0 = back rank for each side
        return switch (type)
        {
            case PAWN   -> PST_PAWN[row][f];
            case KNIGHT -> PST_KNIGHT[row][f];
            case BISHOP -> PST_BISHOP[row][f];
            case ROOK   -> PST_ROOK[row][f];
            case QUEEN  -> PST_QUEEN[row][f];
            case KING   -> PST_KING[row][f];
            default     -> 0;
        };
    }

    // -------------------------------------------------------------------------
    // Utilities
    // -------------------------------------------------------------------------

    private static int[][] copyBoard(int[][] b)
    {
        int[][] c = new int[8][8];
        for (int i = 0; i < 8; i++) c[i] = b[i].clone();
        return c;
    }

    private static boolean[][] copyMoved(boolean[][] m)
    {
        boolean[][] c = new boolean[8][8];
        for (int i = 0; i < 8; i++) c[i] = m[i].clone();
        return c;
    }

    static boolean inBounds(int f, int r) { return f >= 0 && f < 8 && r >= 0 && r < 8; }
}
