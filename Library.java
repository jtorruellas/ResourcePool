public class Library{

	public static void main(String[] args){
		new Library();
	}
	public class Book{
		String title;
		String author;
		String text;
		public Book(String title, String author, String text){
			this.title = title;
			this.author = author;
			this.text = text;
		}
		public String getTitle(){
			return title;
		}
		public String getText(){
			return text;
		}
	}

	public Library(){ //"Public library, hehe"
		ResourcePool<Book> library = new ResourcePool<Book>();
		Book one = new Book("EG", "OSC", "The enemy gate is down.");
		Book two = new Book("TTT", "JRRT", "Forth Eorlingas!");
		Book three = new Book("HP", "JKR", "You're a wizard, Harry.");
		Book four = new Book("1984", "GO", "BIG BROTHER IS WATCHING YOU");
		Book five = new Book("GoT", "GRRM", "Winter is coming.");
		library.add(one);
		library.add(two);
		library.add(three);
		library.add(four);
		library.add(five);
		library.open();
		openLibrary (library, five);
}
	public void openLibrary(ResourcePool<Book> library, Book five){
		library.open();
		System.out.println("The library is open!");
		Patron bob = new Patron("Bob", library, five);
		Patron joe = new Patron("Joe", library, five);
		Patron sue = new Patron("Sue", library, five);
		Patron pat = new Patron("Pat", library, five);
		Patron ted = new Patron("Ted", library, five);
		Patron tif = new Patron("Tif", library, five);
		bob.start();
		joe.start();
		sue.start();
		pat.start();
		ted.start();
		tif.start();
	}
	public class Patron extends Thread {
		String patron;
		ResourcePool<Book> library;
		int booksRead;
		int booksRequested;
		double enterTime;
		double readTime = 0;
		Book removeme;
		public Patron(String patron, ResourcePool<Book> library, Book removeme){
			this.patron = patron;
			this.library = library;
			booksRequested = (int) (Math.random() * 10) + 1;
			this.removeme = removeme;
		}
		public void run(){

			System.out.println("Patron " + patron + " enters library for " + booksRequested +" books.");
			if(patron == "Bob"){	
				try{
					library.remove(removeme);	
					Thread.sleep(1000);
					library.close();
				} catch (InterruptedException e){
					System.out.println("Patron " + patron + " interrupted.");
				}
				
			}
			else{
				try{
					enterTime = System.currentTimeMillis();
					while (booksRead < booksRequested && library.isOpen()){
						long timeout = 10;
						Book book = library.acquire(timeout, java.util.concurrent.TimeUnit.SECONDS);
						if (book != null){
							enterTime = System.currentTimeMillis();
							System.out.println(patron + " got " + booksRead + ": " +  book.getTitle() + " after waiting " + ((System.currentTimeMillis() - enterTime)/1000));
							readTime = System.currentTimeMillis();
							booksRead++;				
							Thread.sleep((int)(50000 * Math.random()));
							library.release(book);
							readTime = (System.currentTimeMillis() - readTime) / 1000;
							System.out.println(patron + " returns " + book.getTitle() + " after " + readTime);
					
						}
						else
							System.out.println(patron + " is impatient!!");
					}
								
				} catch (InterruptedException e){
					System.out.println("Patron " + patron + " interrupted.");
				}
			}	
			System.out.println("Patron " + patron + " left, reading " + booksRead + " books.");
		}
	}
}