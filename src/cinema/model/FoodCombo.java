package cinema.model;

import java.math.BigDecimal;

public class FoodCombo {
    private int comboId;
    private String name;
    private String description;
    private BigDecimal price;
    private String emoji;

    public FoodCombo() {}

    public FoodCombo(int comboId, String name, String description, BigDecimal price, String emoji) {
        this.comboId = comboId;
        this.name = name;
        this.description = description;
        this.price = price;
        this.emoji = emoji;
    }

    public int getComboId()           { return comboId; }
    public void setComboId(int v)     { this.comboId = v; }

    public String getName()           { return name; }
    public void setName(String v)     { this.name = v; }

    public String getDescription()    { return description; }
    public void setDescription(String v) { this.description = v; }

    public BigDecimal getPrice()      { return price; }
    public void setPrice(BigDecimal v){ this.price = v; }

    public String getEmoji()          { return emoji; }
    public void setEmoji(String v)    { this.emoji = v; }

    @Override
    public String toString() { return emoji + " " + name + " - Rs." + price; }
}
