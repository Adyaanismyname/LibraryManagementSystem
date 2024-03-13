// Main.java
import java.io.IOException;
import java.util.ArrayList;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.InputMismatchException;
import java.util.Scanner;


public class Main {


    public static class Library {
        ArrayList<Book> books = new ArrayList<>();
        ArrayList<User> users = new ArrayList<>();
        static int no_of_users = 0;
        static int no_of_books = 0;
        private Connection connection;


        public Library(Connection connection) {
            this.connection = connection;

        }
        // function to load user data from the database

        void loadUserdata() throws SQLException {
            String selectUsersQuery = "SELECT * FROM User";

            try (PreparedStatement preparedStatement = connection.prepareStatement(selectUsersQuery);
                 ResultSet resultSet = preparedStatement.executeQuery()) {

                // Check if the result set is empty
                if (!resultSet.isBeforeFirst()) {
                    System.out.println("No users found.");
                    return;
                }

                // Iterate through the result set and display user information
                while (resultSet.next()) {
                    int userId = resultSet.getInt("user_id");
                    String name = resultSet.getString("name");
                    String contactInformation = resultSet.getString("contact_information");
                    int borrowed_book = resultSet.getInt("borrowed_book");

                    User user = new User(userId , name , contactInformation , borrowed_book);

                    users.add(user);
                }

                System.out.println("User data successfully loaded");
            }

        }
        // function to load book data from the database
        void loadbookdata() throws SQLException {
            String selectBookQuery = "SELECT * FROM Book";

            try (PreparedStatement preparedStatement = connection.prepareStatement(selectBookQuery);
                 ResultSet resultSet = preparedStatement.executeQuery()) {

                // Check if the result set is empty
                if (!resultSet.isBeforeFirst()) {
                    System.out.println("No Book found.");
                    return;
                }

                // Iterate through the result set and display user information
                while (resultSet.next()) {
                    int book_Id = resultSet.getInt("book_id");
                    String title = resultSet.getString("title");
                    String author = resultSet.getString("author");
                    String genre = resultSet.getString("genre");
                    Boolean availability_status = resultSet.getBoolean("availability_status");

                    Book book = new Book(book_Id , title , author , genre , availability_status);

                    books.add(book);

                }

                System.out.println("Book data successfully loaded");
            }
        }
        // function to add user
        void AddUser(String name , String contactInformation) throws SQLException {



            String insertUserQuery = "INSERT INTO User (name, contact_information) VALUES (?, ?)";

            try (PreparedStatement preparedStatement = this.connection.prepareStatement(insertUserQuery, java.sql.Statement.RETURN_GENERATED_KEYS)) {
                preparedStatement.setString(1, name);
                preparedStatement.setString(2, contactInformation);

                // Execute the query
                int affectedRows = preparedStatement.executeUpdate();
                if (affectedRows > 0) {
                    try (ResultSet generatedKeys = preparedStatement.getGeneratedKeys()) {
                        if (generatedKeys.next()) {
                            int user_id = generatedKeys.getInt(1);
                            User new_user = new User(user_id, name, contactInformation, 0);
                            users.add(new_user);
                            System.out.println("User created successfully. User id: " + user_id);
                        }
                    }
                } else {
                    System.out.println("Failed to create user.");
                }
            }

        }
        // function to add book
        void addBook(String title , String author , String genre) throws SQLException {


            String insertBookQuery = "INSERT INTO Book(title, author, genre, availability_status) VALUES (?, ?, ?, true)";

            try (PreparedStatement preparedStatement = this.connection.prepareStatement(insertBookQuery, java.sql.Statement.RETURN_GENERATED_KEYS)) {
                preparedStatement.setString(1, title);
                preparedStatement.setString(2, author);
                preparedStatement.setString(3, genre);

                int affectedRows = preparedStatement.executeUpdate();

                if (affectedRows > 0) {
                    try (ResultSet generatedKeys = preparedStatement.getGeneratedKeys()) {
                        if (generatedKeys.next()) {
                            int book_id = generatedKeys.getInt(1);
                            Book new_book = new Book(book_id, title, author, genre, true);
                            books.add(new_book);
                            System.out.println("Book created successfully. Book ID: " + book_id);
                        }
                    }
                } else {
                    System.out.println("Failed to create book.");
                }
            }



        }
        // function to borrow books
        void check_out_books(int user_id , int book_id) throws SQLException {
            for(Book b : books) { // check if book is available/exists
                if(b.book_id == book_id) {
                    if(b.isAvailable) {
                        for(User u : users) { // if book exists find if user exists
                            if(u.user_id == user_id) {
                                if(u.borrowed_book != 0) {
                                    System.out.println(u.name + " Has already borrowed a book");
                                    return;
                                }
                                u.borrowed_book = b.book_id; // update book and user data
                                b.isAvailable = false;
                                // updating databases
                                String query = "UPDATE User SET borrowed_book = ? WHERE user_id = ?;";
                                try (PreparedStatement preparedStatement = this.connection.prepareStatement(query)) {
                                    preparedStatement.setInt(1, book_id);
                                    preparedStatement.setInt(2, user_id);
                                    preparedStatement.addBatch();

                                    query = "UPDATE Book SET availability_status = false WHERE book_id = ?;";
                                    preparedStatement.setInt(1, book_id);
                                    preparedStatement.addBatch();

                                    int[] affected_rows = preparedStatement.executeBatch();

                                    if (affected_rows[0] > 0 && affected_rows[1] > 0) {
                                        System.out.println(b.title + " borrowed successfully by " + u.name);
                                        return;
                                    } else {
                                        System.out.println("Failed to borrow");
                                    }
                                }
                            }
                            else {
                                System.out.println("No User with this user_id");
                                return;
                            }
                        }
                    }
                    else {
                        System.out.println("Book not available");
                    }
                }
                else {
                    System.out.println("No book with this book_id");
                    return;
                }

            }

        }
        // function to return books
        void return_books(int user_Id, int book_id) throws SQLException {
            for (User u : users) {
                if (u.user_id == user_Id) {
                    u.borrowed_book = 0; // since id starts from 1
                    for (Book b : books) {
                        if (b.book_id == book_id) {
                            b.isAvailable = true;
                            // updating database
                            String query = "UPDATE User SET borrowed_book = 0 WHERE user_id = ?;";
                            try (PreparedStatement preparedStatement = this.connection.prepareStatement(query)) {
                                preparedStatement.setInt(1, user_Id);
                                preparedStatement.addBatch();

                                query = "UPDATE Book SET availability_status = true WHERE book_id = ?;";
                                preparedStatement.setInt(1, book_id);
                                preparedStatement.addBatch();

                                int[] affected_rows = preparedStatement.executeBatch();

                                if (affected_rows[0] > 0 && affected_rows[1] > 0) {
                                    System.out.println(b.title + " returned successfully by " + u.name);
                                    return;
                                } else {
                                    System.out.println("Failed to return");
                                }
                            }
                        }
                        else {
                            System.out.println("No book with this book_id");
                            return;
                        }
                    }

                }
                else {
                    System.out.println("No User with this user_id");
                    return;

                }
            }
        }
        // function to search book by author
        Book search_book_author(String author) {
            for(Book b : books) {
                if(b.author.equals(author)) {
                    return b;
                }
            }
            return null;

        }

