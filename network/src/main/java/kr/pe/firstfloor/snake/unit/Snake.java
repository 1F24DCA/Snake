
package kr.pe.firstfloor.snake.unit;

public class Snake extends Unit {
    public static final int UP = 0;
    public static final int DOWN = 1;
    public static final int LEFT = 2;
    public static final int RIGHT = 3;

    private boolean dead;
    private String userId;
    private int direction;

    protected Snake() {}
    protected Snake(String userId, int x, int y, int direction) {
        move(x, y);
        setUserId(userId);
        setDirection(direction);
    }

    public boolean isHead() { return this instanceof SnakeHead; }
    public boolean isBody() { return this instanceof SnakeBody && !((SnakeBody)this).isTail(); }
    public boolean isTail() { return this instanceof SnakeBody && ((SnakeBody)this).isTail(); }

    public boolean isDead() { return dead; }
    public String getUserId() { return userId; }
    public int getDirection() { return direction; }

    public void setDead(boolean dead) { this.dead = dead; }
    public void setUserId(String userId) { this.userId = userId; }
    public void setDirection(int direction) {
        switch (direction) {
            case UP: case DOWN: case LEFT: case RIGHT:
                this.direction = direction;
        }
    }
}
