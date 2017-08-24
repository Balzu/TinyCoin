reset

# wxt
set terminal wxt size 410,250 enhanced font 'Verdana,9' persist
# png
#set terminal pngcairo size 410,250 enhanced font 'Verdana,9'
#set output 'nice_web_plot.png'
# svg
#set terminal svg size 410,250 fname 'Verdana, Helvetica, Arial, sans-serif' \
#fsize '9' rounded dashed
#set output 'nice_web_plot.svg'

# define axis
# remove border on top and right and set color to gray
set style line 11 lc rgb '#808080' lt 1
set border 3 back ls 11
set tics nomirror
# define grid
set style line 12 lc rgb '#808080' lt 0 lw 1
set grid back ls 12

# color definitions
set style line 1 lc rgb '#8b1a0e' pt 1 ps 1 lt 1 lw 2 # --- red
set style line 2 lc rgb '#5e9c36' pt 6 ps 1 lt 1 lw 2 # --- green

set key bottom right

set xlabel 'Cycle'
set ylabel 'Forks'
set xrange [0:50]
set yrange [0:5]

plot 'forks.dat' u 2:1 t 'parameter' w lp ls 1
