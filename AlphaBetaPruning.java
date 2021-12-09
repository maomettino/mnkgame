package mnkgame;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.PriorityQueue;

import Test.Move;
import mnkgame.Node;
import mnkgame.State;

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
		Node father = new Node(foeLastCell.i, foeLastCell.j, ALPHA, BETA, ALPHA, saddamLastCell.i, saddamLastCell.j, false);
		Node node = alphaBetaPruning(father);
		return new MNKCell(node.i, node.j);
	}

	private Node alphaBetaPruning(Node father) {
		currentDepth++;
		Node[] children = findBestNodes(father);
		if (children.length == 1 && children[1].value == WIN)
			return children[1];
		if (currentDepth == MAX_DEPTH)
			return children[1];
		int i;
		for (i = 0; i < children.length; i++) {
			globalBoard[children[i].i][children[i].j] = children[i].isSaddam? saddam : foe;
			Node child = alphaBetaPruning(children[i]);
			globalBoard[child.i][child.j] = MNKCellState.FREE;
			if (children[i].isSaddam) {
				if (currentDepth == -1 && Math.max(father.value, child.value) == child.value) {
					// relative best child, in the end it will be the best
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
	/*
	 * PriorityQueue q;
	 * if (full)
	 * q = new PriorityQueue<Move>(8, new Comparatore());
	 */
	public Node[] findBestNodes(Node father) {
		Node[] children;
		Moves myMoves = new Moves();
		checkAround(father.iParent, father.jParent, myMoves, true);
		if(myMoves.win==null) {
			Moves foeMoves = new Moves();
			checkAround(father.i, father.j, foeMoves, false);
			if(foeMoves.win==null) {
				if(myMoves.twoWin==null) {
					if(foeMoves.twoWin==null) {
						for(int i=0; !myMoves.q.isEmpty();i++) {
							//vernichtungskrieg
						}
					}
					else 
						children = new Node(foeMoves.win[0], foeMoves.win[1], father.alpha, father.beta,father.isSaddam?BETA:ALPHA, father.i,father.j, !father.isSaddam) ;
				}
				else
					children = new Node(foeMoves.win[0], foeMoves.win[1], !father.isSaddam,!father.isSaddam?WIN:DEFEAT);
			}
			else 
				children = new Node(foeMoves.win[0], foeMoves.win[1], father.alpha, father.beta,father.isSaddam?BETA:ALPHA, father.i,father.j, !father.isSaddam);
		}
		else 
			return new Node(myMoves.win[0], myMoves.win[1], !father.isSaddam,!father.isSaddam?WIN:DEFEAT);
		return children;	
	}

	public void checkAround(int i, int j, Moves moves, boolean full) {
		PriorityQueue q;
		if (full)
			q = new PriorityQueue<Move>(8, new Comparatore());
		MNKCellState player = globalBoard[i][j];
		int length = 1, backExtra = 0, forwardExtra = 0, backCount, forwardCount;
		boolean freeBack[] = { false, false }, freeForward[] = { false, false };
		for (backCount = 1; j - backCount >= 0 && globalBoard[i][j - backCount] == player; backCount++)
			length++;
		for (forwardCount = 1; j + forwardCount < n && globalBoard[i][j + forwardCount] == player; forwardCount++)
			length++;
		if (j - backCount >= 0 && globalBoard[i][j - backCount] == MNKCellState.FREE) {
			if (length == k - 1) {
				int move[] = { i, j - backCount };
				moves.win = move;
				break;
			}
			int rest = k - length - 1;
			for (int c = 1; j - backCount - c >= 0 && globalBoard[i][j - backCount - c] == player && c <= rest; c++)
				backExtra++;
			if (length + backExtra == k - 1) {
				int move[] = { i, j - backCount };
				moves.win = move;
				break;
			}
			freeBack[0] = true;
			if (j - backCount - 1 >= 0 && globalBoard[i][j - backCount - 1] == MNKCellState.FREE)
				freeBack[1] = true;
			if (full) {
				int[] backMove = { i, j - backCount, length + backExtra };
				q.add(backMove);
			}
		}
		if (j + forwardCount < n && globalBoard[i][j + forwardCount] == MNKCellState.FREE) {
			if (length == k - 1) {
				int move[] = { i, j + forwardCount };
				moves.win = move;
				break;
			}
			int rest = k - length - 1;
			for (int c = 1; j + forwardCount + c < n && globalBoard[i][j + forwardCount + c] == player
					&& c <= rest; c++)
				forwardExtra++;
			if (length + forwardExtra == k - 1) {
				int move[] = { i, j + forwardCount };
				moves.win = move;
				break;
			}
			freeForward[0] = true;
			if (j + forwardCount + 1 < n && globalBoard[i][j + forwardCount + 1] == MNKCellState.FREE)
				freeForward[1] = true;
			if (full) {
				int[] forwardMove = { i, j + forwardCount, length + forwardExtra };
				q.add(forwardMove);
			}
		}
		if (length == k - 2 && freeBack[0] && freeForward[0] && (freeBack[1] || freeForward[1])) {
			j = freeBack[1] ? j - backCount : j + forwardCount;
			int move[] = { i, j };
			moves.twoWin = move;
		}
		length = 1; backExtra = 0; forwardExtra = 0; 
		freeBack[0] = false; freeBack[1] = false; freeForward[0] = false; freeForward[1] = false;

		// Vertical
		for (backCount = 1; i - backCount >= 0 && globalBoard[i - backCount][j] == player; backCount++)
			length++;
		for (forwardCount = 1; i + forwardCount < n && globalBoard[i + forwardCount][j] == player; forwardCount++)
			length++;
		if (i - backCount >= 0 && globalBoard[i - backCount][j] == MNKCellState.FREE) {
			if (length == k - 1) {
				int move[] = { i - backCount, j };
				moves.win = move;
				break;
			}
			int rest = k - length - 1;
			for (int c = 1; i - backCount - c >= 0 && globalBoard[i - backCount - c][j] == player && c <= rest; c++)
				backExtra++;
			if (length + backExtra == k - 1) {
				int move[] = { i - backCount, j };
				moves.win = move;
				break;
			}
			freeBack[0] = true;
			if (i - backCount - 1 >= 0 && globalBoard[i - backCount - 1][j] == MNKCellState.FREE)
				freeBack[1] = true;
			if (full) {
				int[] backMove = { i - backCount, j, length + backExtra };
				q.add(backMove);
			}
		}
		if (i + forwardCount < n && globalBoard[i + forwardCount][j] == MNKCellState.FREE) {
			if (length == k - 1) {
				int move[] = { i + forwardCount, j };
				moves.win = move;
				break;
			}
			int rest = k - length - 1;
			for (int c = 1; i + forwardCount + c < n && globalBoard[i + forwardCount + c][j] == player
					&& c <= rest; c++)
				forwardExtra++;
			if (length + forwardExtra == k - 1) {
				int move[] = { i + forwardCount, j };
				moves.win = move;
				break;
			}
			freeForward[0] = true;
			if (i + forwardCount + 1 < n && globalBoard[i + forwardCount + 1][j] == MNKCellState.FREE)
				freeForward[1] = true;
			if (full) {
				int[] forwardMove = { i + forwardCount, j, length + forwardExtra };
				q.add(forwardMove);
			}
		}
		if (length == k - 2 && freeBack[0] && freeForward[0] && (freeBack[1] || freeForward[1])) {
			i = freeBack[1] ? i - backCount : i + forwardCount;
			int move[] = { i, j };
			moves.twoWin = move;
		}
		length = 1; backExtra = 0; forwardExtra = 0; 
		freeBack[0] = false; freeBack[1] = false; freeForward[0] = false; freeForward[1] = false;

		// Diagonal
		for (backCount = 1; j - backCount >= 0 && i - backCount >= 0
				&& globalBoard[i - backCount][j - backCount] == player; backCount++)
			length++;
		for (forwardCount = 1; j + forwardCount < n && i + forwardCount < n
				&& globalBoard[i + forwardCount][j + forwardCount] == player; forwardCount++)
			length++;
		if (j - backCount >= 0 && i - backCount >= 0
				&& globalBoard[i - backCount][j - backCount] == MNKCellState.FREE) {
			if (length == k - 1) {
				int move[] = { i - backCount, j - backCount };
				moves.win = move;
				break;
			}
			int rest = k - length - 1;
			for (int c = 1; j - backCount - c >= 0 && i - backCount - c >= 0
					&& globalBoard[i - backCount - c][j - backCount - c] == player && c <= rest; c++)
				backExtra++;
			if (length + backExtra == k - 1) {
				int move[] = { i - backCount, j - backCount };
				moves.win = move;
				break;
			}
			freeBack[0] = true;
			if (j - backCount - 1 >= 0 && i - backCount - 1 >= 0
					&& globalBoard[i - backCount - 1][j - backCount - 1] == MNKCellState.FREE)
				freeBack[1] = true;
			if (full) {
				int[] backMove = { i - backCount, j - backCount, length + backExtra };
				q.add(backMove);
			}
		}
		if (j + forwardCount < n && i + forwardCount < n
				&& globalBoard[i + forwardCount][j + forwardCount] == MNKCellState.FREE) {
			if (length == k - 1) {
				int move[] = { i + forwardCount, j + forwardCount };
				moves.win = move;
				break;
			}
			int rest = k - length - 1;
			for (int c = 1; j + forwardCount + c < n && i + forwardCount + c < n
					&& globalBoard[i + forwardCount + c][j + forwardCount + c] == player && c <= rest; c++)
				forwardExtra++;
			if (length + forwardExtra == k - 1) {
				int move[] = { i + forwardCount, j + forwardCount };
				moves.win = move;
				break;
			}
			freeForward[0] = true;
			if (j + forwardCount + 1 < n && i + forwardCount + 1 < n
					&& globalBoard[i + forwardCount + 1][j + forwardCount + 1] == MNKCellState.FREE)
				freeForward[1] = true;
			if (full) {
				int[] forwardMove = { i + forwardCount, j + forwardCount, length + forwardExtra };
				q.add(forwardMove);
			}
		}
		if (length == k - 2 && freeBack[0] && freeForward[0] && (freeBack[1] || freeForward[1])) {
			i = freeForward[1] ? i - backCount : i + forwardCount;
			j = freeBack[1] ? j - backCount : j + forwardCount;
			int move[] = { i, j };
			moves.twoWin = move;
		}
		length = 1; backExtra = 0; forwardExtra = 0; 
		freeBack[0] = false; freeBack[1] = false; freeForward[0] = false; freeForward[1] = false;

		// Antidiagonal
		for (backCount = 1; j + backCount < n && i - backCount >= 0
				&& globalBoard[i - backCount][j + backCount] == player; backCount++)
			length++;
		for (forwardCount = 1; j - forwardCount >= 0 && i + forwardCount < n
				&& globalBoard[i + forwardCount][j - forwardCount] == player; forwardCount++)
			length++;
		if (j + backCount < n && i - backCount >= 0 && globalBoard[i - backCount][j + backCount] == MNKCellState.FREE) {
			if (length == k - 1) {
				int move[] = { i - backCount, j + backCount };
				moves.win = move;
				break;
			}
			int rest = k - length - 1;
			for (int c = 1; j + backCount + c < n && i - backCount - c >= 0
					&& globalBoard[i - backCount - c][j + backCount + c] == player && c <= rest; c++)
				backExtra++;
			if (length + backExtra == k - 1) {
				int move[] = { i - backCount, j + backCount };
				moves.win = move;
				break;
			}
			freeBack[0] = true;
			if (j + backCount + 1 >= 0 && i - backCount - 1 >= 0
					&& globalBoard[i - backCount - 1][j + backCount - 1] == MNKCellState.FREE)
				freeBack[1] = true;
			if (full) {
				int[] backMove = { i - backCount, j + backCount, length + backExtra };
				q.add(backMove);
			}
		}
		if (j - forwardCount >= 0 && i + forwardCount < n
				&& globalBoard[i + forwardCount][j - forwardCount] == MNKCellState.FREE) {
			if (length == k - 1) {
				int move[] = { i - forwardCount, j + forwardCount };
				moves.win = move;
				break;
			}
			int rest = k - length - 1;
			for (int c = 1; j - forwardCount - c >= 0 && i + forwardCount + c < n
					&& globalBoard[i + forwardCount + c][j - forwardCount - c] == player && c <= rest; c++)
				forwardExtra++;
			if (length + forwardExtra == k - 1) {
				int move[] = { i - forwardCount, j + forwardCount };
				moves.win = move;
				break;
			}
			freeForward[0] = true;
			if (j - forwardCount - 1 < n && i + forwardCount + 1 < n
					&& globalBoard[i + forwardCount + 1][j - forwardCount - 1] == MNKCellState.FREE)
				freeForward[1] = true;
			if (full) {
				int[] forwardMove = { i + forwardCount, j - forwardCount, length + forwardExtra };
				q.add(forwardMove);
			}
		}
		if (length == k - 2 && freeBack[0] && freeForward[0] && (freeBack[1] || freeForward[1])) {
			i = freeBack[1] ? i - backCount : i + forwardCount;
			j = freeBack[1] ? j + backCount : j - forwardCount;
			int move[] = { i, j };
			moves.twoWin = move;
		}
		if (full)
			moves.q = q;
	}

	private void printMatrix(MNKCellState[][] matrix) {
		Arrays.stream(matrix).forEach((row) -> {
			System.out.print("[");
			Arrays.stream(row).forEach((el) -> System.out.print(" " + el + " "));
			System.out.println("]");
		});
	}
}
