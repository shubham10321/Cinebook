package cinema.model;

public class Hall {
    private int hallId;
    private String name;
    private int hallRows;
    private int cols;

    public Hall() {}

    public Hall(int hallId, String name, int hallRows, int cols) {
        this.hallId = hallId;
        this.name = name;
        this.hallRows = hallRows;
        this.cols = cols;
    }

    public int getHallId()        { return hallId; }
    public void setHallId(int v)  { this.hallId = v; }

    public String getName()        { return name; }
    public void setName(String v)  { this.name = v; }

    public int getHallRows()        { return hallRows; }
    public void setHallRows(int v)  { this.hallRows = v; }

    public int getCols()        { return cols; }
    public void setCols(int v)  { this.cols = v; }

    @Override
    public String toString() { return name; }
}
