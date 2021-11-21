package mnkgame;
import java.util.ArrayList;
public class AlphaBetaPruning extends P {
    private final int[][] DIRECTION = { { -1, 0 }, // up
			{ -1, 1 }, // up-right
			{ 0, 1 }, // right
			{ 1, 1 }, // down-right
			{ 1, 0 }, // down
			{ 1, -1 }, // down-left
			{ 0, -1 }, // left
			{ -1, -1 } // up-left
	};
	private final int MAX_DEPTH = 5; 
    private final int ALPHA = -1000;
    private final int BETA = 1000;
    private int currentDepth;
    private boolean gameover;
    private MNKCell bestMove;
    /** @param b local board to do stuff without touching the actual board
     */
    private MNKCellState[][] b;
    
    public AlphaBetaPruning() {
        gameover = false;
        currentDepth = 0;
    }

    public MNKCell getMove(MNKCell[] possibleMoves, MNKCell myLastMove, MNKCell foeLastMove) {
        //initially the best move is just a random move
        bestMove = possibleMoves[0];
        //make sure that local board matches the global one
        b = P.b;
        alphaBetaPruning(possibleMoves, foeLastMove, myLastMove, true, ALPHA, BETA);
        return bestMove;
    }

	private int alphaBetaPruning(MNKCell[] possibleMoves, MNKCell foeLastMove, MNKCell myLastMove, Boolean maximizingPlayer, int alpha, int beta) {
		// the moves array is already ordered, so we pick up the first move,
		// then the second and so on inside the for
		// i have another terminating condition which occurs when the move
		// of the current node ends the game somehow, to do so
		// i must keep track of all the moves made in a root-leaf path in a copy
		// of the local board
        ArrayList<MNKCell> moves = orderMoves(possibleMoves, myLastMove, foeLastMove);
        //now the best move is the one provided by the heuristic algorithm
		bestMove = moves.get(0);
		if(gameover) {
			gameover = false;
			return maximizingPlayer?BETA:ALPHA;
		}
		// Terminating condition. i.e
		// leaf node is reached
		if (currentDepth == MAX_DEPTH) {
			// heuristic value for the node get_heuristic_value(moves[0])
			return 0;
		}

		if (maximizingPlayer) {
			int best = ALPHA;

			// Recur for left and
			// right children
			// this will be more likely a foreach through moves
			for (int i = 0; i < moves.size(); i++) {
				// remove the marked move from moves before the recursive call
				int val = 0;// beta_pruning(depth + 1, nodeIndex * 2 + i, false, moves, alpha, beta);
				best = Math.max(best, val);
				alpha = Math.max(alpha, best);

				// Alpha Beta Pruning
				if (beta <= alpha) {
                    //bestMove = current move
                    break;
                }
					
			}
            //must return the cell with the best score
			return best;
		} else {
			int best = BETA;

			// Recur for left and
			// right children

			for (int i = 0; i < 2; i++) {

				int val = 0;// beta_pruning(depth + 1, nodeIndex * 2 + i, true, moves, alpha, beta);
				best = Math.min(best, val);
				beta = Math.min(beta, best);

				// Alpha Beta Pruning
				if (beta <= alpha)
					break;
			}
			return best;
		}
	}
  
	private ArrayList<MNKCell> orderMoves(MNKCell[] possibleMoves,MNKCell myLastMove, MNKCell foeLastMove) {
		ArrayList<MNKCell> moves = new ArrayList<MNKCell>();
		// check if i can win in one move
		if (P.turn >= P.k) {
			int result[] = findWinningMove(myLastMove);
			if (result[0] != -1) {
				moves.add(new MNKCell(result[0], result[1]));
				//i need to tell beta-pruning that the game is over
				gameover = true;
				return moves;
			}
		}
		// check if the foe can win in one move
		//is it worth it?
		int foeMoves = ( (P.m*P.n)- possibleMoves.length) - P.turn + 1;
		if (foeMoves >= P.k - 1) {
			System.out.println("checking foe's moves...");
			int result[] = findWinningMove(foeLastMove);
			if (result[0] != -1) { // i found a winning move
				moves.add(new MNKCell(result[0], result[1]));
				return moves;
			}
			System.out.println("no danger for now");
		}
		// order moves according to heuristic
		return moves;
	}

	// returns the one-turn-winning move if it exists, returns -1,-1 if it doesn't
	// O(8* check_direction ) = O(8*9*(k-1)) = O(72(k-1))
	// IsWinningCell is O(8(k-1)), which means that checking each move
	// is more efficient as long as they are less than 9
	// the issue is that we don't know how many moves we will evaluate
	// so for now we just use IsWinningCell each time and in some
	// specific occasions we may prefer findWinningMove
	private int[] findWinningMove(MNKCell move) {
		for (int[] DIR : DIRECTION) {
			int result[] = checkDirection(move.i, move.j, DIR, move.state);
			if (result[0] != -1) // i found a winning move
				return new int[] { result[0], result[1] };
		}
		return new int[] { -1, -1 };
	}

	// from the given move moves in the given direction and returns the winning move
	// if it exists, [-1,-1 ] otherwise
	// O( (k-1)+IsWinningCell() ) = O( k-1 + 8k-8) = O(9k-9)
	private int[] checkDirection(int i, int j, int[] DIR, MNKCellState cellState) {
		// the move is out of the board bound or it's marked by the other player
		i += DIR[0];
		j += DIR[1];
		if (i < 0 || i >= m || j < 0 || j >= n || (b[i][j] != MNKCellState.FREE && b[i][j] != cellState))
			return new int[] { -1, -1 };
		else if (b[i][j] == MNKCellState.FREE) { // the move is free
			Boolean win = isWinningCell(i, j, cellState);
			if (win)
				return new int[] { i, j };
			else {
				return new int[] { -1, -1 };
			}
		}
		// recursive case
		return checkDirection(i, j, DIR, cellState);
	}

	// marks the given move for the given player and checks if he wins
	// O(8(k-1))
	private boolean isWinningCell(int i, int j, MNKCellState player) {
		// can't check a move if it's already marked
		if (b[i][j] != MNKCellState.FREE)
			return false;
		int c;
		// Horizontal check
		c = 1;
		for (int k = 1; j - k >= 0 && b[i][j - k] == player; k++)
			c++; // backward check
		for (int k = 1; j + k < n && b[i][j + k] == player; k++)
			c++; // forward check
		if (c >= k)
			return true;

		// Vertical check
		c = 1;
		for (int k = 1; i - k >= 0 && b[i - k][j] == player; k++)
			c++; // backward check
		for (int k = 1; i + k < m && b[i + k][j] == player; k++)
			c++; // forward check
		if (c >= k)
			return true;

		// Diagonal check
		c = 1;
		for (int k = 1; i - k >= 0 && j - k >= 0 && b[i - k][j - k] == player; k++)
			c++; // backward check
		for (int k = 1; i + k < m && j + k < n && b[i + k][j + k] == player; k++)
			c++; // forward check
		if (c >= k)
			return true;

		// Anti-diagonal check
		c = 1;
		for (int k = 1; i - k >= 0 && j + k < n && b[i - k][j + k] == player; k++)
			c++; // backward check
		for (int k = 1; i + k < m && j - k >= 0 && b[i + k][j - k] == player; k++)
			c++; // backward check
		if (c >= k)
			return true;

		return false;
	}
}
