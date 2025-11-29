package backend;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

public class FoodManager {
    private Map<String,FoodItem> items = new LinkedHashMap<>();

    public void addFood(FoodItem f) { items.put(f.getId(), f); }

    public Optional<FoodItem> findById(String id) { return Optional.ofNullable(items.get(id)); }

    public List<FoodItem> getAvailableItems() {
        List<FoodItem> res=new ArrayList<>();
        for(FoodItem f:items.values())
            if(f.getQty()>0) res.add(f);
        return res;
    }
}
