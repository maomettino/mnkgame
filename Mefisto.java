package mnkgame;

import java.util.List;
import java.util.PriorityQueue;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Random;

public class Mefisto implements MNKPlayer {
	private int m;
	private int n;
	private int k;
	private int turn;
	private MNKCellState[][] board;
	private MNKCellState mefisto;
	private MNKCellState foe;
	private int TIMEOUT;

	private class Moves {
		public int[] win;
		public int[] twoWin;

		public Moves() {

		}
	}

	/**
	 * Default empty constructor
	 */
	public Mefisto() {
	}

	public void initPlayer(int M, int N, int K, boolean first, int timeout_in_secs) {
		turn = 0;
		mefisto = first ? MNKCellState.P1 : MNKCellState.P2;
		foe = first ? MNKCellState.P2 : MNKCellState.P1;
		board = new MNKCellState[M][N];
		this.m = M;
		this.n = N;
		this.k = K;
		this.TIMEOUT = timeout_in_secs;
		for (int i = 0; i < M; i++)
			for (int j = 0; j < N; j++)
				board[i][j] = MNKCellState.FREE;
	}

	public MNKCell selectCell(MNKCell[] FC, MNKCell[] MC) {
		turn++;
		System.out.println("turno: "+turn);
		if (turn > 1) {
			if (FC.length == 1)
				return FC[0];
			MNKCell mefistoLastCell = MC[MC.length - 2];
			MNKCell foeLastCell = MC[MC.length - 1];
			board[mefistoLastCell.i][mefistoLastCell.j] = mefisto;
			board[foeLastCell.i][foeLastCell.j] = foe;
			MNKCell cell = getMove(FC, mefistoLastCell, foeLastCell);
			return cell;
		}
		// When it's my first turn
		else {
			if (MC.length > 0) {
				MNKCell foeCell = MC[MC.length - 1]; 
				board[foeCell.i][foeCell.j] = foe;
			}
			int[] e = getQueueHead(FC);
			return new MNKCell(e[0], e[1]);
		}

	}

	private MNKCell getMove(MNKCell[] FC, MNKCell mefistoLastCell, MNKCell foeLastCell) {
		Moves mefistoMoves = findVictory(mefistoLastCell, true);
		if (!(mefistoMoves.win == null)) {
			return new MNKCell(mefistoMoves.win[0], mefistoMoves.win[1]);
		}	
		Moves foeMoves = findVictory(foeLastCell, false);
		if (!(foeMoves.win == null)) {
			return new MNKCell(foeMoves.win[0], foeMoves.win[1]);
		}
		if (!(mefistoMoves.twoWin == null)) {
			return new MNKCell(mefistoMoves.twoWin[0], mefistoMoves.twoWin[1]);
		}
		if (!(foeMoves.twoWin == null)) {
			return new MNKCell(foeMoves.twoWin[0], foeMoves.twoWin[1]);
		}
		int[] e = getQueueHead(FC);
		return new MNKCell(e[0], e[1]);
	}

	private int[] getQueueHead(MNKCell[] FC) {
		long start = System.currentTimeMillis();
		PriorityQueue<int[]> q;
		q = new PriorityQueue<int[]>(FC.length, new Comparatore());
		for (MNKCell cell : FC) {
			if ((System.currentTimeMillis() - start) / 1000.0 > TIMEOUT * (98.0 / 100.0)) {
				System.out.println("running out of time!!!");
				return q.peek();
			}
			q.add(new int[] { cell.i, cell.j, getHeuristicValue(cell.i, cell.j) });
		}
		return q.peek();
	}

	private Moves findVictory(MNKCell lastCell, boolean isMefisto) {
		Moves moves = new Moves();
		checkAround(lastCell.i, lastCell.j, moves, isMefisto);
		return moves;
	}

