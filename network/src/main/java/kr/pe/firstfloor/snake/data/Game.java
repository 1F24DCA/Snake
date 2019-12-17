
package kr.pe.firstfloor.snake.data;

import java.util.*;

import kr.pe.firstfloor.snake.unit.*;

import kr.pe.firstfloor.network.data.Data;

// TODO: need to re-design.
public class Game extends Data {
	private boolean gameOver = false;

	// need to go like SnakeGame.Settings
	private int width = 100;
	private int height = 100;

	private HashMap<Integer, Unit> units = new HashMap<>();

	private final HashMap<String, Integer> maxSnakeSizes = new HashMap<>();
	private final HashMap<String, ArrayList<Snake>> userSnakes = new HashMap<>();
	private final ArrayList<Feed> feeds = new ArrayList<>();


	protected Game() {}


	public boolean isGameOver() { return gameOver; }
	public int getWidth() { return this.width; }
	public int getHeight() { return this.height; }
	public HashMap<Integer, Unit> getUnits() { return this.units; }

	public void setGameOver(boolean gameOver) { this.gameOver = gameOver; }
	public void setWidth(int width) { this.width = width; }
	public void setHeight(int height) { this.height = height; }
	public void setUnits(HashMap<Integer, Unit> units) { this.units = units; }


	private int getEmptyUnitId() {
		int unitIdToCheck = 0;
		while (true) {
			if (units.keySet().contains(unitIdToCheck)) {
				unitIdToCheck++;
				continue;
			}

			return unitIdToCheck;
		}
	}

	void addPlayer(final String userId) {
		/*final Snake snake = new SnakeHead(userId, 0, 0, 0);
		snake.move(Math.abs(snake.hashCode()) % getWidth(), Math.abs(snake.hashCode()) % getHeight());
		snake.setDirection(Math.abs(snake.hashCode()) % 4);

		if (addUnit(snake)) {
			setMaxSnakeSize(userId, 5);
			getSnakes(userId).add(snake);

			addUnitMoveListener(snake, new Unit.MoveListener() {
				@Override
				public boolean canMove(int x, int y) {
					if (snake.isDead()) return false;

					for (Unit unit : units.values()) {
						if (snake.equals(unit)) continue;
						if (unit.getX() == x && unit.getY() == y) {
							if (unit instanceof SnakeBody) {
								for (Snake snake : getSnakes(userId))
									snake.setDead(true);

								return false;
							} else if (unit instanceof SnakeHead) {
								// Head(final Snake snake...) vs Head(Unit unit...)!
								// stop until turn left or right
								return false;
							} else if (unit instanceof Feed) {
								removeUnit(unit);
								setMaxSnakeSize(userId, getMaxSnakeSize(userId) + 1);

								return true;
							}
						}
					}

					if (x < 0 || y < 0 || x > getWidth() || y > getHeight()) {
						for (Snake snake : getSnakes(userId))
							snake.setDead(true);

						return false;
					}

					return true;
				}

				@Override
				public void onMove(int x, int y) {
				}
			});
		}*/
	}

	void move(String userId) {
		Snake snakeHead = getSnakeHead(userId);
		int x = snakeHead.getX();
		int y = snakeHead.getY();
		int direction = snakeHead.getDirection();

		boolean moved = false;
		switch (snakeHead.getDirection()) {
			case Snake.UP:
				moved = snakeHead.move(snakeHead.getX(), snakeHead.getY() - 1);
				break;
			case Snake.DOWN:
				moved = snakeHead.move(snakeHead.getX(), snakeHead.getY() + 1);
				break;
			case Snake.LEFT:
				moved = snakeHead.move(snakeHead.getX() - 1, snakeHead.getY());
				break;
			case Snake.RIGHT:
				moved = snakeHead.move(snakeHead.getX() + 1, snakeHead.getY());
				break;
		}

		if (moved) {
			int nextDirection = snakeHead.getDirection();
			if (getSnakes(userId).size() > 1 && getSnakes(userId).get(1) != null)
				nextDirection = getSnakes(userId).get(1).getDirection();

			/*Snake snakeBody = new SnakeBody(userId, x, y, direction, nextDirection);
			if (addUnit(snakeBody)) {
				getSnakes(userId).add(snakeBody);

				if (getSnakes(userId).size() > getMaxSnakeSize(userId))
					removeUnit(getSnakeTail(userId));
			}*/
		}
	}

