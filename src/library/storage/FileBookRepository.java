package library.storage;

import java.io.IOException;
import java.nio.file.Path;
import java.util.List;
import library.model.Book;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.ArrayList;
import java.io.BufferedReader;
import java.nio.file.StandardCopyOption;
import java.nio.file.AtomicMoveNotSupportedException;

public class FileBookRepository implements BookRepository {
    private final Path path;
    public FileBookRepository(Path path){
        this.path = path;
    }

    @Override
    public List<Book> load() throws IOException {
        List<Book> books = new ArrayList<>();
        if (Files.notExists(path)) {
            return books;
        }
        try(BufferedReader reader = Files.newBufferedReader(path, StandardCharsets.UTF_8)){
            String header = reader.readLine();
            int expectedColumns;
            if ("LIBRARY\t1".equals(header)) {
                expectedColumns = 4;
            } else if ("LIBRARY\t2".equals(header)) {
                expectedColumns = 5;
            } else {
                throw new IOException("第1行：文件头不正确");
            }
            int lineNumber = 1;
            String line;
            while ((line = reader.readLine()) != null){
                lineNumber++;  
                String[] fields = line.split("\t", -1);
                if (fields.length != expectedColumns) {
                    throw new IOException("第" + lineNumber + "行：图书记录应有" + expectedColumns + "列");
                }

                boolean borrowed;
                if("BORROWED".equals(fields[3])){
                    borrowed = true;
                } 
                else if("AVAILABLE".equals(fields[3])){
                    borrowed = false;
                } 
                else{
                    throw new IOException("第" + lineNumber + "行：借阅状态不正确");
                }

                try{
                    Book book;
                    if (expectedColumns == 4) {
                        book = new Book(fields[0], fields[1], fields[2], borrowed);
                    } else {
                        book = new Book(fields[0], fields[1], fields[2], borrowed, fields[4]);
                    }

                    for (Book existing : books) {
                        if (existing.getId().equals(book.getId())) {
                            throw new IOException("第" + lineNumber + "行：重复编号 " + book.getId());
                        }
                    }

                    books.add(book);
                } 
                catch (IllegalArgumentException e){
                    throw new IOException("第" + lineNumber + "行：" + e.getMessage(), e);
                }
            }
        }
        return books;
    }

    @Override
    public void save(List<Book> books) throws IOException {
        List<String> lines = new ArrayList<>();
        lines.add("LIBRARY\t2");
        List<String> seenIds = new ArrayList<>();

        for(Book book : books){
            if (seenIds.contains(book.getId())) {
                throw new IOException("保存失败：重复编号 " + book.getId());
            }
            seenIds.add(book.getId());

            String status;
            if(book.isBorrowed()){
                status = "BORROWED";
            }
            else{
                status = "AVAILABLE";
            }
            lines.add(book.getId() + "\t" +
                book.getTitle() + "\t" +
                book.getAuthor() + "\t" +
                status + "\t" +
                book.getCategory()
            );
        }

        Path target = path.toAbsolutePath();
        Path directory = target.getParent();
        Files.createDirectories(directory);
        Path temporary = Files.createTempFile(directory, "books-", ".tmp");

        try {
            Files.write(temporary, lines, StandardCharsets.UTF_8);

            try {
                Files.move(temporary, target,
                        StandardCopyOption.ATOMIC_MOVE,
                        StandardCopyOption.REPLACE_EXISTING);
            } 
            catch (AtomicMoveNotSupportedException e) {
                Files.move(temporary, target, StandardCopyOption.REPLACE_EXISTING);
            }
        } 
        finally {
            Files.deleteIfExists(temporary);
        }
    }
}
