package app.book

import kotlin.random.Random


class Book(val title: String, val author: String, val isbn: String) {

  val mediumCover: String
    get() = "http://covers.openlibrary.org/b/isbn/" + this.isbn + "-M.jpg"

  val largeCover: String
    get() = "http://covers.openlibrary.org/b/isbn/" + this.isbn + "-L.jpg"
}

object bookDao {

  private val books = listOf (
      Book("Moby Dick", "Herman Melville", "9789583001215"),
      Book("A Christmas Carol", "Charles Dickens", "9780141324524"),
      Book("Pride and Prejudice", "Jane Austen", "9781936594290"),
      Book("The Fellowship of The Ring", "J. R. R. Tolkien", "0007171978"),
      Book("Harry Potter", "J. K. Rowling", "0747532699"),
      Book("War and Peace", "Leo Tolstoy", "9780060798871"),
      Book("Don Quixote", "Miguel Cervantes", "9789626345221"),
      Book("Ulysses", "James Joyce", "9780394703800"),
      Book("The Great Gatsby", "F. Scott Fitzgerald", "9780743273565"),
      Book("One Hundred Years of Solitude", "Gabriel Garcia Marquez", "9780060531041"),
      Book("The adventures of Huckleberry Finn", "Mark Twain", "9781591940296"),
      Book("Alice In Wonderland", "Lewis Carrol", "9780439291491")
  )

  val allBooks: List<Book>
    get() = books

  val randomBook: Book
    get() = books[Random.nextInt(books.size)]

  fun getBookByIsbn(isbn: String): Book? {
    return books.stream().filter { b -> b.isbn == isbn }.findFirst().orElse(null)
  }
}
