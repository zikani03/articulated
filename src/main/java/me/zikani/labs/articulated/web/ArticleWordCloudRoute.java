/**
 * MIT License
 * <p>
 * Copyright (c) 2020 - 2022 Zikani Nyirenda Mwase and Contributors
 * <p>
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 * <p>
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 * <p>
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */
package me.zikani.labs.articulated.web;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.javalin.http.Context;
import me.zikani.labs.articulated.dao.ArticleDAO;
import me.zikani.labs.articulated.model.Article;
import me.zikani.labs.articulated.processor.WordFrequencyCounter;
import org.jetbrains.annotations.NotNull;

import java.nio.file.Files;
import java.nio.file.Path;

/**
 * This endpoint outputs articles as plaintext file for labeling in a format that's
 * compatible with OpenCraftML project
 */
public class ArticleWordCloudRoute extends AbstractBaseRoute {
    private final ArticleDAO articleDAO;
    private final Path wordCloudBinary;

    public ArticleWordCloudRoute(ObjectMapper objectMapper, ArticleDAO articleDAO,
                                 Path pathToWordCloudBinary) {
        super(objectMapper);
        this.articleDAO = articleDAO;
        this.wordCloudBinary = pathToWordCloudBinary;
    }


    @Override
    public void handle(@NotNull Context context) throws Exception {
        Article article = articleDAO.getRandomArticle();

        var path = Files.createTempFile("articulated", ".txt");
        Files.writeString(path, article.getBody());

        Process p = new ProcessBuilder()
                .directory(wordCloudBinary.toFile())
                .command("wordcloudgen", "--input", "file", "", "data")
                .start();

        p.getOutputStream();

    }
}
