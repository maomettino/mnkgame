package mnkgame;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.PriorityQueue;
import java.util.Set;
import java.util.Stack;

import mnkgame.MNKCell;
import mnkgame.MNKCellState;
import mnkgame.Node;

public class AlphaBetaPruning {
	private final int m, n, k;
	private final int MAX_DEPTH = 3;
	private final int WIN = 5000;
	private final int DEFEAT = -5000;
	private final int ALPHA = -10000;
	private final int BETA = 10000;
	private final int TIMEOUT;
	private int currentDepth;
	private MNKCellState saddam;
	private MNKCellState foe;
	private MNKCellState[][] b;
	private boolean timeouted;
	Set<MNKCell> saddamAdjacentCells;
	Set<MNKCell> foeAdjacentCells;
	Set<MNKCell> saddamAdjacentCellsCopy;
	Set<MNKCell> foeAdjacentCellsCopy;
	int[][] direction = new int[][] { { -1, -1 }, { -1, 0 }, { -1, 1 }, { 0, 1 }, { 1, 1 }, { 1, 0 }, { 1, -1 },
			{ 0, -1 } };

	public AlphaBetaPruning(int m, int n, int k, boolean first, int timeout_in_secs) {
		currentDepth = -1;
		TIMEOUT = timeout_in_secs;
		this.m = m;
		this.n = n;
		this.k = k;
		saddam = first ? MNKCellState.P1 : MNKCellState.P2;
		foe = first ? MNKCellState.P2 : MNKCellState.P1;
		b = new MNKCellState[m][n];
		for (int i = 0; i < m; i++)
			for (int j = 0; j < n; j++)
				b[i][j] = MNKCellState.FREE;
		timeouted = false;
		saddamAdjacentCells = new HashSet<MNKCell>();
		foeAdjacentCells = new HashSet<MNKCell>();
		saddamAdjacentCellsCopy = new HashSet<MNKCell>();
		foeAdjacentCellsCopy = new HashSet<MNKCell>();
	}

	public void signFoeMove(MNKCell foeCell) {
		b[foeCell.i][foeCell.j] = foe;
		updateAdjacentCells(foeAdjacentCells, foeCell);
	}

	public MNKCell getMove(MNKCell saddamLastCell, MNKCell foeLastCell, MNKCellState[][] board) {
		currentDepth = -1;
		saddamLastCell = new MNKCell(saddamLastCell.i, saddamLastCell.j);
		foeLastCell = new MNKCell(foeLastCell.i, foeLastCell.j);
		updateAdjacentCells(saddamAdjacentCellsCopy, saddamLastCell, foeLastCell);
		updateAdjacentCells(foeAdjacentCellsCopy, foeLastCell, saddamLastCell);
		if (!timeouted) {
			b[saddamLastCell.i][saddamLastCell.j] = saddam;
			b[foeLastCell.i][foeLastCell.j] = foe;
			updateAdjacentCells(saddamAdjacentCells, saddamLastCell, foeLastCell);
			updateAdjacentCells(foeAdjacentCells, foeLastCell, saddamLastCell);
		} else {
			for (int i = 0; i < m; i++)
				for (int j = 0; j < n; j++)
					b[i][j] = board[i][j];
			timeouted = false;
			saddamAdjacentCells.clear();
			saddamAdjacentCells.addAll(saddamAdjacentCellsCopy);
			foeAdjacentCells.clear();
			foeAdjacentCells.addAll(foeAdjacentCellsCopy);
		}
		Node father = new Node(foeLastCell.i, foeLastCell.j, ALPHA, BETA, ALPHA, false, true);
		Node node = alphaBetaPruning(father);
		if (node.bestChild != null) {
			System.out.println("cell selected by beta-pruning: " + node.bestChild.i + " " + node.bestChild.j
					+ " with regularity " + node.bestChild.regular);
			return new MNKCell(node.bestChild.i, node.bestChild.j);
		}
		System.out.println("no cell was found by beta-pruning: ");
		return new MNKCell(-1, -1);
	}

