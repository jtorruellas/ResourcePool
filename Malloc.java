public class Malloc{
	public static void main(String[] args){
		new Malloc();
	}
/*
Malloc-esque simulation that allocates integers to represent memory locations.
A thread requests a number of memory units (int array), holds them for a time,
and then "frees" them back into the pool.  

*/	
	private ResourcePool<Integer> memory;
	private int size;
	//private Visualizer vis;
	public Malloc(){
		memory = new ResourcePool<Integer>();
		//vis = new Visualizer();
		size = 0;
		addMemory(5000);
		execute();
	}
	public void addMemory(int num){
		for (int i=size; i<num; i++)
			memory.add(i);
		size = size + num;
	}
	public void removeMemory(int num){
		for (int i=size; i<num; i++)
			memory.add(i);
		size = size + num;
	}
	public int[] malloc(int num){
		int[] alloc = new int[num];
		for (int i=0; i<num; i++){
			alloc[i] = memory.acquire();
		}
		return alloc;
	}
	public void free(int[] alloc){
		for (int i = 0; i<alloc.length; i++){
			memory.release(alloc[i]);
		}
	}
	public int available(){
		return memory.available();
	}
	public void execute(){
		memory.open();
		int n = (int) (Math.random() * 50) + 5;
		Program[] programs = new Program[n];
		for (int i=0; i<n; i++){
			programs[i] = new Program(i);
			programs[i].start();
		}
	}
	public class Program extends Thread {
		int programID;
		int num; // number of allocated blocks
		int pc; //Program Counter
		int instructions; //Number of instructions
		int[] alloc;
		double allocTime;
		double heldTime;
		double processTime;

		public Program(int programID){
			this.programID = programID;
			
			pc = 0;
			instructions = (int) (Math.random() * 10) + 1;
		}
		public void run(){ 
			System.out.println("Program " + programID + " runs for " 
				                          + instructions +" instructions.");
			System.out.println(available() + " blocks available.");
				try{
					while (pc < instructions){
						allocTime = System.currentTimeMillis();
						num = (int) (Math.random() * 500) + 1;
						System.out.println(available() + " blocks available.");
						while (true){
							if(available() > num){  //insures adequate available 
								alloc = malloc(num);//memory to prevent deadlock
								break;
							}
						}
						System.out.println(programID + " allocated " + num + 
							                      " blocks after waiting " + 
							((System.currentTimeMillis() - allocTime)/1000));
						heldTime = System.currentTimeMillis();
						pc++;		
						Thread.sleep((int)( Math.random() * 5000)); //up to 5sec 
						                                       //per instruction
						free(alloc);
						heldTime=(System.currentTimeMillis() - heldTime) / 1000;
						System.out.println(programID+ " frees " + num + 
							                          " after " + heldTime);
					}			
				} catch (InterruptedException e){
					System.out.println("Program "+programID + " interrupted.");
				}	
			System.out.println("Program " + programID + " finishes after " 
				                          + instructions + " instructions.");
		}
	}
}