        // function to search book by title
        Book search_book_title(String title) {
            for(Book b : books) {
                if(b.title.equals(title)) {
                    return b;
                }
            }
            return null;
        }

        // function to display books
        void display_books() {
            for(Book b : books) {
                System.out.printf("Book_id : %d    Name : %s   Author : %s  Genre : %s Availability : %b  \n " , b.book_id , b.title , b.author , b.genre , b.isAvailable);
            }
        }
        // function to check the book the user has borrowed
        void check_borrowed_book(int user_id) {
            for(User u : users) {
                if(u.user_id == user_id) {
                    int book_id = u.borrowed_book;
                    if(book_id != 0) { // checks if a book is borrowed
                        for(Book b : books) {
                            if(b.book_id == book_id) {
                                System.out.printf("Book_id : %d    Name : %s   Author : %s  Genre : %s  \n " , b.book_id , b.title , b.author , b.genre);
                                break;
                            }

                        }
                    }
                    else {
                        System.out.println("No book Borrowed");
                    }
                }

            }
        }

    }


    public static void main(String[] args) {
        String url = "jdbc:sqlite:src/assignment1.db";


        try {
            // Load the SQLite JDBC driver (this step is crucial)
            Class.forName("org.sqlite.JDBC");

            // Establish a connection to the database
            Connection connection = DriverManager.getConnection(url);

            // create a library database

            Library library = new Library(connection);

            Scanner scanner = new Scanner(System.in);
            int choice;

            System.out.println("Welcome to the Library Management System");
            library.loadbookdata();
            library.loadUserdata();
            // creating the actual menu
            do {
                System.out.println("\nMenu:");
                System.out.println("1. Add Book");
                System.out.println("2. Add User");
                System.out.println("3. Display Books");
                System.out.println("4. Borrow Book");
                System.out.println("5. Return Book");
                System.out.println("6. Search for books borrowed by user");
                System.out.println("7. Search for book by author");
                System.out.println("8. Search for book by title");
                System.out.println("9. Exit");
                System.out.print("Enter your choice: ");

                choice = scanner.nextInt();
                scanner.nextLine(); // Consume the newline character

                switch (choice) {
                    case 1:
                        try {
                            System.out.println("Option 1: Add Book");
                            System.out.print("Enter the title of the book :  ");
                            String title = scanner.nextLine();
                            System.out.print("Enter the author of the book :  ");
                            String author = scanner.nextLine();
                            System.out.print("Enter the genre of the book :  ");
                            String genre = scanner.nextLine();
                            library.addBook(title,author,genre);
                            break;
                        }
                        catch(InputMismatchException e) {
                            System.out.println("Enter data in the correct format");
                            scanner.nextLine();


                        }



                    case 2:
                        try {
                            System.out.println("Option 2: Add User");
                            System.out.print("Enter the name of the user :  ");
                            String name = scanner.nextLine();
                            System.out.print("Enter the contact_info of the User :  ");
                            String contact_info = scanner.nextLine();
                            library.AddUser(name , contact_info);
                        }
                        catch(InputMismatchException e) {
                            System.out.println("Enter data in the correct format");
                            scanner.nextLine();


                        }



                        break;
                    case 3:
                        System.out.println("Option 3: Display Books");
                        library.display_books();
                        break;
                    case 4:
                        try {
                            System.out.println("Option 4: Borrow Book");
                            System.out.print("Enter the user_id of the user :  ");
                            int user_id = scanner.nextInt();
                            System.out.print("Enter the book_id of the book :  ");
                            int book_id = scanner.nextInt();
                            library.check_out_books(user_id , book_id);
                        }
                        catch(InputMismatchException e) {
                            System.out.println("Enter data in the correct format");
                            scanner.nextLine();


                        }
                        break;
                    case 5:
                        try {
                            System.out.println("Option 5: Return Book");
                            System.out.print("Enter the user_id of the user :  ");
                            int user_id1 = scanner.nextInt();
                            System.out.print("Enter the book_id of the book :  ");
                            int book_id1 = scanner.nextInt();
                            library.return_books(user_id1,book_id1);
                        }
                        catch(InputMismatchException e) {
                            System.out.println("Enter data in the correct format");
                            scanner.nextLine();
                        }

                        break;
                    case 6:
                        try {
                            System.out.println("Option 6: Search Books by User ID");
                            System.out.print("Enter the user_id of the user :  ");
                            int user_id2 = scanner.nextInt();
                            library.check_borrowed_book(user_id2);
                        }
                        catch(InputMismatchException e) {
                            System.out.println("Enter data in the correct format");
                            scanner.nextLine();
                        }

                        break;
                    case 7:
                        try {
                            System.out.println("Option 7: Search for book by author");
                            System.out.println("Enter the author : ");
                            String author1 = scanner.nextLine();

                            Book b1 = library.search_book_author(author1);

                            if(b1 == null) {
                                System.out.println("No book with this author");
                            }
                            System.out.printf("Book_id : %d    Name : %s   Author : %s  Genre : %s  \n " , b1.book_id , b1.title , b1.author , b1.genre);
                        }
                        catch(InputMismatchException e) {
                            System.out.println("Enter data in the correct format");
                            scanner.nextLine();

                        }

                        break;
                    case 8:
                        try {
                            System.out.println("Option 8: Search for book by title");
                            System.out.println("Enter the title : ");

                            String title1 = scanner.nextLine();

                            Book b = library.search_book_title(title1);

                            if(b == null) {
                                System.out.println("No book with this title");
                                break;
                            }
                            System.out.printf("Book_id : %d    Name : %s   Author : %s  Genre : %s  \n " , b.book_id , b.title , b.author , b.genre);

                        }
                        catch(InputMismatchException e) {
                            System.out.println("Enter data in the correct format");
                            scanner.nextLine();
                        }
                        break;
                    case 9:
                        System.out.println("Exiting... Goodbye!");
                        break;
                    default:
                        System.out.println("Invalid choice. Please enter a valid option.");
                }

            } while (choice != 9);

            scanner.close();

            // Close the connection
            connection.close();

        } catch (ClassNotFoundException | SQLException e) {
            e.printStackTrace();
        }


    }
}
