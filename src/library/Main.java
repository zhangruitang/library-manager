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

/** 选择书单文件、加载数据并组装控制台程序的入口。 */
public class Main {
    /**
     * 启动一次会话。加载失败时返回 1；其余退出码由控制台界面决定。
     * 输入和输出由调用方传入，便于复用和检查运行结果。
     */
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
    
    /** 从命令行参数读取可选书单路径，默认使用 data/books.tsv。 */
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