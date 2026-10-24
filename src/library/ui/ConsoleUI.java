package library.ui;

import java.io.IOException;
import java.io.PrintStream;
import java.util.List;
import java.util.Scanner;

import library.model.Book;
import library.service.LibraryService;
import library.storage.BookRepository;
import java.util.NoSuchElementException;

public class ConsoleUI {
    private final LibraryService service;
    private final BookRepository repository;
    private final Scanner input;
    private final PrintStream output;

    public ConsoleUI(LibraryService service,BookRepository repository,
        Scanner input,PrintStream output){
        this.service = service;
        this.repository = repository;
        this.input = input;
        this.output = output;
    }

    public int run(){
        while(true){
            output.println("请输入菜单：1添加 | 2全部 | 3编号查询 | 4借出 | 5归还 | 6删除 | 7保存继续 | 8修改 | 9书名查询 | 10分类添加 | 11修改分类 | 12分类查询 | 0退出");

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
                    //添加
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
                    //全部
                    case "2":
                        if (service.listBooks().isEmpty()){
                            output.println("目前没有图书");
                        } 
                        else{
                            printBooks(service.listBooks());
                        }
                        break;
                    //编号查询
                    case "3":
                        output.println("请输入要查询的书的编号：");
                        String searchId  = input.nextLine();
                        Book foundBook = service.findById(searchId);
                        printBooks(List.of(foundBook));
                        break;
                    //借出
                    case "4":
                        output.println("请输入要借出的书的编号：");
                        String borrowId = input.nextLine();
                        service.borrowBook(borrowId);
                        output.println("已成功借出");
                        break;
                    //归还
                    case "5":
                        output.println("请输入要归还的书的编号：");
                        String returnId = input.nextLine();
                        service.returnBook(returnId);
                        output.println("已成功归还");
                        break;
                    //删除
                    case "6":
                        output.println("请输入要删除的书的编号：");
                        String deleteId = input.nextLine();
                        service.deleteBook(deleteId);
                        output.println("删除成功");
                        break;
                    //保存并继续使用
                    case "7":
                        try {
                            repository.save(service.listBooks());
                            output.println("保存成功");
                        }
                        catch (IOException e) {
                            output.println("保存失败：" + e.getMessage());
                        }
                        break;
                    //修改
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
                    //书名查询
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
                    //带分类添加
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
                    //修改分类
                    case "11":
                        output.print("请输入要修改分类的书的编号：");
                        String categoryBookId = input.nextLine();
                        output.print("请输入新分类：");
                        String newCategory = input.nextLine();
                        service.updateBookCategory(categoryBookId, newCategory);
                        output.println("分类修改成功");
                        break;
                    //分类查询
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
                    //输入不合规
                    default:
                        output.println("没有这个菜单选项");
                }
            }
            catch (IllegalArgumentException | IllegalStateException e) {
                output.println(e.getMessage());
            }
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
