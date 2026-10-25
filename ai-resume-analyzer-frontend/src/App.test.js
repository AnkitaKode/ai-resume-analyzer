import React from "react";
import { render, screen, waitFor } from "@testing-library/react";
import App from "./App";
import * as api from "./services/api";

jest.mock("./services/api", () => {
  const actual = jest.requireActual("./services/api");
  return {
    ...actual,
    getCurrentUserApi: jest.fn(),
    signInApi: jest.fn(),
    signUpApi: jest.fn(),
    continueAsGuestApi: jest.fn(),
    logoutApi: jest.fn(),
  };
});

describe("App Authentication Routing", () => {
  beforeEach(() => {
    jest.clearAllMocks();
    window.history.pushState({}, "Root", "/");
  });

  test("renders sign in interface when unauthenticated", async () => {
    api.getCurrentUserApi.mockRejectedValue(new Error("Unauthorized"));

    render(<App />);

    // Brand should be visible
    const brands = screen.getAllByText(/Resume/i);
    expect(brands.length).toBeGreaterThan(0);

    // Sign in form elements should appear after session check finishes
    const emailInput = await screen.findByPlaceholderText(/name@company.com/i);
    expect(emailInput).toBeInTheDocument();

    expect(screen.getByRole("button", { name: /sign in/i })).toBeInTheDocument();
    expect(screen.getByRole("button", { name: /continue with google/i })).toBeInTheDocument();
    expect(screen.getByRole("button", { name: /continue as guest/i })).toBeInTheDocument();
  });

  test("renders dashboard when authenticated as registered user", async () => {
    api.getCurrentUserApi.mockResolvedValue({
      id: "u-123",
      fullName: "Jane Doe",
      email: "jane@example.com",
      isGuest: false,
      role: "ROLE_USER",
    });

    render(<App />);

    // Wait for Dashboard to render user profile
    await waitFor(() => {
      expect(screen.getByText("Jane Doe")).toBeInTheDocument();
    });

    expect(screen.getByTitle("Sign Out")).toBeInTheDocument();
  });

  test("renders guest banner when authenticated as guest", async () => {
    api.getCurrentUserApi.mockResolvedValue({
      id: "g-456",
      fullName: "Guest User",
      email: "guest_456@resumeai.internal",
      isGuest: true,
      role: "ROLE_GUEST",
      uploadCount: 1,
      chatMessageCount: 2,
    });

    render(<App />);

    // Wait for Dashboard to render guest badge and banner
    await waitFor(() => {
      expect(screen.getByText("Exploring in Guest Mode")).toBeInTheDocument();
    });

    expect(screen.getByText(/Used: 1\/2 uploads · 2\/5 questions/i)).toBeInTheDocument();
    expect(screen.getByText("Save My Resumes →")).toBeInTheDocument();
  });
});
