package library.service;

import java.util.ArrayList;
import java.util.List;
import library.model.Book;

/** 管理内存中的书库，集中处理查询、修改和借还规则。 */
public class LibraryService {
    private List<Book> books = new ArrayList<>();

    /** 复制初始书单，避免外部继续修改服务内部持有的图书。 */
    public LibraryService(List<Book> initialBooks){
        for (Book book : initialBooks) {
            books.add(new Book(book.getId(),book.getTitle(),book.getAuthor(),book.isBorrowed(),book.getCategory()));
        }
    }

    /** 添加图书；未指定分类时使用“未分类”。 */
    public void addBook(String id, String title, String author){
        addBook(id, title, author, "未分类");
    }

    /** 先校验新书，再用规范化后的编号检查重复。 */
    public void addBook(String id, String title, String author, String category){
        Book newBook = new Book(id, title, author, category);
        for(Book book : books){
            if(book.getId().equals(newBook.getId())){
                throw new IllegalArgumentException("重复编号：" + id);
            }
        }
        books.add(newBook);
    }

    /** 返回书单快照，调用方修改返回值不会影响内部书库。 */
    public List<Book> listBooks() {
        List<Book> snapshot = new ArrayList<>();
        for (Book book : books) {
            snapshot.add(new Book(book.getId(),book.getTitle(),book.getAuthor(),book.isBorrowed(),book.getCategory()));
        }
        return snapshot;
    }

    /** 仅供业务操作使用：返回内部对象，以便直接修改其状态。 */
    private Book findInternalById(String id){
        String target = id.trim();
        for(Book book : books){
            if(book.getId().equals(target)){
                return book;
            }
        }
        throw new IllegalArgumentException("找不到编号：" + id);
    }

    /** 按编号查询，并返回图书副本。 */
    public Book findById(String id){
        Book book = findInternalById(id);
        return new Book(book.getId(), book.getTitle(), book.getAuthor(), book.isBorrowed(),book.getCategory());
    }

    /** 按书名包含关系查询，忽略大小写。 */
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

    /** 按完整分类名查询，忽略大小写。 */
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

    /** 修改指定图书的书名和作者。 */
    public void updateBook(String id, String title, String author){
        Book found = findInternalById(id);
        found.updateDetails(title, author);
    }

    /** 修改指定图书的分类。 */
    public void updateBookCategory(String id, String category){
        Book found = findInternalById(id);
        found.updateCategory(category);
    }

    /** 删除未借出的图书。 */
    public void deleteBook(String id){
        Book found = findInternalById(id);
        if(found.isBorrowed() == true){
            throw new IllegalStateException("该书已被借出");
        }
        books.remove(found);
    } 

    /** 将指定图书标记为已借出。 */
    public void borrowBook(String id){
        Book found = findInternalById(id);
        found.borrow();
    }                     

    /** 将指定图书标记为可借阅。 */
    public void returnBook(String id){
        Book found = findInternalById(id);
        found.returnBook();
    }
}
