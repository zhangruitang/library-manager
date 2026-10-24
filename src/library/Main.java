package library;

import java.util.ArrayList;
import library.model.Book;
import library.service.LibraryService;
import java.util.Scanner;
import java.util.List;
import java.io.IOException;
import java.nio.file.Path;
import library.storage.FileBookRepository;
import library.ui.ConsoleUI;
import java.io.PrintStream;

public class Main {
    public static int run(Path dataFile, Scanner input, PrintStream output) {
        FileBookRepository repository = new FileBookRepository(dataFile);

        List<Book> initialBooks;
        try {
            initialBooks = repository.load();
        } 
        catch (IOException e) {
            output.println("读取书单失败：" + e.getMessage());
            return 1;
        }

        LibraryService service = new LibraryService(initialBooks);
        ConsoleUI ui = new ConsoleUI(service, repository, input, output);
        return ui.run();
    }
    
    public static void main(String[] args){
        Path dataFile = Path.of("data", "books.tsv");
        if (args.length > 0) {
            dataFile = Path.of(args[0]);
        }

        Scanner input = new Scanner(System.in, "UTF-8");
        int exitCode = run(dataFile, input, System.out);

        input.close();
        System.exit(exitCode);
    }
}