package mnkgame;

import java.util.ArrayList;
import java.util.Arrays;

import javax.lang.model.util.ElementScanner14;
import javax.swing.text.html.FormView;

import mnkgame.ChainState;
import mnkgame.MNKCell;
import mnkgame.MNKCellState;

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
	// the level of the current father
	private int currentDepth;
	private boolean iWin;
	private boolean foeWins;
	private MNKCell bestMove;
	/**
	 * @param b local board to do stuff without touching the actual board
	 */
	private MNKCellState[][] b;

	public AlphaBetaPruning() {
		iWin = false;
		foeWins = false;
		currentDepth = -1;
	}

	public MNKCell getMove(MNKCell myLastMove, MNKCell foeLastMove) {
		// make sure that local board matches the global one
		b = P.b;
		currentDepth = -1;
		findBestMoves(foeLastMove, P.foe);
		//alphaBetaPruning(foeLastMove, myLastMove, true, ALPHA, BETA);
		return bestMove;
	}

	private int alphaBetaPruning(MNKCell foeLastMove, MNKCell myLastMove, Boolean maximizingPlayer, int alpha, int beta) {
		currentDepth++;

		// leaf node is reached
		if (currentDepth == MAX_DEPTH) {
			currentDepth--;
			// heuristic value for the node get_heuristic_value(moves[0])
			return 0;
		}
		MNKCell[] moves = findBestMoves(myLastMove, maximizingPlayer?P.me:P.foe);
		// now the best move is the one provided by the heuristic algorithm
		if (currentDepth == 0) {
			bestMove = moves[0];
		}

		if (iWin) {
			iWin = false;
			currentDepth--;
			return maximizingPlayer ? BETA : ALPHA;
		} else if (foeWins) {
			foeWins = false;
			currentDepth--;
			return maximizingPlayer ? BETA : ALPHA;
		}

		if (maximizingPlayer) {
			int best = ALPHA, i;

			// dfs for the tree rooted in each move
			for (i = 0; i < 8; i++) {
				MNKCell move = moves[i];

				// mark current move
				b[move.i][move.j] = P.me;
				int val = alphaBetaPruning(foeLastMove, move, false, alpha, beta);
				b[move.i][move.j] = MNKCellState.FREE;
				best = Math.max(best, val);
				alpha = Math.max(alpha, best);

				// Alpha Beta Pruning
				// System.out.println("alpha "+alpha+ " beta "+beta);
				if (beta <= alpha) {
					if (currentDepth == 1)
						bestMove = move;
					// System.out.println("CUTOFF PER SADDAM HUSSEIN");
					break;
				}
			}
			return best;
		} else {
			// same stuff but from foe's pov
			int best = BETA, i;
			for (i = 0; i < 8; i++) {
				MNKCell move = moves[i];

				// mark current move
				b[move.i][move.j] = P.foe;
				int val = alphaBetaPruning(move, myLastMove, true, alpha, beta);
				b[move.i][move.j] = MNKCellState.FREE;
				best = Math.min(best, val);
				beta = Math.min(beta, best);
				// System.out.println("alpha "+alpha+ "beta "+beta);
				// Alpha Beta Pruning
				if (beta <= alpha) {
					// System.out.println("CUTOFF PER IL NEMICO");
					break;
				}

			}
			currentDepth--;
			return best;
		}
	}

	// O(4((k-2)+ 2(k-1)))= O(12k-16)=O(12(k-4/3)), 4 axes with k-2 for the around
	// forward and backward and k-1 for jump
	// may be optimized breaking when length + extra + jump cell =k, in this case
	// O(4(k))
	public MNKCell[] findBestMoves(MNKCell cell, MNKCellState player) {
		int i = cell.i;
		int j = cell.j;
		// around for each axis
		int totalH, totalV, totalD, totalAD;
		int length = 1, backExtra = 0, forwardExtra = 0, backCount, forwardCount;

		// Horizontal check
		// backward check
		for (backCount = 1; j - backCount >= 0 && b[i][j - backCount] == player; backCount++)
			length++;

		// forward check
		for (forwardCount = 1; j + forwardCount < n && b[i][j + forwardCount] == player; forwardCount++)
			length++;

		// back jump and extra chain check
		if (j - backCount >= 0 && b[i][j - backCount] == MNKCellState.FREE) {
			if (length == P.k - 1) {
				System.out.println("diocane H ");
			}
			int rest = P.k - length - 1;
			for (int c = 1; j - backCount - c >= 0 && b[i][j - backCount - c] == player
					&& c<= rest;c++)
				backExtra++;
			if (length + backExtra >= P.k - 1) {
				System.out.println("diocane al quadrato H");
			}
		}

		// forward jump and extra chain check
		if (j + forwardCount < n && b[i][j + forwardCount] == MNKCellState.FREE) {
			if (length == P.k - 1) {
				System.out.println("diocane H");
			}
			int rest = P.k - length - 1;
			for (int c = 1; j + forwardCount + c < n && b[i][j + forwardCount + c] == player
					&& c <= rest; c++)
				forwardExtra++;
			if (length + forwardExtra >= P.k - 1) {
				System.out.println("diocane al quadrato H");
			}
		}
		totalH = length + Math.max(backExtra, forwardExtra);
		length = 1; backExtra = 0; forwardExtra = 0;

		// Vertical check
		// backward check
		for (backCount = 1; i - backCount >= 0 && b[i- backCount][j] == player; backCount++)
			length++;

		// forward check
		for (forwardCount = 1; i + forwardCount < n && b[i+ forwardCount][j] == player; forwardCount++)
			length++;

		// back jump and extra chain check
		if (i - backCount >= 0 && b[i- backCount][j] == MNKCellState.FREE) {
			if (length == P.k - 1) {
				System.out.println("diocane V");
			}
			int rest = P.k - length - 1;
			for (int c = 1; i - backCount - c >= 0 && b[i - backCount - c][j] == player
					&& c<= rest;c++)
				backExtra++;
			if (length + backExtra >= P.k - 1) {
				System.out.println("diocane al quadrato V");
			}
		}

		// forward jump and extra chain check
		if (i + forwardCount < n && b[i + forwardCount][j] == MNKCellState.FREE) {
			if (length == P.k - 1) {
				System.out.println("diocane V");
			}
			int rest = P.k - length - 1;
			for (int c = 1; i + forwardCount + c < n && b[i + forwardCount + c][j] == player
					&& c <= rest; c++)
				forwardExtra++;
			if (length + forwardExtra >= P.k - 1) {
				System.out.println("diocane al quadrato V");
			}
		}
		totalV = length + Math.max(backExtra, forwardExtra);
		length = 1; backExtra = 0; forwardExtra = 0;

		// Diagonal check
		// backward check
		for (backCount = 1; j - backCount >= 0 && i - backCount >= 0 && b[i - backCount][j - backCount] == player; backCount++)
			length++;

		// forward check
		for (forwardCount = 1; j + forwardCount <n && i + forwardCount < n && b[i + forwardCount][j + forwardCount] == player; forwardCount++)
			length++;

		// back jump and extra chain check
		if (j - backCount >= 0 && i - backCount >= 0 && b[i - backCount][j - backCount] == MNKCellState.FREE) {
			if (length == P.k - 1) {
				System.out.println("diocane D");
			}
			int rest = P.k - length - 1;
			for (int c = 1; j - backCount - c >= 0 && i - backCount - c >= 0 && b[i - backCount - c][j - backCount - c] == player
					&& c<= rest;c++)
				backExtra++;
			if (length + backExtra >= P.k - 1) {
				System.out.println("diocane al quadrato D");
			}
		}

		// forward jump and extra chain check
		if (j + forwardCount < n && i + forwardCount < n && b[i + forwardCount][j + forwardCount] == MNKCellState.FREE) {
			if (length == P.k - 1) {
				System.out.println("diocane D");
			}
			int rest = P.k - length - 1;
			for (int c = 1; j + forwardCount + c < n && i + forwardCount + c < n && b[i + forwardCount + c][j + forwardCount + c] == player
					&& c <= rest; c++)
				forwardExtra++;
			if (length + forwardExtra >= P.k - 1) {
				System.out.println("diocane al quadrato D");
			}
		}
		totalH = length + Math.max(backExtra, forwardExtra);
		length = 1; backExtra = 0; forwardExtra = 0;

		// Antidiagonal check
		// backward check
		for (backCount = 1; j + backCount < n && i - backCount >= 0 && b[i - backCount][j + backCount] == player; backCount++)
			length++;

		// forward check
		for (forwardCount = 1; j - forwardCount >= 0 && i + forwardCount < n && b[i + forwardCount][j - forwardCount] == player; forwardCount++)
			length++;

		// back jump and extra chain check
		if (j + backCount <n && i - backCount >= 0 && b[i - backCount][j + backCount] == MNKCellState.FREE) {
			if (length == P.k - 1) {
				System.out.println("diocane AD");
			}
			int rest = P.k - length - 1;
			for (int c = 1; j + backCount + c <n && i - backCount - c >= 0 && b[i - backCount - c][j + backCount + c] == player
					&& c<= rest;c++)
				backExtra++;
			if (length + backExtra >= P.k - 1) {
				System.out.println("diocane al quadrato AD");
			}
		}

		// forward jump and extra chain check
		if (j - forwardCount >=0 && i + forwardCount < n && b[i + forwardCount][j - forwardCount] == MNKCellState.FREE) {
			if (length == P.k - 1) {
				System.out.println("diocane AD");
			}
			int rest = P.k - length - 1;
			for (int c = 1; j - forwardCount - c >=0 && i + forwardCount + c < n && b[i + forwardCount + c][j - forwardCount - c] == player
					&& c <= rest; c++)
				forwardExtra++;
			if (length + forwardExtra >= P.k - 1) {
				System.out.println("diocane al quadrato AD");
			}
		}
		totalH = length + Math.max(backExtra, forwardExtra);
		length = 1; backExtra = 0; forwardExtra = 0;
		return new MNKCell[] {new MNKCell(cell.i, cell.j)};
		// reset values for the next axis
		
	}

	/*
	 * //i may win marking one of the ends if (lV >= k-1) return true; // Vertical
	 * check int lV = 1; for (int k = 1; i - k >= 0 && b[i - k][j] == player; k++)
	 * lV++; // backward check for (int k = 1; i + k < m && b[i + k][j] == player;
	 * k++) lV++; // forward check if (lV >= k) return true;
	 * 
	 * // Diagonal check int lD = 1; for (int k = 1; i - k >= 0 && j - k >= 0 && b[i
	 * - k][j - k] == player; k++) lD++; // backward check for (int k = 1; i + k < m
	 * && j + k < n && b[i + k][j + k] == player; k++) lD++; // forward check if (lD
	 * >= k) return true;
	 * 
	 * // Anti-diagonal check int lAD = 1; for (int k = 1; i - k >= 0 && j + k < n
	 * && b[i - k][j + k] == player; k++) lAD++; // backward check for (int k = 1; i
	 * + k < m && j - k >= 0 && b[i + k][j - k] == player; k++) lAD++; // backward
	 * check if (lAD >= k) return true;
	 */
	// returns the one-turn-winning move if it exists, returns -1,-1 if it doesn't
	// O(8* check_direction ) = O(8*9*(k-1)) = O(72(k-1))
	// IsWinningCell is O(8(k-1)), which means that checking each move
	// is more efficient as long as they are less than 9
	// the issue is that we don't know how many moves we will evaluate
	// so for now we use findWinningMove since its already implemented
	// inside the heuristic and we work so hard for it
	private int[] findWinningMove(MNKCell move) {
		for (int[] DIR : DIRECTION) {
			int result[] = checkDirection(move.i, move.j, DIR, b[move.i][move.j]);// move.state
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

	private void printMatrix(MNKCellState[][] matrix) {
		Arrays.stream(matrix).forEach((row) -> {
			System.out.print("[");
			Arrays.stream(row).forEach((el) -> System.out.print(" " + el + " "));
			System.out.println("]");
		});
	}
}
