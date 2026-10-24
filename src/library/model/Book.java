package library.model;

/** 一本书的数据及其字段、借阅状态的约束。 */
public class Book {
    private String id;
    private String title;
    private String author;
    private boolean borrowed;
    private String category;

    /** 创建未借出、未分类的图书，并校验必填字段。 */
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
    /** 从旧版书单恢复图书状态；旧版记录没有分类。 */
    public Book(String id, String title, String author, boolean borrowed) {
        this(id, title, author);
        this.borrowed = borrowed;
    }
    /** 创建带分类的新图书。 */
    public Book(String id, String title, String author, String category) {
        this(id, title, author);
        this.updateCategory(category);
    }
    /** 从新版书单或快照恢复完整的图书信息。 */
    public Book(String id, String title, String author,boolean borrowed, String category) {
        this(id, title, author, category);
        this.borrowed = borrowed;
    }

    /** 校验分类后更新；内部普通空格会保留。 */
    public void updateCategory(String category) {
        if (category == null || category.trim().isEmpty()) {
            throw new IllegalArgumentException("分类不能为空");
        }
        checkSeparators(category);
        this.category = category.trim();
    }

    /** 两个字段都通过校验后，再一起更新书名和作者。 */
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

    /** 借出图书；重复借出属于非法状态。 */
    public void borrow(){
        if(this.borrowed == true){
            throw new IllegalStateException("该书已经借出");
        }
        else{
            this.borrowed = true;
        }
    }
    /** 归还图书；尚未借出的图书不能归还。 */
    public void returnBook(){
        if(this.borrowed == false){
            throw new IllegalStateException("该书尚未借出");
        }
        else{
            this.borrowed = false;
        }
    }

    /** TSV 字段不能包含会破坏列或行边界的字符。 */
    private static void checkSeparators(String text) {
        if (text.contains("\t") || text.contains("\n") || text.contains("\r")) {
            throw new IllegalArgumentException("字段不能包含制表符或换行符");
        }
    }
}
