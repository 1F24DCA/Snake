
package kr.pe.firstfloor.snake.unit;

public class SnakeBody extends Snake {
    int nextDirection;
    boolean tail = false;

    protected SnakeBody() {}
    protected SnakeBody(String userId, int x, int y, int direction, int nextDirection) {
        super(userId, x, y, direction);
        setNextDirection(nextDirection);
    }

    public boolean isTail() { return tail; }
    public int getNextDirection() { return nextDirection; }

    public void setTail(boolean tail) { this.tail = tail; }
    public void setNextDirection(int nextDirection) { this.nextDirection = nextDirection; }
}
