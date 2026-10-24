package library.storage;

import java.util.List;
import java.io.IOException;
import library.model.Book;

/** 书单读写契约；调用方无需知道具体保存介质。 */
public interface BookRepository {
    /** 加载书单；文件缺失时由实现类返回空列表。 */
    List<Book> load() throws IOException;
    /** 保存整份书单；写入失败时向调用方报告 IOException。 */
    void save(List<Book> books) throws IOException;
}
