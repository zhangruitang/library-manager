package library.storage;

import java.util.List;
import java.io.IOException;
import library.model.Book;

public interface BookRepository {
    List<Book> load() throws IOException;
    void save(List<Book> books) throws IOException;
}
