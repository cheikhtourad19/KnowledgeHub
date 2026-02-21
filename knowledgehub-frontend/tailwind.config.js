/** @type {import('tailwindcss').Config} */
module.exports = {
  content: ["./src/**/*.{html,ts}"],
  darkMode: "class", // ← add this line
  theme: {
    extend: {
      colors: {
        primary: {
          50: "#fef2f3",
          100: "#fde6e8",
          200: "#fbd0d5",
          300: "#f7aab2",
          400: "#f27a8a",
          500: "#e74c60",
          600: "#d32f4a",
          700: "#C60C30",
          800: "#a51032",
          900: "#8b1230",
        },
      },
    },
  },
  plugins: [require("flowbite/plugin")],
};
