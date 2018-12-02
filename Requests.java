package roucairolCaravalho;

import java.net.Socket;

public class Requests {
    public Socket soc; 
    public long ts; 
    
    public Requests(Socket soc, long ts) { 
        this.soc = soc; 
        this.ts = ts; 
    } 
      
    public Socket getRequest() { 
        return soc; 
    }  
}
