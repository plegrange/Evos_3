import java.util.List;

/**
 * Created by FuBaR on 12/2/2016.
 */
public class Item {

    public String itemName;
    public List<Component> components;

    //public float itemAmount;
    public Item(String name, List<Component> components) {
        this.itemName = name;
        this.components = components;
    }

    public float[] getComponentAmounts() {
        float[] componentAmounts = new float[components.size()];
        for (int i = 0; i < components.size(); i++) {
            componentAmounts[i] = components.get(i).componentAmount;
        }
        return componentAmounts;
    }

    public float gainPerDollar() {
        float gain = 0.0f;
        for (Component component : components) {
            gain += component.componentAmount;
        }
        return gain;
    }

    public Item cloneItem() {
        return new Item(this.itemName, this.components);
    }
}
