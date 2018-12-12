import java.net.Socket;

public class Requests {
    public Socket soc; 
    public long ts; 
    public boolean reqStatus;
    
    public Requests(Socket soc, long ts) { 
        this.soc = soc; 
        this.ts = ts; 
        this.reqStatus = false;
    }
      
    public Socket getRequest() { 
        return soc; 
    } 
	
	public long getTS() { 
        return ts; 
    } 
}