	private void checkAroundHorizontal(int i, int j, Moves moves, boolean isMefisto) {
		MNKCellState player = isMefisto ? mefisto : foe;
		int length = 1, backExtra = 0, forwardExtra = 0, backCount, forwardCount;
		boolean freeBack[] = { false, false }, freeForward[] = { false, false }, freeExtraForward = false,
				freeExtraBack = false;
		for (backCount = 1; j - backCount >= 0 && board[i][j - backCount] == player; backCount++)
			length++;
		for (forwardCount = 1; j + forwardCount < n && board[i][j + forwardCount] == player; forwardCount++)
			length++;
		if (j - backCount >= 0 && board[i][j - backCount] == MNKCellState.FREE) {
			if (length == k - 1) {
				int move[] = { i, j - backCount };
				moves.win = move;
				return;
			}
			int rest = k - length - 1;
			for (int c = 1; j - backCount - c >= 0 && board[i][j - backCount - c] == player && c <= rest; c++)
				backExtra++;
			if (length + backExtra == k - 1) {
				int move[] = { i, j - backCount };
				moves.win = move;
				return;
			}
			freeBack[0] = true;
			if (j - backCount - 1 >= 0 && board[i][j - backCount - 1] == MNKCellState.FREE)
				freeBack[1] = true;
			if (j - backCount - backExtra - 1 >= 0
					&& board[i][j - backCount - backExtra - 1] == MNKCellState.FREE)
				freeExtraBack = true;

		}
		if (j + forwardCount < n && board[i][j + forwardCount] == MNKCellState.FREE) {
			if (length == k - 1) {
				int move[] = { i, j + forwardCount };
				moves.win = move;
				return;
			}
			int rest = k - length - 1;

			for (int c = 1; j + forwardCount + c < n && board[i][j + forwardCount + c] == player
					&& c <= rest; c++)
				forwardExtra++;
			if (length + forwardExtra == k - 1) {
				int move[] = { i, j + forwardCount };
				moves.win = move;
				return;
			}
			freeForward[0] = true;
			if (j + forwardCount + 1 < n && board[i][j + forwardCount + 1] == MNKCellState.FREE)
				freeForward[1] = true;
			if (j + forwardCount + forwardExtra + 1 < n
					&& board[i][j + forwardCount + forwardExtra + 1] == MNKCellState.FREE)
				freeExtraForward = true;
		}
		if (length == k - 2) {
			if (freeBack[0] && freeForward[0] && (freeBack[1] || freeForward[1])) {
				j = freeBack[1] ? j - backCount : j + forwardCount;
				int move[] = { i, j };
				moves.twoWin = move;
			}
		} else {
			if (freeBack[0] && length + backExtra == k - 2 && freeForward[0] && freeExtraBack) {
				int move[] = { i, j - backCount };
				moves.twoWin = move;
				return;
			}
			if (freeForward[0] && length + forwardExtra == k - 2 && freeBack[0] && freeExtraForward) {
				int move[] = { i, j + forwardCount };
				moves.twoWin = move;
				return;
			}
		}

	}

	private void checkAroundVertical(int i, int j, Moves moves, boolean isMefisto) {
		MNKCellState player = isMefisto ? mefisto : foe;
		int length = 1, backExtra = 0, forwardExtra = 0, backCount, forwardCount;
		boolean freeBack[] = { false, false }, freeForward[] = { false, false }, freeExtraForward = false,
				freeExtraBack = false;
		for (backCount = 1; i - backCount >= 0 && board[i - backCount][j] == player; backCount++)
			length++;
		for (forwardCount = 1; i + forwardCount < n && board[i + forwardCount][j] == player; forwardCount++)
			length++;
		if (i - backCount >= 0 && board[i - backCount][j] == MNKCellState.FREE) {
			if (length == k - 1) {
				int move[] = { i - backCount, j };
				moves.win = move;
				return;
			}
			int rest = k - length - 1;
			for (int c = 1; i - backCount - c >= 0 && board[i - backCount - c][j] == player && c <= rest; c++)
				backExtra++;
			if (length + backExtra == k - 1) {
				int move[] = { i - backCount, j };
				moves.win = move;
				return;
			}
			freeBack[0] = true;
			if (i - backCount - 1 >= 0 && board[i - backCount - 1][j] == MNKCellState.FREE)
				freeBack[1] = true;
			if (i - backCount - backExtra - 1 >= 0
					&& board[i - backCount - backExtra - 1][j] == MNKCellState.FREE)
				freeExtraBack = true;
		}
		if (i + forwardCount < n && board[i + forwardCount][j] == MNKCellState.FREE) {
			if (length == k - 1) {
				int move[] = { i + forwardCount, j };
				moves.win = move;
				return;
			}
			int rest = k - length - 1;
			for (int c = 1; i + forwardCount + c < n && board[i + forwardCount + c][j] == player
					&& c <= rest; c++)
				forwardExtra++;
			if (length + forwardExtra == k - 1) {
				int move[] = { i + forwardCount, j };
				moves.win = move;
				return;
			}
			freeForward[0] = true;
			if (i + forwardCount + 1 < n && board[i + forwardCount + 1][j] == MNKCellState.FREE)
				freeForward[1] = true;
			if (i + forwardCount + forwardExtra + 1 < n
					&& board[i + forwardCount + forwardExtra + 1][j] == MNKCellState.FREE)
				freeExtraForward = true;
		}
		if (length == k - 2) {
			if (freeBack[0] && freeForward[0] && (freeBack[1] || freeForward[1])) {
				i = freeBack[1] ? i - backCount : i + forwardCount;
				int move[] = { i, j };
				moves.twoWin = move;
			}
		} else {
			if (freeBack[0] && length + backExtra == k - 2 && freeForward[0] && freeExtraBack) {
				int move[] = { i - backCount, j };
				moves.twoWin = move;
				return;
			}
			if (freeForward[0] && length + forwardExtra == k - 2 && freeBack[0] && freeExtraForward) {
				int move[] = { i + forwardCount, j };
				moves.twoWin = move;
				return;
			}
		}

	}

