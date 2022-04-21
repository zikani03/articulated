/**
 * MIT License
 *
 * Copyright (c) 2020 - 2022 Zikani Nyirenda Mwase and Contributors
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */
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
