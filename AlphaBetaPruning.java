package mnkgame;

import java.util.ArrayList;
import java.util.Arrays;

import mnkgame.MNKCell;
import mnkgame.Node;

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
	// the level of the current father
	private int currentDepth;
	private boolean iWin;
	private boolean foeWins;
	/**
	 * @param b local board to do stuff without touching the actual board
	 */
	private MNKCellState[][] b;

	public AlphaBetaPruning() {
		iWin = false;
		foeWins = false;
		currentDepth = -1;
	}

	public MNKCell getMove(MNKCell saddamLastMove, MNKCell foeLastMove) {
		// make sure that local board matches the global one
		b = P.b;
		currentDepth = -1;
		Node node = alphaBetaPruning(foeLastMove, saddamLastMove, P.ALPHA,P.BETA, true);
		return new MNKCell(node.i, node.j);
	}

	private Node alphaBetaPruning(MNKCell foeLastMove, MNKCell saddamLastMove, int alpha, int beta, Boolean maximizingPlayer) {
		currentDepth++;
		// in each recursive call i create a node and update it by iterating through its
		// children
		// then i return said node to update the node at the upper level
		// if the latter node is at depth 0 then must return the node
		// that caused the cut-off if there was any, otherwise the beta-pruning
		// failed and we the return the first move computed by the heuristic
		// create node for the current move with default value	
		// if current node is a leaf then
		// return new Node(foeLastMove.i, foeLastMove.j,getHeuristicValue());
		// the paramter is either victory/defeat for saddam or a heuristic value

		MNKCell[] moves = findBestMoves(saddamLastMove, maximizingPlayer ? P.me : P.foe);
		if(moves.length ==1)
			return new Node(foeLastMove.i, foeLastMove.j, maximizingPlayer?1:-1);
		if(currentDepth==MAX_DEPTH)
			return new Node(foeLastMove.i, foeLastMove.j, 0);//getHeuristicValue()
		Node node = new Node(foeLastMove.i, foeLastMove.j, alpha, beta, maximizingPlayer);
		// basically if i'm Saddam or not
		if (maximizingPlayer) {
			int i;
			for (i = 0; i < moves.length; i++) {
				MNKCell move = moves[i];

				// mark current move
				b[move.i][move.j] = P.me;

				// create child node for the current move, aplha and beta are inherited bu the
				// father
				// Node child = new Node(move.i, move.j, node.alpha, node.beta,
				// !maximizingPlayer);

				// returns the given node with updated value
				Node child = alphaBetaPruning(foeLastMove, move,node.alpha, node.beta, false);
				b[move.i][move.j] = MNKCellState.FREE;
				node.value = Math.max(node.value, child.value);
				node.alpha = Math.max(node.alpha, node.value);

				// best move found for this sub-tree, no need to check other childre
				if (node.beta <= node.alpha) {
					// return the move that caused the cut-off at depth 0, i.e the best move
					if (currentDepth == 0)
						return child;
					// return the updated node
					else
						return node;
					// System.out.println("CUTOFF PER SADDAM HUSSEIN");
				}
			}
		} else {
			// same stuff but from foe's pov
			int i;
			for (i = 0; i < moves.length; i++) {
				MNKCell move = moves[i];

				// mark current move
				b[move.i][move.j] = P.foe;

				// create child node for the current move, aplha and beta are inherited bu the
				// father
				// Node child = new Node(move.i, move.j, node.alpha, node.beta,
				// maximizingPlayer);
				Node child = alphaBetaPruning(move, saddamLastMove, node.alpha, node.beta, true);
				b[move.i][move.j] = MNKCellState.FREE;
				node.value = Math.min(node.value, child.value);
				node.beta = Math.min(node.beta, node.value);
				if (node.beta <= node.alpha) {
					// return the move that caused the cut-off at depth 0, i.e the best move we're
					if (currentDepth == 0)
						return child;
					// return the updated node
					else
						return node;
				}
			}

		}
		currentDepth--;

		// if i get here yhen i have examined all the children of the current node and
		// no best move was found
		// this means that the move samples wasn't big enough, so we return
		// the move suggested by the heuristic, i.e. the first one in the array if the
		// current node is the root, otherwise we return the node itself which is now updated
		// and ready to be used to update the upper node
		if( currentDepth == -1 )
			return new Node(moves[0].i, moves[1].j, 0);
		else
			return node;
	}

	/*
	 * leaf node is reached if (currentDepth == MAX_DEPTH) { currentDepth--; //
	 * heuristic value for the node get_heuristic_value(moves[0]) return 0; }
	 */
	/*
	 * now the best move is the one provided by the heuristic algorithm if
	 * (currentDepth == 0) { bestMove = moves[0]; }
	 * 
	 * if (iWin) { iWin = false; currentDepth--; return maximizingPlayer ? 1 : -1; }
	 * else if (foeWins) { foeWins = false; currentDepth--; return maximizingPlayer
	 * ? 1 : -1; }
	 */
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
			for (int c = 1; j - backCount - c >= 0 && b[i][j - backCount - c] == player && c <= rest; c++)
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
			for (int c = 1; j + forwardCount + c < n && b[i][j + forwardCount + c] == player && c <= rest; c++)
				forwardExtra++;
			if (length + forwardExtra >= P.k - 1) {
				System.out.println("diocane al quadrato H");
			}
		}
		totalH = length + Math.max(backExtra, forwardExtra);
		length = 1;
		backExtra = 0;
		forwardExtra = 0;

		// Vertical check
		// backward check
		for (backCount = 1; i - backCount >= 0 && b[i - backCount][j] == player; backCount++)
			length++;

		// forward check
		for (forwardCount = 1; i + forwardCount < n && b[i + forwardCount][j] == player; forwardCount++)
			length++;

		// back jump and extra chain check
		if (i - backCount >= 0 && b[i - backCount][j] == MNKCellState.FREE) {
			if (length == P.k - 1) {
				System.out.println("diocane V");
			}
			int rest = P.k - length - 1;
			for (int c = 1; i - backCount - c >= 0 && b[i - backCount - c][j] == player && c <= rest; c++)
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
			for (int c = 1; i + forwardCount + c < n && b[i + forwardCount + c][j] == player && c <= rest; c++)
				forwardExtra++;
			if (length + forwardExtra >= P.k - 1) {
				System.out.println("diocane al quadrato V");
			}
		}
		totalV = length + Math.max(backExtra, forwardExtra);
		length = 1;
		backExtra = 0;
		forwardExtra = 0;

		// Diagonal check
		// backward check
		for (backCount = 1; j - backCount >= 0 && i - backCount >= 0
				&& b[i - backCount][j - backCount] == player; backCount++)
			length++;

		// forward check
		for (forwardCount = 1; j + forwardCount < n && i + forwardCount < n
				&& b[i + forwardCount][j + forwardCount] == player; forwardCount++)
			length++;

		// back jump and extra chain check
		if (j - backCount >= 0 && i - backCount >= 0 && b[i - backCount][j - backCount] == MNKCellState.FREE) {
			if (length == P.k - 1) {
				System.out.println("diocane D");
			}
			int rest = P.k - length - 1;
			for (int c = 1; j - backCount - c >= 0 && i - backCount - c >= 0
					&& b[i - backCount - c][j - backCount - c] == player && c <= rest; c++)
				backExtra++;
			if (length + backExtra >= P.k - 1) {
				System.out.println("diocane al quadrato D");
			}
		}

		// forward jump and extra chain check
		if (j + forwardCount < n && i + forwardCount < n
				&& b[i + forwardCount][j + forwardCount] == MNKCellState.FREE) {
			if (length == P.k - 1) {
				System.out.println("diocane D");
			}
			int rest = P.k - length - 1;
			for (int c = 1; j + forwardCount + c < n && i + forwardCount + c < n
					&& b[i + forwardCount + c][j + forwardCount + c] == player && c <= rest; c++)
				forwardExtra++;
			if (length + forwardExtra >= P.k - 1) {
				System.out.println("diocane al quadrato D");
			}
		}
		totalD = length + Math.max(backExtra, forwardExtra);
		length = 1;
		backExtra = 0;
		forwardExtra = 0;

		// Antidiagonal check
		// backward check
		for (backCount = 1; j + backCount < n && i - backCount >= 0
				&& b[i - backCount][j + backCount] == player; backCount++)
			length++;

		// forward check
		for (forwardCount = 1; j - forwardCount >= 0 && i + forwardCount < n
				&& b[i + forwardCount][j - forwardCount] == player; forwardCount++)
			length++;

		// back jump and extra chain check
		if (j + backCount < n && i - backCount >= 0 && b[i - backCount][j + backCount] == MNKCellState.FREE) {
			if (length == P.k - 1) {
				System.out.println("diocane AD");
			}
			int rest = P.k - length - 1;
			for (int c = 1; j + backCount + c < n && i - backCount - c >= 0
					&& b[i - backCount - c][j + backCount + c] == player && c <= rest; c++)
				backExtra++;
			if (length + backExtra >= P.k - 1) {
				System.out.println("diocane al quadrato AD");
			}
		}

		// forward jump and extra chain check
		if (j - forwardCount >= 0 && i + forwardCount < n
				&& b[i + forwardCount][j - forwardCount] == MNKCellState.FREE) {
			if (length == P.k - 1) {
				System.out.println("diocane AD");
			}
			int rest = P.k - length - 1;
			for (int c = 1; j - forwardCount - c >= 0 && i + forwardCount + c < n
					&& b[i + forwardCount + c][j - forwardCount - c] == player && c <= rest; c++)
				forwardExtra++;
			if (length + forwardExtra >= P.k - 1) {
				System.out.println("diocane al quadrato AD");
			}
		}
		totalAD = length + Math.max(backExtra, forwardExtra);
		length = 1;
		backExtra = 0;
		forwardExtra = 0;

		return new MNKCell[] { new MNKCell(cell.i, cell.j) };

	}

	private void printMatrix(MNKCellState[][] matrix) {
		Arrays.stream(matrix).forEach((row) -> {
			System.out.print("[");
			Arrays.stream(row).forEach((el) -> System.out.print(" " + el + " "));
			System.out.println("]");
		});
	}
}
