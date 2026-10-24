package library.model;

public class Book {
    private String id;
    private String title;
    private String author;
    private boolean borrowed;
    private String category;

    public Book(String id, String title, String author) {
        if(id==null || id.trim().isEmpty()){
            throw new IllegalArgumentException("编号不能为空");
        }
        if(title==null || title.trim().isEmpty()){
            throw new IllegalArgumentException("书名不能为空");
        }
        if(author==null || author.trim().isEmpty()){
            throw new IllegalArgumentException("作者不能为空");
        }
        checkSeparators(id);
        checkSeparators(title);
        checkSeparators(author);
        this.id = id.trim();
        this.title = title.trim();
        this.author = author.trim();
        this.borrowed = false;
        this.category = "未分类";
    }
    public Book(String id, String title, String author, boolean borrowed) {
        this(id, title, author);
        this.borrowed = borrowed;
    }
    public Book(String id, String title, String author, String category) {
        this(id, title, author);
        this.updateCategory(category);
    }
    public Book(String id, String title, String author,boolean borrowed, String category) {
        this(id, title, author, category);
        this.borrowed = borrowed;
    }

    public void updateCategory(String category) {
        if (category == null || category.trim().isEmpty()) {
            throw new IllegalArgumentException("分类不能为空");
        }
        checkSeparators(category);
        this.category = category.trim();
    }

    public void updateDetails(String title, String author){
        if(title==null || title.trim().isEmpty()){
            throw new IllegalArgumentException("书名不能为空");
        }
        if(author==null || author.trim().isEmpty()){
            throw new IllegalArgumentException("作者不能为空");
        }
        checkSeparators(title);
        checkSeparators(author);
        this.title = title.trim();
        this.author = author.trim();
    }

    public String getId() {return id;}
    public String getTitle() {return title;}
    public String getAuthor() {return author;}
    public String getCategory() {return category;}
    public boolean isBorrowed() {return borrowed;}

    public void borrow(){
        if(this.borrowed == true){
            throw new IllegalStateException("该书已经借出");
        }
        else{
            this.borrowed = true;
        }
    }
    public void returnBook(){
        if(this.borrowed == false){
            throw new IllegalStateException("该书尚未借出");
        }
        else{
            this.borrowed = false;
        }
    }

    private static void checkSeparators(String text) {
        if (text.contains("\t") || text.contains("\n") || text.contains("\r")) {
            throw new IllegalArgumentException("字段不能包含制表符或换行符");
        }
    }
}