	void addFeed() {
		if (getFeeds().size() < getWidth()*getHeight()/1000+2) {
			/*Feed feed = new Feed();
			feed.move(Math.abs(feed.hashCode() % getWidth()), Math.abs(feed.hashCode() % getHeight()));

			if (addUnit(feed)) {
				getFeeds().add(feed);
			}*/
		}
	}

	private void removePlayer(String userId) {
		for (Snake snake : getSnakes(userId))
			removeUnit(snake);
	}

	// Unit handling functions
	protected boolean addUnit(Unit unit) {
		int x = unit.getX();
		int y = unit.getY();
		if (x < 0 || y < 0 || x > getWidth() || y > getHeight()) return false;

		int unitId = getEmptyUnitId();
		units.put(unitId, unit);

		return true;
	}

	private void addUnitMoveListener(Unit unit, Unit.MoveListener moveListener) {
		unit.addMoveListener(moveListener);
	}

	private int getUnitId(Unit unit) {
		for (int unitId : units.keySet())
			if (units.get(unitId).equals(unit))
				return unitId;
		return -1;
	}

	private void removeUnit(Unit unit) {
		removeUnit(getUnitId(unit));

		if (unit instanceof Snake)
			for (ArrayList<Snake> snakes : userSnakes.values())
				snakes.remove(unit);
	}

	private void removeUnit(int unitId) {
		units.remove(unitId);
	}

	// Snake control
	void moveUp(String userId) {
		if (getSnakes(userId) == null) return;

		Snake snakeHead = getSnakeHead(userId);

		if (snakeHead != null && snakeHead.getDirection() != Snake.DOWN)
			snakeHead.setDirection(Snake.UP);
	}
	void moveDown(String userId) {
		if (getSnakes(userId) == null) return;

		Snake snakeHead = getSnakeHead(userId);

		if (snakeHead != null && snakeHead.getDirection() != Snake.UP)
			snakeHead.setDirection(Snake.DOWN);
	}
	void moveLeft(String userId) {
		if (getSnakes(userId) == null) return;

		Snake snakeHead = getSnakeHead(userId);

		if (snakeHead != null && snakeHead.getDirection() != Snake.RIGHT)
			snakeHead.setDirection(Snake.LEFT);
	}
	void moveRight(String userId) {
		if (getSnakes(userId) == null) return;

		Snake snakeHead = getSnakeHead(userId);

		if (snakeHead != null && snakeHead.getDirection() != Snake.LEFT)
			snakeHead.setDirection(Snake.RIGHT);
	}

	// Snake handling functions
	private ArrayList<Snake> getSnakes(String userId) {
		if (userSnakes.get(userId) == null)
			userSnakes.put(userId, new ArrayList<Snake>());

		return userSnakes.get(userId);
	}
	private int getSnakeSize(String userId) {
		return getSnakes(userId).size();
	}
	private int getMaxSnakeSize(String userId) {
		if (maxSnakeSizes.get(userId) == null)
			setMaxSnakeSize(userId, 5);

		return maxSnakeSizes.get(userId);
	}
	private Snake getSnakeHead(String userId) {
		ArrayList<Snake> snakes = getSnakes(userId);

		return (snakes.size()>0)?snakes.get(0):null;
	}
	private Snake getSnakeTail(String userId) {
		ArrayList<Snake> snakes = getSnakes(userId);

		return (snakes.size()>1)?snakes.get(snakes.size()-1):null;
	}
	private void setMaxSnakeSize(String userId, int size) {
		maxSnakeSizes.put(userId, size);
	}

	// Feed handling functions
	private ArrayList<Feed> getFeeds() {
		return feeds;
	}

	// Tick
	protected void tick() {}
	public static class Ticker extends Thread {
		private final Game game;
		public Ticker(Game game) {
			this.game = game;

			this.setPriority(Thread.MIN_PRIORITY);
			this.start();
		}

		@Override
		public void run() {
			while (true) {
				try { Thread.sleep(500); } catch (Exception e) {}

				if (game.isGameOver()) break;
				game.tick();
			}
		}
	}
}