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

/** 用 UTF-8 TSV 文件实现书单持久化，兼容 V1 和 V2 格式。 */
public class FileBookRepository implements BookRepository {
    private final Path path;
    /** 指定要读取和保存的书单路径。 */
    public FileBookRepository(Path path){
        this.path = path;
    }

    /** 逐行读取并校验书单；错误信息保留文件行号。 */
    @Override
    public List<Book> load() throws IOException {
        List<Book> books = new ArrayList<>();
        if (Files.notExists(path)) {
            return books;
        }
        try(BufferedReader reader = Files.newBufferedReader(path, StandardCharsets.UTF_8)){
            String header = reader.readLine();
            // 文件头决定记录列数：V1 没有分类，V2 增加分类列。
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
                // -1 保留末尾空列，以便发现分类等字段为空的坏数据。
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
                // 将模型校验错误转成带行号的文件读取错误，并保留原始原因。
                catch (IllegalArgumentException e){
                    throw new IOException("第" + lineNumber + "行：" + e.getMessage(), e);
                }
            }
        }
        return books;
    }

    /** 统一写成 V2 格式，并通过同目录临时文件替换目标文件。 */
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

        // 先写临时文件，避免在写入过程中直接截断正式书单。
        try {
            Files.write(temporary, lines, StandardCharsets.UTF_8);

            try {
                Files.move(temporary, target,
                        StandardCopyOption.ATOMIC_MOVE,
                        StandardCopyOption.REPLACE_EXISTING);
            } 
            // 文件系统不支持原子移动时，退回到普通替换。
            catch (AtomicMoveNotSupportedException e) {
                Files.move(temporary, target, StandardCopyOption.REPLACE_EXISTING);
            }
        } 
        // 移动成功后临时文件已不存在；失败时清理残留文件。
        finally {
            Files.deleteIfExists(temporary);
        }
    }
}
