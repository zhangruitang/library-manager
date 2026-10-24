package library.ui;

import java.io.IOException;
import java.io.PrintStream;
import java.util.List;
import java.util.Scanner;

import library.model.Book;
import library.service.LibraryService;
import library.storage.BookRepository;
import java.util.NoSuchElementException;

/** 负责菜单交互和结果展示；图书规则由 LibraryService 处理。 */
public class ConsoleUI {
    private final LibraryService service;
    private final BookRepository repository;
    private final Scanner input;
    private final PrintStream output;

    /** 注入业务、存储和输入输出对象，便于独立运行菜单会话。 */
    public ConsoleUI(LibraryService service,BookRepository repository,
        Scanner input,PrintStream output){
        this.service = service;
        this.repository = repository;
        this.input = input;
        this.output = output;
    }

    /** 运行菜单循环；0 表示正常退出，2 表示输入结束时保存失败。 */
    public int run(){
        while(true){
            output.println("请输入菜单：1添加 | 2全部 | 3编号查询 | 4借出 | 5归还 | 6删除 | 7保存继续 | 8修改 | 9书名查询 | 10分类添加 | 11修改分类 | 12分类查询 | 0退出");

            // 输入流已结束时，先保存已完成的操作再退出。
            if (!input.hasNextLine()) {
                try {
                    repository.save(service.listBooks());
                    output.println("保存成功");
                    return 0;
                }
                catch (IOException e) {
                    output.println("保存失败：" + e.getMessage());
                    return 2;
                }
            }
            String choice = input.nextLine().trim();
            
            // 主动退出若保存失败，保留会话供用户重试。
            if (choice.equals("0")) {
                try{
                    repository.save(service.listBooks());
                    output.println("保存成功");
                    return 0;
                }
                catch(IOException e){
                    output.println("保存失败：" + e.getMessage());
                    continue;
                }
            }
            try{
                switch(choice){
                    // 添加普通图书；分类由业务层设为“未分类”，并由业务层校验字段和编号。
                    case "1":
                        output.print("请输入要添加的编号：");
                        String id = input.nextLine();
                        output.print("请输入要添加的书名：");
                        String title = input.nextLine();
                        output.print("请输入要添加的作者：");
                        String author = input.nextLine();
                        service.addBook(id,title,author);
                        output.println("添加成功");
                        break;
                    // 展示当前书单；空书库时给出单独提示。
                    case "2":
                        if (service.listBooks().isEmpty()){
                            output.println("目前没有图书");
                        } 
                        else{
                            printBooks(service.listBooks());
                        }
                        break;
                    // 按编号查询，并复用统一打印方法显示完整图书信息。
                    case "3":
                        output.println("请输入要查询的书的编号：");
                        String searchId  = input.nextLine();
                        Book foundBook = service.findById(searchId);
                        printBooks(List.of(foundBook));
                        break;
                    // 借出指定图书；是否已借出由业务层检查。
                    case "4":
                        output.println("请输入要借出的书的编号：");
                        String borrowId = input.nextLine();
                        service.borrowBook(borrowId);
                        output.println("已成功借出");
                        break;
                    // 归还指定图书；是否尚未借出由业务层检查。
                    case "5":
                        output.println("请输入要归还的书的编号：");
                        String returnId = input.nextLine();
                        service.returnBook(returnId);
                        output.println("已成功归还");
                        break;
                    // 删除指定图书；借出的图书不能删除。
                    case "6":
                        output.println("请输入要删除的书的编号：");
                        String deleteId = input.nextLine();
                        service.deleteBook(deleteId);
                        output.println("删除成功");
                        break;
                    // 将当前书单写入文件；无论保存结果如何都继续显示菜单。
                    case "7":
                        try {
                            repository.save(service.listBooks());
                            output.println("保存成功");
                        }
                        catch (IOException e) {
                            output.println("保存失败：" + e.getMessage());
                        }
                        break;
                    // 按编号修改书名和作者；业务层会先校验新内容。
                    case "8":
                        output.println("请输入要修改的书的编号：");
                        String updateId = input.nextLine();
                        output.println("请输入修改后的书名：");
                        String updateTitle = input.nextLine();
                        output.println("请输入修改后的作者：");
                        String updateAuthor = input.nextLine();
                        service.updateBook(updateId,updateTitle,updateAuthor);
                        output.println("更新成功");
                        break;
                    // 按书名关键词查询；没有匹配结果时给出提示。
                    case "9":
                        output.println("请输入要查询的书名关键词：");
                        String keyword = input.nextLine();
                        List<Book> foundBooks = service.searchByTitle(keyword);
                        if(foundBooks.isEmpty()){
                            output.println("没有找到匹配的图书");
                        }
                        else{
                            printBooks(foundBooks);
                        }
                        break;
                    // 添加带指定分类的图书；字段校验和编号查重仍由业务层完成。
                    case "10":
                        output.print("请输入要添加的编号：");
                        String classifiedId = input.nextLine();
                        output.print("请输入要添加的书名：");
                        String classifiedTitle = input.nextLine();
                        output.print("请输入要添加的作者：");
                        String classifiedAuthor = input.nextLine();
                        output.print("请输入分类：");
                        String classifiedCategory = input.nextLine();
                        service.addBook(classifiedId, classifiedTitle, classifiedAuthor, classifiedCategory);
                        output.println("添加成功");
                        break;
                    // 按编号找到图书并更新分类；新分类由业务层校验。
                    case "11":
                        output.print("请输入要修改分类的书的编号：");
                        String categoryBookId = input.nextLine();
                        output.print("请输入新分类：");
                        String newCategory = input.nextLine();
                        service.updateBookCategory(categoryBookId, newCategory);
                        output.println("分类修改成功");
                        break;
                    // 按完整分类名查询；没有匹配结果时给出提示。
                    case "12":
                        output.print("请输入要查询的分类：");
                        String categoryKeyword = input.nextLine();
                        List<Book> categoryMatches = service.searchByCategory(categoryKeyword);
                        if(categoryMatches.isEmpty()){
                            output.println("没有找到该分类的图书");
                        }
                        else{
                            printBooks(categoryMatches);
                        }
                        break;
                    // 菜单编号不在已定义的选项中，提示后返回菜单循环。
                    default:
                        output.println("没有这个菜单选项");
                }
            }
            // 将可预期的业务错误显示给用户，然后继续接受菜单输入。
            catch (IllegalArgumentException | IllegalStateException e) {
                output.println(e.getMessage());
            }
            // 输入可能在填写某个菜单项的过程中结束，此时仍尝试保存。
            catch (NoSuchElementException e){
                try{
                    repository.save(service.listBooks());
                    output.println("保存成功");
                    return 0;
                } 
                catch (IOException io) {
                    output.println("保存失败：" + io.getMessage());
                    return 2;
                }
            }
        }
    }
    /** 统一显示图书字段与借阅状态。 */
    private void printBooks(List<Book> books) {
        for (Book book : books) {
            String status;
            if (book.isBorrowed()){
                status = "已借出";
            } 
            else{
                status = "可借阅";
            }
            output.println(
                book.getId() + " | " +
                book.getTitle() + " | " +
                book.getAuthor() + " | " +
                book.getCategory() + " | " +
                status
            );
        }
    }
}
