package me.zikani.labs.articulated.dao;

import org.jdbi.v3.sqlobject.customizer.Bind;
import org.jdbi.v3.sqlobject.statement.SqlUpdate;

public interface EntityDAO {

     @SqlUpdate("""
         INSERT INTO entities(id, entity_name, entity_type, num_occurrences) 
         VALUES (:id , :name , :type , :occurrences)
         ON CONFLICT(id) DO UPDATE SET num_occurrences = num_occurrences + :occurrences;
     """)
     void insert(@Bind("id") String id, @Bind("name") String name, @Bind("type") String entityType, @Bind("occurrences") int occurrences);

     @SqlUpdate("INSERT INTO article_entities(article_id, entity_id) VALUES (:articleId , :entityId) ON CONFLICT (entity_id, article_id) DO NOTHING;")
     void link(@Bind("articleId") String articleId, @Bind("entityId") String entityId);
}
