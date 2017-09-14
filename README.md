## TinyCoin: Simulating fraudolent mining strategies in a simplified Bitcoin Network

Final Project for the course of P2P Systems of the Master Degree in Computer Science and Networking

### Folder structure
The *src* folder contains the source of the project, the *lib* folder the library needed to run the Peersim Simulator, the *bin* folder contains the classes compiled and packaged in a .jar file and the *docs* folder contains the relation of the project and two subfolders *statistics*, containing the statistics collected by running the program, and *plots*, containing the plots of the statistics. The subfolders inside *docs* are populated after a simulation is executed.
 
### Running an experiment
Running an experiment means running a single run of the Peersim Simulator with the configuration parameters defined in `tinycoin_config.txt`. To run an experiment you simply open the terminal and type 


`java -jar bin/tinycoin.jar peersim.Simulator tinycoin_config.txt `

### Running a simulation
A simulation consists in choosing a set of values for the parameters and, for each combination of such values, in runnning the experiment for a given number of times. At the end the experiment will be carried out several times with different parameters values and the results of the repeated execution of the same experiment will be averaged and plotted. 
It is possible to set up a simulation thanks to the use of scripts: 


* `start_simulation.py`: it's the script that actually starts the simulation. The set of parameters for the simulation are defined in this file. The values provided for these parameters will be substituted in the `tinycoin_config_for_script.txt` file and a temporary file will be created to run the experiments.
* `build_avg_statistics.py`: this script, called by `start_simulation.py`, collects the statistics produced by the executions of the experiments, makes the proper averages and writes in the `docs/statistics/avg` subfolder these files ready to be plotted
* `plot_avg_statistics.py`: this script, called by `start_simulation.py`, reads the files in the `docs/statistics/avg` subfolder and uses Gnuplot to create the graphs that will be stored in the `docs/plots` subfolder.

In order to run a simulation, after having set up the values for the parameters in `start_simulation.py`, you simply need to open terminal and type


`python start_simulation.py [repetitions]`

The `repetitions` argument specifies how many times each experiment has to be repeated. If omitted, it defaults to zero.


A description of this project, together with the execution of a simulation and analysis of the results is present in the [relation](https://github.com/Balzu/TinyCoin/blob/master/docs/relation.pdf).


