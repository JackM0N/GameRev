/** @type {import('tailwindcss').Config} */
module.exports = {
  content: [
    "./src/**/*.{html,ts}",
  ],
  theme: {
    fontFamily: {
      arcade: ["arcade"],
    },
    extend: {
      colors: {
        titleTextBackground: '#1A1825',
        arcadeBackGround: '#1E1B2B',
        arcadeLines: 'rgb(71 26 80)',
        arcadeBigLines: '#3C003E',
        arcadeBlueLight: '#00F0FF',
        arcadeScreenBackground: '#232031',
        inputBackGround: '#e8f0fe',
        arcadeGradient1: '#1A1825',
        arcadeGradient2: '#182B38',
      },
      backgroundImage: {
        'arcade-radial-bg': 'radial-gradient(circle, rgba(0,144,153,1) 30%, rgba(30,27,43,1) 80%)',
        'arcade-internal-edge': 'linear-gradient(to top left, rgba(0,0,0,0) 0%, rgba(0,0,0,0) calc(50% - 2px), rgba(71, 26, 80, 1) 50%, rgba(0,0,0,0) calc(50% + 2px), rgba(0,0,0,0) 100%), linear-gradient(to top right, rgba(0,0,0,0) 0%, rgba(0,0,0,0) calc(50% - 2px), rgba(71, 26, 80, 1) 50%, rgba(0,0,0,0) calc(50% + 2px), rgba(0,0,0,0) 100%);'
      },
      boxShadow: {
        'arcadeBorderShadow': 'inset 0px 0px 4px 4px rgba(60, 0, 62, 1)',
        'arcadeScreenShadow': '0px 0px 15px -1px rgba(0, 240, 255, 1)',
        'arcadeScreenInnerShadow': 'inset 0px 0px 15px -1px rgba(0, 240, 255, 1)',
      }
    },
  },
  plugins: [],
}

