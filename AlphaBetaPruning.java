package mnkgame;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.PriorityQueue;
import java.util.Stack;

public class AlphaBetaPruning {
	private final int m, n, k;
	private final int MAX_DEPTH = 2;
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
	private int saddamJump;
	private int foeJump;
	private Stack<int[]> saddamHistory;
	private Stack<int[]> foeHistory;

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
		saddamHistory = new Stack<int[]>();
		foeHistory = new Stack<int[]>();
		saddamJump = 0;
		foeJump = 0;
	}

	public void signFoeMove(MNKCell foeCell) {
		b[foeCell.i][foeCell.j] = foe;
		this.foeHistory.push(new int[] { foeCell.i, foeCell.j });
	}

	@SuppressWarnings("unchecked")
	public MNKCell getMove(MNKCell saddamLastCell, MNKCell foeLastCell, MNKCellState[][] board,
			Stack<int[]> saddamHistory, Stack<int[]> foeHistory) {
		currentDepth = -1;
		if (!timeouted) {
			b[saddamLastCell.i][saddamLastCell.j] = saddam;
			b[foeLastCell.i][foeLastCell.j] = foe;
			this.saddamHistory.push(new int[] { saddamLastCell.i, saddamLastCell.j });
			this.foeHistory.push(new int[] { foeLastCell.i, foeLastCell.j });
		} else {
			for (int i = 0; i < m; i++)
				for (int j = 0; j < n; j++)
					b[i][j] = board[i][j];
			this.saddamHistory = (Stack) saddamHistory.clone();
			this.foeHistory = (Stack) foeHistory.clone();
			timeouted = false;
		}

		Node father = new Node(foeLastCell.i, foeLastCell.j, ALPHA, BETA, ALPHA, false, true);
		//System.out.println("prima del beta-pruning: ");
		Node node = alphaBetaPruning(father);
		if (node.bestChild != null) {
			System.out.println("cell selected by beta-pruning: " + node.bestChild.i + " " + node.bestChild.j);
			return new MNKCell(node.bestChild.i, node.bestChild.j);
		}
		System.out.println("no cell was found by beta-pruning: ");
		return new MNKCell(-1, -1);
	}

	private Node alphaBetaPruning(Node father) {
		currentDepth++;
		// System.out.println("depth " + currentDepth);
		System.out.println(
				"examining node i: " + father.i + " j: " + father.j
						+ (father.isLeaf ? ", which is a leaf" : " at depth " + currentDepth));
		Node[] children = findBestNodes(father);
		//System.out.println("dopo find best nodes ");

	/*	printMatrix(b);
						 * System.out.println("children:");
						 * for (Node child : children) {
						 * System.out.println("i: " + child.i + " j: " + child.j);
						 * }
						 */
		int childrenJump = father.isSaddam? foeJump: saddamJump;
		for (int i = 0; i < children.length; i++) {
			Node child;
			//System.out.println("for child i: "+children[i].i +" j: "+children[i].j + " saddamJump " + saddamJump + " foeJump "+ foeJump+" saddamHistory size "+ saddamHistory.size()+ " foeHistory size "+ foeHistory.size() );
			if (currentDepth != MAX_DEPTH - 1 && !children[i].isLeaf) {
				if (children[i].isSaddam) {
					if (children[i].regular)
						saddamJump=0;
					else
						saddamJump++;
					saddamHistory.push(new int[] {children[i].i,children[i].j});
				} else {
					if (children[i].regular)
						foeJump=0;
					else
						foeJump++;
					foeHistory.push(new int[] {children[i].i, children[i].j});
				}
				b[children[i].i][children[i].j] = children[i].isSaddam ? saddam : foe;
				//System.out.println("prima della ricorsione: ");
				child = alphaBetaPruning(children[i]);
				//System.out.println("dopo la ricorsione ");
				b[child.i][child.j] = MNKCellState.FREE;
				if (children[i].isSaddam) {
					saddamJump = childrenJump;
					saddamHistory.pop();
				}
				else {
					foeJump = childrenJump;
					foeHistory.pop();
				}
					
			} else
				child = children[i];
			if (children[i].isSaddam) {
				/*
				 * if (currentDepth == 0) {
				 * System.out.println("il valore del figlio aggiornato " + child.value);
				 * System.out.println("il valore del padre " + father.value);
				 * }
				 */
				if (currentDepth == 0 && child.value > father.value) {
					// relative best child, in the end it will be the best
					// System.out.println("you are my favourite nigger");
					father.bestChild = child;
				}
				// System.out.println("depth " + currentDepth);
				// System.out.println("il valore del figlio pre-confronto max " + child.value);
				// System.out.println("il valore del padre pre-confronto max " + father.value);
				father.value = Math.max(father.value, child.value);
				father.alpha = Math.max(father.alpha, father.value);
				// System.out.println("il valore del padre post-confronto max " + father.value);
			} else {
				// System.out.println("depth " + currentDepth);
				// System.out.println("il valore del figlio pre-confronto min " + child.value);
				// System.out.println("il valore del padre pre-confronto min " + father.value);
				father.value = Math.min(father.value, child.value);
				father.beta = Math.min(father.beta, father.value);
				// System.out.println("il valore del padre post-confronto min " + father.value);
			}
			if (father.beta <= father.alpha) {
				System.out.println("cut off");
				break;
			}
		}
		currentDepth--;
		return father;
	}

	private Node[] findBestNodes(Node father) {
		//System.out.println("for father i: "+father.i +" j: "+father.j + " saddamJump " + saddamJump + " foeJump "+ foeJump+" saddamHistory size "+ saddamHistory.size()+ " foeHistory size "+ foeHistory.size() );
		Node[] children;
		Moves myMoves = new Moves();
		int[] myAroundCell = !father.isSaddam ? (saddamHistory.get(saddamHistory.size() - 1 - saddamJump) )
				: (foeHistory.get(foeHistory.size() - 1 - foeJump));
		int[] foeAroundCell = !father.isSaddam ? (foeHistory.get(foeHistory.size() - 1 - foeJump))
				: (saddamHistory.get(saddamHistory.size() - 1 - saddamJump));
		System.out.println("checking " + (!father.isSaddam ? "saddam" : "foe") + " around with pivot i: "
				+ myAroundCell[0] + " j: " + myAroundCell[1]);
		checkAround(myAroundCell[0], myAroundCell[1], myMoves, true);
		if (myMoves.win == null) {
			Moves foeMoves = new Moves();
			System.out.println("checking " + (!father.isSaddam ? "foe" : "saddam") + " around with pivot i: "
					+ foeAroundCell[0] + " j: " + foeAroundCell[1]);
			checkAround(foeAroundCell[0], foeAroundCell[1], foeMoves, false);
			if (foeMoves.win == null) {
				if (myMoves.twoWin == null) {
					if (foeMoves.twoWin == null) {
						children = new Node[myMoves.q.size()];
						if (currentDepth == MAX_DEPTH - 1) {
							int foeValue = getHeuristicValue(father.i, father.j,
									father.isSaddam);
							for (int i = 0; !myMoves.q.isEmpty(); i++) {
								int[] m = myMoves.q.remove();
								Node child;
								child = getHeuristicLeaf(m[0], m[1], !father.isSaddam, foeValue);
								children[i] = child;
							}
						} else {
							for (int i = 0; !myMoves.q.isEmpty(); i++) {
								int[] m = myMoves.q.remove();
								Node child;
								child = new Node(m[0], m[1], father.alpha, father.beta, father.isSaddam ? ALPHA : BETA,
										!father.isSaddam, true);
								children[i] = child;
							}
						}
					} else {
						if (currentDepth == MAX_DEPTH - 1) {
							children = new Node[] { getHeuristicLeaf(foeMoves.twoWin[0], foeMoves.twoWin[1],
									!father.isSaddam, getHeuristicValue(father.i,
											father.j, father.isSaddam)) };
						} else {
							children = new Node[] {
									new Node(foeMoves.twoWin[0], foeMoves.twoWin[1], father.alpha, father.beta,
											father.isSaddam ? ALPHA : BETA, !father.isSaddam, checkRegularity(myMoves.q,foeMoves.twoWin)) };
						}
					}
				} else {
					children = new Node[] { new Node(myMoves.twoWin[0], myMoves.twoWin[1], !father.isSaddam,
							!father.isSaddam ? WIN : DEFEAT) };
				}

			} else {
				if (currentDepth == MAX_DEPTH - 1) {
					children = new Node[] {
							getHeuristicLeaf(foeMoves.win[0], foeMoves.win[1], !father.isSaddam, getHeuristicValue(
									father.i, father.j, father.isSaddam)) };
				} else {
					children = new Node[] { new Node(foeMoves.win[0], foeMoves.win[1], father.alpha, father.beta,
							father.isSaddam ? ALPHA : BETA, !father.isSaddam,checkRegularity(myMoves.q,foeMoves.win)) };
				}

			}

		} else
			children = new Node[] {
					new Node(myMoves.win[0], myMoves.win[1], !father.isSaddam, !father.isSaddam ? WIN : DEFEAT) };
		return children;
	}

	private void checkAround(int i, int j, Moves moves, boolean full) {
		// System.out.println("checking around of i: " + i + " j: " + j);
		PriorityQueue<int[]> q;
		q = new PriorityQueue<int[]>(8, new Comparatore());
		MNKCellState player = b[i][j];
		int length = 1, backExtra = 0, forwardExtra = 0, backCount, forwardCount;
		boolean freeBack[] = { false, false }, freeForward[] = { false, false }, freeExtraForward = false,
				freeExtraBack = false;
		// System.out.println("Starting check around for depth " + currentDepth);

		// Horizontal
		for (backCount = 1; j - backCount >= 0 && b[i][j - backCount] == player; backCount++)
			length++;
		for (forwardCount = 1; j + forwardCount < n && b[i][j + forwardCount] == player; forwardCount++)
			length++;
		if (j - backCount >= 0 && b[i][j - backCount] == MNKCellState.FREE) {
			// System.out.println("checking free back H cell " + i + " " + (j - backCount));
			if (length == k - 1) {
				// System.out.println("winning back H move " + i + " " + (j - backCount));
				int move[] = { i, j - backCount };
				moves.win = move;
				return;
			}
			int rest = k - length - 1;
			for (int c = 1; j - backCount - c >= 0 && b[i][j - backCount - c] == player && c <= rest; c++)
				backExtra++;
			if (length + backExtra == k - 1) {
				// System.out.println("winning back H move " + i + " " + (j - backCount));
				int move[] = { i, j - backCount };
				moves.win = move;
				return;
			}
			freeBack[0] = true;
			if (j - backCount - 1 >= 0 && b[i][j - backCount - 1] == MNKCellState.FREE)
				freeBack[1] = true;
			if (j - backCount - backExtra - 1 >= 0 && b[i][j - backCount - backExtra - 1] == MNKCellState.FREE)
				freeExtraBack = true;
			if (full) {
				int[] backMove = { i, j - backCount, length + backExtra };
				// System.out.println("adding to the queue the back H move " + backMove[0] + " "
				// + backMove[1]);
				q.add(backMove);
			}
		}
		if (j + forwardCount < n && b[i][j + forwardCount] == MNKCellState.FREE) {
			// System.out.println("checking free forward H cell " + i + " " + (j +
			// forwardCount));
			if (length == k - 1) {
				// System.out.println("winning H forward move " + i + " " + (j + forwardCount));
				int move[] = { i, j + forwardCount };
				moves.win = move;
				return;
			}
			int rest = k - length - 1;

			for (int c = 1; j + forwardCount + c < n && b[i][j + forwardCount + c] == player
					&& c <= rest; c++)
				forwardExtra++;
			if (length + forwardExtra == k - 1) {
				// System.out.println("winning forward H move " + i + " " + (j + forwardCount));
				int move[] = { i, j + forwardCount };
				moves.win = move;
				return;
			}
			freeForward[0] = true;
			if (j + forwardCount + 1 < n && b[i][j + forwardCount + 1] == MNKCellState.FREE)
				freeForward[1] = true;
			if (j + forwardCount + forwardExtra + 1 < n
					&& b[i][j + forwardCount + forwardExtra + 1] == MNKCellState.FREE)
				freeExtraForward = true;
			if (full) {
				int[] forwardMove = { i, j + forwardCount, length + forwardExtra };
				q.add(forwardMove);
				// System.out.println("adding to queue move forward H " + forwardMove[0] + " " +
				// forwardMove[1]);

			}
		}
		if (length == k - 2) {
			if (freeBack[0] && freeForward[0] && (freeBack[1] || freeForward[1])) {
				j = freeBack[1] ? j - backCount : j + forwardCount;
				int move[] = { i, j };
				// System.out.println("found 2 win H cell : " + i + " " + j);
				moves.twoWin = move;
			}
		} else {
			if (freeBack[0] && length + backExtra == k - 2 && freeForward[0] && freeExtraBack) {
				// System.out.println("found 2 win back H move " + i + " " + (j - backCount));
				int move[] = { i, j - backCount };
				moves.twoWin = move;
				return;
			}
			if (freeForward[0] && length + forwardExtra == k - 2 && freeBack[0] && freeExtraForward) {
				// System.out.println("found 2 win forward H move " + i + " " + (j +
				// forwardCount));
				int move[] = { i, j + forwardCount };
				moves.twoWin = move;
				return;
			}
		}
		length = 1;
		backExtra = 0;
		forwardExtra = 0;
		freeBack[0] = false;
		freeBack[1] = false;
		freeForward[0] = false;
		freeForward[1] = false;
		freeExtraForward = false;
		freeExtraBack = false;

		// Vertical
		for (backCount = 1; i - backCount >= 0 && b[i - backCount][j] == player; backCount++)
			length++;
		for (forwardCount = 1; i + forwardCount < n && b[i + forwardCount][j] == player; forwardCount++)
			length++;
		if (i - backCount >= 0 && b[i - backCount][j] == MNKCellState.FREE) {
			// System.out.println("checking free V back cell " + (i - backCount) + " " + j);
			if (length == k - 1) {
				// System.out.println("found winning V back cell " + (i - backCount) + " " + j);
				int move[] = { i - backCount, j };
				moves.win = move;
				return;
			}
			int rest = k - length - 1;
			for (int c = 1; i - backCount - c >= 0 && b[i - backCount - c][j] == player && c <= rest; c++)
				backExtra++;
			if (length + backExtra == k - 1) {
				// System.out.println("found winning V back cell " + (i - backCount) + " " + j);
				int move[] = { i - backCount, j };
				moves.win = move;
				return;
			}
			freeBack[0] = true;
			if (i - backCount - 1 >= 0 && b[i - backCount - 1][j] == MNKCellState.FREE)
				freeBack[1] = true;
			if (i - backCount - backExtra - 1 >= 0 && b[i - backCount - backExtra - 1][j] == MNKCellState.FREE)
				freeExtraBack = true;
			if (full) {
				// System.out.println("adding to queue V back cell " + (i - backCount) + " " +
				// j);
				int[] backMove = { i - backCount, j, length + backExtra };
				q.add(backMove);
			}
		}
		if (i + forwardCount < n && b[i + forwardCount][j] == MNKCellState.FREE) {
			// System.out.println("checking free V forward cell " + (i + forwardCount) + " "
			// + j);
			if (length == k - 1) {
				// System.out.println("found winning V forward cell " + (i + forwardCount) + " "
				// + j);
				int move[] = { i + forwardCount, j };
				moves.win = move;
				return;
			}
			int rest = k - length - 1;
			for (int c = 1; i + forwardCount + c < n && b[i + forwardCount + c][j] == player
					&& c <= rest; c++)
				forwardExtra++;
			if (length + forwardExtra == k - 1) {
				// System.out.println("found winning V forward cell " + (i + forwardCount) + " "
				// + j);
				int move[] = { i + forwardCount, j };
				moves.win = move;
				return;
			}
			freeForward[0] = true;
			if (i + forwardCount + 1 < n && b[i + forwardCount + 1][j] == MNKCellState.FREE)
				freeForward[1] = true;
			if (i + forwardCount + forwardExtra + 1 < n
					&& b[i + forwardCount + forwardExtra + 1][j] == MNKCellState.FREE)
				freeExtraForward = true;
			if (full) {
				// System.out.println("adding to queue V forward cell " + (i + forwardCount) + "
				// " + j);
				int[] forwardMove = { i + forwardCount, j, length + forwardExtra };
				q.add(forwardMove);
			}
		}
		if (length == k - 2) {
			if (freeBack[0] && freeForward[0] && (freeBack[1] || freeForward[1])) {
				i = freeBack[1] ? i - backCount : i + forwardCount;
				int move[] = { i, j };
				// System.out.println("found 2 winning V cell " + i + " " + j);
				moves.twoWin = move;
			}
		} else {
			if (freeBack[0] && length + backExtra == k - 2 && freeForward[0] && freeExtraBack) {
				int move[] = { i - backCount, j };
				// System.out.println("found 2 winning back Vcell " + (i - backCount) + " " +
				// j);
				moves.twoWin = move;
				return;
			}
			if (freeForward[0] && length + forwardExtra == k - 2 && freeBack[0] && freeExtraForward) {
				int move[] = { i + forwardCount, j };
				// System.out.println("found 2 winning forward Vcell " + (i + forwardCount) + "
				// " + j);
				moves.twoWin = move;
				return;
			}
		}
		length = 1;
		backExtra = 0;
		forwardExtra = 0;
		freeBack[0] = false;
		freeBack[1] = false;
		freeForward[0] = false;
		freeForward[1] = false;
		freeExtraForward = false;
		freeExtraBack = false;

		// Diagonal
		for (backCount = 1; j - backCount >= 0 && i - backCount >= 0
				&& b[i - backCount][j - backCount] == player; backCount++)
			length++;
		for (forwardCount = 1; j + forwardCount < n && i + forwardCount < n
				&& b[i + forwardCount][j + forwardCount] == player; forwardCount++)
			length++;
		if (j - backCount >= 0 && i - backCount >= 0
				&& b[i - backCount][j - backCount] == MNKCellState.FREE) {
			// System.out.println("checking free D back cell " + (i - backCount) + " " + (j
			// - backCount));
			if (length == k - 1) {
				// System.out.println("found winning D back cell " + (i - backCount) + " " + (j
				// - backCount));
				int move[] = { i - backCount, j - backCount };
				moves.win = move;
				return;
			}
			int rest = k - length - 1;
			for (int c = 1; j - backCount - c >= 0 && i - backCount - c >= 0
					&& b[i - backCount - c][j - backCount - c] == player && c <= rest; c++)
				backExtra++;
			if (length + backExtra == k - 1) {
				// System.out.println("found winning D back cell " + (i - backCount) + " " + (j
				// - backCount));
				int move[] = { i - backCount, j - backCount };
				moves.win = move;
				return;
			}
			freeBack[0] = true;
			if (j - backCount - 1 >= 0 && i - backCount - 1 >= 0
					&& b[i - backCount - 1][j - backCount - 1] == MNKCellState.FREE)
				freeBack[1] = true;
			if (j - backCount - backExtra - 1 >= 0 && i - backCount - backExtra - 1 >= 0
					&& b[i - backCount - backExtra - 1][j - backCount - backExtra - 1] == MNKCellState.FREE)
				freeExtraBack = true;
			if (full) {
				// System.out.println("adding to queue D back cell " + (i - backCount) + " " +
				// (j - backCount));
				int[] backMove = { i - backCount, j - backCount, length + backExtra };
				q.add(backMove);
			}
		}
		if (j + forwardCount < n && i + forwardCount < n
				&& b[i + forwardCount][j + forwardCount] == MNKCellState.FREE) {
			// System.out.println("checking free D forward cell " + (i + forwardCount) + " "
			// + (j + forwardCount));
			if (length == k - 1) {
				// System.out.println("found winning D forward cell " + (i + forwardCount) + " "
				// + (j + forwardCount));
				int move[] = { i + forwardCount, j + forwardCount };
				moves.win = move;
				return;
			}
			int rest = k - length - 1;
			for (int c = 1; j + forwardCount + c < n && i + forwardCount + c < n
					&& b[i + forwardCount + c][j + forwardCount + c] == player && c <= rest; c++)
				forwardExtra++;
			if (length + forwardExtra == k - 1) {
				// System.out.println("found winning D forward cell " + (i + forwardCount) + " "
				// + (j + forwardCount));
				int move[] = { i + forwardCount, j + forwardCount };
				moves.win = move;
				return;
			}
			freeForward[0] = true;
			if (j + forwardCount + 1 < n && i + forwardCount + 1 < n
					&& b[i + forwardCount + 1][j + forwardCount + 1] == MNKCellState.FREE)
				freeForward[1] = true;
			if (j + forwardCount + forwardExtra + 1 < n && i + forwardCount + forwardExtra + 1 < n
					&& b[i + forwardCount + forwardExtra + 1][j + forwardCount + forwardExtra + 1] == MNKCellState.FREE)
				freeExtraForward = true;
			if (full) {
				// System.out.println("adding to queue D forward cell " + (i + forwardCount) + "
				// " + (j + forwardCount));
				int[] forwardMove = { i + forwardCount, j + forwardCount, length + forwardExtra };
				q.add(forwardMove);
			}
		}
		if (length == k - 2) {
			if (freeBack[0] && freeForward[0] && (freeBack[1] || freeForward[1])) {
				i = freeBack[1] ? i - backCount : i + forwardCount;
				j = freeBack[1] ? j - backCount : j + forwardCount;
				int move[] = { i, j };
				// System.out.println("found 2 win D cell " + i + " " + j);
				moves.twoWin = move;
			}
		} else {
			if (freeBack[0] && length + backExtra == k - 2 && freeForward[0] && freeExtraBack) {
				int move[] = { i - backCount, j - backCount };
				moves.twoWin = move;
				// System.out.println("found 2 win back D cell " + (i - backCount) + " " + (j -
				// backCount));
				return;
			}
			if (freeForward[0] && length + forwardExtra == k - 2 && freeBack[0] && freeExtraForward) {
				int move[] = { i + forwardCount, j + forwardCount };
				moves.twoWin = move;
				// System.out.println("found 2 win forward D cell " + (i + forwardCount) + " " +
				// (j + forwardCount));
				return;
			}
		}
		length = 1;
		backExtra = 0;
		forwardExtra = 0;
		freeBack[0] = false;
		freeBack[1] = false;
		freeForward[0] = false;
		freeForward[1] = false;
		freeExtraForward = false;
		freeExtraBack = false;

		// Antidiagonal
		for (backCount = 1; j + backCount < n && i - backCount >= 0
				&& b[i - backCount][j + backCount] == player; backCount++)
			length++;
		for (forwardCount = 1; j - forwardCount >= 0 && i + forwardCount < n
				&& b[i + forwardCount][j - forwardCount] == player; forwardCount++)
			length++;

		if (j + backCount < n && i - backCount >= 0 && b[i - backCount][j + backCount] == MNKCellState.FREE) {
			// System.out.println("checking free back AD cell " + (i - backCount) + " " + (j
			// + backCount));
			if (length == k - 1) {
				// System.out.println("found winning back AD cell " + (i - backCount) + " " + (j
				// + backCount));
				int move[] = { i - backCount, j + backCount };
				moves.win = move;
				return;
			}
			int rest = k - length - 1;
			for (int c = 1; j + backCount + c < n && i - backCount - c >= 0
					&& b[i - backCount - c][j + backCount + c] == player && c <= rest; c++)
				backExtra++;
			if (length + backExtra == k - 1) {
				// System.out.println("found winning back AD cell " + (i - backCount) + " " + (j
				// + backCount));
				int move[] = { i - backCount, j + backCount };
				moves.win = move;
				return;
			}
			freeBack[0] = true;
			if (j + backCount + 1 >= 0 && i - backCount - 1 >= 0
					&& b[i - backCount - 1][j + backCount - 1] == MNKCellState.FREE)
				freeBack[1] = true;
			if (i - backCount - backExtra - 1 >= 0 && j + backCount + backExtra + 1 < n
					&& b[i - backCount - backExtra - 1][j + backCount + backExtra + 1] == MNKCellState.FREE)
				freeExtraBack = true;
			if (full) {
				int[] backMove = { i - backCount, j + backCount, length + backExtra };
				// System.out.println("adding to queue back AD cell " + (i - backCount) + " " +
				// (j + backCount));
				q.add(backMove);
			}
		}

		if (j - forwardCount >= 0 && i + forwardCount < n
				&& b[i + forwardCount][j - forwardCount] == MNKCellState.FREE) {
			// System.out.println("checking forward AD cell " + (i + forwardCount) +" " + (j
			// - forwardCount));
			if (length == k - 1) {
				// System.out.println("found winning forward AD cell " + (i + forwardCount) + "
				// " + (j - forwardCount));
				int move[] = { i + forwardCount, j - forwardCount };
				moves.win = move;
				return;
			}

			int rest = k - length - 1;
			for (int c = 1; j - forwardCount - c >= 0 && i + forwardCount + c < n
					&& b[i + forwardCount + c][j - forwardCount - c] == player && c <= rest; c++)
				forwardExtra++;

			if (length + forwardExtra == k - 1) {
				// System.out.println("found winning forward AD cell " + (i + forwardCount) + "
				// " + (j - forwardCount));
				int move[] = { i + forwardCount, j - forwardCount };
				moves.win = move;
				return;
			}
			freeForward[0] = true;
			if (j - forwardCount - 1 >= 0 && i + forwardCount + 1 < n
					&& b[i + forwardCount + 1][j - forwardCount - 1] == MNKCellState.FREE)
				freeForward[1] = true;
			if (i + forwardCount + forwardExtra + 1 < n && j - forwardCount - forwardExtra - 1 >= 0
					&& b[i + forwardCount + forwardExtra + 1][j - forwardCount - forwardExtra - 1] == MNKCellState.FREE)
				freeExtraForward = true;
			if (full) {
				// System.out.println("adding to queue forward AD cell " + (i + forwardCount) +
				// " " + (j - forwardCount));
				int[] forwardMove = { i + forwardCount, j - forwardCount, length + forwardExtra };
				q.add(forwardMove);
			}
		}

		if (length == k - 2) {
			if (freeBack[0] && freeForward[0] && (freeBack[1] || freeForward[1])) {
				i = freeBack[1] ? i - backCount : i + forwardCount;
				j = freeBack[1] ? j + backCount : j - forwardCount;
				int move[] = { i, j };
				// System.out.println("found 2 winning AD cell " + i + " " + j);
				moves.twoWin = move;
			}
		} else {
			if (freeBack[0] && length + backExtra == k - 2 && freeForward[0] && freeExtraBack) {
				int move[] = { i - backCount, j + backCount };
				// System.out.println("found 2 winning back AD cell " + (i - backCount) + " " +
				// (j + backCount));
				moves.twoWin = move;
				return;
			}
			if (freeForward[0] && length + forwardExtra == k - 2 && freeBack[0] && freeExtraForward) {
				int move[] = { i + forwardCount, j - forwardCount };
				// System.out.println("found 2 winning forwardAD cell " + (i + forwardCount) + "
				// " + (j - forwardCount));
				moves.twoWin = move;
				return;
			}
		}
		if (full)
			moves.q = q;
	}

	private boolean checkRegularity(PriorityQueue<int[]> q, int[] cell) {
		for (int[] m : q) {
			if(m[0]==cell[0] && m[1]==cell[1])
				return true;
		}
		return false;
	}

	private Node getHeuristicLeaf(int i, int j, boolean isSaddam, int foeValue) {
		int myValue = getHeuristicValue(i, j, isSaddam);
		// System.out.println("for the node i: " + i + " j: " + j + ", heuristic value:
		// " + (myValue - foeValue)
		// + " obtained substracting myValue " + myValue + " and foeValue " + foeValue);
		return new Node(i, j, isSaddam, myValue - foeValue);
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

		int value = ((extraH + lengthH) >= k ? (int) Math.pow(10, lengthH - k + 3) + lengthH : 0) +
				((extraV + lengthV) >= k ? (int) Math.pow(10, lengthV - k + 3) + lengthV : 0) +
				((extraD + lengthD) >= k ? (int) Math.pow(10, lengthD - k + 3) + lengthD : 0) +
				((extraAD + lengthAD) >= k ? (int) Math.pow(10, lengthAD - k + 3) + lengthAD : 0);
		/*
		 * System.out.println("for node i: "+i+" j: "+j+":");
		 * System.out.println("lengthH "+lengthH+" extraH: "+extraH);
		 * System.out.println("lengthV "+lengthV+" extraV: "+extraV);
		 * System.out.println("lengthD "+lengthD+" extraD: "+extraD);
		 * System.out.println("lengthAD "+lengthAD+" extraAD: "+extraAD);
		 * System.out.println("H "+(int)Math.pow(10,lengthH-k+3));
		 * System.out.println("V "+(int)Math.pow(10,lengthV-k+3));
		 * System.out.println("D "+(int)Math.pow(10,lengthD-k+3));
		 * System.out.println("AD "+(int)Math.pow(10,lengthAD-k+3));
		 */
		return value;
	}

	private void printMatrix(MNKCellState[][] matrix) {
		Arrays.stream(matrix).forEach((row) -> {
			System.out.print("[");
			Arrays.stream(row).forEach((el) -> System.out.print(" " + el + " "));
			System.out.println("]");
		});
	}

	private void checkAroundLog(int i, int j, Moves moves, boolean full) {
		// System.out.println("checking around of i: " + i + " j: " + j);
		PriorityQueue<int[]> q;
		q = new PriorityQueue<int[]>(8, new Comparatore());
		MNKCellState player = b[i][j];
		int length = 1, backExtra = 0, forwardExtra = 0, backCount, forwardCount;
		boolean freeBack[] = { false, false }, freeForward[] = { false, false }, freeExtraForward = false,
				freeExtraBack = false;
		// System.out.println("Starting check around for depth " + currentDepth);

		// Horizontal
		for (backCount = 1; j - backCount >= 0 && b[i][j - backCount] == player; backCount++)
			length++;
		for (forwardCount = 1; j + forwardCount < n && b[i][j + forwardCount] == player; forwardCount++)
			length++;
		if (j - backCount >= 0 && b[i][j - backCount] == MNKCellState.FREE) {
			// System.out.println("checking free back H cell " + i + " " + (j - backCount));
			if (length == k - 1) {
				// System.out.println("winning back H move " + i + " " + (j - backCount));
				int move[] = { i, j - backCount };
				moves.win = move;
				return;
			}
			int rest = k - length - 1;
			for (int c = 1; j - backCount - c >= 0 && b[i][j - backCount - c] == player && c <= rest; c++)
				backExtra++;
			if (length + backExtra == k - 1) {
				System.out.println("winning back H move " + i + " " + (j - backCount));
				int move[] = { i, j - backCount };
				moves.win = move;
				return;
			}
			freeBack[0] = true;
			if (j - backCount - 1 >= 0 && b[i][j - backCount - 1] == MNKCellState.FREE)
				freeBack[1] = true;
			if (j - backCount - backExtra - 1 >= 0 && b[i][j - backCount - backExtra - 1] == MNKCellState.FREE)
				freeExtraBack = true;
			if (full) {
				int[] backMove = { i, j - backCount, length + backExtra };
				System.out.println("adding to the queue the back H move " + backMove[0] + " "
						+ backMove[1]);
				q.add(backMove);
			}
		}
		if (j + forwardCount < n && b[i][j + forwardCount] == MNKCellState.FREE) {
			// System.out.println("checking free forward H cell " + i + " " + (j +
			// forwardCount));
			if (length == k - 1) {
				System.out.println("winning H forward move " + i + " " + (j + forwardCount));
				int move[] = { i, j + forwardCount };
				moves.win = move;
				return;
			}
			int rest = k - length - 1;

			for (int c = 1; j + forwardCount + c < n && b[i][j + forwardCount + c] == player
					&& c <= rest; c++)
				forwardExtra++;
			if (length + forwardExtra == k - 1) {
				System.out.println("winning forward H move " + i + " " + (j + forwardCount));
				int move[] = { i, j + forwardCount };
				moves.win = move;
				return;
			}
			freeForward[0] = true;
			if (j + forwardCount + 1 < n && b[i][j + forwardCount + 1] == MNKCellState.FREE)
				freeForward[1] = true;
			if (j + forwardCount + forwardExtra + 1 < n
					&& b[i][j + forwardCount + forwardExtra + 1] == MNKCellState.FREE)
				freeExtraForward = true;
			if (full) {
				int[] forwardMove = { i, j + forwardCount, length + forwardExtra };
				q.add(forwardMove);
				System.out.println("adding to queue move forward H " + forwardMove[0] + " " +
						forwardMove[1]);

			}
		}
		if (length == k - 2) {
			if (freeBack[0] && freeForward[0] && (freeBack[1] || freeForward[1])) {
				j = freeBack[1] ? j - backCount : j + forwardCount;
				int move[] = { i, j };
				System.out.println("found 2 win H cell : " + i + " " + j);
				moves.twoWin = move;
			}
		} else {
			if (freeBack[0] && length + backExtra == k - 2 && freeForward[0] && freeExtraBack) {
				System.out.println("found 2 win back H move " + i + " " + (j - backCount));
				int move[] = { i, j - backCount };
				moves.twoWin = move;
				return;
			}
			if (freeForward[0] && length + forwardExtra == k - 2 && freeBack[0] && freeExtraForward) {
				System.out.println("found 2 win forward H move " + i + " " + (j +
						forwardCount));
				int move[] = { i, j + forwardCount };
				moves.twoWin = move;
				return;
			}
		}
		length = 1;
		backExtra = 0;
		forwardExtra = 0;
		freeBack[0] = false;
		freeBack[1] = false;
		freeForward[0] = false;
		freeForward[1] = false;
		freeExtraForward = false;
		freeExtraBack = false;

		// Vertical
		for (backCount = 1; i - backCount >= 0 && b[i - backCount][j] == player; backCount++)
			length++;
		for (forwardCount = 1; i + forwardCount < n && b[i + forwardCount][j] == player; forwardCount++)
			length++;
		if (i - backCount >= 0 && b[i - backCount][j] == MNKCellState.FREE) {
			// System.out.println("checking free V back cell " + (i - backCount) + " " + j);
			if (length == k - 1) {
				System.out.println("found winning V back cell " + (i - backCount) + " " + j);
				int move[] = { i - backCount, j };
				moves.win = move;
				return;
			}
			int rest = k - length - 1;
			for (int c = 1; i - backCount - c >= 0 && b[i - backCount - c][j] == player && c <= rest; c++)
				backExtra++;
			if (length + backExtra == k - 1) {
				System.out.println("found winning V back cell " + (i - backCount) + " " + j);
				int move[] = { i - backCount, j };
				moves.win = move;
				return;
			}
			freeBack[0] = true;
			if (i - backCount - 1 >= 0 && b[i - backCount - 1][j] == MNKCellState.FREE)
				freeBack[1] = true;
			if (i - backCount - backExtra - 1 >= 0 && b[i - backCount - backExtra - 1][j] == MNKCellState.FREE)
				freeExtraBack = true;
			if (full) {
				System.out.println("adding to queue V back cell " + (i - backCount) + " " +
						j);
				int[] backMove = { i - backCount, j, length + backExtra };
				q.add(backMove);
			}
		}
		if (i + forwardCount < n && b[i + forwardCount][j] == MNKCellState.FREE) {
			// System.out.println("checking free V forward cell " + (i + forwardCount) + " "
			// + j);
			if (length == k - 1) {
				System.out.println("found winning V forward cell " + (i + forwardCount) + " "
						+ j);
				int move[] = { i + forwardCount, j };
				moves.win = move;
				return;
			}
			int rest = k - length - 1;
			for (int c = 1; i + forwardCount + c < n && b[i + forwardCount + c][j] == player
					&& c <= rest; c++)
				forwardExtra++;
			if (length + forwardExtra == k - 1) {
				System.out.println("found winning V forward cell " + (i + forwardCount) + " "
						+ j);
				int move[] = { i + forwardCount, j };
				moves.win = move;
				return;
			}
			freeForward[0] = true;
			if (i + forwardCount + 1 < n && b[i + forwardCount + 1][j] == MNKCellState.FREE)
				freeForward[1] = true;
			if (i + forwardCount + forwardExtra + 1 < n
					&& b[i + forwardCount + forwardExtra + 1][j] == MNKCellState.FREE)
				freeExtraForward = true;
			if (full) {
				// System.out.println("adding to queue V forward cell " + (i + forwardCount) + "
				// " + j);
				int[] forwardMove = { i + forwardCount, j, length + forwardExtra };
				q.add(forwardMove);
			}
		}
		if (length == k - 2) {
			if (freeBack[0] && freeForward[0] && (freeBack[1] || freeForward[1])) {
				i = freeBack[1] ? i - backCount : i + forwardCount;
				int move[] = { i, j };
				System.out.println("found 2 winning V cell " + i + " " + j);
				moves.twoWin = move;
			}
		} else {
			if (freeBack[0] && length + backExtra == k - 2 && freeForward[0] && freeExtraBack) {
				int move[] = { i - backCount, j };
				System.out.println("found 2 winning back Vcell " + (i - backCount) + " " +
						j);
				moves.twoWin = move;
				return;
			}
			if (freeForward[0] && length + forwardExtra == k - 2 && freeBack[0] && freeExtraForward) {
				int move[] = { i + forwardCount, j };
				System.out.println("found 2 winning forward Vcell " + (i + forwardCount) + " " + j);
				moves.twoWin = move;
				return;
			}
		}
		length = 1;
		backExtra = 0;
		forwardExtra = 0;
		freeBack[0] = false;
		freeBack[1] = false;
		freeForward[0] = false;
		freeForward[1] = false;
		freeExtraForward = false;
		freeExtraBack = false;

		// Diagonal
		for (backCount = 1; j - backCount >= 0 && i - backCount >= 0
				&& b[i - backCount][j - backCount] == player; backCount++)
			length++;
		for (forwardCount = 1; j + forwardCount < n && i + forwardCount < n
				&& b[i + forwardCount][j + forwardCount] == player; forwardCount++)
			length++;
		if (j - backCount >= 0 && i - backCount >= 0
				&& b[i - backCount][j - backCount] == MNKCellState.FREE) {
			// System.out.println("checking free D back cell " + (i - backCount) + " " + (j
			// - backCount));
			if (length == k - 1) {
				System.out.println("found winning D back cell " + (i - backCount) + " " + (j
						- backCount));
				int move[] = { i - backCount, j - backCount };
				moves.win = move;
				return;
			}
			int rest = k - length - 1;
			for (int c = 1; j - backCount - c >= 0 && i - backCount - c >= 0
					&& b[i - backCount - c][j - backCount - c] == player && c <= rest; c++)
				backExtra++;
			if (length + backExtra == k - 1) {
				System.out.println("found winning D back cell " + (i - backCount) + " " + (j
						- backCount));
				int move[] = { i - backCount, j - backCount };
				moves.win = move;
				return;
			}
			freeBack[0] = true;
			if (j - backCount - 1 >= 0 && i - backCount - 1 >= 0
					&& b[i - backCount - 1][j - backCount - 1] == MNKCellState.FREE)
				freeBack[1] = true;
			if (j - backCount - backExtra - 1 >= 0 && i - backCount - backExtra - 1 >= 0
					&& b[i - backCount - backExtra - 1][j - backCount - backExtra - 1] == MNKCellState.FREE)
				freeExtraBack = true;
			if (full) {
				// System.out.println("adding to queue D back cell " + (i - backCount) + " " +
				// (j - backCount));
				int[] backMove = { i - backCount, j - backCount, length + backExtra };
				q.add(backMove);
			}
		}
		if (j + forwardCount < n && i + forwardCount < n
				&& b[i + forwardCount][j + forwardCount] == MNKCellState.FREE) {
			// System.out.println("checking free D forward cell " + (i + forwardCount) + " "
			// + (j + forwardCount));
			if (length == k - 1) {
				System.out.println("found winning D forward cell " + (i + forwardCount) + " "
						+ (j + forwardCount));
				int move[] = { i + forwardCount, j + forwardCount };
				moves.win = move;
				return;
			}
			int rest = k - length - 1;
			for (int c = 1; j + forwardCount + c < n && i + forwardCount + c < n
					&& b[i + forwardCount + c][j + forwardCount + c] == player && c <= rest; c++)
				forwardExtra++;
			if (length + forwardExtra == k - 1) {
				System.out.println("found winning D forward cell " + (i + forwardCount) + " "
						+ (j + forwardCount));
				int move[] = { i + forwardCount, j + forwardCount };
				moves.win = move;
				return;
			}
			freeForward[0] = true;
			if (j + forwardCount + 1 < n && i + forwardCount + 1 < n
					&& b[i + forwardCount + 1][j + forwardCount + 1] == MNKCellState.FREE)
				freeForward[1] = true;
			if (j + forwardCount + forwardExtra + 1 < n && i + forwardCount + forwardExtra + 1 < n
					&& b[i + forwardCount + forwardExtra + 1][j + forwardCount + forwardExtra + 1] == MNKCellState.FREE)
				freeExtraForward = true;
			if (full) {
				System.out.println("adding to queue D forward cell " + (i + forwardCount) + " " + (j + forwardCount));
				int[] forwardMove = { i + forwardCount, j + forwardCount, length + forwardExtra };
				q.add(forwardMove);
			}
		}
		if (length == k - 2) {
			if (freeBack[0] && freeForward[0] && (freeBack[1] || freeForward[1])) {
				i = freeBack[1] ? i - backCount : i + forwardCount;
				j = freeBack[1] ? j - backCount : j + forwardCount;
				int move[] = { i, j };
				System.out.println("found 2 win D cell " + i + " " + j);
				moves.twoWin = move;
			}
		} else {
			if (freeBack[0] && length + backExtra == k - 2 && freeForward[0] && freeExtraBack) {
				int move[] = { i - backCount, j - backCount };
				moves.twoWin = move;
				System.out.println("found 2 win back D cell " + (i - backCount) + " " + (j -
						backCount));
				return;
			}
			if (freeForward[0] && length + forwardExtra == k - 2 && freeBack[0] && freeExtraForward) {
				int move[] = { i + forwardCount, j + forwardCount };
				moves.twoWin = move;
				System.out.println("found 2 win forward D cell " + (i + forwardCount) + " " +
						(j + forwardCount));
				return;
			}
		}
		length = 1;
		backExtra = 0;
		forwardExtra = 0;
		freeBack[0] = false;
		freeBack[1] = false;
		freeForward[0] = false;
		freeForward[1] = false;
		freeExtraForward = false;
		freeExtraBack = false;

		// Antidiagonal
		for (backCount = 1; j + backCount < n && i - backCount >= 0
				&& b[i - backCount][j + backCount] == player; backCount++)
			length++;
		for (forwardCount = 1; j - forwardCount >= 0 && i + forwardCount < n
				&& b[i + forwardCount][j - forwardCount] == player; forwardCount++)
			length++;

		if (j + backCount < n && i - backCount >= 0 && b[i - backCount][j + backCount] == MNKCellState.FREE) {
			// System.out.println("checking free back AD cell " + (i - backCount) + " " + (j
			// + backCount));
			if (length == k - 1) {
				System.out.println("found winning back AD cell " + (i - backCount) + " " + (j
						+ backCount));
				int move[] = { i - backCount, j + backCount };
				moves.win = move;
				return;
			}
			int rest = k - length - 1;
			for (int c = 1; j + backCount + c < n && i - backCount - c >= 0
					&& b[i - backCount - c][j + backCount + c] == player && c <= rest; c++)
				backExtra++;
			if (length + backExtra == k - 1) {
				System.out.println("found winning back AD cell " + (i - backCount) + " " + (j
						+ backCount));
				int move[] = { i - backCount, j + backCount };
				moves.win = move;
				return;
			}
			freeBack[0] = true;
			if (j + backCount + 1 >= 0 && i - backCount - 1 >= 0
					&& b[i - backCount - 1][j + backCount - 1] == MNKCellState.FREE)
				freeBack[1] = true;
			if (i - backCount - backExtra - 1 >= 0 && j + backCount + backExtra + 1 < n
					&& b[i - backCount - backExtra - 1][j + backCount + backExtra + 1] == MNKCellState.FREE)
				freeExtraBack = true;
			if (full) {
				int[] backMove = { i - backCount, j + backCount, length + backExtra };
				System.out.println("adding to queue back AD cell " + (i - backCount) + " " +
						(j + backCount));
				q.add(backMove);
			}
		}

		if (j - forwardCount >= 0 && i + forwardCount < n
				&& b[i + forwardCount][j - forwardCount] == MNKCellState.FREE) {
			// System.out.println("checking forward AD cell " + (i + forwardCount) +" " + (j
			// - forwardCount));
			if (length == k - 1) {
				System.out.println("found winning forward AD cell " + (i + forwardCount) + " " + (j - forwardCount));
				int move[] = { i + forwardCount, j - forwardCount };
				moves.win = move;
				return;
			}

			int rest = k - length - 1;
			for (int c = 1; j - forwardCount - c >= 0 && i + forwardCount + c < n
					&& b[i + forwardCount + c][j - forwardCount - c] == player && c <= rest; c++)
				forwardExtra++;

			if (length + forwardExtra == k - 1) {
				System.out.println("found winning forward AD cell " + (i + forwardCount) + " " + (j - forwardCount));
				int move[] = { i + forwardCount, j - forwardCount };
				moves.win = move;
				return;
			}
			freeForward[0] = true;
			if (j - forwardCount - 1 >= 0 && i + forwardCount + 1 < n
					&& b[i + forwardCount + 1][j - forwardCount - 1] == MNKCellState.FREE)
				freeForward[1] = true;
			if (i + forwardCount + forwardExtra + 1 < n && j - forwardCount - forwardExtra - 1 >= 0
					&& b[i + forwardCount + forwardExtra + 1][j - forwardCount - forwardExtra - 1] == MNKCellState.FREE)
				freeExtraForward = true;
			if (full) {
				System.out.println("adding to queue forward AD cell " + (i + forwardCount) +
						" " + (j - forwardCount));
				int[] forwardMove = { i + forwardCount, j - forwardCount, length + forwardExtra };
				q.add(forwardMove);
			}
		}

		if (length == k - 2) {
			if (freeBack[0] && freeForward[0] && (freeBack[1] || freeForward[1])) {
				i = freeBack[1] ? i - backCount : i + forwardCount;
				j = freeBack[1] ? j + backCount : j - forwardCount;
				int move[] = { i, j };
				System.out.println("found 2 winning AD cell " + i + " " + j);
				moves.twoWin = move;
			}
		} else {
			if (freeBack[0] && length + backExtra == k - 2 && freeForward[0] && freeExtraBack) {
				int move[] = { i - backCount, j + backCount };
				System.out.println("found 2 winning back AD cell " + (i - backCount) + " " +
						(j + backCount));
				moves.twoWin = move;
				return;
			}
			if (freeForward[0] && length + forwardExtra == k - 2 && freeBack[0] && freeExtraForward) {
				int move[] = { i + forwardCount, j - forwardCount };
				System.out.println("found 2 winning forwardAD cell " + (i + forwardCount) + " " + (j - forwardCount));
				moves.twoWin = move;
				return;
			}
		}
		if (full)
			moves.q = q;
	}

}