	private void checkAroundDiagonal(int i, int j, Moves moves, boolean isMefisto) {
		MNKCellState player = isMefisto ? mefisto : foe;
		int length = 1, backExtra = 0, forwardExtra = 0, backCount, forwardCount;
		boolean freeBack[] = { false, false }, freeForward[] = { false, false }, freeExtraForward = false,
				freeExtraBack = false;
		for (backCount = 1; j - backCount >= 0 && i - backCount >= 0
				&& board[i - backCount][j - backCount] == player; backCount++)
			length++;
		for (forwardCount = 1; j + forwardCount < n && i + forwardCount < n
				&& board[i + forwardCount][j + forwardCount] == player; forwardCount++)
			length++;
		if (j - backCount >= 0 && i - backCount >= 0
				&& board[i - backCount][j - backCount] == MNKCellState.FREE) {
			if (length == k - 1) {
				int move[] = { i - backCount, j - backCount };
				moves.win = move;
				return;
			}
			int rest = k - length - 1;
			for (int c = 1; j - backCount - c >= 0 && i - backCount - c >= 0
					&& board[i - backCount - c][j - backCount - c] == player && c <= rest; c++)
				backExtra++;
			if (length + backExtra == k - 1) {
				int move[] = { i - backCount, j - backCount };
				moves.win = move;
				return;
			}
			freeBack[0] = true;
			if (j - backCount - 1 >= 0 && i - backCount - 1 >= 0
					&& board[i - backCount - 1][j - backCount - 1] == MNKCellState.FREE)
				freeBack[1] = true;
			if (j - backCount - backExtra - 1 >= 0 && i - backCount - backExtra - 1 >= 0
					&& board[i - backCount - backExtra - 1][j - backCount - backExtra - 1] == MNKCellState.FREE)
				freeExtraBack = true;
		}
		if (j + forwardCount < n && i + forwardCount < n
				&& board[i + forwardCount][j + forwardCount] == MNKCellState.FREE) {
			if (length == k - 1) {
				int move[] = { i + forwardCount, j + forwardCount };
				moves.win = move;
				return;
			}
			int rest = k - length - 1;
			for (int c = 1; j + forwardCount + c < n && i + forwardCount + c < n
					&& board[i + forwardCount + c][j + forwardCount + c] == player && c <= rest; c++)
				forwardExtra++;
			if (length + forwardExtra == k - 1) {
				int move[] = { i + forwardCount, j + forwardCount };
				moves.win = move;
				return;
			}
			freeForward[0] = true;
			if (j + forwardCount + 1 < n && i + forwardCount + 1 < n
					&& board[i + forwardCount + 1][j + forwardCount + 1] == MNKCellState.FREE)
				freeForward[1] = true;
			if (j + forwardCount + forwardExtra + 1 < n && i + forwardCount + forwardExtra + 1 < n
					&& board[i + forwardCount + forwardExtra + 1][j + forwardCount + forwardExtra
							+ 1] == MNKCellState.FREE)
				freeExtraForward = true;
		}
		if (length == k - 2) {
			if (freeBack[0] && freeForward[0] && (freeBack[1] || freeForward[1])) {
				i = freeBack[1] ? i - backCount : i + forwardCount;
				j = freeBack[1] ? j - backCount : j + forwardCount;
				int move[] = { i, j };
				moves.twoWin = move;
			}
		} else {
			if (freeBack[0] && length + backExtra == k - 2 && freeForward[0] && freeExtraBack) {
				int move[] = { i - backCount, j - backCount };
				moves.twoWin = move;
				return;
			}
			if (freeForward[0] && length + forwardExtra == k - 2 && freeBack[0] && freeExtraForward) {
				int move[] = { i + forwardCount, j + forwardCount };
				moves.twoWin = move;
				return;
			}
		}

	}

