ResourcePool
============


--OVERVIEW--
	A ResourcePool is a collection of generic resources of type R, where 
	resources can be added, removed, acquired, and released.  The pool is open 
	or closed and is closed by default with no initial resources.  
	It is thread-safe and defensive.

--DESIGN DECISIONS--
	The foundational data structures for the ResourcePool are a HashMap, using
	the type R resource as a key, paired with a boolean value representing 
	the availability of the resource.  The HashMap was chosen for constant
	time access to the availability of a given resource and to keep track of
	all resources maintained by the pool.  A queue (linked list) of all 
	available resources is also maintained to allow for constant time removal 
	of an available resource (not provided by HashMap).  

	I initially experimented with Java's integrated synchronous data structures, 
	the ConcurrentHashMap and ConcurrentLinkedQueue, but I found myself getting
	race conditions between methods, so I developed an internal locking system.
	Every method must request access to the pool through the unlockPool method 
	which is protected with a reentrant lock.  This guarantees that the data
	in the pool will be current at the time of access for any call.  A potential
	drawback of this design is that it forces a bottleneck for every thread. 
	This is minimized, as time spent inside the lock is relatively short and all 
	while loops are	in the unlocked sections.  There is also some storage
	overhead with the return class.

--PERFORMANCE AND TESTING--
	I created two test implementations: a public library and a memory allocation 
	system.  Although by no means exhaustive, the tests show that the methods
	work as expected and that no obvious race conditions exist.  This is evident
	from the timestamps for each acquire/release in the print statements as well
	as non-displayed items.  Not all of the printed statements reflect the 
	actual status of the pool, though.  I intend to make it clearer with a 
	graphical visualization in a future update.

--FUTURE IMPROVEMENTS--
	I plan on building a Visualizer class to display the ResourcePool.  I also
	will consider other alternatives to the unlockPool system I currently have.
	It was more reliable than my first approach, but there may be a better way 
	in terms of performance.  