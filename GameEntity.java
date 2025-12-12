public abstract class GameEntity {
    protected int x, y; 
    protected boolean isActive; 

    public GameEntity(int x, int y) {
        this.x = x;
        this.y = y;
        this.isActive = true; 
    }

    public abstract void update(long currentTime);
    
    public int getX() { return x; }
    public int getY() { return y; }
    public boolean isActive() { return isActive; }
    
    public void setX(int x) { this.x = x; }
    public void setY(int y) { this.y = y; }
    public void setActive(boolean active) { this.isActive = active; }
}