	private void checkAroundAntiDiagonal(int i, int j, Moves moves, boolean isMefisto) {
		MNKCellState player = isMefisto ? mefisto : foe;
		int length = 1, backExtra = 0, forwardExtra = 0, backCount, forwardCount;
		boolean freeBack[] = { false, false }, freeForward[] = { false, false }, freeExtraForward = false,
				freeExtraBack = false;
		for (backCount = 1; j + backCount < n && i - backCount >= 0
				&& board[i - backCount][j + backCount] == player; backCount++)
			length++;
		for (forwardCount = 1; j - forwardCount >= 0 && i + forwardCount < n
				&& board[i + forwardCount][j - forwardCount] == player; forwardCount++)
			length++;
		if (j + backCount < n && i - backCount >= 0
				&& board[i - backCount][j + backCount] == MNKCellState.FREE) {
			if (length == k - 1) {
				int move[] = { i - backCount, j + backCount };
				moves.win = move;
				return;
			}
			int rest = k - length - 1;
			for (int c = 1; j + backCount + c < n && i - backCount - c >= 0
					&& board[i - backCount - c][j + backCount + c] == player && c <= rest; c++)
				backExtra++;
			if (length + backExtra == k - 1) {
				int move[] = { i - backCount, j + backCount };
				moves.win = move;
				return;
			}
			freeBack[0] = true;
			if (j + backCount + 1 >= 0 && i - backCount - 1 >= 0
					&& board[i - backCount - 1][j + backCount - 1] == MNKCellState.FREE)
				freeBack[1] = true;
			if (i - backCount - backExtra - 1 >= 0 && j + backCount + backExtra + 1 < n
					&& board[i - backCount - backExtra - 1][j + backCount + backExtra + 1] == MNKCellState.FREE)
				freeExtraBack = true;
		}

		if (j - forwardCount >= 0 && i + forwardCount < n
				&& board[i + forwardCount][j - forwardCount] == MNKCellState.FREE) {
			if (length == k - 1) {
				int move[] = { i + forwardCount, j - forwardCount };
				moves.win = move;
				return;
			}

			int rest = k - length - 1;
			for (int c = 1; j - forwardCount - c >= 0 && i + forwardCount + c < n
					&& board[i + forwardCount + c][j - forwardCount - c] == player && c <= rest; c++)
				forwardExtra++;

			if (length + forwardExtra == k - 1) {
				int move[] = { i + forwardCount, j - forwardCount };
				moves.win = move;
				return;
			}
			freeForward[0] = true;
			if (j - forwardCount - 1 >= 0 && i + forwardCount + 1 < n
					&& board[i + forwardCount + 1][j - forwardCount - 1] == MNKCellState.FREE)
				freeForward[1] = true;
			if (i + forwardCount + forwardExtra + 1 < n && j - forwardCount - forwardExtra - 1 >= 0
					&& board[i + forwardCount + forwardExtra + 1][j - forwardCount - forwardExtra
							- 1] == MNKCellState.FREE)
				freeExtraForward = true;
		}

		if (length == k - 2) {
			if (freeBack[0] && freeForward[0] && (freeBack[1] || freeForward[1])) {
				i = freeBack[1] ? i - backCount : i + forwardCount;
				j = freeBack[1] ? j + backCount : j - forwardCount;
				int move[] = { i, j };
				moves.twoWin = move;
			}
		} else {
			if (freeBack[0] && length + backExtra == k - 2 && freeForward[0] && freeExtraBack) {
				int move[] = { i - backCount, j + backCount };
				moves.twoWin = move;
				return;
			}
			if (freeForward[0] && length + forwardExtra == k - 2 && freeBack[0] && freeExtraForward) {
				int move[] = { i + forwardCount, j - forwardCount };
				moves.twoWin = move;
				return;
			}
		}
	}

