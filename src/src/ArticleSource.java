/*
 * CS3810 - Principles of Database Systems - Spring 2021
 * Instructor: Thyago Mota
 * Description: DB 04 - ArticleSource
 * Student(s) Name(s): Jonathan Grant
 */

import org.bson.Document;
import org.bson.types.ObjectId;

public class ArticleSource {

    private String id;
    private String name;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    @Override
    public String toString() {
        return "source={" +
                "id='" + id + '\'' +
                ", name='" + name + '\'' +
                '}';
    }

    // TO/DO: return a Document object representing the article source
    public Document toDocument() {
        Document articleSource = new Document("_id", new ObjectId());
        articleSource.append("id", getId())
                    .append("name", getName());
        return articleSource;
    }
}
