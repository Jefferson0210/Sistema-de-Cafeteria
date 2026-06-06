export default {
  content: ['./index.html', './src/**/*.{js,ts,jsx,tsx}'],
  darkMode: 'class',
  theme: {
    extend: {
      colors: {
        brand:{50:'#fdf2f7',100:'#fce7f1',200:'#fbd0e5',300:'#f9a8cc',400:'#f472a8',500:'#910048',600:'#7a003d',700:'#630032',800:'#4c0027',900:'#3a001e'},
        gold:{50:'#fffdf0',100:'#fff9d6',200:'#fff3ad',300:'#ffeb7a',400:'#EAAA00',500:'#d49a00',600:'#b38200',700:'#8a6500',800:'#6b4e00',900:'#4d3800'},
        navy:{50:'#f0f4f9',100:'#dce5f2',200:'#b9cce6',300:'#8baad4',400:'#5d87c2',500:'#002D72',600:'#002660',700:'#001f4e',800:'#00183c',900:'#00112a'},
        sidebar:{DEFAULT:'#0f172a',hover:'#1e293b',active:'#334155',border:'#1e293b'}
      },
      fontFamily:{display:['Inter','system-ui','sans-serif'],body:['Inter','system-ui','sans-serif']}
    }
  },
  plugins: []
};
