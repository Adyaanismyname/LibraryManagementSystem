public class User {

    int user_id;
    public static int no_of_users = 0;

    String name;
    String contact_info;
    int borrowed_book; // stores book_id


    public User(int user_id , String name , String contact_info , int borrowed_book) {
        this.user_id = user_id;
        this.name = name;
        this.contact_info = contact_info;
        this.borrowed_book = borrowed_book;

        no_of_users++;


    }

    String getName() {
        return this.name;
    }
    String get_contact() {
        return this.contact_info;
    }
}