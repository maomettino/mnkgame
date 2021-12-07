package mnkgame;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.PriorityQueue;

public class AlphaBetaPruning {
	private final int m, n, k;
	private final int MAX_DEPTH = 5;
	private final int WIN = 1;
	private final int DEFEAT = -1;
	private final int ALPHA = -1000;
	private final int BETA = 1000;
	private final int TIMEOUT;
	private int currentDepth;
	private int saddamMoves, foeMoves;
	private MNKCellState saddam;
	private MNKCellState foe;
	private MNKCellState[][] b;
	private MNKCellState[][] globalBoard;

	public AlphaBetaPruning(int m, int n, int k, boolean first, int timeout_in_secs) {
		currentDepth = -1;
		saddamMoves = 0;
		foeMoves = 0;
		TIMEOUT = timeout_in_secs;
		this.m = m;
		this.n = n;
		this.k = k;
		saddam = first ? MNKCellState.P1 : MNKCellState.P2;
		foe = first ? MNKCellState.P2 : MNKCellState.P1;
		b = new MNKCellState[m][n];
		globalBoard = new MNKCellState[m][n];
		for (int i = 0; i < m; i++)
			for (int j = 0; j < n; j++)
				globalBoard[i][j] = MNKCellState.FREE;
	}

	public void signFoeMove(MNKCell foeCell) {
		foeMoves++;
		globalBoard[foeCell.i][foeCell.j] = foe;
	}

	public MNKCell getMove(MNKCell saddamLastCell, MNKCell foeLastCell) {
		// make sure that local board matches the global one
		for (int i = 0; i < m; i++)
			for (int j = 0; j < n; j++)
				b[i][j] = globalBoard[i][j];
		currentDepth = -1;
		saddamMoves++;
		foeMoves++;
		globalBoard[saddamLastCell.i][saddamLastCell.j] = saddam;
		globalBoard[foeLastCell.i][foeLastCell.j] = foe;
		Node father = new Node(foeLastCell.i, foeLastCell.j, ALPHA, BETA, ALPHA, saddamLastCell.i, saddamLastCell.j);
		Node node = alphaBetaPruning(father, true);
		return new MNKCell(node.i, node.j);
	}

	private Node alphaBetaPruning(Node father, Boolean max) {
		currentDepth++;
		Node[] children = findBestNodes(father);
		if (children.length == 1 && children[1].value == WIN)
			return children[1];
		if (currentDepth == MAX_DEPTH)
			return children[1];
		int i;
		for (i = 0; i < children.length; i++) {
			globalBoard[children[i].i][children[i].j] = max ? saddam : foe;
			Node child = alphaBetaPruning(children[i], !max);
			globalBoard[child.i][child.j] = MNKCellState.FREE;
			if (max) {
				if(currentDepth == -1 && Math.max(father.value, child.value)== child.value ) {
					//relative best child, in the end it will be the best
					father.bestChild = child;
				}
				father.value = Math.max(father.value, child.value);
				father.alpha = Math.max(father.alpha, father.value);
			} else {
				father.value = Math.min(father.value, child.value);
				father.beta = Math.min(father.beta, father.value);
			}
			if (father.beta <= father.alpha) {
				break;
			}
		}
		currentDepth--;
		if (currentDepth == -1)
			return father.bestChild;
		else
			return father;
	}

	// O(4((k-2)+ 2(k-1)))= O(12k-16)=O(12(k-4/3)), 4 axes with k-2 for the around
	// forward and backward and k-1 for jump
	// may be optimized breaking when length + extra + jump cell =k, in this case
	// O(4(k))
	public Node[] findBestNodes(Node father) {
		// moves to come closer to victory
		// Move[] s = checkAround(isSaddam ? saddamMove : foeMove, true);

		// moves to disrupt the enemy
		// Move[] f = checkAround(isSaddam ? foeMove : saddamMove, false);
		return father;

	}

