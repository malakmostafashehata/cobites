package backend;
	public interface Orderable {
	    boolean canOrder(int quantity);  
	    void reduceQuantity(int quantity);
	}

