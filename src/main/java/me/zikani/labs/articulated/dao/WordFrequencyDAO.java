package me.zikani.labs.articulated.dao;

import org.jdbi.v3.sqlobject.customizer.Bind;
import org.jdbi.v3.sqlobject.statement.SqlQuery;
import org.jdbi.v3.sqlobject.statement.SqlUpdate;

import java.util.Map;

public interface WordFrequencyDAO {
    @SqlQuery("SELECT word, frequency FROM word_frequencies")
    Map<String, Integer> fetchAll();

    @SqlUpdate("INSERT OR REPLACE INTO word_frequencies(word, frequency) VALUES(:word, :frequency)")
    void insert(@Bind("word") String word, @Bind("frequency") int frequency);

    /**
     * TODO: wait for SQLite 3.24 support in jdbc-sqlite for this to work :_(
     *
    @SqlUpdate("INSERT INTO word_frequencies(word, frequency) VALUES(:word, :frequency) " +
              " ON CONFLICT(word) DO UPDATE SET frequency = frequency + :frequency")
    void insert(@Bind("word") String word, @Bind("frequency") int frequency);
    */

    @SqlUpdate("CREATE TABLE IF NOT EXISTS word_frequencies(word varchar(256) not null primary key, frequency integer)")
    void createTable();
}