	public Move[] checkAround(Move cell, boolean full) {
		PriorityQueue q;
		if (full)
			q = new PriorityQueue<Move>(8, new Comparatore());
		// check stuff for current player
		int i = cell.i;
		int j = cell.j;
		MNKCellState player = b[i][j];
		int length = 1, backExtra = 0, forwardExtra = 0, backCount, forwardCount;

		// first and second are about the jump cell and its immediate next
		boolean[] freeBack = { false, false }, freeForward = { false, false };
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
				return new Move[] { new Move(i, j - backCount, true) };
			}
			int rest = P.k - length - 1;
			for (int c = 1; j - backCount - c >= 0 && b[i][j - backCount - c] == player && c <= rest; c++)
				backExtra++;
			if (length + backExtra == P.k - 1) {
				System.out.println("diocane al quadrato H");
				return new Move[] { new Move(i, j - backCount) };
			}

			// check for the k-2 chain stuff

			freeBack[0] = true;
			if (j - backCount - 1 >= 0 && b[i][j - backCount - 1] == MNKCellState.FREE)
				freeBack[1] = true;

		}

		// forward jump and extra chain check
		if (j + forwardCount < n && b[i][j + forwardCount] == MNKCellState.FREE) {
			;
			if (length == P.k - 1) {
				System.out.println("diocane H");
				return new Move[] { new Move(i, j + forwardCount, true) };
			}
			int rest = P.k - length - 1;
			for (int c = 1; j + forwardCount + c < n && b[i][j + forwardCount + c] == player && c <= rest; c++)
				forwardExtra++;
			if (length + forwardExtra == P.k - 1) {
				System.out.println("diocane al quadrato H");
				return new Move[] { new Move(i, j + forwardCount, true) };
			}

			freeForward[0] = true;
			if (j + forwardCount + 1 < n && b[i][j + forwardCount + 1] == MNKCellState.FREE)
				freeForward[1] = true;

		}
		if (length == P.k - 2 && freeBack[0] && freeForward[0] && (freeBack[1] || freeForward[1])) {
			// if both directions are avaiable choose one of them, in this case the back
			j = freeBack[1] ? j - backCount : j + forwardCount;
			return new Move[] { new Move(i, j) };
		}
		freeBack[0] = false;
		freeBack[1] = false;
		freeForward[0] = false;
		freeForward[1] = false;
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
				return new Move[] { new Move(i - backCount, j) };
			}
			int rest = P.k - length - 1;
			for (int c = 1; i - backCount - c >= 0 && b[i - backCount - c][j] == player && c <= rest; c++)
				backExtra++;
			if (length + backExtra == P.k - 1) {
				System.out.println("diocane al quadrato V");
				return new Move[] { new Move(i - backCount, j) };
			}

			// check for the k-2 chain stuff

			freeBack[0] = true;
			if (i - backCount - 1 >= 0 && b[i - backCount - 1][j] == MNKCellState.FREE)
				freeBack[1] = true;

		}

		// forward jump and extra chain check
		if (i + forwardCount < n && b[i + forwardCount][j] == MNKCellState.FREE) {
			if (length == P.k - 1) {
				System.out.println("diocane V");
				return new Move[] { new Move(i + forwardCount, j) };
			}
			int rest = P.k - length - 1;
			for (int c = 1; i + forwardCount + c < n && b[i + forwardCount + c][j] == player && c <= rest; c++)
				forwardExtra++;
			if (length + forwardExtra == P.k - 1) {
				System.out.println("diocane al quadrato V");
				return new Move[] { new Move(i + forwardCount, j) };
			}

			freeForward[0] = true;
			if (i + forwardCount + 1 < n && b[i + forwardCount + 1][j] == MNKCellState.FREE)
				freeForward[1] = true;

		}
		if (length == P.k - 2 && freeBack[0] && freeForward[0] && (freeBack[1] || freeForward[1])) {
			// if both directions are avaiable choose one of them, in this case the back
			i = freeBack[1] ? i - backCount : i + forwardCount;
			return new Move[] { new Move(i, j) };

		}
		freeBack[0] = false;
		freeBack[1] = false;
		freeForward[0] = false;
		freeForward[1] = false;
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
				return new Move[] { new Move(i - backCount, j - backCount) };
			}
			int rest = P.k - length - 1;
			for (int c = 1; j - backCount - c >= 0 && i - backCount - c >= 0
					&& b[i - backCount - c][j - backCount - c] == player && c <= rest; c++)
				backExtra++;
			if (length + backExtra == P.k - 1) {
				System.out.println("diocane al quadrato D");
				return new Move[] { new Move(i - backCount, j - backCount) };
			}

			// check for the k-2 chain stuff

			freeBack[0] = true;
			if (j - backCount - 1 >= 0 && i - backCount - 1 >= 0
					&& b[i - backCount - 1][j - backCount - 1] == MNKCellState.FREE)
				freeBack[1] = true;

		}

		// forward jump and extra chain check
		if (j + forwardCount < n && i + forwardCount < n
				&& b[i + forwardCount][j + forwardCount] == MNKCellState.FREE) {
			if (length == P.k - 1) {
				System.out.println("diocane D");
				return new Move[] { new Move(i + forwardCount, j + forwardCount) };
			}
			int rest = P.k - length - 1;
			for (int c = 1; j + forwardCount + c < n && i + forwardCount + c < n
					&& b[i + forwardCount + c][j + forwardCount + c] == player && c <= rest; c++)
				forwardExtra++;
			if (length + forwardExtra == P.k - 1) {
				System.out.println("diocane al quadrato D");
				return new Move[] { new Move(i + forwardCount, j + forwardCount) };
			}

			freeForward[0] = true;
			if (j + forwardCount + 1 < n && i + forwardCount + 1 < n
					&& b[i + forwardCount + 1][j + forwardCount + 1] == MNKCellState.FREE)
				freeForward[1] = true;

		}
		if (length == P.k - 2 && freeBack[0] && freeForward[0] && (freeBack[1] || freeForward[1])) {
			// if both directions are avaiable choose one of them, in this case the back
			i = freeForward[1] ? i - backCount : i + forwardCount;
			j = freeBack[1] ? j - backCount : j + forwardCount;
			return new Move[] { new Move(i, j) };

		}
		freeBack[0] = false;
		freeBack[1] = false;
		freeForward[0] = false;
		freeForward[1] = false;
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
				return new Move[] { new Move(i - backCount, j + backCount) };
			}
			int rest = P.k - length - 1;
			for (int c = 1; j + backCount + c < n && i - backCount - c >= 0
					&& b[i - backCount - c][j + backCount + c] == player && c <= rest; c++)
				backExtra++;
			if (length + backExtra == P.k - 1) {
				System.out.println("diocane al quadrato AD");
				return new Move[] { new Move(i - backCount, j + backCount) };
			}

			freeBack[0] = true;
			if (j + backCount + 1 >= 0 && i - backCount - 1 >= 0
					&& b[i - backCount - 1][j + backCount - 1] == MNKCellState.FREE)
				freeBack[1] = true;

		}

		// forward jump and extra chain check
		if (j - forwardCount >= 0 && i + forwardCount < n
				&& b[i + forwardCount][j - forwardCount] == MNKCellState.FREE) {
			if (length == P.k - 1) {
				System.out.println("diocane AD");
				return new Move[] { new Move(i + forwardCount, j - forwardCount) };
			}
			int rest = P.k - length - 1;
			for (int c = 1; j - forwardCount - c >= 0 && i + forwardCount + c < n
					&& b[i + forwardCount + c][j - forwardCount - c] == player && c <= rest; c++)
				forwardExtra++;
			if (length + forwardExtra == P.k - 1) {
				System.out.println("diocane al quadrato AD");
				return new Move[] { new Move(i + forwardCount, j - forwardCount) };
			}

			freeForward[0] = true;
			if (j - forwardCount - 1 < n && i + forwardCount + 1 < n
					&& b[i + forwardCount + 1][j - forwardCount - 1] == MNKCellState.FREE)
				freeForward[1] = true;

		}
		if (length == P.k - 2 && freeBack[0] && freeForward[0] && (freeBack[1] || freeForward[1])) {
			// if both directions are avaiable choose one of them, in this case the back
			i = freeBack[1] ? i - backCount : i + forwardCount;
			j = freeBack[1] ? j + backCount : j - forwardCount;
			return new Move[] { new Move(i, j) };

		}
		freeBack[0] = false;
		freeBack[1] = false;
		freeForward[0] = false;
		freeForward[1] = false;
		backExtra = 0;
		forwardExtra = 0;

		return new Move[] { new Move(cell.i, cell.j) };

	}

	private void printMatrix(MNKCellState[][] matrix) {
		Arrays.stream(matrix).forEach((row) -> {
			System.out.print("[");
			Arrays.stream(row).forEach((el) -> System.out.print(" " + el + " "));
			System.out.println("]");
		});
	}
}
