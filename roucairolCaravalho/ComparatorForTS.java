package roucairolCaravalho;

import java.util.Comparator;

public class ComparatorForTS implements Comparator<Requests> {
	
	public int compare(Requests req1, Requests req2) { 
		long arg1 = req1.ts;
		long arg2 = req2.ts;
		
		if (arg1 > arg2) 
            return 1; 
        else if (arg1 < arg2) 
            return -1; 
        return 0; 
    }

}
