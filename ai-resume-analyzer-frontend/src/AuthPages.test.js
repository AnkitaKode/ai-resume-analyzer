import React from "react";
import { render, screen, fireEvent, waitFor } from "@testing-library/react";
import { MemoryRouter } from "react-router-dom";
import { AuthProvider } from "./context/AuthContext";
import SignIn from "./pages/SignIn";
import SignUp from "./pages/SignUp";
import ForgotPassword from "./pages/ForgotPassword";
import ResetPassword from "./pages/ResetPassword";
import * as api from "./services/api";

jest.mock("./services/api", () => {
  const actual = jest.requireActual("./services/api");
  return {
    ...actual,
    getCurrentUserApi: jest.fn().mockRejectedValue(new Error("Unauthenticated")),
    signInApi: jest.fn(),
    signUpApi: jest.fn(),
    continueAsGuestApi: jest.fn(),
    forgotPasswordApi: jest.fn(),
    resetPasswordApi: jest.fn(),
  };
});

describe("Authentication Pages and Form Validations", () => {
  beforeEach(() => {
    jest.clearAllMocks();
  });

  describe("SignIn Page", () => {
    test("renders all sign-in controls and provider buttons", () => {
      render(
        <MemoryRouter>
          <AuthProvider>
            <SignIn />
          </AuthProvider>
        </MemoryRouter>
      );

      expect(screen.getByPlaceholderText(/name@company.com/i)).toBeInTheDocument();
      expect(screen.getByPlaceholderText(/••••••••/i)).toBeInTheDocument();
      expect(screen.getByRole("button", { name: /sign in/i })).toBeInTheDocument();
      expect(screen.getByRole("button", { name: /continue with google/i })).toBeInTheDocument();
      expect(screen.getByRole("button", { name: /continue as guest/i })).toBeInTheDocument();
      expect(screen.getByText(/remember me for 30 days/i)).toBeInTheDocument();
      expect(screen.getByText(/forgot password\?/i)).toBeInTheDocument();
    });

    test("toggles password visibility", () => {
      render(
        <MemoryRouter>
          <AuthProvider>
            <SignIn />
          </AuthProvider>
        </MemoryRouter>
      );

      const passwordInput = screen.getByPlaceholderText(/••••••••/i);
      expect(passwordInput).toHaveAttribute("type", "password");

      const toggleBtn = screen.getByRole("button", { name: /show password/i });
      fireEvent.click(toggleBtn);
      expect(passwordInput).toHaveAttribute("type", "text");

      const hideBtn = screen.getByRole("button", { name: /hide password/i });
      fireEvent.click(hideBtn);
      expect(passwordInput).toHaveAttribute("type", "password");
    });
  });

  describe("SignUp Page", () => {
    test("validates password confirmation and terms acceptance", async () => {
      render(
        <MemoryRouter>
          <AuthProvider>
            <SignUp />
          </AuthProvider>
        </MemoryRouter>
      );

      const nameInput = screen.getByPlaceholderText(/alex morgan/i);
      const emailInput = screen.getByPlaceholderText(/name@company.com/i);
      const passwordInput = screen.getByPlaceholderText(/at least 8 characters/i);
      const confirmInput = screen.getByPlaceholderText(/re-enter password/i);
      const submitBtn = screen.getByRole("button", { name: /create account/i });

      // Fill in mismatched passwords
      fireEvent.change(nameInput, { target: { value: "Sarah Connor" } });
      fireEvent.change(emailInput, { target: { value: "sarah@example.com" } });
      fireEvent.change(passwordInput, { target: { value: "P@ssword123!" } });
      fireEvent.change(confirmInput, { target: { value: "Different123!" } });

      fireEvent.click(submitBtn);

      await waitFor(() => {
        expect(screen.getByText("Passwords do not match.")).toBeInTheDocument();
      });
    });

    test("displays password strength indicator", () => {
      render(
        <MemoryRouter>
          <AuthProvider>
            <SignUp />
          </AuthProvider>
        </MemoryRouter>
      );

      const passwordInput = screen.getByPlaceholderText(/at least 8 characters/i);

      fireEvent.change(passwordInput, { target: { value: "weak" } });
      expect(screen.getByText(/weak/i)).toBeInTheDocument();

      fireEvent.change(passwordInput, { target: { value: "StrongerP@ssw0rd!" } });
      expect(screen.getByText(/strong/i)).toBeInTheDocument();
    });
  });

  describe("ForgotPassword Page", () => {
    test("renders email input and handles dev mode reset token", async () => {
      api.forgotPasswordApi.mockResolvedValue({
        message: "Password reset link generated successfully.",
        devMode: true,
        resetToken: "dev-test-token-xyz",
        resetUrl: "http://localhost:3000/reset-password?token=dev-test-token-xyz",
      });

      render(
        <MemoryRouter>
          <AuthProvider>
            <ForgotPassword />
          </AuthProvider>
        </MemoryRouter>
      );

      const emailInput = screen.getByPlaceholderText(/name@company.com/i);
      const submitBtn = screen.getByRole("button", { name: /send recovery link/i });

      fireEvent.change(emailInput, { target: { value: "developer@example.com" } });
      fireEvent.click(submitBtn);

      await waitFor(() => {
        expect(screen.getByText(/Dev Mode/i)).toBeInTheDocument();
        expect(screen.getByText(/Proceed to Reset Password Now/i)).toBeInTheDocument();
      });
    });
  });

  describe("ResetPassword Page", () => {
    test("validates matching new passwords before submit", async () => {
      render(
        <MemoryRouter initialEntries={["/reset-password?token=valid-token"]}>
          <AuthProvider>
            <ResetPassword />
          </AuthProvider>
        </MemoryRouter>
      );

      const passwordInput = screen.getByPlaceholderText(/at least 8 characters/i);
      const confirmInput = screen.getByPlaceholderText(/re-enter new password/i);
      const submitBtn = screen.getByRole("button", { name: /update password/i });

      fireEvent.change(passwordInput, { target: { value: "NewPassword123!" } });
      fireEvent.change(confirmInput, { target: { value: "DifferentPassword123!" } });
      fireEvent.click(submitBtn);

      await waitFor(() => {
        expect(screen.getByText("Passwords do not match.")).toBeInTheDocument();
      });
    });
  });
});
