package com.duorou.ieltsbackend.reading.dto;

import com.duorou.ieltsbackend.reading.entity.ReadingTest;

import java.util.List;

/**
 * ReadingTestDetailResponse
 *
 * 用于返回一个完整 ReadingTest 的详情。
 *
 * 当前结构：
 *
 * ReadingTest
 *      ↓
 * Passages
 *
 * 每一个 Passage 后面会继续包含自己的 Questions。
 */
public class ReadingTestDetailResponse {

    /**
     * 当前 Reading Test 基本信息。
     */
    private ReadingTest test;

    /**
     * 当前 Test 下的所有 Passage。
     */
    private List<ReadingPassageResponse> passages;

    public ReadingTestDetailResponse() {
    }

    public ReadingTestDetailResponse(
            ReadingTest test,
            List<ReadingPassageResponse> passages
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

    public List<ReadingPassageResponse> getPassages() {
        return passages;
    }

    public void setPassages(List<ReadingPassageResponse> passages) {
        this.passages = passages;
    }
}