	private Node alphaBetaPruning(Node father) {
		currentDepth++;
		System.out.println("current father " + father.i + " " + father.j + " at depth " + currentDepth);
		boolean removed = false;
		Set<MNKCell> saveState = new HashSet<MNKCell>();
		saveState.addAll(father.isSaddam ? saddamAdjacentCells : foeAdjacentCells);
		updateAdjacentCells((father.isSaddam ? saddamAdjacentCells : foeAdjacentCells),
				new MNKCell(father.i, father.j));
		if ((father.isSaddam ? foeAdjacentCells : saddamAdjacentCells).remove(new MNKCell(father.i, father.j))) {
			removed = true;
		}

		Node[] children = getNodes(father);
		if (children.length == 0) {
			System.out.println("i: " + father.i + " j: " + father.j + " has no children");
			return new Node(father.i, father.j, father.isSaddam, 0);
		}

		System.out.println("children:");
		for (Node child : children) {
			System.out.println("i: " + child.i + " j: " + child.j);
		}
		int foeValue = 0;
		if (currentDepth == MAX_DEPTH - 1) {
			foeValue = getHeuristicValue(father.i, father.j, father.isSaddam);
		}
		for (Node c : children) {
			Node child;
			if (isWinningCell(c.i, c.j, c.isSaddam ? saddam : foe)) {
				child = new Node(c.i, c.j, c.isSaddam, c.isSaddam ? WIN : DEFEAT);
				System.out.println("the child " + child.i + " " + child.j + " is winning for "
						+ (child.isSaddam ? "saddam" : "foe"));
			} else {

				if (currentDepth == MAX_DEPTH - 1) {
					child = getHeuristicLeaf(c.i, c.j, c.isSaddam, foeValue);
					System.out.println("the child " + child.i + " " + child.j + " is just a leaf ");
				} else {
					b[c.i][c.j] = c.isSaddam ? saddam : foe;
					child = alphaBetaPruning(c);
					b[c.i][c.j] = MNKCellState.FREE;
				}
			}
			if (child.isSaddam) {
				if (currentDepth == 0 && child.value > father.value) {
					father.bestChild = child;
				}
				father.value = Math.max(father.value, child.value);
				father.alpha = Math.max(father.alpha, father.value);
			} else {
				father.value = Math.min(father.value, child.value);
				father.beta = Math.min(father.beta, father.value);
			}
			if (father.beta <= father.alpha) {
				System.out.println("cut off");
				break;
			}
			if ((child.isSaddam && child.value == WIN) || (!child.isSaddam && child.value == DEFEAT)) {
				System.out.println(
						"since this move is winning for the current player we look no further in the current sub-tree");
				break;
			}
		}

		if (father.isSaddam) {
			saddamAdjacentCells = saveState;
		}

		else {
			foeAdjacentCells = saveState;
		}
		if (removed) {
			(father.isSaddam ? foeAdjacentCells : saddamAdjacentCells).add(new MNKCell(father.i, father.j));
		}

		currentDepth--;
		return father;
	}

	private Node[] getNodes(Node father) {
		Set<MNKCell> set = new HashSet<MNKCell>();
		set.addAll(saddamAdjacentCells);
		set.addAll(foeAdjacentCells);
		Node[] children = new Node[set.size()];
		int i = 0;
		if (currentDepth == MAX_DEPTH - 1) {
			int otherPlayerValue = getHeuristicValue(father.i, father.j, father.isSaddam);
			for (MNKCell cell : set) {
				children[i] = getHeuristicLeaf(cell.i, cell.j, !father.isSaddam, otherPlayerValue);
				i++;
			}
		} else {
			for (MNKCell cell : set) {
				children[i] = new Node(cell.i, cell.j, father.alpha, father.beta, father.isSaddam ? ALPHA : BETA,
						!father.isSaddam, true);
				i++;
			}
		}
		return children;
	}

	private void updateAdjacentCells(Set<MNKCell> set, MNKCell myCell, MNKCell foeCell) {
			set.addAll(getCellAdjacents(myCell));
			set.remove(myCell);
			set.remove(foeCell);
	}

	private void updateAdjacentCells(Set<MNKCell> set, MNKCell myCell) {
			set.addAll(getCellAdjacents(myCell));
			set.remove(myCell);
	}

	private boolean isWinningCell(int i, int j, MNKCellState s) {
		int n;
		// Horizontal check
		n = 1;
		// backward check
		for (int k = 1; j - k >= 0 && b[i][j - k] == s; k++) {
			// System.out.println("checking cell i: " +i+" j: " +(j-k));
			n++;
		}
		for (int k = 1; j + k < this.n && b[i][j + k] == s; k++)
			n++; 
		// forward check
		if (n >= this.k)
			return true;

		// Vertical check
		n = 1;
		for (int k = 1; i - k >= 0 && b[i - k][j] == s; k++)
			n++; // backward check
		for (int k = 1; i + k < m && b[i + k][j] == s; k++)
			n++; // forward check
		if (n >= this.k)
			return true;

		// Diagonal check
		n = 1;
		for (int k = 1; i - k >= 0 && j - k >= 0 && b[i - k][j - k] == s; k++)
			n++; // backward check
		for (int k = 1; i + k < m && j + k < this.n && b[i + k][j + k] == s; k++)
			n++; // forward check
		if (n >= this.k)
			return true;

		// Anti-diagonal check
		n = 1;
		for (int k = 1; i - k >= 0 && j + k < this.n && b[i - k][j + k] == s; k++)
			n++; // backward check
		for (int k = 1; i + k < m && j - k >= 0 && b[i + k][j - k] == s; k++)
			n++; // backward check
		if (n >= this.k)
			return true;

		return false;
	}

