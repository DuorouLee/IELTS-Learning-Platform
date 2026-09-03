package com.duorou.ieltsbackend.reading.dto;

import com.duorou.ieltsbackend.reading.entity.ReadingPassage;
import com.duorou.ieltsbackend.reading.entity.ReadingTest;

import java.util.List;

/**
 * ReadingTestDetailResponse
 *
 * 这是一个 DTO（Data Transfer Object）。
 *
 * 作用：
 * 专门定义 API 要返回给前端的数据结构。
 *
 * 这里我们不直接返回 ReadingTest Entity，
 * 而是额外把它下面的 passages 一起返回。
 */
public class ReadingTestDetailResponse {

    /**
     * 当前 Reading Test 本身的信息。
     */
    private ReadingTest test;

    /**
     * 当前 Test 下的所有 Passage。
     */
    private List<ReadingPassage> passages;

    public ReadingTestDetailResponse() {
    }

    public ReadingTestDetailResponse(
            ReadingTest test,
            List<ReadingPassage> passages
    ) {
        this.test = test;
        this.passages = passages;
    }

    public ReadingTest getTest() {
        return test;
    }

    public void setTest(ReadingTest test) {
        this.test = test;
    }

    public List<ReadingPassage> getPassages() {
        return passages;
    }

    public void setPassages(List<ReadingPassage> passages) {
        this.passages = passages;
    }
}
