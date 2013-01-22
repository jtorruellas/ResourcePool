import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;
import java.util.HashMap;
import java.util.LinkedList;
/*
	A ResourcePool is a collection of generic resources of type R, where 
	resources can be added, removed, acquired, and released.  The pool is open 
	or closed and is closed by default with no initial resources.    
	It is thread-safe and defensive.
*/	
public class ResourcePool<R>{
	private boolean open = false;
	private Lock lock;
	private HashMap<R, Boolean> resources;
	private LinkedList<R> available;

	public ResourcePool(){
		resources = new HashMap<R, Boolean>();
		available = new LinkedList<R>();
		lock = new ReentrantLock();
	}
/*
 	Internal class to hold potential return values of the various methods,
 	specifically needed for the return value of the unlockPool method.
*/
	public class Result{
		R r; //Resource type for methods returning resources
		int i; //integer returns
		boolean b; //boolean returns
		public Result(R r, int i, boolean b){
			this.r = r;
			this.i = i;
			this.b = b;
		}
		public R getR(){ return r;}
		public int getInt(){ return i;}
		public boolean getBool(){ return b;}
	}
/*
 	Methods required by API, following contracts also given by specification.  
 	These methods only request access to the ResourcePool and pass their 
 	arguments to unlockPool with their respective opcode.  
 	Actual implementations are found in *Safe.
*/
	public int available(){ //returns number of available resources
		Result result = unlockPool(0, null);
		return result.getInt();
	}
	public boolean isOpen(){
		Result result = unlockPool(1, null);
		return result.getBool();
	}
	public void open(){
		unlockPool(2, null);
	}
	public void close(){ //waits for all resources to be released
		boolean closed = false;
		Result result;
		
		while (!closed){
			result = unlockPool(3, null);
			closed = result.getBool();
		}
	}
	public void closeNow(){
		unlockPool(4, null);
	}
	public boolean add(R resource){
		Result result = unlockPool(5, resource);
		return result.getBool();
	}
	public R acquire(){
		while(true){
			Result result = unlockPool(6, null);
			R resource = result.getR();
			if (resource != null)
				return resource;
		}
	}
	public R acquire(long timeout, java.util.concurrent.TimeUnit unit){
		long endtime = System.currentTimeMillis() + unit.toMillis(timeout);
		while(System.currentTimeMillis() < endtime){
			Result result = unlockPool(6, null);
			R resource = result.getR();
			if (resource != null)
				return resource;
		}
		return null;
	}
	public void release(R resource){
		unlockPool(7, resource);
	}
	public boolean remove(R resource){
		Result result;
		int remCase = 1;    // 3 possible remove results (bool insufficient): 
		boolean b = false;  // 0 = unmanaged, 1 = unavailable, 2 = removed
		while (remCase == 1){ // while unavilable, keep checking for release
			 result = unlockPool(8, resource);
			 remCase = result.getInt();
		}
		switch (remCase){
			case 0: b = false; // is not managed by pool, return false
				break;
			case 2: b = true; // removed from pool when available, return true
				break;
		}                     // case 1 not possible as a remCase of 1 will
		return b;             // not exit while loopp
	}
	public boolean removeNow(R resource){
		Result result = unlockPool(9, resource);
		return result.getBool();
	}
/*
	"Vault" method that guards data structures from race conditions by 
	only allowing one thread to call a method at a time.  Enforced by
	reentrant lock.  Stores return value (if any) in Result class.
*/
	public Result unlockPool(int op, R resource){
		Result result = null;
		lock.lock();
		if (op == 0){         //R     Integer          Boolean
			result = new Result(null, availableSafe(), false);
		}
		else if (op == 1){
			result = new Result(null, 0, isOpenSafe());
		}
		else if (op == 2){
			openSafe();
		}
		else if (op == 3){		
			result = new Result(null, 0, closeSafe());
		}
		else if (op == 4){
			closeNowSafe();
		}
		else if (op == 5){
			result = new Result(null, 0, addSafe(resource));
		}
		else if (op == 6){
			result = new Result(acquireSafe(), 0, false);
		}
		else if (op == 7){
			releaseSafe(resource);
		}
		else if (op == 8){
			result = new Result(null, removeSafe(resource), false);
		}
		else if (op == 9){
			result = new Result(null, 0, removeNowSafe(resource));
		}
		lock.unlock();
		return result;
	}
/*
	Thread-safe methods accessed through unlockPool
*/
	public int availableSafe(){
		return available.size();
	}
	public boolean isOpenSafe(){
		return open;
	}
	public void openSafe(){
		this.open = true;
	}
	public boolean closeSafe(){ // returns true if closed, false if still open
		if (available.size() == resources.size()){ //all resources are available
			open = false;
			
			return true;
			}
		else
			return false;
	}
	public void closeNowSafe(){
		this.open = false;
	}
	public boolean addSafe(R resource){
		if(resources.containsKey(resource))
			return false;
		resources.put(resource, true);
		available.add(resource);
		return true;
	}
	public R acquireSafe(){
		R resource = null;
		if (!isOpen())
			return resource;
		while (!available.isEmpty() && resources.get(available.peek()) == null)
			available.poll(); //removes removed resources, unlikely to occur
		if (!available.isEmpty() && resources.get(available.peek())){
			resources.put(available.peek(), false);
			resource = available.poll();
		}
		return resource;
	}
	public void releaseSafe(R resource){
		if (!resources.containsKey(resource))
				System.out.println("Error: Releasing unacquired resource.");
		else{
			resources.put(resource, true);
			available.add(resource);
		} 	
	}
	public int removeSafe(R resource){
		if (!resources.containsKey(resource))
			return 0; //unmanaged
		else{
			if (resources.get(resource)){
				resources.remove(resource);
				available.remove(resource);
				return 2;//found and removed
			}
			else
				return 1;//unavailable
		}
	}
	public boolean removeNowSafe(R resource){
		if (!resources.containsKey(resource))
			return false;
		resources.remove(resource);
		available.remove(resource);
		return true;
	}
}
