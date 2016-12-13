/**
 * Created by FuBaR on 12/2/2016.
 */
import java.lang.Float;
public class Component {
    public String componentName, unit;
    public float componentAmount;
    public float recommendedAnnually;

    public Component(String componentName, String componentAmount, String recommendedAnnually, String unit) {
        this.componentName = componentName;
        this.unit = unit;
        this.recommendedAnnually = Float.valueOf(recommendedAnnually);
        this.componentAmount = Float.valueOf(componentAmount);
    }
}
