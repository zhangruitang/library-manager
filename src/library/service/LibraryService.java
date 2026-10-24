package library.service;

import java.util.ArrayList;
import java.util.List;
import library.model.Book;

public class LibraryService {
    private List<Book> books = new ArrayList<>();

    public LibraryService(List<Book> initialBooks){
        for (Book book : initialBooks) {
            books.add(new Book(book.getId(),book.getTitle(),book.getAuthor(),book.isBorrowed(),book.getCategory()));
        }
    }

    public void addBook(String id, String title, String author){
        addBook(id, title, author, "未分类");
    }

    public void addBook(String id, String title, String author, String category){
        Book newBook = new Book(id, title, author, category);
        for(Book book : books){
            if(book.getId().equals(newBook.getId())){
                throw new IllegalArgumentException("重复编号：" + id);
            }
        }
        books.add(newBook);
    }

    public List<Book> listBooks() {
        List<Book> snapshot = new ArrayList<>();
        for (Book book : books) {
            snapshot.add(new Book(book.getId(),book.getTitle(),book.getAuthor(),book.isBorrowed(),book.getCategory()));
        }
        return snapshot;
    }

    private Book findInternalById(String id){
        String target = id.trim();
        for(Book book : books){
            if(book.getId().equals(target)){
                return book;
            }
        }
        throw new IllegalArgumentException("找不到编号：" + id);
    }

    public Book findById(String id){
        Book book = findInternalById(id);
        return new Book(book.getId(), book.getTitle(), book.getAuthor(), book.isBorrowed(),book.getCategory());
    }

    public List<Book> searchByTitle(String keyword){
        if(keyword==null || keyword.trim().isEmpty()){
            throw new IllegalArgumentException("书名关键词不能为空");
        }
        String target = keyword.trim().toLowerCase();
        List<Book> results = new ArrayList<>();
        for(Book book : books){
            if(book.getTitle().toLowerCase().contains(target)){
                results.add(new Book(book.getId(),book.getTitle(),book.getAuthor(),book.isBorrowed(),book.getCategory()));
            }
        }
        return results;
    }           

    public List<Book> searchByCategory(String category){
        if(category == null || category.trim().isEmpty()){
            throw new IllegalArgumentException("分类关键词不能为空");
        }

        String target = category.trim();
        List<Book> results = new ArrayList<>();
        for(Book book : books){
            if(book.getCategory().equalsIgnoreCase(target)){
                results.add(new Book(book.getId(), book.getTitle(), book.getAuthor(),
                    book.isBorrowed(), book.getCategory()));
            }
        }
        return results;
    }

    public void updateBook(String id, String title, String author){
        Book found = findInternalById(id);
        found.updateDetails(title, author);
    }

    public void updateBookCategory(String id, String category){
        Book found = findInternalById(id);
        found.updateCategory(category);
    }

    public void deleteBook(String id){
        Book found = findInternalById(id);
        if(found.isBorrowed() == true){
            throw new IllegalStateException("该书已被借出");
        }
        books.remove(found);
    } 

    public void borrowBook(String id){
        Book found = findInternalById(id);
        found.borrow();
    }                     

    public void returnBook(String id){
        Book found = findInternalById(id);
        found.returnBook();
    }
}