	private Set<MNKCell> getCellAdjacents(MNKCell cell) {
		Set<MNKCell> set = new HashSet<MNKCell>();
		for (int[] d : direction) {
			int i = cell.i + d[0];
			int j = cell.j + d[1];
			if (i < n && j < n && i >= 0 && j >= 0 && b[i][j] == MNKCellState.FREE) {
				set.add(new MNKCell(i, j));
			}
		}
		return set;
	}

	private Node getHeuristicLeaf(int i, int j, boolean isSaddam, int foeValue) {
		int myValue = getHeuristicValue(i, j, isSaddam);
		Node node = new Node(i, j, isSaddam, myValue - foeValue);
		return node;
	}

	private int getHeuristicValue(int i, int j, boolean isSaddam) {
		MNKCellState player = isSaddam ? saddam : foe;
		int lengthH = 1, lengthV = 1, lengthD = 1, lengthAD = 1;
		int extraH = 0, extraV = 0, extraD = 0, extraAD = 0;
		int backCount, forwardCount;
		// Horizontal
		for (backCount = 1; j - backCount >= 0 && b[i][j - backCount] == player; backCount++)
			lengthH++;
		for (forwardCount = 1; j + forwardCount < n && b[i][j + forwardCount] == player; forwardCount++)
			lengthH++;
		for (; j - backCount >= 0 && b[i][j - backCount] == MNKCellState.FREE && lengthH + extraH < k; backCount++)
			extraH++;
		for (; j + forwardCount < n && b[i][j + forwardCount] == MNKCellState.FREE
				&& lengthH + extraH < k; forwardCount++)
			extraH++;

		// Vertical
		for (backCount = 1; i - backCount >= 0 && b[i - backCount][j] == player; backCount++)
			lengthV++;

		for (forwardCount = 1; i + forwardCount < n && b[i + forwardCount][j] == player; forwardCount++)
			lengthV++;

		for (; i - backCount >= 0 && b[i - backCount][j] == MNKCellState.FREE && lengthV + extraV < k; backCount++)
			extraV++;
		for (; i + forwardCount < n && b[i + forwardCount][j] == MNKCellState.FREE
				&& lengthV + extraV < k; forwardCount++)
			extraV++;

		// Diagonal
		for (backCount = 1; j - backCount >= 0 && i - backCount >= 0
				&& b[i - backCount][j - backCount] == player; backCount++)
			lengthD++;

		for (forwardCount = 1; j + forwardCount < n && i + forwardCount < n
				&& b[i + forwardCount][j + forwardCount] == player; forwardCount++)
			lengthD++;

		for (; j - backCount >= 0 && i - backCount >= 0
				&& b[i - backCount][j - backCount] == MNKCellState.FREE && lengthD + extraD < k; backCount++)
			extraD++;
		for (; j + forwardCount < n && i + forwardCount < n
				&& b[i + forwardCount][j + forwardCount] == MNKCellState.FREE
				&& lengthD + extraD < k; forwardCount++)
			extraD++;

		// Antidiagonal
		for (backCount = 1; j + backCount < n && i - backCount >= 0
				&& b[i - backCount][j + backCount] == player; backCount++)
			lengthAD++;

		for (forwardCount = 1; j - forwardCount >= 0 && i + forwardCount < n
				&& b[i + forwardCount][j - forwardCount] == player; forwardCount++)
			lengthAD++;
		for (; j + backCount < n && i - backCount >= 0
				&& b[i - backCount][j + backCount] == MNKCellState.FREE && lengthAD + extraAD < k; backCount++)
			extraAD++;
		for (; j - forwardCount >= 0 && i + forwardCount < n
				&& b[i + forwardCount][j - forwardCount] == MNKCellState.FREE
				&& lengthAD + extraAD < k; forwardCount++)
			extraAD++;

		int value;
		value = ((extraH + lengthH) >= k ? (int) Math.pow(10, lengthH - k + 3) + lengthH : 0) +
				((extraV + lengthV) >= k ? (int) Math.pow(10, lengthV - k + 3) + lengthV : 0) +
				((extraD + lengthD) >= k ? (int) Math.pow(10, lengthD - k + 3) + lengthD : 0) +
				((extraAD + lengthAD) >= k ? (int) Math.pow(10, lengthAD - k + 3) + lengthAD : 0);
		return value;
	}

	private void printMatrix(Set<MNKCell> matrix) {
		matrix.forEach((row) -> {
			System.out.print("[ " + row.i);
			System.out.println(row.j + " ]");
		});
	}

	private void printMatrix(MNKCellState[][] matrix) {
		Arrays.stream(matrix).forEach((row) -> {
			System.out.print("[");
			Arrays.stream(row).forEach((el) -> System.out.print(" " + el + " "));
			System.out.println("]");
		});
	}
	
}
