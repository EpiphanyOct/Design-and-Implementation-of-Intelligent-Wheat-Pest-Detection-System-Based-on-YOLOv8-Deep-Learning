/**
 * 害虫识别组件测试
 * Feature: Pest Detection
 */
import React from "react";
import { render, screen, fireEvent, waitFor } from "@testing-library/react";
import userEvent from "@testing-library/user-event";
import PestSearch from "../src/frontend/PestSearch";
import pestDetectionService from "../src/frontend/pestDetectionService";

// Mock pestDetectionService
jest.mock("../src/frontend/pestDetectionService");

describe("PestSearch Component", () => {
  const mockDetectResult = {
    success: true,
    pestType: "红蜘蛛",
    confidence: 0.92,
    diagnosis: "寄生于小麦叶片，取食汁液导致叶片枯黄",
    suggestions: "1. 定期检查叶片\n2. 施用生物农药",
    detectTime: "2024-01-01 10:00:00",
    identifiedPests: [
      {
        pestId: "P001",
        pestName: "红蜘蛛",
        confidence: 0.92,
        description: "寄生于小麦叶片",
        preventionTips: ["定期检查叶片", "施用生物农药"],
      },
    ],
  };

  const mockHistoryData = {
    success: true,
    data: {
      records: [
        {
          id: 1,
          pestType: "红蜘蛛",
          confidence: 0.92,
          diagnosis: "寄生于小麦叶片",
          suggestions: "定期检查叶片",
          detectTime: "2024-01-01 10:00:00",
        },
      ],
      total: 1,
      current: 1,
      pageSize: 5,
    },
  };

  beforeEach(() => {
    jest.clearAllMocks();
    pestDetectionService.getHistory.mockResolvedValue(mockHistoryData);
  });

  test("renders pest search page correctly", () => {
    render(<PestSearch />);

    expect(screen.getByText("虫害识别")).toBeInTheDocument();
    expect(screen.getByText("智能分析上传图片，识别小麦害虫种类")).toBeInTheDocument();
    expect(screen.getByText("历史检测记录查询")).toBeInTheDocument();
  });

  test("shows warning when detecting without image", async () => {
    render(<PestSearch />);

    const detectButton = screen.getByText("开始检测");
    fireEvent.click(detectButton);

    await waitFor(() => {
      expect(screen.getByText("请先上传图片")).toBeInTheDocument();
    });
  });

  test("successful detection shows result", async () => {
    pestDetectionService.detect.mockResolvedValue(mockDetectResult);

    render(<PestSearch />);

    // 模拟文件上传
    const file = new File(["test"], "test.jpg", { type: "image/jpeg" });
    const input = screen.getByText("上传图片").parentElement.querySelector("input");
    
    await userEvent.upload(input, file);

    const detectButton = screen.getByText("开始检测");
    fireEvent.click(detectButton);

    await waitFor(() => {
      expect(screen.getByText("检测完成")).toBeInTheDocument();
      expect(screen.getByText("红蜘蛛")).toBeInTheDocument();
      expect(screen.getByText("92%")).toBeInTheDocument();
    });
  });

  test("failed detection shows error message", async () => {
    pestDetectionService.detect.mockResolvedValue({
      success: false,
      message: "检测失败",
    });

    render(<PestSearch />);

    const file = new File(["test"], "test.jpg", { type: "image/jpeg" });
    const input = screen.getByText("上传图片").parentElement.querySelector("input");
    
    await userEvent.upload(input, file);

    const detectButton = screen.getByText("开始检测");
    fireEvent.click(detectButton);

    await waitFor(() => {
      expect(screen.getByText("检测失败")).toBeInTheDocument();
    });
  });

  test("loads history data on mount", async () => {
    render(<PestSearch />);

    await waitFor(() => {
      expect(pestDetectionService.getHistory).toHaveBeenCalled();
      expect(screen.getByText("红蜘蛛")).toBeInTheDocument();
    });
  });

  test("opens detail modal when clicking detail button", async () => {
    render(<PestSearch />);

    await waitFor(() => {
      expect(screen.getByText("详情")).toBeInTheDocument();
    });

    const detailButton = screen.getByText("详情");
    fireEvent.click(detailButton);

    await waitFor(() => {
      expect(screen.getByText("检测详情")).toBeInTheDocument();
    });
  });

  test("search history with date range", async () => {
    render(<PestSearch />);

    const searchButton = screen.getByText("查询");
    fireEvent.click(searchButton);

    await waitFor(() => {
      expect(pestDetectionService.getHistory).toHaveBeenCalled();
    });
  });

  test("resets date range and reloads history", async () => {
    render(<PestSearch />);

    const resetButton = screen.getByText("重置");
    fireEvent.click(resetButton);

    await waitFor(() => {
      expect(pestDetectionService.getHistory).toHaveBeenCalled();
    });
  });
});
