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
        arcadeOrangeLight: '#FF3D00',
        arcadeScreenBackground: '#232031',
        inputBackGround: '#e8f0fe',
        arcadeGradient1: '#1A1825',
        arcadeGradient2: '#182B38',
        arcadeOrangeGradient2: '#4A201D',
      },
      backgroundImage: {
        'title-background': 'radial-gradient(ellipse at center, rgba(255, 255, 255, 0.3) 0%, rgba(255, 255, 255, 0) 60%)',
        'arcade-radial-bg': 'radial-gradient(circle, rgba(0,144,153,1) 30%, rgba(30,27,43,1) 80%)',
        'arcade-radial-bg-orange': 'radial-gradient(circle, #FF3D00 30%, rgba(30,27,43,1) 80%)',
        'arcade-internal-edge': 'linear-gradient(to top left, rgba(0,0,0,0) 0%, rgba(0,0,0,0) calc(50% - 2px), rgba(71, 26, 80, 1) 50%, rgba(0,0,0,0) calc(50% + 2px), rgba(0,0,0,0) 100%), linear-gradient(to top right, rgba(0,0,0,0) 0%, rgba(0,0,0,0) calc(50% - 2px), rgba(71, 26, 80, 1) 50%, rgba(0,0,0,0) calc(50% + 2px), rgba(0,0,0,0) 100%);',
        'arcade-rainbow': 'linear-gradient(to top left, rgba(0,0,0,0) 0%, rgba(0,0,0,0) calc(38% - 3px), #3C003E 38%, #ED0280 39%, #ED0280 45%, black 46%, #00B2FF 47%, #00B2FF 53%, black 54%, #FF3D00 55%, #FF3D00 61%, #3C003E 62%, rgba(0,0,0,0) calc(62% + 3px), rgba(0,0,0,0) 100%);',
      },
      boxShadow: {
        'arcadeBorderShadow': 'inset 0px 0px 4px 4px rgba(60, 0, 62, 1)',
        'arcadeScreenShadow': '0px 0px 15px -1px rgba(0, 240, 255, 1)',
        'arcadeScreenShadowOrange': '0px 0px 15px -1px #FF3D00',
        'arcadeScreenInnerShadow': 'inset 0px 0px 15px -1px rgba(0, 240, 255, 1)',
        'arcadeScreenInnerShadow': 'inset 0px 0px 15px -1px #FF3D00',
      },
      keyframes: {
        wiggle: {
          '0%, 100%': { transform: 'rotate(-3deg)' },
          '50%': { transform: 'rotate(3deg)' },
        },
        'wiggle-reverse': {
          '0%, 100%': { transform: 'rotate(3deg)' },
          '50%': { transform: 'rotate(-3deg)' },
        }
      },
      animation: {
        wiggle: 'wiggle 2s ease-in-out infinite',
        'wiggle-reverse': 'wiggle-reverse 2s ease-in-out infinite',
        'wiggle-slow': 'wiggle 3s ease-in-out infinite',
        'wiggle-reverse-slow': 'wiggle-reverse 3s ease-in-out infinite',
      },
    },
  },
  plugins: [
    
  ],
}

