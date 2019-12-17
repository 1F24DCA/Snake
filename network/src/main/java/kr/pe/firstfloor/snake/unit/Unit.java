
package kr.pe.firstfloor.snake.unit;

import java.util.ArrayList;

import kr.pe.firstfloor.network.data.Data;

// TODO: need to re-design. (includes extended classes...)
public class Unit extends Data {
	private int x;
	private int y;

	private ArrayList<MoveListener> moveListeners = new ArrayList<>();
	
	protected Unit() {}

	public int getX() { return x; }
	public int getY() { return y; }
	
	public void setX(int x) { this.x = x; }
	public void setY(int y) { this.y = y; }
	
	public boolean move(int x, int y) {
		if (moveListeners != null)
			for (MoveListener moveListener : moveListeners)
				if (!moveListener.canMove(x, y)) return false;

		setX(x);
		setY(y);

		if (moveListeners != null)
			for (MoveListener moveListener : moveListeners)
				moveListener.onMove(x, y);

		return true;
	}

	public void addMoveListener(MoveListener moveListener) {
		moveListeners.add(moveListener);
	}

	public interface MoveListener {
		boolean canMove(int x, int y);
		void onMove(int x, int y);
	}
}