	private void checkAround(int i, int j, Moves moves, boolean isMefisto) {
		checkAroundHorizontal(i, j, moves, isMefisto);
		if (moves.win == null) {
			checkAroundVertical(i, j, moves, isMefisto);
			if (moves.win == null) {
				checkAroundDiagonal(i, j, moves, isMefisto);
				if (moves.win == null) {
					checkAroundAntiDiagonal(i, j, moves, isMefisto);
				}
			}
		}
	}

	private int getHeuristicValue(int i, int j) {
		int lengthH = 1, lengthV = 1, lengthD = 1, lengthAD = 1;
		int extraH = 0, extraV = 0, extraD = 0, extraAD = 0;
		int backCount, forwardCount;
		// Horizontal
		for (backCount = 1; j - backCount >= 0 && board[i][j - backCount] == mefisto; backCount++)
			lengthH++;
		for (forwardCount = 1; j + forwardCount < this.n && board[i][j + forwardCount] == mefisto; forwardCount++)
			lengthH++;
		for (; j - backCount >= 0 && board[i][j - backCount] == MNKCellState.FREE
				&& lengthH + extraH < this.k; backCount++)
			extraH++;
		for (; j + forwardCount < this.n && board[i][j + forwardCount] == MNKCellState.FREE
				&& lengthH + extraH < this.k; forwardCount++)
			extraH++;

		// Vertical
		for (backCount = 1; i - backCount >= 0 && board[i - backCount][j] == mefisto; backCount++)
			lengthV++;

		for (forwardCount = 1; i + forwardCount < this.n && board[i + forwardCount][j] == mefisto; forwardCount++)
			lengthV++;

		for (; i - backCount >= 0 && board[i - backCount][j] == MNKCellState.FREE
				&& lengthV + extraV < this.k; backCount++)
			extraV++;
		for (; i + forwardCount < this.n && board[i + forwardCount][j] == MNKCellState.FREE
				&& lengthV + extraV < this.k; forwardCount++)
			extraV++;

		// Diagonal
		for (backCount = 1; j - backCount >= 0 && i - backCount >= 0
				&& board[i - backCount][j - backCount] == mefisto; backCount++)
			lengthD++;

		for (forwardCount = 1; j + forwardCount < this.n && i + forwardCount < this.n
				&& board[i + forwardCount][j + forwardCount] == mefisto; forwardCount++)
			lengthD++;

		for (; j - backCount >= 0 && i - backCount >= 0
				&& board[i - backCount][j - backCount] == MNKCellState.FREE
				&& lengthD + extraD < this.k; backCount++)
			extraD++;
		for (; j + forwardCount < this.n && i + forwardCount < this.n
				&& board[i + forwardCount][j + forwardCount] == MNKCellState.FREE
				&& lengthD + extraD < this.k; forwardCount++)
			extraD++;

		// Antidiagonal
		for (backCount = 1; j + backCount < this.n && i - backCount >= 0
				&& board[i - backCount][j + backCount] == mefisto; backCount++)
			lengthAD++;

		for (forwardCount = 1; j - forwardCount >= 0 && i + forwardCount < this.n
				&& board[i + forwardCount][j - forwardCount] == mefisto; forwardCount++)
			lengthAD++;
		for (; j + backCount < this.n && i - backCount >= 0
				&& board[i - backCount][j + backCount] == MNKCellState.FREE
				&& lengthAD + extraAD < this.k; backCount++)
			extraAD++;
		for (; j - forwardCount >= 0 && i + forwardCount < this.n
				&& board[i + forwardCount][j - forwardCount] == MNKCellState.FREE
				&& lengthAD + extraAD < this.k; forwardCount++)
			extraAD++;

		int value;
		value = ((extraH + lengthH) >= this.k ? (int) Math.pow(10, lengthH - this.k + 3) + lengthH : 0) +
				((extraV + lengthV) >= this.k ? (int) Math.pow(10, lengthV - this.k + 3) + lengthV : 0) +
				((extraD + lengthD) >= this.k ? (int) Math.pow(10, lengthD - this.k + 3) + lengthD : 0) +
				((extraAD + lengthAD) >= this.k ? (int) Math.pow(10, lengthAD - this.k + 3) + lengthAD : 0);
		return value;
	}

	public String playerName() {
		return "Mefisto";
	}

}
