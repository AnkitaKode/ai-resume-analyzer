/** @type {import('tailwindcss').Config} */
module.exports = {
  content: ["./src/**/*.{js,jsx,ts,tsx}"],
  theme: {
    extend: {
      fontFamily: {
        display: ["'Playfair Display'", "serif"],
        body: ["'DM Sans'", "sans-serif"],
        mono: ["'JetBrains Mono'", "monospace"],
      },
      colors: {
        obsidian: {
          950: "#050508",
          900: "#0a0a12",
          800: "#111120",
          700: "#1a1a2e",
          600: "#22223b",
        },
        ember: {
          400: "#ff6b35",
          500: "#ff4500",
          600: "#e03d00",
        },
        gold: {
          300: "#ffd166",
          400: "#ffbe0b",
          500: "#f5a623",
        },
        slate: {
          glass: "rgba(255,255,255,0.04)",
        },
      },
      backgroundImage: {
        "gradient-radial": "radial-gradient(var(--tw-gradient-stops))",
        "mesh-dark":
          "radial-gradient(at 40% 20%, hsla(28,100%,54%,0.08) 0px, transparent 50%), radial-gradient(at 80% 0%, hsla(189,100%,56%,0.05) 0px, transparent 50%), radial-gradient(at 0% 50%, hsla(355,100%,93%,0.03) 0px, transparent 50%)",
      },
      animation: {
        "fade-up": "fadeUp 0.6s ease forwards",
        "fade-in": "fadeIn 0.4s ease forwards",
        pulse2: "pulse2 2s cubic-bezier(0.4, 0, 0.6, 1) infinite",
        shimmer: "shimmer 2s infinite",
        "score-fill": "scoreFill 1.5s ease-out forwards",
      },
      keyframes: {
        fadeUp: {
          "0%": { opacity: 0, transform: "translateY(20px)" },
          "100%": { opacity: 1, transform: "translateY(0)" },
        },
        fadeIn: {
          "0%": { opacity: 0 },
          "100%": { opacity: 1 },
        },
        pulse2: {
          "0%, 100%": { opacity: 1 },
          "50%": { opacity: 0.4 },
        },
        shimmer: {
          "0%": { backgroundPosition: "-200% 0" },
          "100%": { backgroundPosition: "200% 0" },
        },
        scoreFill: {
          "0%": { width: "0%" },
          "100%": { width: "var(--score-width)" },
        },
      },
      backdropBlur: {
        xs: "2px",
      },
      boxShadow: {
        glow: "0 0 30px rgba(255, 107, 53, 0.15)",
        "glow-gold": "0 0 30px rgba(255, 190, 11, 0.12)",
        glass: "0 8px 32px rgba(0, 0, 0, 0.4), inset 0 1px 0 rgba(255,255,255,0.06)",
      },
    },
  },
  plugins: [],
};