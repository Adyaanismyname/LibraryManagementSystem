public  class Book {
    int book_id;
    static int no_of_books = 0;
    String title;
    String author;
    String genre;
    boolean  isAvailable;


    public Book(int book_id ,String title , String author , String genre , boolean isAvailable) {
        this.book_id = book_id;
        this.title = title;
        this.author = author;
        this.genre = genre;
        this.isAvailable = isAvailable;


        no_of_books++;



    }



}